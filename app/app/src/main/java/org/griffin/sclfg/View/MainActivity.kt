package org.griffin.sclfg.View

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activty_main.*
import org.griffin.sclfg.Models.*
import org.griffin.sclfg.R
import org.griffin.sclfg.View.Tabs.ListFragment
import org.griffin.sclfg.View.Tabs.ProfileFragment
import org.griffin.sclfg.View.Tabs.CreateFragment


class MainActivity : AppCompatActivity()
{
    /*
        Firebase / FireStore Setup
     */
    private lateinit var userRef : CollectionReference
    private lateinit var shipRef : CollectionReference
    private lateinit var locRef  : CollectionReference
    private var auth = FirebaseAuth.getInstance()
    private var db = Firebase.firestore
    private val FTAG = "FIRESTORE -> "

    /* Fragment Frameworks */
    private lateinit var pa : PageAdapter
    private val vm : ViewModel by viewModels()

    private lateinit var shipList : List<Ship>
    private lateinit var locList : List<Location>
    private lateinit var grpList : List<Group>
    private lateinit var user : User

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_main)

        lateSetup()
    }

    private fun lateSetup()
    {
        paSetup()
        vpSetup()
        firestoreSetup()
        vmSetup()

        viewPager.setCurrentItem(0, true)
    }

    private fun vmSetup()
    {
        /* View Model Setup */

        /* UI Updaters on Observation Changes from FireStore */
        vm.getShips().observe(this, Observer {
            shipList = it!!
        })
        vm.getLocs().observe(this, Observer {
            locList = it!!
        })

        vm.getUser().observe(this, Observer {
            user = it!!
        })

        vm.getGroups().observe(this, Observer {
            grpList = it!!
        })

    }

    private fun firestoreSetup()
    {
        userRef = db.collection("users")
        shipRef = db.collection("ships")
        locRef = db.collection("locations")
    }

    private fun paSetup()
    {
        pa = PageAdapter(supportFragmentManager)

        /* Create and add fragments to page adapter */
        pa.addFragments(CreateFragment(), "Search")
        pa.addFragments(ListFragment(), "List")
        pa.addFragments(ProfileFragment(), "Profile")
    }


    private fun vpSetup()
    {
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

    /*
    Used for one time initing the ship list and location list
    in our FireStore db.
    ***********************************************************
    private fun shipPush()
    {
        var shipFile = Scanner(this.assets.open("scShipList"))
        var shipColumns = shipFile.nextLine().split("\t")
        while(shipFile.hasNextLine())
        {
            var line = shipFile.nextLine()
            val items =  line.split("\t")
            val ship = hashMapOf(
                "name" to items[0].trim(),
                "manufacturer" to items[1].trim(),
                "role" to items[2].trim(),
                "size" to items[3].trim(),
                "mass" to items[4].trim(),
                "prod_state" to items[5].trim(),
                "price" to items[6].trim()
            )
            shipRef.document().set(ship)
        }
    }
    private fun locPush()
    {
        var shipFile = Scanner(this.assets.open("scLocations"))
        var locColumns = shipFile.nextLine().split("\t")
        while(shipFile.hasNextLine())
        {
            var line = shipFile.nextLine()
            val loc = hashMapOf(
                "name" to line.trim()
            )
            locRef.document().set(loc, SetOptions.merge())
        }
    }
     */

}