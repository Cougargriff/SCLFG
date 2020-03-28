package org.griffin.sclfg.View.Tabs

import android.content.Context
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
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R

class ListFragment : Fragment()
{
    private val vm : ViewModel by activityViewModels()
    private lateinit var groupsList : List<Group>

    /* Recycler View Setup */
    private lateinit var rv : RecyclerView
    private lateinit var rvManager : RecyclerView.LayoutManager
    private lateinit var rvAdapter : RecyclerView.Adapter<*>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        var view = inflater.inflate(R.layout.tab_list, container, false)

        /* Setup Fragment View here */
        rv = view.listView
        rvManager = LinearLayoutManager(context)
        rvAdapter = GroupListAdapter(ArrayList<Group>())

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
        vm.getGroups().observe(viewLifecycleOwner, Observer {
            groupsList = it!!
            var newAdapter = GroupListAdapter(ArrayList(groupsList))
            rv.adapter = newAdapter
        })
    }
}

/* Adapter for our Group List Recycler View */
class GroupListAdapter(val groupList: ArrayList<Group>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    class ViewHolder(val cellView : LinearLayout) : RecyclerView.ViewHolder(cellView)

    /* Inflates and creates the actual cell view for our groups list */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        val cellView = LayoutInflater.from(parent.context).inflate(R.layout.group_cell,
            parent, false) as LinearLayout
        return ViewHolder(cellView)
    }

    /* Set attributes to text in bind */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val curr = groupList[position]
        holder.itemView.groupName.text = curr.name
        holder.itemView.currCount.text = curr.currCount.toString()
        holder.itemView.maxCount.text = curr.maxPlayers.toString() + "  ...  Players Joined"

        /* TODO BIND USER LIST */
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