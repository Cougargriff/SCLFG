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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tab_list.*
import kotlinx.android.synthetic.main.tab_list.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupViewModel
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.R
import org.griffin.sclfg.Redux.Thunks.*
import org.griffin.sclfg.Redux.configureStore
import org.griffin.sclfg.Redux.store
import org.griffin.sclfg.Utils.Adapters.GroupListAdapter
import org.griffin.sclfg.View.Group.GroupActivity

class ListFragment : Fragment() {
    private val vm: GroupViewModel by activityViewModels()
    private var groupsList = ArrayList<Group>()
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
        store.dispatch(joinGroup(gid) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Too many people already", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private val openModal = fun(gid: String) {
        var intent = Intent(requireActivity(), GroupActivity::class.java)
        intent.putExtra("gid", gid)
        ContextCompat.startActivity(requireContext(), intent, null)
    }

    private val leaveGroup = fun(gid: String, uid: String, cb: () -> Unit) {
        store.dispatch(leaveGroup(gid) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Too few people already", Toast.LENGTH_LONG)
                    .show()
            }
        })
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
        SetupRedux()
    }

    private fun SetupRedux() {
        /* Redux Store Setup */
        store.subscribe {
            requireActivity().runOnUiThread {
            //    if(groupsList != store.state.groups)
                    render(store.getState().groups)

               // if(user != store.state.user)
                    render(store.getState().user)
            }
        }
       // store.dispatch(getUser())
        //store.dispatch(getGroups())
    }

    private fun setupSwipeRefresh() {
        swipe_layout.setOnRefreshListener {
            /*
                Updates group list in view model.
                Observer in fragment will update local list.
             */
            store.dispatch(getGroups())
            swipe_layout.isRefreshing = false
        }
    }

    private fun render(newUser : User) {
        user = newUser
        /*
            possible in the future to just make local
            call to function for getGroups instead of vm call
         */
        (rv.adapter as GroupListAdapter).apply {
            authUser = user
            notifyDataSetChanged()
        }
    }

    private fun render(newGroups : ArrayList<Group>) {
        groupsList = ArrayList(newGroups.filter {  it.active })

        (rv.adapter as GroupListAdapter).apply {
            update(groupsList as ArrayList<Group>)
        }
    }
}

