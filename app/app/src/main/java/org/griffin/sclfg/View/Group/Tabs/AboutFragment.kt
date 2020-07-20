package org.griffin.sclfg.View.Group.Tabs

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
import org.griffin.sclfg.Utils.Adapters.AboutUserAdapter

class AboutFragment(val gid: String) : Fragment() {

    private val vm: GroupViewModel by activityViewModels()
    private var user = User("Loading...", "loading", ArrayList(), -1)
    private lateinit var group: Group
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

        rvAdapter = AboutUserAdapter(
            ArrayList(),
            lookUp
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
        setupVM()
    }

    private val lookUp = fun(uid: String, cb: (name: String) -> Unit) {
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
                    (rv.adapter as AboutUserAdapter).update(userList)
                })
            }
        })

    }
}


