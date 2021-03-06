package org.griffin.sclfg.View.Group.Tabs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tab_about.*
import kotlinx.android.synthetic.main.tab_about.view.*
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.R
import org.griffin.sclfg.Redux.Action
import org.griffin.sclfg.Redux.Thunks.clearSelectedGroup
import org.griffin.sclfg.Redux.Thunks.loadSelect
import org.griffin.sclfg.Redux.initialGroup
import org.griffin.sclfg.Redux.store
import org.griffin.sclfg.Utils.Adapters.AboutUserAdapter
import org.reduxkotlin.StoreSubscription

class AboutFragment() : Fragment() {

    private var userList = ArrayList<String>()
    private var selectedGroup = initialGroup
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
        store.dispatch(clearSelectedGroup())
        unsub()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        SetupRedux()
    }

    private fun SetupRedux() {
        unsub = store.subscribe {
            try {
                    val changedGroup = store.getState().selectedGroup!!
                    if(selectedGroup != changedGroup) {
                        selectedGroup = changedGroup
                        render(store.getState().selectedGroup!!)
                    }
            } catch (e : Exception) {}
        }
    }

    private fun render(group : Group) {
        try {
            requireActivity().runOnUiThread {
                val curr_count = group.playerList.size
                val max_count = group.maxPlayers
                GroupName.text = group.name
                playerCount.text = "${curr_count}  /  ${max_count}"

                nameContainer.visibility = View.VISIBLE
                listContainer.visibility = View.VISIBLE
            userList = group.playerList
            (rv.adapter as AboutUserAdapter).update(userList)
            }
        } catch (e : Exception) {}
    }
}