package org.griffin.sclfg.View.Group.Tabs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tab_about.*
import kotlinx.android.synthetic.main.tab_about.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.GroupViewModel
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.R
import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.Thunks.loadSelect
import org.griffin.sclfg.Redux.Thunks.setMessageListener
import org.griffin.sclfg.Redux.store
import org.griffin.sclfg.Utils.Adapters.AboutUserAdapter
import org.reduxkotlin.StoreSubscription

class AboutFragment(val gid: String) : Fragment() {

    private var userList = ArrayList<String>()
    private lateinit var unsub : StoreSubscription
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
        rvAdapter = AboutUserAdapter(
            ArrayList()
        )
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
    }

    override fun onDetach() {
        super.onDetach()
        store.dispatch(Action.CLEAR_SELECTED_GROUP)
        unsub()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        SetupRedux()
    }

    private fun SetupRedux() {
        unsub = store.subscribe {
            try {
                requireActivity().runOnUiThread {
                    render(store.getState().selectedGroup!!)
                }
            } catch (e : Exception) {}
        }
        store.dispatch(loadSelect(gid))
    }

    private fun render(group : Group) {
        try {
            val curr_count = group.currCount
            val max_count = group.maxPlayers
            GroupName.text = group.name
            playerCount.text = "${curr_count}  /  ${max_count}"

            userList = group.playerList
            (rv.adapter as AboutUserAdapter).update(userList)

        } catch (e : Exception) {}
    }
}