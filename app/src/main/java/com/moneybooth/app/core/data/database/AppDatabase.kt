package com.moneybooth.app.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moneybooth.app.core.data.database.dao.AuditLogDao
import com.moneybooth.app.core.data.database.dao.BoothDao
import com.moneybooth.app.core.data.database.dao.BusinessDao
import com.moneybooth.app.core.data.database.dao.CashMovementDao
import com.moneybooth.app.core.data.database.dao.EmployeeDao
import com.moneybooth.app.core.data.database.dao.MobileMoneyAccountDao
import com.moneybooth.app.core.data.database.dao.RawSmsDao
import com.moneybooth.app.core.data.database.dao.ReconciliationDao
import com.moneybooth.app.core.data.database.dao.ShiftDao
import com.moneybooth.app.core.data.database.dao.SyncOutboxDao
import com.moneybooth.app.core.data.database.dao.TransactionDao
import com.moneybooth.app.core.data.database.entities.AuditLogEntity
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.database.entities.BusinessEntity
import com.moneybooth.app.core.data.database.entities.CashMovementEntity
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.core.data.database.entities.MobileMoneyAccountEntity
import com.moneybooth.app.core.data.database.entities.RawSmsEntity
import com.moneybooth.app.core.data.database.entities.ReconciliationEntity
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.data.database.entities.SyncOutboxEntity
import com.moneybooth.app.core.data.database.entities.TransactionEntity

@Database(
    entities = [
        BusinessEntity::class,
        BoothEntity::class,
        EmployeeEntity::class,
        MobileMoneyAccountEntity::class,
        ShiftEntity::class,
        RawSmsEntity::class,
        TransactionEntity::class,
        CashMovementEntity::class,
        ReconciliationEntity::class,
        AuditLogEntity::class,
        SyncOutboxEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
    abstract fun boothDao(): BoothDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun mobileMoneyAccountDao(): MobileMoneyAccountDao
    abstract fun shiftDao(): ShiftDao
    abstract fun rawSmsDao(): RawSmsDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cashMovementDao(): CashMovementDao
    abstract fun reconciliationDao(): ReconciliationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun syncOutboxDao(): SyncOutboxDao

    companion object {
        const val DATABASE_NAME = "moneybooth.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN commissionMinor INTEGER")
            }
        }

        /**
         * Adds a device-independent uid to every synced table, creates the upload queue, and queues
         * every pre-existing row (parents first) so history recorded before sync existed still uploads.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            private val syncedTables = listOf(
                "businesses" to "BUSINESS",
                "booths" to "BOOTH",
                "employees" to "EMPLOYEE",
                "mobile_money_accounts" to "MOBILE_MONEY_ACCOUNT",
                "shifts" to "SHIFT",
                "raw_sms" to "RAW_SMS",
                "transactions" to "TRANSACTION",
                "cash_movements" to "CASH_MOVEMENT",
                "reconciliations" to "RECONCILIATION",
                "audit_log" to "AUDIT_LOG",
            )

            override fun migrate(db: SupportSQLiteDatabase) {
                for ((table, _) in syncedTables) {
                    db.execSQL("ALTER TABLE $table ADD COLUMN uid TEXT NOT NULL DEFAULT ''")
                    db.execSQL("UPDATE $table SET uid = lower(hex(randomblob(16)))")
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_${table}_uid ON $table (uid)")
                }
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_outbox (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entityType TEXT NOT NULL,
                        entityUid TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        attempts INTEGER NOT NULL,
                        lastError TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sync_outbox_entityType_entityUid ON sync_outbox (entityType, entityUid)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_createdAt ON sync_outbox (createdAt)")

                val base = System.currentTimeMillis()
                syncedTables.forEachIndexed { order, (table, type) ->
                    db.execSQL(
                        "INSERT INTO sync_outbox (entityType, entityUid, createdAt, attempts) " +
                            "SELECT '$type', uid, ${base + order}, 0 FROM $table ORDER BY id",
                    )
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN discrepancyMinor INTEGER")
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
    }
}
