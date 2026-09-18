package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.BusinessDao
import com.moneybooth.app.core.data.database.entities.BusinessEntity
import com.moneybooth.app.core.sync.SyncEntityType
import com.moneybooth.app.core.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val businessDao: BusinessDao, private val outbox: SyncOutbox) {
    fun observeFirst(): Flow<BusinessEntity?> = businessDao.observeFirst()

    fun observeAll(): Flow<List<BusinessEntity>> = businessDao.observeAll()

    suspend fun getFirstOnce(): BusinessEntity? = businessDao.getFirstOnce()

    suspend fun createBusiness(name: String, defaultCurrency: String = "ZMW"): Long {
        val entity = BusinessEntity(name = name, defaultCurrency = defaultCurrency, createdAt = System.currentTimeMillis())
        val id = businessDao.insert(entity)
        outbox.changed(SyncEntityType.BUSINESS, entity.uid)
        return id
    }

    suspend fun updateBusiness(business: BusinessEntity) {
        businessDao.update(business)
        outbox.changed(SyncEntityType.BUSINESS, business.uid)
    }
}
