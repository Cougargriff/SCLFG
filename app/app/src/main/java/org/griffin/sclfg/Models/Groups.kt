package org.griffin.sclfg.Models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

/* Need to pass Ship and Loc db ref to get lists */
class Groups : CoroutineScope {
    companion object {
        fun userFromHash(result: DocumentSnapshot): User {
            var time = result["timeCreated"].toString()
            var name = result["screenName"].toString()
            var inGroups : ArrayList<String>
            inGroups = try {
                result["inGroups"] as ArrayList<String>
            } catch (e : Exception) {
                ArrayList()
            }
            return User(name, result.id, inGroups, time.toLong())
        }

        fun groupToHash(grp: Group, uid: String): HashMap<String, Any> {
            return hashMapOf(
                "name" to grp.name,
                "timeCreated" to grp.timeCreated,
                "createdBy" to uid,
                "ship" to grp.ship,
                "location" to grp.loc,
                "maxPlayers" to grp.maxPlayers,
                "playerList" to listOf(uid), /* init group with creator as only user */
                "active" to grp.active,
                "description" to grp.description
            )
        }

        fun groupFromHash(result: DocumentSnapshot): Group {
            var name = result["name"].toString()
            var time = result["timeCreated"].toString().toLong()
            var ship = result["ship"].toString()
            var loc = result["location"].toString()
            var maxPlyr = result["maxPlayers"].toString()
            var playerList = result["playerList"] as ArrayList<String>
            var active = result["active"] as Boolean
            var createdBy = result["createdBy"] as String
            var description = result["description"] as String

            return Group(
                name, result.id, time, playerList, ship,
                loc, maxPlyr.toInt(),
                active, createdBy, description
            )
        }

    }

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userRef = db.collection("users")

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    suspend fun initUser(displayName : String, cb: () -> Unit = {}) {
        try {
            val user = userRef.document(auth.uid!!).get().await()
            if(!user.exists()) {
                val initUser = hashMapOf(
                    "timeCreated" to System.currentTimeMillis().toString(),
                    "inGroups" to emptyList<String>(),
                    "screenName" to displayName
                )
                userRef.document(auth.uid!!).set(initUser).also {
                    cb()
                }
            }
        } catch (e : Exception) {}
    }




}
