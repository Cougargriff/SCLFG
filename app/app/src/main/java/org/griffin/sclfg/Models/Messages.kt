package org.griffin.sclfg.Models

import com.google.firebase.firestore.DocumentSnapshot
class Messages  {
    companion object {
        fun messageToHash(msg: Message): HashMap<String, Any> {
            return hashMapOf(
                "time" to msg.timeCreated,
                "author" to msg.author,
                "content" to msg.content
            )
        }

        fun messageFromHash(result: DocumentSnapshot): Message {
            val time = result["time"] as Long
            val author = result["author"].toString()
            var content = result["content"].toString()
            val mid = result.id

            return Message(author, time, content, mid)
        }
    }
}