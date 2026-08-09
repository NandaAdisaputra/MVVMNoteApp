package com.nandaadisaputra.noteapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // 1. Ambil semua catatan (diurutkan dari yang terbaru)
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    // 2. Pencarian catatan (Tipe return diperbaiki menjadi Flow<List<Note>>)
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    // 3. Tambah atau Perbarui catatan
    // Menggunakan @Upsert (Fitur Room 2.5.0+) pengganti @Insert(onConflict = REPLACE)
    @Upsert
    suspend fun upsertNote(note: Note)

    // 4. Hapus catatan
    @Delete
    suspend fun deleteNote(note: Note)
}