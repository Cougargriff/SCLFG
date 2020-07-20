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
class GroupViewModel : ViewModel(), CoroutineScope {
    companion object {

        fun findShip(searchName: String, shipList: List<Ship>): Ship? {
            for (ship in shipList) {
                if (ship.name.compareTo(searchName) == 0) {
                    return ship
                }
            }
            return null
        }

        fun findLoc(searchLoc: String, locList: List<Location>): Location? {
            for (loc in locList) {
                if (loc.name.compareTo(searchLoc) == 0) {
                    return loc
                }
            }
            return null
        }


        fun userFromHash(result: DocumentSnapshot): User {
            var time = result["timeCreated"].toString()
            var name = result["screenName"].toString()
            var inGroups = result["inGroups"] as ArrayList<String>
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
                "currCount" to grp.currCount,
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
            var currCnt = result["currCount"].toString()
            var playerList = result["playerList"] as ArrayList<String>
            var active = result["active"] as Boolean
            var createdBy = result["createdBy"] as String
            var description = result["description"] as String

            return Group(
                name, result.id, time, playerList, ship,
                loc, maxPlyr.toInt(), currCnt.toInt(),
                active, createdBy, description
            )
        }

    }
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val shipRef = db.collection("ships")
    private val locRef = db.collection("locations")
    private val userRef = db.collection("users")
    private val grpRef = db.collection("groups")


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    fun lookupUID(uid: String, cb: (name: String) -> Unit) {
        if (uid.isNotBlank()) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    val result = userFromHash(it)
                    cb(result.screenName)
                }
        }
    }

    val modifyGroup = fun(gid: String, action: GroupMod, err_cb: () -> Unit) {
        groupExists(gid, err_cb) {
            when (action) {
                GroupMod.MAKE_PRIVATE -> makePrivate(gid)
                GroupMod.MAKE_PUBLIC -> makePublic(gid)
                GroupMod.DELETE -> delete(gid)
            }
        }
    }

    private val user: MutableLiveData<User> by lazy {
        MutableLiveData<User>().also {
            loadUser()
        }
    }
    private val groups: MutableLiveData<List<Group>> by lazy {
        MutableLiveData<List<Group>>().also {
            launchLoadGroups()
        }
    }

    private val ships: MutableLiveData<List<Ship>> by lazy {
        MutableLiveData<List<Ship>>().also {
           launchLoadShips()
        }
    }

    private val locations: MutableLiveData<List<Location>> by lazy {
        MutableLiveData<List<Location>>().also {
            loadLocs()
        }
    }

    fun getUser(): LiveData<User> {
        return user
    }

    private fun launchLoadShips() {
        launch {
            loadShips {
                ships.value = it
            }
        }
    }

    fun launchLoadGroups() {
        launch {
            loadGroups {
                groups.value = it
            }
        }
    }

    fun findUser(uid: String, cb: (User) -> Unit) {
        if (uid.isNotBlank()) {
            userRef.document(uid).get()
                .addOnCompleteListener {
                    if (it.isSuccessful && it.result!!.exists()) {
                        cb(userFromHash(it.result!!))
                    }
                }
        }
    }

    fun updateScreenName(name: String) {
        userRef.document(auth.uid!!).set(
            hashMapOf(
                "screenName" to name
            ), SetOptions.merge()
        ).addOnCompleteListener {
            loadUser().also {
               launchLoadGroups()
            }
        }
    }


    fun addGroupToUser(gid: String) {
        var currIngroups = user.value!!.inGroups
        currIngroups.add(gid)
        userRef.document(user.value!!.uid)
            .set(
                hashMapOf(
                    "inGroups" to currIngroups
                ), SetOptions.merge()
            )
            .addOnCompleteListener {
                loadUser()
            }
    }


    fun getGroup(gid: String, cb: (MutableLiveData<Group>) -> Unit) {
        grpRef.document(gid).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            try {
                val result = groupFromHash(documentSnapshot!!)
                cb(MutableLiveData(result))
            } catch (err: Exception) {}
        }

    }

    fun removeGroupFromUser(gid: String, uid: String = user.value!!.uid) {
        var newIngroups = ArrayList<String>()
        user.value!!.inGroups.forEach {
            if (it.compareTo(gid) != 0) {
                newIngroups.add(it)
            }
        }
        userRef.document(uid)
            .set(
                hashMapOf(
                    "inGroups" to newIngroups
                ), SetOptions.merge()
            )
            .addOnCompleteListener {
                loadUser()
            }
    }


    fun joinGroup(gid: String, hash: HashMap<String, Serializable>, cb: () -> Unit) {
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                addGroupToUser(gid)
            }
            .addOnCompleteListener {
                cb()
            }
    }

    fun leaveGroup(gid: String, hash: HashMap<String, Serializable>, cb: () -> Unit) {
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                removeGroupFromUser(gid)
            }
            .addOnCompleteListener {
                cb()
            }
    }

    fun getGroups(): LiveData<List<Group>> {
        return groups
    }

    fun getShips(): LiveData<List<Ship>> {
        return ships
    }

    fun getLocs(): LiveData<List<Location>> {
        return locations
    }

    /**
    Checks to see if passed group with id (gid)  still exists in remote db.

    @param gid the id of the group to check
    @param err the callback function invoked when group doesn't exist
    @param cb callback invoked when group does exist
     */
    fun groupExists(gid: String, err: () -> Unit, cb: (DocumentSnapshot) -> Unit) {
        grpRef.document(gid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                var result = it.result!! /* QuerySnapshot */
                var grpDoc = result
                if (grpDoc.exists()) {
                    /* pass most current document to update */
                    cb(grpDoc)
                } else {
                    /* load updated groups to flush out non-existent group entries locally */
                   launchLoadGroups()
                    /* Error callback to alert user that group doesn't exist anymore */
                    err()
                }
            }
        }
    }

    private fun delete(gid: String) {
        grpRef.document(gid).get().addOnSuccessListener {
            var grp = groupFromHash(it)
            grpRef.document(gid).delete()
                .addOnSuccessListener {
                    /* remove group from possible in groups */
                    grp.playerList.forEach {
                        removeGroupFromUser(gid, it)
                    }
                   launchLoadGroups()
                }
        }
    }

    private fun makePublic(gid: String) {
        val hash = hashMapOf(
            "active" to true
        )
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                launchLoadGroups()
            }
    }

    private fun makePrivate(gid: String) {
        grpRef.document(gid)
            .set(hashMapOf(
                "active" to false
            ), SetOptions.merge())
            .addOnSuccessListener {
                launchLoadGroups()
            }
    }

    fun pushGroup(grp: Group, uiCb: () -> Unit, cb: (gid: String) -> Unit) {

        val grpHash = groupToHash(grp, auth.uid!!)

        grpRef.add(grpHash).addOnCompleteListener {
            if (it.isSuccessful) {
                val resultId = it.result!!.id
                cb(resultId)
            }
        }
       launchLoadGroups()
    }

    fun syncInGroups(tUser: User) {
        val grps = groups.value
        var temp_user = tUser
        var temp_gids = ArrayList<String>()
        grps!!.forEach {
            temp_gids.add(it.gid)
        }

        tUser.inGroups.forEachIndexed { index, gid ->
            if (temp_gids.contains(gid)) {
                temp_user.inGroups.removeAt(index)
            }
        }

        user.value = temp_user
        userRef.document(tUser.uid).set(
            hashMapOf(
                "inGroups" to temp_gids
            ), SetOptions.merge()
        )
    }

    private fun loadUser() {
        userRef.document(auth.uid!!).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    var result = it.result!!
                    if (result.exists()) {
                        user.value = userFromHash(result)
                    } else {
                        initUser("")
                    }
                    launchLoadGroups()
                }
            }
    }

    fun initUser(displayName : String, cb: () -> Unit = {}) {
        userRef.document(auth.uid!!).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    var result = it.result!!
                    /* create the user if they don't exist already */
                    if (!result.exists()) {
                        var initUser = hashMapOf(
                            "timeCreated" to System.currentTimeMillis().toString(),
                            "inGroups" to emptyList<String>(),
                            "screenName" to displayName
                        )
                        userRef.document(auth.uid!!).set(initUser).also {
                            cb()
                        }
                    }
                }
            }
        getUser()
    }

    private fun groupListFromDocs(grpDocs: MutableList<DocumentSnapshot>): ArrayList<Group> {
        var grpList = ArrayList<Group>()
        for (grp in grpDocs) {
            grpList.add(groupFromHash(grp))
        }
        return grpList
    }

    /* loads oldest first */
    private suspend fun loadGroups(cb: (ArrayList<Group>) -> Unit) {
        /* Listens to db changes and reloads groups */
        grpRef.orderBy("timeCreated", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                /* Listen for changes and update groups list */
                val docs = querySnapshot!!.documents
                var grpList = groupListFromDocs(docs)
                groups.value = grpList
            }

        try {
            val grps = grpRef.orderBy("timeCreated", Query.Direction.ASCENDING)
                .get()
                .await()
            cb(groupListFromDocs(grps.documents))
        } catch (e : Exception) {

        }
    }

    private fun loadLocs() {

        locRef.get()
            .addOnCompleteListener {
                /* Sanity Check */
                if (it.isSuccessful && !it.result!!.isEmpty) {
                    var result = it.result!! /* QuerySnapshot */
                    var locDocs = result.documents /* Ships in collection */
                    var locList = ArrayList<Location>()
                    for (loc in locDocs) {
                        var name = loc["name"] as String
                        /* Save the current ship to the list */
                        locList.add(Location(name))
                    }
                    /* Update our new ship list */
                    locations.value = locList
                }
            }
    }

    private suspend fun loadShips(cb: (ship : ArrayList<Ship>) -> Unit) {

        try {
            val ships = shipRef.get().await()
            cb(ArrayList(ships.documents.toList().map {
                val ship = it
                val name = ship["name"] as String
                val mass = ship["mass"] as String
                val manu = ship["manufacturer"] as String
                val price = ship["price"] as String
                val prod = ship["prod_state"] as String
                val role = ship["role"] as String
                val size = ship["size"] as String
                Ship(name, manu, mass, price, prod, role, size)
            }))
        } catch (e : Exception) {

        }
    }



}
