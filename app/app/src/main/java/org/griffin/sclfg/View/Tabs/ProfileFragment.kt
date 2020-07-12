package org.griffin.sclfg.View.Tabs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.group_cell.view.*
import kotlinx.android.synthetic.main.profile_group_cell.view.*
import kotlinx.android.synthetic.main.profile_group_cell.view.currCount
import kotlinx.android.synthetic.main.profile_group_cell.view.groupName
import kotlinx.android.synthetic.main.profile_group_cell.view.maxCount
import kotlinx.android.synthetic.main.profile_group_cell.view.shiploc
import kotlinx.android.synthetic.main.tab_profile.*
import kotlinx.android.synthetic.main.tab_profile.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.lang.NullPointerException

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
    private var user = User("", "", 0)

    /* Recycler View Setup */
    private lateinit var rv : RecyclerView
    private lateinit var rvManager : RecyclerView.LayoutManager
    private lateinit var rvAdapter : RecyclerView.Adapter<*>

    private lateinit var groupsList : List<Group>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_profile, container, false)

        /* Setup Fragment View here */
        rv = view.my_groups
        rvManager = LinearLayoutManager(context)

        rvAdapter = GListAdapter(ArrayList(), user)

        rv.apply {
            layoutManager = rvManager
            adapter = rvAdapter
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupVM()
        try {
            asyncLoadProfileImg()
        }
        catch (err : Exception) {
            profileImage.setImageResource(R.drawable.astro_prof)
        }
        profileImage.setOnClickListener {

            /* Confirm selection with alertDialog */
            AlertDialog.Builder(this.requireContext()).apply {
                setTitle("Choose a new profile image?")
                setPositiveButton("Yes") { dialog, which ->
                    /* Continue to image picker on confirm */
                    doImagePicker()
                }
                setNegativeButton("Cancel") { dialog, which ->
                    /* Do nothing and return to profile fragment*/
                }
            }.show()
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
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(R.drawable.astro_prof)
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

        vm.getGroups().observe(viewLifecycleOwner, Observer {

            var temp_list = ArrayList<Group>()
            /* Filter out groups that user doesn't own */
            it!!.forEach {
                /*
                    checks if owner of group in db
                    appends item to list in callback
                 */
                vm.isOwner(it.gid) {
                    temp_list.add(it)
                }
            }
            groupsList = temp_list

            var newAdapter = GListAdapter(ArrayList(groupsList), user)
            rv.adapter = newAdapter
        })
    }
}

class GListAdapter(val groupList: ArrayList<Group>,
                       val authUser: User)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    class ViewHolder(val cellView : LinearLayout) : RecyclerView.ViewHolder(cellView)
    private lateinit var vParent : ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(R.layout.profile_group_cell,
        parent, false) as LinearLayout

        vParent = parent

        return ViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = groupList[position]
        var item = holder.itemView

        item.groupName.text = curr.name
        item.currCount.text = curr.currCount.toString()
        item.maxCount.text = curr.maxPlayers.toString() + "  ...  Players Joined"
        item.shiploc.text = curr.ship + " - " + curr.loc
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

}