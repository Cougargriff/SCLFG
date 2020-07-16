package org.griffin.sclfg.Models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MessageViewModel() : ViewModel() {
        companion object {
                fun messageToHash(msg: Message) : HashMap<String, Any> {
                        return hashMapOf(
                                "time" to msg.timeCreated,
                                "author" to msg.author,
                                "content" to msg.content
                        )
                }

                fun messageFromHash(result: DocumentSnapshot) : Message {
                        val time = result["time"] as Long
                        val author = result["author"].toString()
                        var content = result["content"].toString()
                        val mid = result.id

                        return Message(author, time, content, mid)
                }
        }

        private val db = Firebase.firestore
        private lateinit var gid : String

        fun setGid(id : String) {
                gid = id
        }
        private val msgs : MutableLiveData<List<Message>> by lazy {
                MutableLiveData<List<Message>>().also {
                        initMsgs()
                }
        }

        fun getMsgs() : LiveData<List<Message>> {
                return msgs
        }

       fun sendMessage(msg: Message, cb :() -> Unit) {
               db.collection("groups")
                       .document(gid)
                       .collection("messages")
                       .add(
                           messageToHash(
                               msg
                           )
                       )
                       .addOnSuccessListener {
                               /* should update on its own */
                               cb()
                       }
       }

        private fun msgListFromDocs(msgDocs : MutableList<DocumentSnapshot>) : ArrayList<Message> {
                val msgList = ArrayList<Message>()
                msgDocs.forEach {
                        msgList.add(
                            messageFromHash(
                                it
                            )
                        )
                }
                return msgList
        }

        private fun initMsgs() {
                db.collection("groups")
                        .document(gid).collection("messages")
                        .orderBy("time", Query.Direction.DESCENDING)
                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                val docs = querySnapshot!!.documents
                                var initMsgs = msgListFromDocs(docs)
                                msgs.value = initMsgs
                        }
        }

}