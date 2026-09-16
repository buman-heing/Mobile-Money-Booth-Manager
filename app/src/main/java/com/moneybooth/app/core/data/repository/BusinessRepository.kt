package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.BusinessDao
import com.moneybooth.app.core.data.database.entities.BusinessEntity
import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val businessDao: BusinessDao) {
    fun observeFirst(): Flow<BusinessEntity?> = businessDao.observeFirst()

    fun observeAll(): Flow<List<BusinessEntity>> = businessDao.observeAll()

    suspend fun getFirstOnce(): BusinessEntity? = businessDao.getFirstOnce()

    suspend fun createBusiness(name: String, defaultCurrency: String = "ZMW"): Long {
        val now = System.currentTimeMillis()
        return businessDao.insert(
            BusinessEntity(name = name, defaultCurrency = defaultCurrency, createdAt = now),
        )
    }

    suspend fun updateBusiness(business: BusinessEntity) = businessDao.update(business)
}
