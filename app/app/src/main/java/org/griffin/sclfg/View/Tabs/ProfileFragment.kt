package org.griffin.sclfg.View.Tabs

import android.app.Activity
import android.content.Intent
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
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.tab_profile.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

/* TODO put active joined groups in profile */
/* TODO build out dedicated modal group screen */

class ProfileFragment : Fragment()
{
    private val PICK_PHOTO_FOR_PROF_PIC = 0
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

        profileImage.setOnClickListener {
            doImagePicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PHOTO_FOR_PROF_PIC && resultCode == Activity.RESULT_OK)
        {
            if(data == null)
                return

            val imgInputStream = requireContext().contentResolver.openInputStream(data.data!!)

            Firebase.storage.reference.child(vm.getUser().value!!.uid).putStream(imgInputStream!!)

        }

    }

    private fun doImagePicker()
    {
        val imgPicker = Intent(Intent.ACTION_GET_CONTENT)
        imgPicker.setType("image/*")
        startActivityForResult(imgPicker, PICK_PHOTO_FOR_PROF_PIC)
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