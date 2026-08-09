package com.nandaadisaputra.noteapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.noteapp.data.local.Note
import com.nandaadisaputra.noteapp.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * NoteViewModel bertanggung jawab untuk mengelola data UI terkait catatan
 * serta menjembatani komunikasi antara UI (MainActivity) dan Data Layer (NoteRepository).
 *
 * @property repository Sumber data utama yang menangani operasi ke Room Database.
 */
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    /**
     * StateFlow untuk menyimpan query pencarian yang diinput oleh pengguna.
     * Nilai awal (default) adalah string kosong ("").
     */
    private val searchQuery = MutableStateFlow("")

    /**
     * LiveData yang berisi daftar catatan yang ditampilkan di UI.
     *
     * Menggunakan [flatMapLatest] untuk mentransformasi `searchQuery` secara dinamis:
     * - Jika query kosong: Mengambil seluruh catatan dari repository (`repository.allNotes`).
     * - Jika query berisi teks: Mengambil hasil pencarian dari repository (`repository.searchNotes(query)`).
     *
     * `asLiveData()` mengubah Coroutine Flow dari Room menjadi LiveData agar dapat diamati (observed) oleh Activity/Fragment dengan aman terhadap siklus hidup (Lifecycle-aware).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: LiveData<List<Note>> = searchQuery
        .flatMapLatest { query ->
            if (query.trim().isEmpty()) {
                repository.allNotes
            } else {
                repository.searchNotes(query)
            }
        }
        .asLiveData()

    /**
     * Memperbarui teks pencarian pada [searchQuery].
     * Panggilan fungsi ini akan otomatis memicu ulang transformasi pada `notes`.
     *
     * @param query Teks pencarian dari SearchView
     */
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    /**
     * Menambahkan catatan baru atau memperbarui catatan yang sudah ada (Upsert).
     * Operasi dijalankan pada Background Thread melalui [viewModelScope].
     *
     * @param note Objek catatan yang akan disimpan
     */
    fun insertOrUpdate(note: Note) = viewModelScope.launch {
        repository.insertOrUpdate(note)
    }

    /**
     * Menghapus catatan dari database.
     * Operasi dijalankan pada Background Thread melalui [viewModelScope].
     *
     * @param note Objek catatan yang akan dihapus
     */
    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}

/**
 * Factory class yang berfungsi untuk menginstansiasi [NoteViewModel]
 * dengan memberikan parameter [NoteRepository] pada konstruktor-nya.
 */
class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {

    /**
     * Membuat instance ViewModel yang sesuai jika `modelClass` berjenis [NoteViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}