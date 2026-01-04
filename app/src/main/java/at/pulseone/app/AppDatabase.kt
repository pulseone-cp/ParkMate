package at.pulseone.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ParkingTicket::class], version = 3) // <--- Version incremented
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun parkingTicketDao(): ParkingTicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parkmate_database"
                )
                .fallbackToDestructiveMigration() // <--- Added migration strategy
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}