package com.moneybooth.app.core.domain.reconciliation

import com.moneybooth.app.core.data.database.entities.ReconciliationEntity
import com.moneybooth.app.core.data.repository.AuditLogRepository
import com.moneybooth.app.core.data.repository.CashMovementRepository
import com.moneybooth.app.core.data.repository.ReconciliationRepository
import com.moneybooth.app.core.data.repository.ShiftRepository
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.accounting.LedgerCalculator
import com.moneybooth.app.core.domain.audit.AuditActionType
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.TransactionDirection

/**
 * Cash side is computed from the shift's explicit [com.moneybooth.app.core.data.database.entities.CashMovementEntity]
 * log (staff-entered, never SMS-derived). Mobile-money side is read-only/informational for this
 * milestone: it summarizes the shift's ledgered transactions and compares against the latest
 * provider-reported balance when one exists, but does not support manual mobile-money adjustments yet.
 */
class ReconciliationService(
    private val shiftRepository: ShiftRepository,
    private val cashMovementRepository: CashMovementRepository,
    private val transactionRepository: TransactionRepository,
    private val reconciliationRepository: ReconciliationRepository,
    private val auditLogRepository: AuditLogRepository,
) {
    suspend fun closeShift(shiftId: Long, actualCashMinor: Long, closedBy: String, notes: String?): Long? {
        val shift = shiftRepository.getByIdOnce(shiftId) ?: return null
        val movements = cashMovementRepository.getByShiftOnce(shiftId)

        val cashInMinor = movements
            .filter { it.direction == CashMovementDirection.IN && it.reason != CashMovementReason.ADJUSTMENT }
            .sumOf { it.amountMinor }
        val cashOutMinor = movements
            .filter { it.direction == CashMovementDirection.OUT && it.reason != CashMovementReason.ADJUSTMENT }
            .sumOf { it.amountMinor }
        val adjustmentsMinor = movements
            .filter { it.reason == CashMovementReason.ADJUSTMENT }
            .sumOf { if (it.direction == CashMovementDirection.IN) it.amountMinor else -it.amountMinor }

        val expectedCashMinor = ReconciliationCalculator.expectedCashMinor(
            shift.openingCashMinor,
            cashInMinor,
            cashOutMinor,
            adjustmentsMinor,
        )
        val cashDifferenceMinor = ReconciliationCalculator.cashDifferenceMinor(actualCashMinor, expectedCashMinor)

        val transactions = transactionRepository.getByShiftOnce(shiftId)
        val mobileMoneyInMinor = LedgerCalculator.sumByDirection(transactions, TransactionDirection.IN)
        val mobileMoneyOutMinor = LedgerCalculator.sumByDirection(transactions, TransactionDirection.OUT)
        val latestProviderBalanceMinor = LedgerCalculator.latestKnownBalanceMinor(transactions)
        val expectedMobileMoneyMinor = shift.openingMobileMoneyBalanceMinor?.let {
            ReconciliationCalculator.expectedMobileMoneyMinor(it, mobileMoneyInMinor, mobileMoneyOutMinor)
        }
        val mobileMoneyDifferenceMinor = expectedMobileMoneyMinor?.let {
            ReconciliationCalculator.mobileMoneyDifferenceMinor(latestProviderBalanceMinor, it)
        }
        val unclassifiedCount = transactions.count { it.businessClassification == BusinessClassification.UNKNOWN }

        val now = System.currentTimeMillis()
        val reconciliationId = reconciliationRepository.insert(
            ReconciliationEntity(
                shiftId = shiftId,
                boothId = shift.boothId,
                employeeId = shift.employeeId,
                date = now,
                openingCashMinor = shift.openingCashMinor,
                cashInMinor = cashInMinor,
                cashOutMinor = cashOutMinor,
                cashAdjustmentsMinor = adjustmentsMinor,
                expectedCashMinor = expectedCashMinor,
                actualCashMinor = actualCashMinor,
                cashDifferenceMinor = cashDifferenceMinor,
                openingMobileMoneyBalanceMinor = shift.openingMobileMoneyBalanceMinor,
                mobileMoneyInMinor = mobileMoneyInMinor,
                mobileMoneyOutMinor = mobileMoneyOutMinor,
                expectedMobileMoneyBalanceMinor = expectedMobileMoneyMinor,
                latestProviderReportedBalanceMinor = latestProviderBalanceMinor,
                mobileMoneyDifferenceMinor = mobileMoneyDifferenceMinor,
                unclassifiedTransactionCount = unclassifiedCount,
                closedAt = now,
                closedBy = closedBy,
                notes = notes,
                createdAt = now,
            ),
        )

        shiftRepository.closeShift(shiftId, confirmed = true)

        auditLogRepository.record(
            entityType = "shift",
            entityId = shiftId,
            actionType = AuditActionType.RECONCILIATION_CLOSED,
            performedBy = closedBy,
            newValue = "cashDifferenceMinor=$cashDifferenceMinor",
            reason = notes,
        )

        return reconciliationId
    }
}
