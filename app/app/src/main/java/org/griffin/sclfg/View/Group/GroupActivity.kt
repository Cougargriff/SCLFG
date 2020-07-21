package org.griffin.sclfg.View.Group

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_modal_group.*
import org.griffin.sclfg.R
import org.griffin.sclfg.Utils.Adapters.PageAdapter
import org.griffin.sclfg.View.Group.Tabs.AboutFragment
import org.griffin.sclfg.View.Group.Tabs.MessageFragment
import org.griffin.sclfg.View.Home.HomeActivity

class GroupActivity : AppCompatActivity() {

    private lateinit var pa: PageAdapter
    private var gid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modal_group)


        try {
            gid = intent.extras!!.getString("gid", "")
        } catch (e : Exception) {
            val intent = Intent(this@GroupActivity, HomeActivity::class.java)
            ContextCompat.startActivity(this@GroupActivity, intent, null)
        }

        paSetup()
        vpSetup()
        modalViewPager.setCurrentItem(1, true)
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
                        /* when search is selected *

                         */
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