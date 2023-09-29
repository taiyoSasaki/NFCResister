package jp.co.ods.nfcresister

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfcCard_table")
data class NfcCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "page")
    val page: Int,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "idm")
    val idm :String
    )