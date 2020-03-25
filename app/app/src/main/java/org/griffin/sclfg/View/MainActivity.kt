package org.griffin.sclfg.View

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.griffin.sclfg.R

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


    }

    private fun late_setup()
    {
        pa = PageAdapter(supportFragmentManager)

        /* Create and add fragments to page adapter */

    }

}