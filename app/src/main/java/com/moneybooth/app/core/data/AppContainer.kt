package com.moneybooth.app.core.data

import android.content.Context
import androidx.room.Room
import com.moneybooth.app.core.data.database.AppDatabase
import com.moneybooth.app.core.data.repository.AuditLogRepository
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.core.data.repository.BusinessRepository
import com.moneybooth.app.core.data.repository.CashMovementRepository
import com.moneybooth.app.core.data.repository.EmployeeRepository
import com.moneybooth.app.core.data.repository.MobileMoneyAccountRepository
import com.moneybooth.app.core.data.repository.RawSmsRepository
import com.moneybooth.app.core.data.repository.ReconciliationRepository
import com.moneybooth.app.core.data.repository.ShiftRepository
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.employees.AttributionService
import com.moneybooth.app.core.domain.reconciliation.ReconciliationService
import com.moneybooth.app.security.PinCredentialStore
import com.moneybooth.app.security.SessionManager
import com.moneybooth.app.sms.airtel.AirtelMoneyProvider
import com.moneybooth.app.sms.android.SmsPermissionManager
import com.moneybooth.app.sms.common.SmsIngestionPipeline
import com.moneybooth.app.sms.providers.ProviderRegistry

/**
 * Manual service locator (no Hilt/KSP annotation-processor dependency for this build).
 * Constructed once in [com.moneybooth.app.MoneyBoothApplication.onCreate].
 */
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    ).build()

    val businessRepository = BusinessRepository(database.businessDao())
    val boothRepository = BoothRepository(database.boothDao())
    val employeeRepository = EmployeeRepository(database.employeeDao())
    val mobileMoneyAccountRepository = MobileMoneyAccountRepository(database.mobileMoneyAccountDao())
    val shiftRepository = ShiftRepository(database.shiftDao())
    val rawSmsRepository = RawSmsRepository(database.rawSmsDao())
    val transactionRepository = TransactionRepository(database.transactionDao(), database.auditLogDao())
    val cashMovementRepository = CashMovementRepository(database.cashMovementDao())
    val auditLogRepository = AuditLogRepository(database.auditLogDao())
    val reconciliationRepository = ReconciliationRepository(database.reconciliationDao())

    /** Registered providers. New providers plug in here without touching accounting code. */
    val providerRegistry = ProviderRegistry(providers = listOf(AirtelMoneyProvider()))

    val smsIngestionPipeline = SmsIngestionPipeline(providerRegistry, rawSmsRepository, transactionRepository)
    val attributionService = AttributionService(shiftRepository)
    val reconciliationService = ReconciliationService(
        shiftRepository,
        cashMovementRepository,
        transactionRepository,
        reconciliationRepository,
        auditLogRepository,
    )

    val pinCredentialStore = PinCredentialStore(context.applicationContext)
    val sessionManager = SessionManager()
    val deviceSettingsStore = DeviceSettingsStore(context.applicationContext)
    val smsPermissionManager = SmsPermissionManager(context.applicationContext)
}
