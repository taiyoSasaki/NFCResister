package jp.co.ods.nfcresister

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NfcCard::class], version = 1, exportSchema = false)
abstract class NfcCardRoomDatabase : RoomDatabase(){
    abstract fun NfcCardDao() : NfcCardDao

    companion object {
        @Volatile
        private var INSTANCE: NfcCardRoomDatabase? = null
        fun getDatabase(context: Context): NfcCardRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NfcCardRoomDatabase::class.java,
                    "nfcCard_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}