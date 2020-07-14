package org.griffin.sclfg.View.GroupView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

class AboutFragment(val gid : String): Fragment() {

    private val vm: ViewModel by activityViewModels()
    private var user = User("Loading...", "loading", ArrayList(), -1)
    private lateinit var group : Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.tab_about, container, false)

        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupVM()

    }

    private fun setupVM() {
        vm.getUser().observe(viewLifecycleOwner, Observer {
            user = it
        })

    }
}