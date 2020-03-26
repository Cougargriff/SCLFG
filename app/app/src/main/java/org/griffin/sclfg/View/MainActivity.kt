package org.griffin.sclfg.View


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activty_main.*
import org.griffin.sclfg.R
import org.griffin.sclfg.View.Tabs.ListFragment
import org.griffin.sclfg.View.Tabs.ProfileFragment
import org.griffin.sclfg.View.Tabs.SearchFragment
import java.util.*

class MainActivity : AppCompatActivity()
{
    /*
        Firebase / FireStore Setup
     */
    private lateinit var userRef : DocumentReference
    private lateinit var shipRef : CollectionReference
    private var auth = FirebaseAuth.getInstance()
    private var db = Firebase.firestore
    private val FTAG = "FIRESTORE -> "

    /* Fragment Frameworks */
    private lateinit var pa : PageAdapter
    // private lateinit var vm : ViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_main)

        lateSetup()
    }

    private fun lateSetup()
    {
        firestoreSetup()
        paSetup()
        vpSetup()
    }

    private fun firestoreSetup()
    {
        userRef = db.collection("users")
            .document(auth.uid.toString())
        shipRef = db.collection("ships")
        
    }

    /*
    Used for one time initing the ship database list in FireStore

    private fun shipPush()
    {
        var shipFile = Scanner(this.assets.open("scShipList"))
        var shipColumns = shipFile.nextLine().split("\t")

        var name = shipColumns[0]
        var manuf = shipColumns[1]
        var role = shipColumns[2]
        var sz = shipColumns[3]
        var mass = shipColumns[4]
        var prod = shipColumns[5]
        var price = shipColumns[6]


        while(shipFile.hasNextLine())
        {
            var line = shipFile.nextLine()
            val items =  line.split("\t")

            val ship = hashMapOf(
                name to items[0],
                manuf to items[1],
                role to items[2],
                sz to items[3],
                mass to items[4],
                prod to items[5],
                price to items[6]
            )
            shipRef.document().set(ship, SetOptions.merge())
        }
    }
     */

    private fun paSetup()
    {
        pa = PageAdapter(supportFragmentManager)

        /* Create and add fragments to page adapter */
        pa.addFragments(SearchFragment(), "Search")
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
}