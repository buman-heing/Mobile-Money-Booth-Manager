package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.MobileMoneyAccountDao
import com.moneybooth.app.core.data.database.entities.MobileMoneyAccountEntity
import com.moneybooth.app.core.sync.SyncEntityType
import com.moneybooth.app.core.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow

class MobileMoneyAccountRepository(private val accountDao: MobileMoneyAccountDao, private val outbox: SyncOutbox) {
    fun observeByBooth(boothId: Long): Flow<List<MobileMoneyAccountEntity>> = accountDao.observeByBooth(boothId)

    suspend fun getActiveByBoothOnce(boothId: Long): List<MobileMoneyAccountEntity> =
        accountDao.getActiveByBoothOnce(boothId)

    suspend fun createAccount(
        boothId: Long,
        providerId: String,
        label: String,
        phoneNumber: String? = null,
    ): Long {
        val entity = MobileMoneyAccountEntity(
            boothId = boothId,
            providerId = providerId,
            label = label,
            phoneNumber = phoneNumber,
            createdAt = System.currentTimeMillis(),
        )
        val id = accountDao.insert(entity)
        outbox.changed(SyncEntityType.MOBILE_MONEY_ACCOUNT, entity.uid)
        return id
    }
}
