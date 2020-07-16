package org.griffin.sclfg.View.GroupView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.profile_group_cell.view.*
import kotlinx.android.synthetic.main.tab_about.*
import kotlinx.android.synthetic.main.tab_about.view.*
import kotlinx.android.synthetic.main.tab_profile.view.*
import kotlinx.android.synthetic.main.user_cell_message.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupMod
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import org.griffin.sclfg.View.Home.Tabs.GListAdapter

class AboutFragment(val gid : String): Fragment() {

    private val vm: ViewModel by activityViewModels()
    private var user = User("Loading...", "loading", ArrayList(), -1)
    private lateinit var group : Group
    private var userList = ArrayList<String>()

    /* Recycler View Setup */
    private lateinit var rv: RecyclerView
    private lateinit var rvManager: RecyclerView.LayoutManager
    private lateinit var rvAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.tab_about, container, false)

        /* Setup Fragment View here */
        rv = view.user_rv
        rvManager = LinearLayoutManager(context)

        rvAdapter = UserListAdapter(ArrayList(), lookUp)

        rv.apply {
            layoutManager = rvManager
            adapter = rvAdapter
        }



        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        planet_animate.apply {
            setAnimation("planet.json")
            speed = 0.5f
            loop(true)
            playAnimation()
        }
        setupVM()

    }


    private fun getPlayerNames(groupUids : ArrayList<String>) {
        var new_list = ArrayList<String>()
        (0..(groupUids.size - 1)).withIndex().forEach {
            vm.lookupUID(groupUids[it.index]) {
                userList.add(it)
            }
        }
    }

    private val lookUp= fun (uid : String, cb : (name : String) -> Unit) {
        vm.lookupUID(uid) {
            cb(it)
        }
    }

    private fun setupVM() {
        vm.getUser().observe(viewLifecycleOwner, Observer {
            user = it
            vm.getGroup(gid) {
                it.observe(requireActivity(), Observer {
                    val group = it
                    val curr_count = group.currCount
                    val max_count = group.maxPlayers
                    GroupName.text = group.name
                    playerCount.text = "${curr_count}  /  ${max_count}"

                    userList = group.playerList
                    (rv.adapter as UserListAdapter).update(userList)
                })
            }
        })

    }
}

class UserListAdapter(
    var userList : ArrayList<String>,
    val lookUp : (uid : String, cb : (name : String) -> Unit) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private lateinit var vParent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cellView = LayoutInflater.from(parent.context).inflate(
            R.layout.user_cell_message,
            parent, false
        ) as LinearLayout

        vParent = parent

        return ViewHolder(cellView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curr = userList[position]
        var item = holder.itemView

        item.animate()
            .alpha(0f)
            .setDuration(0L)
            .start()
        lookUp(curr) {
            item.userName.text = it
            item.animate()
                .alpha(1f)
                .setDuration(400L)
                .start()
        }

    }

    fun update(user_list : ArrayList<String>) {
        val diff = user_list.minus(userList)
        diff.forEach {
            if(user_list.contains(it)) {
                userList.add(it)
            }
            else {
                userList.remove(it)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}
