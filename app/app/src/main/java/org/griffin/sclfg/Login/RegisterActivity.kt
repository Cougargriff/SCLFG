package org.griffin.sclfg.Login

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import org.griffin.sclfg.R
import org.griffin.sclfg.Utils.Cache.LocalCache
import org.griffin.sclfg.View.Home.HomeActivity

class RegisterActivity : AppCompatActivity() {
    private val BUTTON_ELEVATION by lazy {
        applicationContext.resources.displayMetrics.density * 8
    }
    private lateinit var localCache: LocalCache
    private lateinit var display_name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        reg_anim_ship.apply {
            setAnimation("reg_ship.json")
            speed = 0.25f
            loop(true)
            playAnimation()
        }

        localCache = LocalCache(this)
        setupRegisterOnclick()
    }

    private var err_cb = fun() {
        /* reset button heights on failed attempt */
        signup_button.elevation = BUTTON_ELEVATION
    }

    private fun hideUi() {
        register_container.visibility = View.GONE
    }

    private fun startAnimation(cb_end: () -> Unit, cb_start: () -> Unit) {
        reg_anim.visibility = View.VISIBLE
        reg_anim.apply {
            setAnimation("register_loading.json")
            playAnimation()
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    cb_end()
                }

                override fun onAnimationCancel(animation: Animator?) = Unit
                override fun onAnimationRepeat(animation: Animator?) = Unit
                override fun onAnimationStart(animation: Animator?) {
                    cb_start()
                }
            })
        }
    }

    var cache_register_cb = fun(user: EmailPasswordLoginHandler.User, uid: String) {
        hideUi()
        localCache.cacheCredentials(user)
        startAnimation({
            register_cb(uid)
        }, {
            reg_anim_ship.visibility = View.GONE
            signup_button.visibility = View.GONE
        })
    }

    var register_cb = fun(uid: String) {
        /* INIT USER w/ display name */
        initUser(uid) {
            var intent = Intent(this@RegisterActivity, HomeActivity::class.java)
            intent.putExtra("display_name", display_name)
            ContextCompat.startActivity(this@RegisterActivity, intent, null)
        }
    }

    private fun initUser(uid: String, cb: () -> Unit) {
        val userRef = Firebase.firestore.collection("users")
        userRef.document(uid).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    var result = it.result!!
                    /* create the user if they don't exist already */
                    if (!result.exists()) {
                        var initUser = hashMapOf(
                            "timeCreated" to System.currentTimeMillis().toString(),
                            "inGroups" to emptyList<String>(),
                            "screenName" to display_name
                        )
                        userRef.document(uid).set(initUser).also {
                            cb()
                        }
                    }
                }
            }
    }

    private fun setupRegisterOnclick() {
        signup_button.setOnClickListener {
            signup_button.elevation = 0f
            var rpacket = EmailPasswordLoginHandler.User(email = "", password = "")
            var err = false
            /*
                Check all fields for non empty.
                Check passwords match
             */
            if (emailR.text.isNotBlank() &&
                (passwordR.text.isNotBlank() && passwordR_confirm.text.isNotBlank()) &&
                passwordR.length() >= 6 && passwordR_confirm.length() >= 6 &&
                (passwordR.text.toString().compareTo(passwordR_confirm.text.toString()) == 0) &&
                screen_name.text.isNotBlank()
            ) {
                rpacket.email = emailR.text.toString()
                rpacket.password = passwordR.text.toString()
                display_name = screen_name.text.toString()
            } else {
                err = true
            }

            if (err) {
                Toast.makeText(
                    this,
                    "Either email or password was incorrect!", Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show()
                EmailPasswordLoginHandler(rpacket, err_cb).apply {
                    register(cache_register_cb)
                }
            }
        }
    }
}