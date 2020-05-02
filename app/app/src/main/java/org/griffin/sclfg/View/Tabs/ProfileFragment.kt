package org.griffin.sclfg.View.Tabs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.soundcloud.android.crop.Crop
import kotlinx.android.synthetic.main.tab_profile.*
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import java.io.File
import java.io.InputStream


/* TODO put active joined groups in profile ? */
/* TODO build out dedicated modal group screen */

/* TODO others joined group notification? */

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}

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

        asyncLoadProfileImg()

        profileImage.setOnClickListener {
            doImagePicker()
        }
    }

    private fun asyncLoadProfileImg()
    {
        /* create cache file to store profile pic */
        val storageRef = Firebase.storage.reference.child(vm.getUser().value!!.uid)

        /* image caching and loading lib */
        val glidePlaceholder = CircularProgressDrawable(requireContext()).apply {
            strokeWidth = 5f
            setColorSchemeColors(Color.WHITE)
            centerRadius = 30f
            start()
        }

        Glide.with(requireContext())
            .load(storageRef)
            .placeholder(glidePlaceholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE) /* TODO change to check each day */
            .into(profileImage)
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

    private fun setupVM()
    {
        vm.getUser().observe(viewLifecycleOwner, Observer {
            user = it!!
            nameChange.text = user.screenName
        })
    }
}