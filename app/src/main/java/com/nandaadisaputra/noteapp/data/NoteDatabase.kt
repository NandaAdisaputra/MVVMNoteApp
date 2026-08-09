package com.nandaadisaputra.noteapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nandaadisaputra.noteapp.data.local.Note
import com.nandaadisaputra.noteapp.data.local.NoteDao

/**
 * Berkas abstrak utama Room Database yang berfungsi sebagai titik akses (entry point)
 * ke data lokal aplikasi berbasis SQLite.
 *
 * Annotasi @Database menandakan bahwa kelas ini adalah database Room.
 * - entities: Daftar entitas (tabel) yang dikelola oleh database ini.
 * - version: Versi skema database. Wajib dinaikkan jika ada perubahan struktur tabel.
 * - exportSchema: Diset ke false agar Room tidak menyimpan berkas export skema JSON ke dalam project.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    /**
     * Fungsi abstrak untuk mengakses Data Access Object (DAO).
     * Room akan mengimplementasikan fungsi ini secara otomatis.
     */
    abstract fun noteDao(): NoteDao

    companion object {
        /**
         * Instance tunggal dari NoteDatabase (Singleton).
         * Annotasi @Volatile memastikan bahwa nilai INSTANCE selalu diperbarui secara instan
         * ke seluruh thread pembaca (mencegah isu siklus memori di Multi-threading).
         */
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        /**
         * Fungsi statis untuk mengambil atau membuat instance database secara aman (Thread-Safe).
         *
         * @param context Context dari aplikasi Android.
         * @return Instance tunggal dari [NoteDatabase].
         */
        fun getDatabase(context: Context): NoteDatabase {
            // Mengembalikan INSTANCE jika sudah dibuat.
            // Jika belum (null), eksekusi blok synchronized untuk membuat instance baru.
            return INSTANCE ?: synchronized(this) {
                // Membuat instance Room Database menggunakan Builder
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Menggunakan Application Context untuk mencegah Memory Leak
                    NoteDatabase::class.java,    // Kelas database
                    "note_database"              // Nama berkas file database SQLite
                )
                    // Menghapus dan membuat ulang database jika terjadi perubahan versi tanpa strategi migrasi
                    .fallbackToDestructiveMigration()
                    .build()

                // Menyimpan instance yang baru dibuat ke variabel INSTANCE
                INSTANCE = instance

                // Mengembalikan objek instance
                instance
            }
        }
    }
}