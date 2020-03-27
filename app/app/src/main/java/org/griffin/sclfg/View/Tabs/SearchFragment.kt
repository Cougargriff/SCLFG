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
import org.griffin.sclfg.Models.Ship
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

class SearchFragment : Fragment()
{
    private val vm : ViewModel by activityViewModels()
    private lateinit var shipList : List<Ship>
    private var SHIPS = listOf("")
    private lateinit var aAdapter : ArrayAdapter<String>
    private lateinit var acView : AutoCompleteTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_search, container, false)

        /* Setup Fragment View here */
        vm.getShips().observe(viewLifecycleOwner, Observer {
            shipList = it!!
        })
        return view
    }

    private fun setupAutoComplete()
    {
        aAdapter = ArrayAdapter<String>(this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, SHIPS)
        acView = shipSearchBox as AutoCompleteTextView
        acView.setAdapter(aAdapter)
    }

    private fun updateAutoCompletes()
    {
        var temp = ArrayList<String>()
        for(ship in shipList)
        {
            temp.add(ship.name)
        }
        SHIPS = temp

        aAdapter = ArrayAdapter<String>(this.requireContext(),
            android.R.layout.simple_dropdown_item_1line, SHIPS)

        shipSearchBox.setAdapter(aAdapter)
    }
}