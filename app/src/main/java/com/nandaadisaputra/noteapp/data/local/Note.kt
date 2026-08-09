package com.nandaadisaputra.noteapp.data.local

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Representasi Data Class yang berfungsi sebagai Entitas Tabel Database di Room,
 * sekaligus Objek Model yang digunakan untuk mentransfer data antar komponen Android.
 *
 * Anotasi @Parcelize: Memungkinkan objek [Note] dapat dikirim antar-Activity/Fragment melalui Intent/Bundle.
 * Anotasi @Entity: Menandai kelas ini sebagai entitas tabel dengan nama "notes" di SQLite Database.
 */
@Parcelize
@Entity(tableName = "notes")
data class Note(

    /**
     * Primary Key untuk entitas tabel [Note].
     * Anotasi @PrimaryKey(autoGenerate = true): Membuat ID unik yang nilainya otomatis bertambah (Auto Increment).
     * Nilai default = 0 memberi sinyal pada Room untuk membuatkan ID baru saat proses Insert.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Judul catatan yang diinputkan oleh pengguna.
     */
    val title: String,

    /**
     * Isi atau konten lengkap dari catatan.
     */
    val content: String,

    /**
     * Menyimpan timestamp waktu catatan dibuat atau terakhir diperbarui dalam format milidetik (Epoch Time).
     * Anotasi @ColumnInfo(name = "updated_at"): Mengubah nama kolom di tabel SQLite menjadi "updated_at".
     * Nilai default diisi otomatis menggunakan [System.currentTimeMillis] saat objek diinisialisasi.
     */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()

) : Parcelable // Mengimplementasikan interface Parcelable untuk mendukung passing object via Intent/Bundle