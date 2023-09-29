package jp.co.ods.nfcresister

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NfcCardDao {
    @Insert
    suspend fun insert(nfcCard: NfcCard)

    @Delete
    suspend fun delete(nfcCard: NfcCard)

    @Query("DELETE FROM nfcCard_table")
    suspend fun clear()

    //データをリストで取得
    @Query("SELECT * FROM nfcCard_table")
    suspend fun getAllNfcCard(): List<NfcCard>

}