package org.griffin.sclfg.View.Tabs

import android.os.Bundle
import android.renderscript.Allocation
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.tab_profile.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

class ProfileFragment : Fragment()
{
    private val vm : ViewModel by activityViewModels()
    private lateinit var user : User
    private val userRef = Firebase.firestore.collection("users")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_profile, container, false)

        /* Setup Fragment View here */

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupVM()
        setupNameChange()
    }

    private fun setupNameChange()
    {
        changeButton.setOnClickListener {
            if(!nameChange.text.isBlank())
            {
                vm.updateScreenName(nameChange.text.toString())
            }
        }
    }

    private fun setupVM()
    {
        vm.getUser().observe(viewLifecycleOwner, Observer {
            user = it!!
            nameChange.text.replace(0,nameChange.text.toString().length,
                user.screenName)
        })
    }
}