package com.nandaadisaputra.noteapp.repository

import com.nandaadisaputra.noteapp.data.local.Note
import com.nandaadisaputra.noteapp.data.local.NoteDao
import kotlinx.coroutines.flow.Flow

/**
 * NoteRepository bertindak sebagai abstraksi atau satu-satunya sumber kebenaran (Single Source of Truth)
 * untuk mengelola data catatan. Kelas ini menjembatani antara ViewModel dan Data Access Object (DAO).
 *
 * @property noteDao Interface DAO untuk mengakses query Room Database.
 */
class NoteRepository(private val noteDao: NoteDao) {

    /**
     * Mengambil seluruh data catatan dari database secara asynchronous menggunakan Kotlin Flow.
     * Setiap kali ada perubahan data di tabel Room, Flow ini akan memancarkan (emit) daftar terbaru otomatis.
     */
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    /**
     * Mencari catatan berdasarkan kata kunci (query) pada judul atau isi catatan.
     *
     * @param query Kata kunci pencarian yang dimasukkan pengguna.
     * @return [Flow] berisi daftar catatan yang sesuai dengan kata kunci.
     */
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    /**
     * Menambahkan catatan baru atau memperbarui catatan yang sudah ada (Upsert).
     * Fungsi ini bersifat asynchronous (suspend) dan harus dipanggil di dalam Coroutine.
     *
     * @param note Objek catatan yang akan disimpan atau diperbarui.
     */
    suspend fun insertOrUpdate(note: Note) {
        noteDao.upsertNote(note)
    }

    /**
     * Menghapus catatan tertentu dari database.
     * Fungsi ini bersifat asynchronous (suspend) dan harus dipanggil di dalam Coroutine.
     *
     * @param note Objek catatan yang akan dihapus dari Room.
     */
    suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
    }
}