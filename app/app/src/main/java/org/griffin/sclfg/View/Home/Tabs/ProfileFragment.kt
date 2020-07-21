package org.griffin.sclfg.View.Home.Tabs

import android.animation.Animator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.soundcloud.android.crop.Crop
import kotlinx.android.synthetic.main.tab_profile.*
import kotlinx.android.synthetic.main.tab_profile.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupMod
import org.griffin.sclfg.Models.GroupViewModel
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.R
import org.griffin.sclfg.Redux.Thunks.*
import org.griffin.sclfg.Redux.configureStore
import org.griffin.sclfg.Redux.store
import org.griffin.sclfg.Utils.Adapters.GroupListAdapter
import org.griffin.sclfg.Utils.Adapters.ProfileAdapter
import org.griffin.sclfg.View.Group.GroupActivity
import java.io.File
import java.io.InputStream

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

class ProfileFragment : Fragment() {
    private val PICK_PHOTO_TO_CROP = 0
    private var user = User("", "", ArrayList(), 0)

    /* Recycler View Setup */
    private lateinit var rv: RecyclerView
    private lateinit var rvManager: RecyclerView.LayoutManager
    private lateinit var rvAdapter: RecyclerView.Adapter<*>

    private var groupsList = ArrayList<Group>()

    private val err_cb = fun() {
        Toast.makeText(requireContext(), "Group No Longer Exists", Toast.LENGTH_LONG)
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.tab_profile, container, false)

        /* Setup Fragment View here */
        rv = view.my_groups
        rvManager = LinearLayoutManager(context)

        rvAdapter = ProfileAdapter(ArrayList(), user, {gid, action, err_cb ->
            when(action) {
                GroupMod.MAKE_PRIVATE -> store.dispatch(setPrivate(gid))
                GroupMod.MAKE_PUBLIC -> store.dispatch(setPublic(gid))
                GroupMod.DELETE -> store.dispatch(delete(gid))
            }
        }, err_cb, openModal)

        rv.apply {
            layoutManager = rvManager
            adapter = rvAdapter
        }

        view.loading_profile_groups.apply {
            setAnimation("register_loading.json")
            speed = 2f
            playAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    loading_profile_groups.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator?) = Unit
                override fun onAnimationRepeat(animation: Animator?) = Unit
                override fun onAnimationStart(animation: Animator?) = Unit
            })
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        SetupRedux()

        profile_animate.apply {
            setAnimation("cell_animate.json")
            speed = 1.5f
            loop(true)
            playAnimation()
        }

        little_ship.apply {
            setAnimation("stars_profile.json")
            speed = 0.4f
            loop(true)
            playAnimation()
        }

        try {
            asyncLoadProfileImg()
            profileImage.setOnClickListener {

                /* Confirm selection with alertDialog */
                var dialog = AlertDialog.Builder(this.requireContext()).apply {
                    setTitle("Choose a new profile image?")
                    setPositiveButton("Yes") { dialog, which ->
                        /* Continue to image picker on confirm */
                        doImagePicker()
                    }
                    setNegativeButton("Cancel") { dialog, which ->
                        /* Do nothing and return to profile fragment*/
                    }
                }

                dialog.show().apply {
                    this.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.iosBlue))
                    this.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.iosBlue))
                }
            }
        } catch (err: Exception) {
            profileImage.setImageResource(R.drawable.astro_prof)
        }

    }

    private fun SetupRedux() {
        store.subscribe {
            requireActivity().runOnUiThread {
                render(store.getState().groups)
                render(store.getState().user)
            }

        }
       //store.dispatch(getUser())
       //store.dispatch(getGroups())
    }

    private fun render(newUser : User) {
        user = newUser
        /*
            possible in the future to just make local
            call to function for getGroups instead of vm call
         */
        (rv.adapter as ProfileAdapter).update(groupsList as ArrayList<Group>)
        nameChange.text = user.screenName
        numIn.text = "In ${user.inGroups.size} Groups"
        (rv.adapter as ProfileAdapter).apply {
            authUser = user
            notifyDataSetChanged()
        }
    }

    private fun render(newGroups : ArrayList<Group>) {
        groupsList = ArrayList(newGroups.filter {  store.getState().user.inGroups.contains(it.gid)})

        (rv.adapter as ProfileAdapter).apply {
            update(groupsList as ArrayList<Group>)
        }
    }

    private val openModal = fun(gid: String) {
        var intent = Intent(requireActivity(), GroupActivity::class.java)
        intent.putExtra("gid", gid)
        ContextCompat.startActivity(requireContext(), intent, null)
    }

    /* TODO fallback image now shows. Check to see if upload will refresh */
    private fun asyncLoadProfileImg() {
        /* create cache file to store profile pic */
        val storageRef = Firebase.storage.reference.child(store.state.user.uid)

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
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(R.drawable.astro_prof)
            .into(profileImage)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
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

    private fun pushImageToStorage(uri: Uri) {
        val imgInputStream = requireContext().contentResolver.openInputStream(uri)
        Firebase.storage.reference.child(store.getState().user.uid).putStream(imgInputStream!!)
    }

    private fun startImgCrop(inputURI: Uri) {
        val outputURI = Uri.fromFile(File(requireActivity().externalCacheDir, "cropped"))
        var cropIntent = Crop.of(inputURI, outputURI).asSquare().getIntent(requireContext())
        startActivityForResult(cropIntent, Crop.REQUEST_CROP)
    }

    private fun doImagePicker() {
        val imgPicker = Intent(Intent.ACTION_GET_CONTENT)
        imgPicker.type = "image/*"
        startActivityForResult(imgPicker, PICK_PHOTO_TO_CROP)
    }
}


