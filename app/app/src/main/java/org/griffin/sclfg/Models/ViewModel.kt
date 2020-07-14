package org.griffin.sclfg.Models

import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.grpc.Context
import java.io.Serializable

/* Need to pass Ship and Loc db ref to get lists */
class ViewModel : ViewModel()
{
    companion object {

        fun findShip(searchName : String, shipList : List<Ship>) : Ship?
        {
            for(ship in shipList)
            {
                if(ship.name.compareTo(searchName) == 0)
                {
                    return ship
                }
            }
            return null
        }

        fun findLoc(searchLoc : String, locList : List<Location>) : Location?
        {
            for(loc in locList)
            {
                if(loc.name.compareTo(searchLoc) == 0)
                {
                    return loc
                }
            }
            return null
        }

        fun userFromHash(result : DocumentSnapshot) : User
        {
            var time = result["timeCreated"].toString()
            var name = result["screenName"].toString()
            var inGroups = result["inGroups"] as ArrayList<String>
            return User(name, result.id, inGroups, time.toLong())
        }


        fun groupFromHash(result : DocumentSnapshot) : Group
        {
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

            return Group(name, result.id, time, playerList, ship,
                loc, maxPlyr.toInt(), currCnt.toInt(),
                active, createdBy, description)
        }

    }

    private lateinit var shipRef : CollectionReference
    private lateinit var locRef: CollectionReference
    private lateinit var grpRef: CollectionReference
    private lateinit var userRef: CollectionReference

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val user: MutableLiveData<User> by lazy {
        MutableLiveData<User>().also {
            loadUser()
        }
    }
    private val groups: MutableLiveData<List<Group>> by lazy {
        MutableLiveData<List<Group>>().also {
            loadGroups()
        }
    }

    private val ships: MutableLiveData<List<Ship>> by lazy {
        MutableLiveData<List<Ship>>().also {
            loadShips()
        }
    }

    private val locations: MutableLiveData<List<Location>> by lazy {
        MutableLiveData<List<Location>>().also {
            loadLocs()
        }
    }

    fun getUser(): LiveData<User>
    {
        return user
    }

    fun update()
    {
        loadGroups()
    }

    fun findUser(UID : String, cb: (User) -> Unit)
    {
        userRef.document(UID).get()
            .addOnCompleteListener {
                if(it.isSuccessful && it.result!!.exists())
                {
                    cb(userFromHash(it.result!!))
                }
            }
    }

    fun updateScreenName(name : String)
    {
        userRef.document(auth.uid!!).set(hashMapOf(
            "screenName" to name
        ), SetOptions.merge()).addOnCompleteListener {
            loadUser().also {
                loadGroups()
            }
        }
    }

    fun addGroupToUser(gid : String) {
        var curr_inGroups = user.value!!.inGroups
        curr_inGroups.add(gid)
        userRef.document(user.value!!.uid)
            .set(hashMapOf(
                "inGroups" to curr_inGroups
            ), SetOptions.merge())
            .addOnCompleteListener {
                loadUser()
                loadGroups()
            }
    }

    fun removeGroupFromUser(gid : String, uid : String = user.value!!.uid) {
        var new_inGroups = ArrayList<String>()
        user.value!!.inGroups.forEach {
            if (it.compareTo(gid) != 0) {
                new_inGroups.add(it)
            }
        }
        userRef.document(uid)
            .set(hashMapOf(
                "inGroups" to new_inGroups
            ), SetOptions.merge())
            .addOnCompleteListener {
                loadUser()
                loadGroups()
            }
    }



    fun joinGroup(gid : String, hash : HashMap<String, Serializable>, cb: () -> Unit)
    {
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                addGroupToUser(gid)
                loadGroups()
            }
            .addOnCompleteListener {
                cb()
            }
    }

    fun leaveGroup(gid: String, hash: HashMap<String, Serializable>, cb: () -> Unit)
    {
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                removeGroupFromUser(gid)
                loadGroups()
            }
            .addOnCompleteListener {
                cb()
            }
    }

    fun getGroups(): LiveData<List<Group>>
    {
        return groups
    }

    fun getShips(): LiveData<List<Ship>>
    {
        return ships
    }

    fun getLocs(): LiveData<List<Location>>
    {
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
            if(it.isSuccessful)
            {
                var result = it.result!! /* QuerySnapshot */
                var grpDoc = result
                if(grpDoc.exists()) {
                    /* pass most current document to update */
                    cb(grpDoc)
                }
                else
                {
                    /* load updated groups to flush out non-existent group entries locally */
                    loadGroups()
                    /* Error callback to alert user that group doesn't exist anymore */
                    err()
                }
            }
        }
    }

    fun delete(gid : String) {

        grpRef.document(gid).get().addOnSuccessListener {
            var grp = groupFromHash(it)
            grpRef.document(gid).delete()
                .addOnSuccessListener {
                    /* remove group from possible in groups */
                    grp.playerList.forEach {
                        removeGroupFromUser(gid, it)
                    }
                    loadGroups()
                }
        }



    }

    fun makePublic(gid : String) {
        val hash = hashMapOf(
            "active" to true
        )
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                loadGroups()
            }
    }

    fun makePrivate(gid : String) {
        val hash = hashMapOf(
            "active" to false
        )
        grpRef.document(gid)
            .set(hash, SetOptions.merge())
            .addOnSuccessListener {
                loadGroups()
            }
    }

    fun pushGroup(grp : Group, ui_cb : () -> Unit, cb: (gid : String) -> Unit)
    {
        var grpHash = hashMapOf(
            "name" to grp.name,
            "timeCreated" to grp.timeCreated,
            "createdBy" to auth.uid,
            "ship" to grp.ship,
            "location" to grp.loc,
            "maxPlayers" to grp.maxPlayers,
            "currCount" to grp.currCount,
            "playerList" to listOf(auth.uid), /* init group with creator as only user */
            "active" to grp.active,
            "description" to grp.description
        )

        grpRef.add(grpHash).addOnCompleteListener {
            if(it.isSuccessful) {
                cb(it.result!!.id)
            }
        }
        loadGroups()
        ui_cb()
    }

    private fun loadUser()
    {
        userRef = db.collection("users")
        userRef.document(auth.uid!!).get()
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    var result = it.result!!
                    if(result.exists())
                    {
                        user.value = userFromHash(result)
                    }
                    else
                    {
                        initUser()
                    }
                }
            }
    }

    private fun initUser()
    {
        userRef.document(auth.uid!!).get()
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    var result = it.result!!
                    /* create the user if they don't exist already */
                    if(!result.exists())
                    {
                        var initUser = hashMapOf(
                            "timeCreated" to System.currentTimeMillis().toString(),
                            "inGroups" to emptyList<String>(),
                            "screenName" to "ANONYMOUS"
                        )
                        userRef.document(auth.uid!!).set(initUser)
                    }
                }
            }
        getUser()
    }

    private fun groupListFromDocs(grpDocs: MutableList<DocumentSnapshot>) : ArrayList<Group> {
        var grpList = ArrayList<Group>()
        for(grp in grpDocs)
        {
            grpList.add(groupFromHash(grp))
        }
        return grpList
    }
    /* loads oldest first */
    private fun loadGroups()
    
        grpRef = db.collection("groups")
        grpRef.orderBy("timeCreated", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            /* Listen for changes and update groups list */
            val docs = querySnapshot!!.documents
            var grpList = groupListFromDocs(docs)
            groups.value = grpList
        }

        grpRef.orderBy("timeCreated", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener {
                /* Sanity Check */
                if(it.isSuccessful)
                {
                    var result = it.result!! /* QuerySnapshot */
                    var grpDocs = result.documents
                    var grpList = groupListFromDocs(grpDocs)
                    /* Update our groups list */
                    groups.value = grpList
                }
            }
    }

    private fun loadLocs()
    {
        locRef = db.collection("locations")

        locRef.get()
            .addOnCompleteListener {
                /* Sanity Check */
                if(it.isSuccessful && !it.result!!.isEmpty)
                {
                    var result = it.result!! /* QuerySnapshot */
                    var locDocs = result.documents /* Ships in collection */
                    var locList = ArrayList<Location>()
                    for(loc in locDocs)
                    {
                        var name = loc["name"] as String
                        /* Save the current ship to the list */
                        locList.add(Location(name))
                    }
                    /* Update our new ship list */
                    locations.value = locList
                }
            }
    }

    private fun loadShips()
    {
        shipRef = db.collection("ships")
        // Do an asynchronous operation to fetch users.
        shipRef.get()
            .addOnCompleteListener {
                /* Sanity Check */
                if(it.isSuccessful && !it.result!!.isEmpty)
                {
                    var result = it.result!! /* QuerySnapshot */
                    var shipDocs = result.documents /* Ships in collection */
                    var shipList = ArrayList<Ship>()
                    for(ship in shipDocs)
                    {
                        var name = ship["name"] as String
                        var mass = ship["mass"] as String
                        var manu = ship["manufacturer"] as String
                        var price = ship["price"] as String
                        var prod = ship["prod_state"] as String
                        var role = ship["role"] as String
                        var size = ship["size"] as String
                        /* Save the current ship to the list */
                        shipList.add(Ship(name, manu, mass, price, prod, role, size))
                    }
                    /* Update our new ship list */
                    ships.value = shipList
                }
            }
    }
}
