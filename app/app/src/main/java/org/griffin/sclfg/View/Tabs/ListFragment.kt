package org.griffin.sclfg.View.Tabs

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
import kotlinx.android.synthetic.main.group_cell.view.*
import kotlinx.android.synthetic.main.tab_list.view.*
import kotlinx.android.synthetic.main.user_cell.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

class ListFragment : Fragment()
{
    private val vm : ViewModel by activityViewModels()
    private lateinit var groupsList : List<Group>
    private var user = User("", "", 0)
    private var userLists : ArrayList<ArrayList<User>> = ArrayList()

    /* Recycler View Setup */
    private lateinit var rv : RecyclerView
    private lateinit var rvManager : RecyclerView.LayoutManager
    private lateinit var rvAdapter : RecyclerView.Adapter<*>

    private val joinGroup = fun (g : Group, uid : String)
    {
        if(g.currCount + 1 < g.maxPlayers)
        {
            g.playerList.add(uid)
            val hash = hashMapOf(
                "playerList" to g.playerList,
                "currCount" to g.currCount + 1
            )
            vm.joinGroup(g.gid, hash)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_list, container, false)

        /* Setup Fragment View here */
        rv = view.listView
        rvManager = LinearLayoutManager(context)

        rvAdapter = GroupListAdapter(ArrayList(), ArrayList(), user, joinGroup)


        /* Bind everything together */
        rv.apply {
            layoutManager = rvManager
            adapter = rvAdapter
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupVM()
    }

    private fun setupVM()
    {
        vm.getUser().observe(viewLifecycleOwner, Observer {
            user = it!!
            vm.update()
        })

        vm.getGroups().observe(viewLifecycleOwner, Observer {
            groupsList = it!!
            userLists.clear()

            /* retrieve user lists */
            groupsList.forEachIndexed { i, group ->
                var userList = getUsersForGroup(group)
                userLists.add(userList)
            }

            var newAdapter = GroupListAdapter(ArrayList(groupsList), userLists, user, joinGroup)
            rv.adapter = newAdapter
        })
    }

    private fun getUsersForGroup(group : Group) : ArrayList<User>
    {
        var userList = ArrayList<User>()
        for(uid in group.playerList)
        {
            var addUserToList = fun(user : User) {
                userList.add(user)
            }
            vm.findUser(uid, addUserToList)
        }
        return userList
    }
}

class UserListAdapter(val userList: ArrayList<User>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val cellView = LayoutInflater.from(parent.context).inflate(R.layout.user_cell,
            parent, false) as LinearLayout
        return ViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val curr = userList[position]
        var item = holder.itemView
        item.userName.text = curr.screenName

        /* other setup for user list cell components */
    }

    override fun getItemCount(): Int
    {
        return userList.size
    }
}

/*
 Adapter for our Group List Recycler View.
 Takes as argument Group list and array of user lists for particular group
 */
class GroupListAdapter(val groupList: ArrayList<Group>,
                       val userLists: ArrayList<ArrayList<User>>,
                       val authUser: User,
                       val joinGroup: (grp: Group, uid: String) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    class ViewHolder(val cellView : LinearLayout) : RecyclerView.ViewHolder(cellView)
    private lateinit var grvManager: RecyclerView.LayoutManager

    /* Inflates and creates the actual cell view for our groups list */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val cellView = LayoutInflater.from(parent.context).inflate(R.layout.group_cell,
            parent, false) as LinearLayout

        grvManager = LinearLayoutManager(parent.context)

        return ViewHolder(cellView)
    }

    /* Set attributes to text in bind */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val curr = groupList[position]
        var item = holder.itemView
        item.groupName.text = curr.name
        item.currCount.text = curr.currCount.toString()
        item.maxCount.text = curr.maxPlayers.toString() + "  ...  Players Joined"
        item.shiploc.text = curr.ship + " - " + curr.loc

        /* BIND USER LIST */
        var gRV = item.userListView
        var grvAdapter = UserListAdapter(userLists[position])

        /* Bind everything together */
        gRV.apply {
            layoutManager = grvManager
            adapter = grvAdapter
        }

        /* check if authUser is in current group */
        if(!groupList[position].playerList.contains(authUser.uid))
        {
            item.joinButton.visibility = View.VISIBLE
            item.joinButton.setOnClickListener {
                joinGroup(groupList[position], authUser.uid)
            }
        }
        else
        {
            item.joinButton.visibility = View.GONE
        }


        item.expander.setOnClickListener {
            onExpand(item, curr)
        }
    }

    private fun onExpand(item: View, curr: Group)
    {
        when(item.sub_item.visibility)
        {
            View.GONE -> /* Already gone. Need to switch to Visible */
            {
                item.sub_item.visibility = View.VISIBLE
            }
            View.VISIBLE -> /* Its visible. Need to toggle Gone */
            {
                item.sub_item.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    fun removeAt(pos : Int)
    {
        groupList.removeAt(pos)
        notifyItemRemoved(pos)
    }
}