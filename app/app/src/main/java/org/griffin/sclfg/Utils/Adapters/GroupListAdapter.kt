package org.griffin.sclfg.Utils.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.group_cell.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.R

/*
 Adapter for our Group List Recycler View.
 Takes as argument Group list and array of user lists for particular group
 */
class GroupListAdapter(
    val activity: FragmentActivity,
    var groupList: ArrayList<Group>,
    var authUser: User,
    val joinGroup: (gid: String, uid: String, cb: () -> Unit) -> Unit,
    val leaveGroup: (gid: String, uid: String, cb: () -> Unit) -> Unit,
    val openModal: (gid: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ViewHolder(val cellView: LinearLayout) : RecyclerView.ViewHolder(cellView)

    private lateinit var grvManager: RecyclerView.LayoutManager
    private lateinit var vParent: ViewGroup

    fun update(groups: ArrayList<Group>) {
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
        val grvAdapter = UserListAdapter(curr.playerList)

        /* Bind everything together */
        gRV.apply {
            layoutManager = grvManager
            adapter = grvAdapter
        }


        hideGoneElements(item)

        val isMember = authUser.inGroups.contains(curr.gid)

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
            item.openModalButton.apply {
                visibility = View.GONE
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

    private fun animateChange(view: View, type: Animate) {
        when (type) {
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
