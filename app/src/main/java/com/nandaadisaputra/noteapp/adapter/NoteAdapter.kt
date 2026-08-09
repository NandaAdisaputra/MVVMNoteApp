package com.nandaadisaputra.noteapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nandaadisaputra.noteapp.data.local.Note
import com.nandaadisaputra.noteapp.databinding.ItemNoteBinding

/**
 * Adapter untuk RecyclerView yang mengelola dan menampilkan daftar data [Note].
 * Menggunakan [ListAdapter] agar pembaruan data (tambah, edit, hapus) diproses secara efisien
 * di background thread memanfaatkan [DiffUtil].
 *
 * @property onEditClick Callback lambda yang dipanggil ketika tombol edit pada item diklik.
 * @property onDeleteClick Callback lambda yang dipanggil ketika tombol hapus pada item diklik.
 */
class NoteAdapter(
    private val onEditClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DIFF_CALLBACK) {

    /**
     * Dipanggil saat RecyclerView membutuhkan ViewHolder baru untuk menampilkan item.
     * Meng-inflate layout `item_note.xml` menggunakan ViewBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     * Mengambil item [Note] berdasarkan posisi dan mengikatnya ke ViewHolder.
     */
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        if (note != null) {
            holder.bind(note)
        }
    }

    /**
     * ViewHolder bertindak sebagai pembungkus tampilan (View Wrapper) untuk satu item pada RecyclerView.
     *
     * @property binding Akses langsung ke komponen tampilan di `item_note.xml` via ViewBinding.
     */
    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Mengikat (bind) data dari objek [Note] ke komponen UI serta memasang event listener.
         *
         * @param note Objek catatan yang akan ditampilkan.
         */
        fun bind(note: Note) {
            binding.apply {
                // Set teks judul dan isi catatan
                tvTitle.text = note.title
                tvContent.text = note.content

                // Pasang listener klik untuk tombol Edit dan Delete
                btnEdit.setOnClickListener { onEditClick(note) }
                btnDelete.setOnClickListener { onDeleteClick(note) }
            }
        }
    }

    companion object {
        /**
         * [DiffUtil.ItemCallback] digunakan oleh ListAdapter untuk menghitung perbedaan
         * antara daftar lama dan daftar baru secara efisien.
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {

            /**
             * Memeriksa apakah dua objek mewakili item yang sama berdasarkan identitas uniknya (ID).
             */
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            /**
             * Memeriksa apakah konten/isi dari dua objek identik sama secara keseluruhan.
             * Dipanggil hanya jika [areItemsTheSame] mengembalikan nilai true.
             */
            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }
}