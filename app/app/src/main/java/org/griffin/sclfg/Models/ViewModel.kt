package org.griffin.sclfg.Models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.CollectionReference

/* Need to pass Ship and Loc db ref to get lists */
class ViewModel(val shipRef : CollectionReference, val locRef : CollectionReference) : ViewModel()
{
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

    fun getShips(): LiveData<List<Ship>>
    {
        return ships
    }

    fun getLocs(): LiveData<List<Location>>
    {
        return locations
    }

    private fun loadLocs() {
        // Do an asynchronous operation to fetch users.
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

    private fun loadShips() {
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
