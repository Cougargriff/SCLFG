package org.griffin.sclfg.Utils.Adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.profile_group_cell.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupMod
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.R

class ProfileAdapter(
    var groupList: ArrayList<Group>, var authUser: User,
    val modifyGroup: (gid: String, action: GroupMod, err_cb : () -> Unit) -> Unit,
    val err_cb: () -> Unit,
    val openModal: (gid: String) -> Unit
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

    private val DURATION = 250L
    private fun animateDown(views: List<View>) {
        if (views.size > 0) {
            views[0].animate()
                .alpha(0f)
                .setDuration(DURATION)
                .withStartAction {
                    if (views.size > 1)
                        animateDown(views.subList(1, views.size))

                }
                .withEndAction {
                    views[0].visibility = View.INVISIBLE
                }
                .start()
        }

    }

    private fun animateUp(views: List<View>) {

        if (views.size > 0) {
            views[0].animate()
                .alpha(1f)
                .setDuration(DURATION)
                .withStartAction {
                    if (views.size > 1) {
                        animateUp(views.subList(1, views.size))
                    }

                }
                .withEndAction {
                    views[0].visibility = View.VISIBLE
                }
                .start()
        }
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
            item.active_toggle.isChecked = !curr.active
            item.active_toggle.isActivated = !curr.active
            item.startDelete.visibility = View.VISIBLE

            val swapViews = fun(showDelete: Boolean) {
                when (showDelete) {
                    true -> {
                        animateUp(
                            listOf(
                                item.delete_button!!,
                                item.cancel_button!!
                            )
                        )


                        animateDown(
                            listOf(

                                item.startDelete!!,

                                item.cell_container!!
                            )
                        )
                    }
                    false -> {
                        animateDown(
                            listOf(
                                item.delete_button!!,
                                item.cancel_button!!
                            )
                        )


                        animateUp(
                            listOf(
                                item.startDelete!!,
                                item.cell_container
                            )
                        )
                    }
                }
            }


            /* attach delete button */
            item.startDelete.setOnClickListener {
                swapViews(true)
            }

            item.cancel_button.setOnClickListener {
                swapViews(false)
            }

            item.delete_button.setOnClickListener {
                /* Delete the item */
                swapViews(false)
                removeItem(curr.gid, position)
            }



            when (item.active_toggle.isChecked) {
                true -> {
                    item.active_toggle.thumbTintList =
                        ColorStateList.valueOf(Color.parseColor("#2196F3"))
                    item.active_toggle.trackTintList =
                        ColorStateList.valueOf(Color.parseColor("#2196F3"))
                }
                false -> {
                    item.active_toggle.thumbTintList =
                        ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                    item.active_toggle.trackTintList =
                        ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                }
            }


            item.active_toggle.setOnClickListener {
                /* isActivated is state !BEFORE! switched */
                when (it.isActivated) {
                    false -> {
                        item.active_toggle.thumbTintList =
                            ColorStateList.valueOf(Color.parseColor("#2196F3"))
                        item.active_toggle.trackTintList =
                            ColorStateList.valueOf(Color.parseColor("#2196F3"))
                        modifyGroup(curr.gid, GroupMod.MAKE_PRIVATE, err_cb)
                    }
                    true -> {
                        item.active_toggle.thumbTintList =
                            ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                        item.active_toggle.trackTintList =
                            ColorStateList.valueOf(Color.parseColor("#CECFD1"))
                        modifyGroup(curr.gid, GroupMod.MAKE_PUBLIC, err_cb)
                    }
                }
            }
        } else {
            item.active_toggle.visibility = View.GONE
        }

    }

    fun removeItem(gid: String, position: Int) {
        modifyGroup(gid, GroupMod.DELETE, err_cb)
        notifyItemRemoved(position)
    }

    fun update(group_list: ArrayList<Group>) {
        groupList = group_list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

}
