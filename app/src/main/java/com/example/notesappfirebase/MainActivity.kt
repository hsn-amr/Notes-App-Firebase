package com.example.notesappfirebase

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    lateinit var adapter: RVNotes
    lateinit var rvMain: RecyclerView
    lateinit var noteInput: EditText
    lateinit var addNote: Button

    lateinit var progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val allNotes = ArrayList<Note>()

        showProgressDialog()
        GlobalScope.launch(Main){
            db.collection("notes")
                .get()
                .addOnSuccessListener { result ->
                    for (note in result){
                        note.data.map { (key,value) ->
                            Log.e("TAG","$key - $value")
                            allNotes.add(Note(value.toString(),note.id))
                        }
                    }

                }
                .addOnFailureListener { e -> Log.e("TAG",e.toString()) }
            delay(5000)
            adapter = RVNotes(allNotes,this@MainActivity)
            rvMain.adapter = adapter
            rvMain.layoutManager = LinearLayoutManager(this@MainActivity)
            removeProgressDialog()
        }

        adapter = RVNotes(allNotes, this)

        rvMain = findViewById(R.id.rvMain)

        noteInput = findViewById(R.id.etNoteInput)
        addNote = findViewById(R.id.btnAddNote)

        addNote.setOnClickListener {
            val text = noteInput.text.toString()
            if(text.isNotEmpty()){
                showProgressDialog()
                val n = hashMapOf(
                    "note" to text
                )

                db.collection("notes")
                    .add(n)
                    .addOnSuccessListener {
                        adapter.updateAdapter()
                        removeProgressDialog()
                        Toast.makeText(this, "Added", Toast.LENGTH_LONG).show()
                        noteInput.text.clear()
                    }
                    .addOnFailureListener { Toast.makeText(this, "Error", Toast.LENGTH_LONG).show() }
            }else{
                Toast.makeText(this, "Type a Note", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun showProgressDialog(){
        progress = ProgressDialog(this)
        progress.setTitle("Loading")
        progress.setMessage("Wait while loading...")
        progress.show()
    }
    fun removeProgressDialog(){
        progress.dismiss()
    }
}