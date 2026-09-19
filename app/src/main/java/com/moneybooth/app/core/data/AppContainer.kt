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
import com.moneybooth.app.core.domain.accounting.DiscrepancyDetector
import com.moneybooth.app.core.domain.employees.AttributionService
import com.moneybooth.app.core.domain.reconciliation.ReconciliationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.moneybooth.app.cloud.CloudAuth
import com.moneybooth.app.cloud.CloudPuller
import com.moneybooth.app.cloud.CloudSyncController
import com.moneybooth.app.cloud.FirestoreRemoteStore
import com.moneybooth.app.core.sync.RecordAssembler
import com.moneybooth.app.core.sync.RemoteApplier
import com.moneybooth.app.core.sync.SyncEngine
import com.moneybooth.app.core.sync.SyncOutbox
import com.moneybooth.app.security.PinCredentialStore
import com.moneybooth.app.security.SessionManager
import com.moneybooth.app.sms.airtel.AirtelMoneyProvider
import com.moneybooth.app.sms.android.DiscrepancyNotifier
import com.moneybooth.app.sms.android.SmsPermissionManager
import com.moneybooth.app.sms.android.SyncScheduler
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
    ).addMigrations(*AppDatabase.ALL_MIGRATIONS).build()

    val deviceSettingsStore = DeviceSettingsStore(context.applicationContext)
    val syncScheduler = SyncScheduler(context.applicationContext)

    /** Every repository write lands here; the scheduler then asks the worker to upload when online. */
    val syncOutbox = SyncOutbox(database.syncOutboxDao()).apply { onChanged = { syncScheduler.requestNow() } }

    val businessRepository = BusinessRepository(database.businessDao(), syncOutbox)
    val boothRepository = BoothRepository(database.boothDao(), syncOutbox)
    val employeeRepository = EmployeeRepository(database.employeeDao(), syncOutbox)
    val mobileMoneyAccountRepository = MobileMoneyAccountRepository(database.mobileMoneyAccountDao(), syncOutbox)
    val shiftRepository = ShiftRepository(database.shiftDao(), syncOutbox)
    val rawSmsRepository = RawSmsRepository(database.rawSmsDao(), syncOutbox)
    val transactionRepository = TransactionRepository(database.transactionDao(), database.auditLogDao(), syncOutbox)
    val cashMovementRepository = CashMovementRepository(database.cashMovementDao(), syncOutbox)
    val auditLogRepository = AuditLogRepository(database.auditLogDao(), syncOutbox)
    val reconciliationRepository = ReconciliationRepository(database.reconciliationDao(), syncOutbox)

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        // Our own outbox is the offline queue; Firestore's disk cache would just double-buffer it.
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()
    }
    private val cloudAuth = CloudAuth(FirebaseAuth.getInstance())
    val remoteStore: FirestoreRemoteStore = FirestoreRemoteStore(firestore, cloudAuth)
    val syncEngine = SyncEngine(database.syncOutboxDao(), remoteStore, assemble = RecordAssembler(database)::assemble)
    private val discrepancyNotifier = DiscrepancyNotifier(context.applicationContext)
    private val remoteApplier = RemoteApplier(database).apply {
        // Only the owner is told; the booth phone already shows the flag in its own list.
        onDiscrepancyArrived = { tx -> if (deviceSettingsStore.deviceRole == DeviceRole.OWNER) discrepancyNotifier.notify(tx) }
    }
    val cloudPuller = CloudPuller(firestore, cloudAuth, remoteApplier, deviceSettingsStore)
    val cloudSyncController = CloudSyncController(
        cloudPuller,
        remoteStore,
        deviceSettingsStore,
        businessRepository,
        database.syncOutboxDao(),
    )

    /** Registered providers. New providers plug in here without touching accounting code. */
    val providerRegistry = ProviderRegistry(providers = listOf(AirtelMoneyProvider()))

    val smsIngestionPipeline = SmsIngestionPipeline(
        providerRegistry,
        rawSmsRepository,
        transactionRepository,
        DiscrepancyDetector(transactionRepository),
    )
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
    val smsPermissionManager = SmsPermissionManager(context.applicationContext)
}
