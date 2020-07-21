package org.griffin.sclfg.View.Home.Tabs

import android.content.Intent
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
import kotlinx.android.synthetic.main.tab_list.*
import kotlinx.android.synthetic.main.tab_list.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupViewModel
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.R
import org.griffin.sclfg.Utils.Adapters.GroupListAdapter
import org.griffin.sclfg.View.Group.GroupActivity

class ListFragment : Fragment() {
    private val vm: GroupViewModel by activityViewModels()
    private lateinit var groupsList: List<Group>
    private var user = User("", "", ArrayList(), 0)

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
            var g = GroupViewModel.groupFromHash(it)
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

    private val openModal = fun(gid: String) {
        var intent = Intent(requireActivity(), GroupActivity::class.java)
        intent.putExtra("gid", gid)
        ContextCompat.startActivity(requireContext(), intent, null)
    }

    private val leaveGroup = fun(gid: String, uid: String, cb: () -> Unit) {
        vm.groupExists(gid, err_cb) {
            /* callback invoked after checking if group still exists on db */
            var g = GroupViewModel.groupFromHash(it)
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

        rvAdapter = GroupListAdapter(
            requireActivity(),
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
            groupsList = tempList
            (rv.adapter as GroupListAdapter).apply {
                authUser = user
                update(groupsList as ArrayList<Group>)
            }
        })
    }

    private val lookUp = fun(uid: String, cb: (name: String) -> Unit) {
        vm.lookupUID(uid) {
            cb(it)
        }
    }
}

