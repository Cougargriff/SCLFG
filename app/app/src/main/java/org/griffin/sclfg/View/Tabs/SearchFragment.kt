package org.griffin.sclfg.View.Tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.tab_search.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.Location
import org.griffin.sclfg.Models.Ship
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

class SearchFragment : Fragment()
{
    private val vm : ViewModel by activityViewModels()
    private lateinit var shipList : List<Ship>
    private lateinit var locList : List<Location>
    private var SHIPS = listOf("")
    private var LOCS = listOf("")
    private lateinit var shipAdapter : ArrayAdapter<String>
    private lateinit var locAdapter : ArrayAdapter<String>
    private lateinit var acView : AutoCompleteTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_search, container, false)
        /* Setup Fragment View here */

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        handleCreate()
        setupVM()
        setupAutoComplete()
    }

    private fun handleCreate()
    {
        groupCreateButton.setOnClickListener {
            /* Check for empty box field */
            if(!(groupBox.text.isBlank() || shipSearchBox.text.isBlank() ||
                locSearchBox.text.isBlank() || roleBox.text.isBlank()))
            {

                var newGroup = Group(groupBox.text.toString(), System.currentTimeMillis(),
                    listOf(""), findShip(shipSearchBox.text.toString())!!,
                    findLoc(locSearchBox.text.toString())!!, playNumSelector.value, 1)
            }
        }
    }

    private fun findLoc(searchLoc : String) : Location?
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

    private fun findShip(searchName : String) : Ship?
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

    private fun setupVM()
    {
        vm.getShips().observe(viewLifecycleOwner, Observer {
            shipList = it!!
            updateAutoCompleteShips()
        })

        vm.getLocs().observe(viewLifecycleOwner, Observer {
            locList = it!!
            updateAutoCompleteLocs()
        })
    }

    private fun setupAutoComplete()
    {
        shipAdapter = ArrayAdapter<String>(this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, SHIPS)
        shipSearchBox.setAdapter(shipAdapter)

        locAdapter = ArrayAdapter<String>(this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, LOCS)
        locSearchBox.setAdapter(locAdapter)
    }

    private fun updateAutoCompleteShips()
    {
        var temp = ArrayList<String>()
        for(ship in shipList)
        {
            temp.add(ship.name)
        }
        SHIPS = temp

        shipAdapter = ArrayAdapter(this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, SHIPS)

        shipSearchBox.setAdapter(shipAdapter)
    }

    private fun updateAutoCompleteLocs()
    {
        var temp = ArrayList<String>()
        for(loc in locList)
        {
            temp.add(loc.name)
        }
        LOCS = temp

        locAdapter = ArrayAdapter(this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, LOCS)

        locSearchBox.setAdapter(locAdapter)
    }
}