package com.example.bolaodapelada.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bolaodapelada.data.dao.PartidaDao
import com.example.bolaodapelada.data.dao.UsuarioDao
import com.example.bolaodapelada.data.entity.Partida
import com.example.bolaodapelada.data.entity.Usuario

@Database(entities = [Usuario::class, Partida::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun partidaDao(): PartidaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bolao_pelada_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}