package com.moneybooth.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.ui.auth.PinViewModel
import com.moneybooth.app.ui.dashboard.DashboardViewModel
import com.moneybooth.app.ui.devtools.DevSmsParserViewModel
import com.moneybooth.app.ui.employees.EmployeeViewModel
import com.moneybooth.app.ui.onboarding.OnboardingViewModel
import com.moneybooth.app.ui.reconciliation.ReconciliationViewModel
import com.moneybooth.app.ui.settings.SettingsViewModel
import com.moneybooth.app.ui.shifts.ShiftViewModel
import com.moneybooth.app.ui.transactions.TransactionDetailViewModel
import com.moneybooth.app.ui.transactions.TransactionsViewModel
import com.moneybooth.app.ui.transactions.UnknownSmsReviewViewModel

/**
 * Manual ViewModel factory wired to [AppContainer] (no Hilt for this build). Extended with one
 * branch per new ViewModel as later milestones add screens.
 */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = when (modelClass) {
            PinViewModel::class.java -> PinViewModel(container.pinCredentialStore, container.sessionManager)
            OnboardingViewModel::class.java -> OnboardingViewModel(container.businessRepository, container.boothRepository)
            EmployeeViewModel::class.java -> EmployeeViewModel(container.employeeRepository)
            TransactionsViewModel::class.java ->
                TransactionsViewModel(container.transactionRepository, container.boothRepository)
            TransactionDetailViewModel::class.java -> TransactionDetailViewModel(
                container.transactionRepository,
                container.rawSmsRepository,
                container.auditLogRepository,
            )
            DashboardViewModel::class.java -> DashboardViewModel(
                container.transactionRepository,
                container.shiftRepository,
                container.employeeRepository,
                container.boothRepository,
            )
            DevSmsParserViewModel::class.java ->
                DevSmsParserViewModel(container.providerRegistry, container.smsIngestionPipeline)
            UnknownSmsReviewViewModel::class.java -> UnknownSmsReviewViewModel(
                container.rawSmsRepository,
                container.transactionRepository,
                container.boothRepository,
                container.auditLogRepository,
            )
            ShiftViewModel::class.java -> ShiftViewModel(
                container.shiftRepository,
                container.cashMovementRepository,
                container.reconciliationService,
                container.employeeRepository,
                container.boothRepository,
            )
            ReconciliationViewModel::class.java -> ReconciliationViewModel(container.reconciliationRepository)
            SettingsViewModel::class.java -> SettingsViewModel(container.smsPermissionManager)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return viewModel as T
    }
}
