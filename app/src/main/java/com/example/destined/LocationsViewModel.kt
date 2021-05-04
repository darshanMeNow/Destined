package com.example.destined

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class LocationsViewModel:ViewModel() {
    val dbAuthors = FirebaseDatabase.getInstance().getReference(NODE_LOCATIONS)
    //val dbAuthors = FirebaseFirestore.getInstance().collection(NODE_LOCATIONS)

    private val _locashans = MutableLiveData<List<Locashan>>()
    val locashans: LiveData<List<Locashan>>
        get() = _locashans

    private val _author = MutableLiveData<Locashan>()
    val author: LiveData<Locashan>
        get() = _author

    private val _result = MutableLiveData<Exception?>()
    val result: LiveData<Exception?>
        get() = _result

    fun addLoc(locashan: Locashan) {

        locashan.id = dbAuthors.push().key
        dbAuthors.child(locashan.id!!).setValue(locashan)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _result.value = null
                } else {
                    _result.value = it.exception
                }
            }
    }
    private val childEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {}

        override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {}

        override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
            val author = snapshot.getValue(Locashan::class.java)
            author?.id = snapshot.key
            _author.value = author
        }

        override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
            val author = snapshot.getValue(Locashan::class.java)
            author?.id = snapshot.key
            _author.value = author
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val author = snapshot.getValue(Locashan::class.java)
            author?.id = snapshot.key
            author?.isDeleted = true
            _author.value = author
        }
    }
    fun getRealtimeUpdates() {
        dbAuthors.addChildEventListener(childEventListener)
    }



    fun fetchLoc(){
        dbAuthors.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val locashans = mutableListOf<Locashan>()
                    for (locSnapshot in snapshot.children) {
                        val locashan = locSnapshot.getValue(Locashan::class.java)
                        locashan?.id = locSnapshot.key
                        locashan?.let { locashans.add(it) }
                    }
                    _locashans.value = locashans
                }
            }
        })
    }
    fun updateAuthor(author: Locashan) {
        dbAuthors.child(author.id!!).setValue(author)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _result.value = null
                    } else {
                        _result.value = it.exception
                    }
                }
    }
    fun deleteAuthor(author: Locashan) {
        dbAuthors.child(author.id!!).setValue(null)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _result.value = null
                    } else {
                        _result.value = it.exception
                    }
                }
    }

    override fun onCleared() {
        super.onCleared()
        dbAuthors.removeEventListener(childEventListener)
    }

}