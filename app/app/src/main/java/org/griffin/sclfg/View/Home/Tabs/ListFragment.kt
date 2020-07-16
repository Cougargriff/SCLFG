package org.griffin.sclfg.View.Home.Tabs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.group_cell.view.*
import kotlinx.android.synthetic.main.tab_list.*
import kotlinx.android.synthetic.main.tab_list.view.*
import kotlinx.android.synthetic.main.user_cell.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import org.griffin.sclfg.View.GroupView.ModalGroupActivity

class ListFragment : Fragment() {
    private val vm: ViewModel by activityViewModels()
    private lateinit var groupsList: List<Group>
    private var user = User("", "", ArrayList(), 0)
    private var userLists: ArrayList<ArrayList<User>> = ArrayList()

    /* Recycler View Setup */
    private lateinit var rv: RecyclerView
    private lateinit var rvManager: RecyclerView.LayoutManager
    private lateinit var rvAdapter: RecyclerView.Adapter<*>

    private val err_cb = fun() {
        Toast.makeText(requireContext(), "Group No Longer Exists", Toast.LENGTH_LONG)
            .show()
    }

    /* closures for joining and leaving groups. passed to list adapters */
    private val joinGroup = fun(gid: String, uid: String, cb: () -> Unit) {
        vm.groupExists(gid, err_cb) {
            /* callback invoked after checking if group still exists on db */
            var g = ViewModel.groupFromHash(it)
            if (g.currCount + 1 <= g.maxPlayers) {
                g.playerList.add(uid)
                /* only pass updated data due to nature of merging docs */
                val hash = hashMapOf(
                    "playerList" to g.playerList,
                    "currCount" to g.currCount + 1
                )
                vm.joinGroup(g.gid, hash) {
                    /* after joining group make button clickable again */
                    cb()
                }
            } else {
                Toast.makeText(requireContext(), "Too many people already", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }

    private val openModal = fun (gid : String) {
        var intent = Intent(requireActivity(), ModalGroupActivity::class.java)
        intent.putExtra("gid", gid)
        ContextCompat.startActivity(requireContext(), intent, null)
    }

    private val leaveGroup = fun(gid: String, uid: String, cb: () -> Unit) {
        vm.groupExists(gid, err_cb) {
            /* callback invoked after checking if group still exists on db */
            var g = ViewModel.groupFromHash(it)
            if (g.currCount - 1 >= 0) {
                g.playerList.remove(uid)
                /* only pass updated data due to nature of merging docs */
                val hash = hashMapOf(
                    "playerList" to g.playerList,
                    "currCount" to g.currCount - 1
                )
                vm.leaveGroup(g.gid, hash) {
                    /* after leaving group make button clickable again */
                    cb()
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.tab_list, container, false)

        /* Setup Fragment View here */
        rv = view.listView
        rvManager = LinearLayoutManager(context)

        rvAdapter = GroupListAdapter(requireActivity(),
            ArrayList(),
            user,
            lookUp,
            joinGroup,
            leaveGroup,
            openModal
        )

        /* Bind everything together */
        rv.apply {
            layoutManager = rvManager
            adapter = rvAdapter
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSwipeRefresh()
        setupVM()
    }

    private fun setupSwipeRefresh() {
        swipe_layout.setOnRefreshListener {
            /*
                Updates group list in view model.
                Observer in fragment will update local list.
             */
            vm.update()
            swipe_layout.isRefreshing = false
        }
    }

    private fun setupVM() {
        vm.getUser().observe(viewLifecycleOwner, Observer {
            user = it!!
            /*
                possible in the future to just make local
                call to function for getGroups instead of vm call
             */
            (rv.adapter as GroupListAdapter).apply {
                authUser = user
                notifyDataSetChanged()
            }
        })

        vm.getGroups().observe(viewLifecycleOwner, Observer {
            val tempList = ArrayList<Group>()

            /* Spot to check for conditions on whether to show a specific group */
            /* TODO possible to add filter checks for user inputted tags in the future */
            it!!.forEach {
                if (it.active) {
                    tempList.add(it)
                }
            }
            userLists.clear()
            groupsList = tempList

            /* retrieve user lists for each group */
            groupsList.forEachIndexed { i, group ->
                val userList = getUsersForGroup(group)
                userLists.add(userList)
            }

            (rv.adapter as GroupListAdapter).apply {
                authUser = user
                update(groupsList as ArrayList<Group>)
            }
        })

    }

    private val lookUp= fun (uid : String, cb : (name : String) -> Unit) {
        vm.lookupUID(uid) {
            cb(it)
        }
    }

    private fun getUsersForGroup(group: Group): ArrayList<User> {
        val userList = ArrayList<User>()
        for (uid in group.playerList) {
            val addUserToList = fun(user: User) {
                userList.add(user)
            }
            vm.findUser(uid, addUserToList)
        }
        return userList
    }
}


/*           _             _
    /\      | |           | |
   /  \   __| | __ _ _ __ | |_ ___ _ __ ___
  / /\ \ / _` |/ _` | '_ \| __/ _ \ '__/ __|
 / ____ \ (_| | (_| | |_) | ||  __/ |  \__ \
/_/    \_\__,_|\__,_| .__/ \__\___|_|  |___/
                    | |
                    |_|
*/

/* Adapter for user sub list view */
class UserListAdapter(val userList: ArrayList<String>,
                      val lookUp : (uid: String, cb : (name : String) -> Unit) -> Unit ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_cell,
            parent, false
        ) as LinearLayout
        return ViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = userList[position]
        val item = holder.itemView

        lookUp(curr) {
            item.userName.text = it
        }


        /* other setup for user list cell components */
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}

/*
 Adapter for our Group List Recycler View.
 Takes as argument Group list and array of user lists for particular group
 */
class GroupListAdapter(
    val activity : FragmentActivity,
    var groupList: ArrayList<Group>,
    var authUser: User,
    val lookUp : (uid: String, cb : (name : String) -> Unit) -> Unit,
    val joinGroup: (gid: String, uid: String, cb: () -> Unit) -> Unit,
    val leaveGroup: (gid: String, uid: String, cb: () -> Unit) -> Unit,
    val openModal: (gid: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private lateinit var grvManager: RecyclerView.LayoutManager
    private lateinit var vParent: ViewGroup

    fun update(groups : ArrayList<Group>) {
        groupList = groups
        notifyDataSetChanged()
    }

    private fun hideGoneElements(itemView: View) {
        /* Toggle the views that should be GONE at start */
        itemView.joinButton.visibility = View.GONE
        itemView.leaveButton.visibility = View.GONE
    }

    /* Inflates and creates the actual cell view for our groups list */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(
            R.layout.group_cell,
            parent, false
        ) as LinearLayout
        vParent = parent

        grvManager = LinearLayoutManager(parent.context)

        return ViewHolder(cellView)
    }


    /* Set attributes to text in bind */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = groupList[position]
        val item = holder.itemView
        item.groupName.text = curr.name
        item.currCount.text = curr.currCount.toString()
        item.maxCount.text = "${curr.maxPlayers}  ...  Players Joined"
        item.shiploc.text = "${curr.ship} - ${curr.loc}"
        item.descriptionBox.text = groupList[position].description
        item.space_animate.apply {
            setAnimation("stars_profile.json")
            speed = Math.random().toFloat() * 0.85f
            loop(true)
            playAnimation()
        }

        item.openModalButton.setOnHoverListener { v, event ->
            v.setBackgroundColor(Color.BLUE)
            true
        }

        /* BIND USER LIST */
        grvManager = LinearLayoutManager(vParent.context)
        val gRV = item.userListView
        val grvAdapter = UserListAdapter(curr.playerList, lookUp)

        /* Bind everything together */
        gRV.apply {
            layoutManager = grvManager
            adapter = grvAdapter
        }


        hideGoneElements(item)

        val isMember = groupList[position].playerList.contains(authUser.uid)

        /* check if authUser is in current group */
        if (!isMember) {
            item.leaveButton.visibility = View.GONE
            item.joinButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    /* make button not clickable after starting joinGroup transaction */
                    item.joinButton.visibility = View.GONE
                    joinGroup(groupList[position].gid, authUser.uid) {
                        /* Callback to happen after join group */
                    }
                }
            }
        } else {
            item.joinButton.visibility = View.GONE
            item.leaveButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    /* make button not clickable after starting leaveGroup transaction */
                    item.leaveButton.visibility = View.GONE
                    leaveGroup(groupList[position].gid, authUser.uid) {
                        /* Callback to happen after leave group */
                    }
                }
            }
            item.openModalButton.apply {
                visibility = View.VISIBLE
                openModalButton.setOnClickListener {
                    openModal(groupList[position].gid)
                }
            }
        }

        item.expander.setOnClickListener {
            onExpand(item)
        }
    }

    private enum class Animate {
        HIDE, SHOW
    }
    private fun animateChange(view : View, type : Animate) {
        when(type) {
            Animate.HIDE -> {
                view.animate()
                    .setDuration(150L)
                    .alpha(0f)
                    .withEndAction {
                        view.visibility = View.GONE
                    }
                    .start()
            }
            Animate.SHOW -> {
                view.animate()
                    .alpha(0f)
                    .setDuration(0L)
                    .start()
                view.visibility = View.VISIBLE
                view.animate()
                    .setDuration(500L)
                    .alpha(1f)
                    .start()

            }
        }
    }

    private fun onExpand(item: View) {
        when (item.sub_item.visibility) {
            View.GONE -> /* Already gone. Need to switch to Visible */ {
                animateChange(item.sub_item, Animate.SHOW)
            }
            View.VISIBLE -> /* Its visible. Need to toggle Gone */ {
                animateChange(item.sub_item, Animate.HIDE)
            }
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    fun removeAt(pos: Int) {
        groupList.removeAt(pos)
        notifyItemRemoved(pos)
    }
}