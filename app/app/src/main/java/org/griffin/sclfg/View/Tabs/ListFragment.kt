package org.griffin.sclfg.View.Tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

class ListFragment : Fragment()
{
    private val vm : ViewModel by activityViewModels()
    private lateinit var groupsList : List<Group>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_list, container, false)
        /* Setup Fragment View here */
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupVM()
    }

    private fun setupVM()
    {
        vm.getGroups().observe(viewLifecycleOwner, Observer {
            groupsList = it!!
        })
    }
}