package com.nandaadisaputra.noteapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nandaadisaputra.noteapp.adapter.NoteAdapter
import com.nandaadisaputra.noteapp.data.NoteDatabase
import com.nandaadisaputra.noteapp.data.local.Note
import com.nandaadisaputra.noteapp.databinding.ActivityMainBinding
import com.nandaadisaputra.noteapp.databinding.DialogAddEditNoteBinding
import com.nandaadisaputra.noteapp.repository.NoteRepository

/**
 * MainActivity bertindak sebagai tampilan utama (UI Controller) yang menampilkan
 * daftar catatan, menangani pencarian, serta interaksi CRUD (Create, Read, Update, Delete).
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding untuk mengakses komponen layout activity_main.xml secara aman
    private lateinit var binding: ActivityMainBinding

    // Adapter untuk mengelola dan menampilkan daftar data pada RecyclerView
    private lateinit var noteAdapter: NoteAdapter

    // Inisialisasi ViewModel menggunakan Factory untuk menyuntikkan (inject) Repository
    private val viewModel: NoteViewModel by viewModels {
        val database = NoteDatabase.getDatabase(this)
        val repository = NoteRepository(database.noteDao())
        NoteViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding dan pasang tampilan root ke Activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Memanggil fungsi-fungsi inisialisasi awal
        setupRecyclerView()
        setupListeners()
        setupSearch()
        observeData()
    }

    /**
     * Mengatur konfigurasi awal RecyclerView beserta adapter dan event handling saat item diklik
     */
    private fun setupRecyclerView() {
        // Menginisialisasi adapter beserta lambda callback untuk aksi Edit dan Delete
        noteAdapter = NoteAdapter(
            onEditClick = { note ->
                // Aksi ketika tombol/item Edit diklik -> Buka dialog dengan data catatan lama
                showAddEditDialog(note)
            },
            onDeleteClick = { note ->
                // Aksi ketika tombol Delete diklik -> Buka dialog konfirmasi hapus
                showDeleteConfirmationDialog(note)
            }
        )

        // Menghubungkan RecyclerView dengan Adapter dan LayoutManager
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = noteAdapter
            setHasFixedSize(true) // Optimasi performa jika ukuran item konsisten
        }
    }

    /**
     * Mengatur event listener untuk komponen UI statis seperti FloatingActionButton (FAB)
     */
    private fun setupListeners() {
        // FAB untuk menambah catatan baru (mengirim null karena catatan baru belum memiliki data)
        binding.fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    /**
     * Mengatur listener pencarian pada SearchView untuk merespons ketikan pengguna secara real-time
     */
    private fun setupSearch() {
        binding.svNotes.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Dipanggil ketika tombol submit pada keyboard ditekan
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            // Dipanggil setiap kali ada perubahan teks pada input SearchView
            override fun onQueryTextChange(newText: String?): Boolean {
                // Memperbarui StateFlow pencarian pada ViewModel
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    /**
     * Mengamati (Observe) perubahan data LiveData dari ViewModel secara asynchronous (Real-time)
     */
    private fun observeData() {
        // Setiap ada perubahan data di Room Database, observer ini akan dipanggil otomatis
        viewModel.notes.observe(this) { notes ->
            updateUiState(notes)
        }
    }

    /**
     * Memperbarui tampilan UI berdasarkan kondisi data (kosong atau berisi)
     *
     * @param notes Daftar catatan yang dikirimkan oleh ViewModel
     */
    private fun updateUiState(notes: List<Note>) {
        if (notes.isEmpty()) {
            // Tampilkan teks info kosong dan sembunyikan RecyclerView
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvNotes.visibility = View.GONE
        } else {
            // Sembunyikan teks kosong dan kirimkan data baru ke Adapter
            binding.tvEmpty.visibility = View.GONE
            binding.rvNotes.visibility = View.VISIBLE
            noteAdapter.submitList(notes)
        }
    }

    /**
     * Menampilkan AlertDialog untuk proses CREATE (Tambah) dan UPDATE (Edit) catatan
     *
     * @param note Objek catatan yang akan diedit. Jika null, berarti membuat catatan baru.
     */
    private fun showAddEditDialog(note: Note?) {
        // Inflate layout dialog menggunakan ViewBinding
        val dialogBinding = DialogAddEditNoteBinding.inflate(LayoutInflater.from(this))
        val isEdit = note != null

        // Jika mode Edit, isi bidang input dengan data catatan yang ada
        if (isEdit) {
            dialogBinding.edtTitle.setText(note?.title)
            dialogBinding.edtContent.setText(note?.content)
        }

        // Membangun tampilan Material Alert Dialog
        MaterialAlertDialogBuilder(this)
            .setTitle(if (isEdit) "Edit Catatan" else "Tambah Catatan Baru")
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { dialog, _ ->
                val title = dialogBinding.edtTitle.text.toString().trim()
                val content = dialogBinding.edtContent.text.toString().trim()

                // Validasi input agar tidak boleh kosong
                if (title.isNotEmpty() && content.isNotEmpty()) {
                    val newNote = Note(
                        id = note?.id ?: 0, // ID 0 memberi sinyal pada Room untuk membuat Auto-Increment ID baru
                        title = title,
                        content = content
                    )
                    // Panggil ViewModel untuk menyimpan/memperbarui data
                    viewModel.insertOrUpdate(newNote)
                    Toast.makeText(this, "Catatan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Judul dan isi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Menampilkan AlertDialog konfirmasi sebelum melakukan aksi DELETE (Hapus)
     *
     * @param note Objek catatan yang akan dihapus dari database
     */
    private fun showDeleteConfirmationDialog(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Apakah Anda yakin ingin menghapus '${note.title}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                // Panggil ViewModel untuk menghapus data dari Room Database
                viewModel.delete(note)
                Toast.makeText(this, "Catatan berhasil dihapus", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}