package org.griffin.sclfg.View


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.griffin.sclfg.R
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activty_main.*
import org.griffin.sclfg.View.Tabs.ListFragment
import org.griffin.sclfg.View.Tabs.ProfileFragment
import org.griffin.sclfg.View.Tabs.SearchFragment

class MainActivity : AppCompatActivity()
{
    /*
        Firebase/Firestore Setup
     */
    private lateinit var userRef : DocumentReference


    private lateinit var pa : PageAdapter
    // private lateinit var vm : ViewModel

    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_main)

        lateSetup()
    }

    private fun lateSetup()
    {
        pa = PageAdapter(supportFragmentManager)

        /* Create and add fragments to page adapter */
        pa.addFragments(SearchFragment(), "Search")
        pa.addFragments(ListFragment(), "List")
        pa.addFragments(ProfileFragment(), "Profile")

        /* ViewPager Setup */
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {}
            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int)
            {}

            override fun onPageSelected(position: Int)
            {
                when(position)
                {
                    0 ->
                    {
                        /* when search is selected */
                    }

                    1 ->
                    {
                        /* when list is selected */
                    }

                    2 ->
                    {
                        /* when profile is selected */
                    }

                }
            }
        })

        viewPager.adapter = pa
        tabLayout.setupWithViewPager(viewPager)
    }

}