package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val defaultCurrency: String = "ZMW",
    val createdAt: Long,
)
