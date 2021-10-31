package com.example.notesappfirebase

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_row.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RVNotes(private var notes: ArrayList<Note>, private val context: Context): RecyclerView.Adapter<RVNotes.ItemViewHolder>() {
    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view)

    private val db = Firebase.firestore
    lateinit var progress: ProgressDialog

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_row,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val note = notes[position]

        holder.itemView.apply {
            tvNote.text = note.text
            edit.setOnClickListener { showEditAlart(note,note.text) }
            delete.setOnClickListener { showDeleteAlart(note) }
        }
    }

    override fun getItemCount() = notes.size

    private fun showEditAlart(note: Note, oldText: String){
        val editText = EditText(context)
        editText.setText(oldText)
        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Note")
            .setView(editText)
            .setPositiveButton("Change"){ _, _ ->
                note.text = editText.text.toString()
                updateNote(note)
            }
            .setNegativeButton("cancel"){ dialogFace, _ ->
                dialogFace.cancel()
            }
            .create()
        dialog.show()
    }

    private fun showDeleteAlart(note:Note){
        val dialog = AlertDialog.Builder(context)
            .setTitle("Delete Note")
            .setMessage("Do You Want Delete This Note?")
            .setPositiveButton("Yes"){_,_ ->
                deleteNote(note)
            }
            .setNegativeButton("No"){dialogFace, _ ->
                dialogFace.cancel()
            }
            .create()
        dialog.show()
    }

    fun updateAdapter(){
        if(notes.isNotEmpty()){
            notes.clear()
        }
        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                for (note in result){
                    note.data.map { (key,value) ->
                        notes.add(Note(value.toString(),note.id))
                    }
                }
                notifyDataSetChanged()
            }
            .addOnFailureListener { e -> Log.e("TAG",e.toString()) }
    }


    private fun updateNote(note: Note){
        showProgressDialog()
        db.collection("notes").document(note.id)
            .update("note",note.text)
            .addOnSuccessListener {
                updateAdapter()
                removeProgressDialog()
            }

    }
    private fun deleteNote(note: Note){
        showProgressDialog()
        db.collection("notes").document(note.id)
            .delete()
            .addOnSuccessListener {
                updateAdapter()
                removeProgressDialog()
            }
    }

    private fun showProgressDialog(){
        progress = ProgressDialog(context)
        progress.setTitle("Loading")
        progress.setMessage("Wait while loading...")
        progress.show()
    }
    private fun removeProgressDialog(){
        progress.dismiss()
    }
}