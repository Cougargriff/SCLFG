package org.griffin.sclfg.View.GroupView

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_modal_group.*
import org.griffin.sclfg.Models.ViewModel
import org.griffin.sclfg.R
import org.griffin.sclfg.View.GroupView.Messaging.MessageFragment
import org.griffin.sclfg.Models.MessageViewModel
import org.griffin.sclfg.View.Home.MainActivity
import org.griffin.sclfg.View.Home.PageAdapter
import java.lang.Exception

class ModalGroupActivity : AppCompatActivity() {

    private lateinit var pa : PageAdapter
    private var gid = ""
    private val msgVm : MessageViewModel by viewModels()
    private val vm : ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modal_group)



            gid = intent.extras!!.getString("gid", "")
            msgVm.setGid(gid) {
                /* If fail to get gid, return to main screen */
                val intent = Intent(this@ModalGroupActivity, MainActivity::class.java)
                ContextCompat.startActivity(this@ModalGroupActivity, intent, null)
            }

        paSetup()
        vpSetup()
        initVMs()
        modalViewPager.setCurrentItem(1, true)
    }

    private fun initVMs() {
        vm.getGroups().observe(this, Observer {

        })
        vm.getUser().observe(this, Observer {

        })
    }


    private fun paSetup() {
        pa = PageAdapter(supportFragmentManager)

        /* Create and add fragments to page adapter */
        pa.addFragments(MessageFragment(gid), "Message")
        pa.addFragments(AboutFragment(gid), "About")
    }

    private fun vpSetup() {
        /* ViewPager Setup */
        modalViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                positionOffsetPixels: Int
            ) = Unit

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        /* when search is selected */
                    }

                    1 -> {
                        /* when list is selected */
                    }
                }
            }
        })
        modalViewPager.adapter = pa
        messageTabs.setupWithViewPager(modalViewPager)
    }
}