package org.griffin.sclfg.Models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
            return User(name, time.toLong())
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


    private fun findUser(UID : String, cb: (User) -> Unit)
    {
        if(UID.compareTo(auth.uid!!) == 0)
        {
            return cb(user.value!!)
        }
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
        ), SetOptions.merge())
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

    fun pushGroup(grp : Group, cb : () -> Unit)
    {
        var grpHash = hashMapOf(
            "name" to grp.name,
            "timeCreated" to grp.timeCreated,
            "ship" to grp.ship.name,
            "location" to grp.loc.name,
            "maxPlayers" to grp.maxPlayers,
            "currCount" to grp.currCount,
            "playerList" to listOf(auth.uid),
            "active" to grp.active
        )

        grpRef.add(grpHash)
        loadGroups()
        cb()
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
                            "screenName" to ""
                        )
                        userRef.document(auth.uid!!).set(initUser)
                    }
                }
            }
    }

    private fun loadGroups()
    {

        grpRef = db.collection("groups")

        grpRef.get()
            .addOnCompleteListener {
                /* Sanity Check */
                if(it.isSuccessful && !it.result!!.isEmpty)
                {
                    var result = it.result!! /* QuerySnapshot */
                    var grpDocs = result.documents /* Ships in collection */
                    var grpList = ArrayList<Group>()
                    for(grp in grpDocs)
                    {
                        var name = grp["name"].toString()
                        var time = grp["timeCreated"].toString().toLong()
                        var ship = grp["ship"].toString()
                        var loc = grp["location"].toString()
                        var maxPlyr = grp["maxPlayers"].toString()
                        var currCnt = grp["currCount"].toString()
                        var playerList = grp["playerList"] as List<String>
                        var active = grp["active"] as Boolean

                        grpList.add(Group(name, time, playerList, findShip(ship, ships.value!!)!!,
                            findLoc(loc, locations.value!!)!!, maxPlyr.toInt(), currCnt.toInt(),
                            active))
                    }
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
