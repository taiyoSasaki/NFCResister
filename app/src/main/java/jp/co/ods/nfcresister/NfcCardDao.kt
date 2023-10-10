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

    @Query(/* value = */ "DELETE FROM nfcCard_table")
    suspend fun clear()

    //データをリストで取得
    @Query(/* value = */ "SELECT * FROM nfcCard_table")
    suspend fun getAllNfcCard(): List<NfcCard>

    //最大ページ数(pageが一番大きい値)を調べる
    @Query("SELECT MAX(page) FROM nfcCard_table")
    suspend fun getMaxPage() :Int?

}