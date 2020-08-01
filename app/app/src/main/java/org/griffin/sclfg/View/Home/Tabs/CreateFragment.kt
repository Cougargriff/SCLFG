package org.griffin.sclfg.View.Home.Tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.tab_create.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.Location
import org.griffin.sclfg.Models.Ship
import org.griffin.sclfg.R
import org.griffin.sclfg.Redux.Thunks.createGroup
import org.griffin.sclfg.Redux.store

class CreateFragment : Fragment() {
    private lateinit var shipList: List<Ship>
    private lateinit var locList: List<Location>
    private var SHIPS = listOf("")
    private var LOCS = listOf("")
    private lateinit var shipAdapter: ArrayAdapter<String>
    private lateinit var locAdapter: ArrayAdapter<String>
    private lateinit var acView: AutoCompleteTextView
    private val EMPTY = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.tab_create, container, false)
        /* Setup Fragment View here */
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        handleCreate()
        setupAutoComplete()

        SetupRedux()
    }


    private fun handleCreate() {
        groupCreateButton.setOnClickListener {
            /* Check for empty box field */
            if (!(groupBox.text.isBlank() || shipSearchBox.text.isBlank() ||
                        locSearchBox.text.isBlank() || descriptionBox.text.isBlank())
            ) {

                var newGroup = Group(
                    groupBox.text.toString().trim(), "", System.currentTimeMillis(),
                    ArrayList(), shipSearchBox.text.toString().trim(),
                    locSearchBox.text.toString().trim(),
                    playNumSelector.value, true, "",
                    descriptionBox.text.toString()
                )

                store.dispatch(createGroup(newGroup, resetTextBoxes))
                /* resetTextBoxes -> UI update on success callback */
                Toast.makeText(requireContext(), newGroup.name + " Created!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private var resetTextBoxes = fun() {
        requireActivity().runOnUiThread {
            groupBox.text.clear()
            shipSearchBox.text.clear()
            locSearchBox.text.clear()
            descriptionBox.text.clear()
        }

    }

    private fun SetupRedux() {
        store.subscribe {
            try {
                render(ShipList(store.getState().ships))
                render(LocsList(store.getState().locations))
            } catch (e : Exception) {}
        }
    }
   data class ShipList(val list : ArrayList<Ship>?)
    private fun render(ships : ShipList) {
        try {
            shipList = ships.list!!.toList()
            updateAutoCompleteShips()
        } catch (e : Exception) {}
    }

    data class LocsList(val list : ArrayList<Location>?)
    private fun render(locs : LocsList) {
        try {
            locList = locs.list!!.toList()
            updateAutoCompleteLocs()
        } catch (e : Exception) {}
    }

    private fun setupAutoComplete() {
        shipSearchBox.nextFocusForwardId = locSearchBox.id
        shipAdapter = ArrayAdapter<String>(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, SHIPS
        )
        shipSearchBox.setAdapter(shipAdapter)

        locSearchBox.nextFocusForwardId = descriptionBox.id
        locAdapter = ArrayAdapter<String>(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, LOCS
        )
        locSearchBox.setAdapter(locAdapter)
    }

    private fun updateAutoCompleteShips() {
        var temp = ArrayList<String>()
        for (ship in shipList) {
            temp.add(ship.name)
        }
        SHIPS = temp.toList()

        shipAdapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, SHIPS
        )

        shipSearchBox.setAdapter(shipAdapter)
    }

    private fun updateAutoCompleteLocs() {
        var temp = ArrayList<String>()
        for (loc in locList) {
            temp.add(loc.name)
        }
        LOCS = temp.toList()

        locAdapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, LOCS
        )

        locSearchBox.setAdapter(locAdapter)
    }
}