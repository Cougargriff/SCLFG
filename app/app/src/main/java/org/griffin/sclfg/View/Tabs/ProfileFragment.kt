package org.griffin.sclfg.View.Tabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.soundcloud.android.crop.Crop
import kotlinx.android.synthetic.main.tab_profile.*
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import java.io.File

/* TODO put active joined groups in profile */
/* TODO build out dedicated modal group screen */

class ProfileFragment : Fragment()
{
    private val PICK_PHOTO_TO_CROP = 0
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

        /* create cache file to store profile pic */
        val storageRef = Firebase.storage.reference.apply {
            child(vm.getUser().value!!.uid)
        }

        /* image caching and loading lib */
        Glide.with(this)
            .load(storageRef)
            .into(profileImage)


        profileImage.setOnClickListener {
            doImagePicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && data != null)
        {
            when(requestCode)
            {
                PICK_PHOTO_TO_CROP -> {
                    startImgCrop(data.data!!)
                }

                Crop.REQUEST_CROP -> {
                    /* handle cropped photo push to storage */
                    /* important to use Crop.getOutput(...) NOT data.data.... */
                    profileImage.setImageURI(Crop.getOutput(data))
                    pushImageToStorage(Crop.getOutput(data))
                }
            }
        }
    }

    private fun pushImageToStorage(uri: Uri)
    {
        val imgInputStream = requireContext().contentResolver.openInputStream(uri)
        Firebase.storage.reference.child(vm.getUser().value!!.uid).putStream(imgInputStream!!)
    }

    private fun startImgCrop(inputURI : Uri)
    {
        /* TODO fix outputURI?? is this whats wrong??? */
        val outputURI = Uri.fromFile(File(requireActivity().externalCacheDir, "cropped"))
        var cropIntent = Crop.of(inputURI, outputURI).asSquare().getIntent(requireContext())
        startActivityForResult(cropIntent, Crop.REQUEST_CROP)
    }

    private fun doImagePicker()
    {
        val imgPicker = Intent(Intent.ACTION_GET_CONTENT)
        imgPicker.setType("image/*")
        startActivityForResult(imgPicker, PICK_PHOTO_TO_CROP)
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