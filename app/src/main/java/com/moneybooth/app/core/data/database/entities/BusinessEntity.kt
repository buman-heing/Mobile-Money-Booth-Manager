package com.moneybooth.app.core.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.sync.newUid

@Entity(tableName = "businesses", indices = [Index("uid", unique = true)])
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(defaultValue = "") val uid: String = newUid(),
    val name: String,
    val defaultCurrency: String = "ZMW",
    val createdAt: Long,
)
