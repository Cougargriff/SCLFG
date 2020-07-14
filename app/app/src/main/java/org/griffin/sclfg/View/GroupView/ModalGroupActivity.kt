package org.griffin.sclfg.View.GroupView

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_modal_group.*
import org.griffin.sclfg.R
import org.griffin.sclfg.View.GroupView.Messaging.MessageFragment
import org.griffin.sclfg.View.GroupView.Messaging.MessageViewModel
import org.griffin.sclfg.View.Home.MainActivity
import org.griffin.sclfg.View.Home.PageAdapter
import java.lang.Exception

class ModalGroupActivity : AppCompatActivity() {

    private lateinit var pa : PageAdapter
    private  var gid = ""
    private val msgVm : MessageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modal_group)


        try {
           // gid = savedInstanceState!!.getString("gid", "")
        }
        catch(err : Exception) {
            /* If fail to get gid, return to main screen */
            val intent = Intent(this@ModalGroupActivity, MainActivity::class.java)
            ContextCompat.startActivity(this@ModalGroupActivity, intent, null)
        }


        paSetup()
        vpSetup()
        modalViewPager.setCurrentItem(0, true)

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