package org.griffin.sclfg.View.GroupView.Messaging

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import org.griffin.sclfg.Models.Group
import org.griffin.sclfg.Models.Message
import org.griffin.sclfg.Models.User
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import org.griffin.sclfg.View.GroupView.ModalGroupActivity
import java.util.Observer

class MessageFragment(val gid : String) : Fragment() {

    private val vm : ViewModel by activityViewModels()
    private val msgVm : MessageViewModel by activityViewModels()

    private var msgs = ArrayList<Message>()
    private var user = User("Loading...", "loading", ArrayList(), -1)
    private lateinit var group : Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_message, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupVM()

    }

    private fun loadMsgVm() : MessageViewModel {
        return ViewModelProvider(requireActivity())[MessageViewModel(gid)::class.java]
    }

    private fun setupVM() {
        vm.getUser().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            user = it
        })

//        msgVm.getMsgs().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            var temp = ArrayList<Message>()
//            it.forEach{
//                temp.add(it)
//            }
//            msgs = temp
//        })
    }

}