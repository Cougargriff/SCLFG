package org.griffin.sclfg.View.Home.Tabs

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
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
import kotlinx.android.synthetic.main.fragment_message.view.*
import kotlinx.android.synthetic.main.profile_group_cell.view.*
import kotlinx.android.synthetic.main.tab_profile.*
import kotlinx.android.synthetic.main.tab_profile.numIn
import kotlinx.android.synthetic.main.tab_profile.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupMod
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import org.griffin.sclfg.Utils.Gestures.SwipeToDeleteCallback
import org.griffin.sclfg.View.GroupView.ModalGroupActivity
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
    private val vm: ViewModel by activityViewModels()
    private var user = User("", "", ArrayList(), 0)

    /* Recycler View Setup */
    private lateinit var rv: RecyclerView
    private lateinit var rvManager: RecyclerView.LayoutManager
    private lateinit var rvAdapter: RecyclerView.Adapter<*>

    private var groupsList = emptyList<Group>()

    private val modifyGroup = fun(gid: String, action: GroupMod) {
        vm.groupExists(gid, err_cb) {
            when (action) {
                GroupMod.MAKE_PRIVATE -> vm.makePrivate(gid)
                GroupMod.MAKE_PUBLIC -> vm.makePublic(gid)
                GroupMod.DELETE -> vm.delete(gid)
            }
        }
    }

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

        rvAdapter = GListAdapter(ArrayList(), user, modifyGroup, openModal)

        rv.apply {
            layoutManager = rvManager
            adapter = rvAdapter
        }



        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv.adapter as GListAdapter
                val group = adapter.groupList[viewHolder.adapterPosition]
                var dialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Delete " + group.name + "?")
                    setPositiveButton("Yes") { dialog, which ->
                        adapter.removeItem(group.gid)
                    }
                    setNegativeButton("Cancel") { dialog, which ->
                        vm.update()
                    }
                }.setCancelable(false)

                dialog.show().apply {
                    this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.iosBlue))
                    this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.iosBlue))
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rv)

        view.loading_profile_groups.apply {
            setAnimation("register_loading.json")
            speed = 2f
            playAnimation()
            addAnimatorListener(object: Animator.AnimatorListener {
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



        setupVM()
        try {
            asyncLoadProfileImg()
        } catch (err: Exception) {
            profileImage.setImageResource(R.drawable.astro_prof)
        }
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

                dialog.show().apply{
                    this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.iosBlue))
                    this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.iosBlue))
                }
        }
    }

    private val openModal = fun (gid : String) {
        var intent = Intent(requireActivity(), ModalGroupActivity::class.java)
        intent.putExtra("gid", gid)
        ContextCompat.startActivity(requireContext(), intent, null)
    }

    /* TODO fallback image now shows. Check to see if upload will refresh */
    private fun asyncLoadProfileImg() {
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
        Firebase.storage.reference.child(vm.getUser().value!!.uid).putStream(imgInputStream!!)
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

    /* ****

        FIREBASE OBSERVERS AND UPDATES FOR RV

     * ****/
    private fun setupVM() {
        var a = rv.adapter as GListAdapter
        vm.getUser().observe(viewLifecycleOwner, Observer {
            user = it!!
            a.authUser = user
            nameChange.text = user.screenName
            val num_in = user.inGroups.size
            numIn.text = "In ${num_in} Groups"
        })

        vm.getGroups().observe(viewLifecycleOwner, Observer {

            var tempList = ArrayList<Group>()
            /* Filter out groups that user doesn't own */
            it!!.forEach {
                /*
                    checks if owner of group in db
                    appends item to list in callback
                 */
                if (it.playerList.contains(user.uid)) {
                    tempList.add(it)
                }
            }

                groupsList = tempList
            (rv.adapter as GListAdapter).update(groupsList as ArrayList<Group>)

        })
    }
}


class GListAdapter(
    var groupList: ArrayList<Group>, var authUser: User,
    val modifyGroup: (gid: String, action: GroupMod) -> Unit,
    val openModal : (gid : String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private lateinit var vParent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(
            R.layout.profile_group_cell,
            parent, false
        ) as LinearLayout

        vParent = parent

        return ViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = groupList[position]
        var item = holder.itemView

        item.groupName.text = curr.name
        item.currCount.text = curr.currCount.toString()
        item.maxCount.text = "${curr.maxPlayers}  ...  Players Joined"
        item.shiploc.text = "${curr.ship} - ${curr.loc}"

        item.title_view.setOnClickListener {
            openModal(groupList[position].gid)
        }


        if (curr.createdBy == authUser.uid) {

            when (item.active_toggle.isChecked) {
                true -> {
                    item.active_toggle.thumbTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                    item.active_toggle.trackTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                }
                false -> {
                    item.active_toggle.thumbTintList = ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                    item.active_toggle.trackTintList = ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                }
            }

            item.active_toggle.isChecked = !curr.active
            item.active_toggle.isActivated = !curr.active
            item.active_toggle.setOnClickListener {
                /* isActivated is state !BEFORE! switched */
                when (it.isActivated) {
                    false -> {
                        item.active_toggle.thumbTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                        item.active_toggle.trackTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                        modifyGroup(curr.gid, GroupMod.MAKE_PRIVATE)
                    }
                    true -> {
                        item.active_toggle.thumbTintList = ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                        item.active_toggle.trackTintList = ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                        modifyGroup(curr.gid, GroupMod.MAKE_PUBLIC)
                    }
                }
            }
        } else {
            item.active_toggle.visibility = View.GONE
        }

    }

    fun removeItem(gid: String) {
        modifyGroup(gid, GroupMod.DELETE)
    }

    fun update(group_list : ArrayList<Group>) {
            groupList = group_list


        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

}