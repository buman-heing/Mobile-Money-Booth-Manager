package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.MobileMoneyAccountDao
import com.moneybooth.app.core.data.database.entities.MobileMoneyAccountEntity
import kotlinx.coroutines.flow.Flow

class MobileMoneyAccountRepository(private val accountDao: MobileMoneyAccountDao) {
    fun observeByBooth(boothId: Long): Flow<List<MobileMoneyAccountEntity>> = accountDao.observeByBooth(boothId)

    suspend fun getActiveByBoothOnce(boothId: Long): List<MobileMoneyAccountEntity> =
        accountDao.getActiveByBoothOnce(boothId)

    suspend fun createAccount(
        boothId: Long,
        providerId: String,
        label: String,
        phoneNumber: String? = null,
    ): Long {
        return accountDao.insert(
            MobileMoneyAccountEntity(
                boothId = boothId,
                providerId = providerId,
                label = label,
                phoneNumber = phoneNumber,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }
}
