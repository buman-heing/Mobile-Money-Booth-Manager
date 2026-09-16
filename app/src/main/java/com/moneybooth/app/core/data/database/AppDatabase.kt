package com.moneybooth.app.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.moneybooth.app.core.data.database.dao.AuditLogDao
import com.moneybooth.app.core.data.database.dao.BoothDao
import com.moneybooth.app.core.data.database.dao.BusinessDao
import com.moneybooth.app.core.data.database.dao.CashMovementDao
import com.moneybooth.app.core.data.database.dao.EmployeeDao
import com.moneybooth.app.core.data.database.dao.MobileMoneyAccountDao
import com.moneybooth.app.core.data.database.dao.RawSmsDao
import com.moneybooth.app.core.data.database.dao.ReconciliationDao
import com.moneybooth.app.core.data.database.dao.ShiftDao
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
    ],
    version = 1,
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

    companion object {
        const val DATABASE_NAME = "moneybooth.db"
    }
}
