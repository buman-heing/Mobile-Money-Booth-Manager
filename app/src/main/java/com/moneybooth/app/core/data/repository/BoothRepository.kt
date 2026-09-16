package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.BoothDao
import com.moneybooth.app.core.data.database.entities.BoothEntity
import kotlinx.coroutines.flow.Flow

class BoothRepository(private val boothDao: BoothDao) {
    fun observeById(id: Long): Flow<BoothEntity?> = boothDao.observeById(id)

    suspend fun getByIdOnce(id: Long): BoothEntity? = boothDao.getByIdOnce(id)

    fun observeByBusiness(businessId: Long): Flow<List<BoothEntity>> = boothDao.observeByBusiness(businessId)

    fun observeActiveByBusiness(businessId: Long): Flow<List<BoothEntity>> =
        boothDao.observeActiveByBusiness(businessId)

    suspend fun createBooth(businessId: Long, name: String, location: String? = null): Long {
        val now = System.currentTimeMillis()
        return boothDao.insert(
            BoothEntity(
                businessId = businessId,
                name = name,
                location = location,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateBooth(booth: BoothEntity) =
        boothDao.update(booth.copy(updatedAt = System.currentTimeMillis()))
}
