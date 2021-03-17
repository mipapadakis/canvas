package com.mipapadakis.canvas

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView


private const val ON_BACK_WAIT_TIME_SHORT = 2000L // FYI: Toast.LENGTH_SHORT = 2000ms
//private const val ON_BACK_WAIT_TIME_LONG = 3500L // Toast.LENGTH_LONG = 3500ms
private const val BACK_NOT_PRESSED = 200
private const val BACK_PRESSED_ONCE = 201

class MainActivity : AppCompatActivity(), InterfaceMainActivity{
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var backIsPressed = BACK_NOT_PRESSED
    private var fab: FloatingActionButton? = null
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        toast = Toast(this)
        fab = findViewById(R.id.fab)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_gallery, R.id.nav_canvas, R.id.nav_about), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        fab?.setOnClickListener { //view ->
            Toast.makeText(this, "This is gallery fragment!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else{
            if(backIsPressed == BACK_NOT_PRESSED){
                backIsPressed = BACK_PRESSED_ONCE
                val timer = object: CountDownTimer(ON_BACK_WAIT_TIME_SHORT, ON_BACK_WAIT_TIME_SHORT) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() { backIsPressed = BACK_NOT_PRESSED }
                }
                showToast("Press back again to exit application")
                timer.start()
            }
            else{
                if (backIsPressed == BACK_PRESSED_ONCE) {
                    toast.cancel()
                }
                super.onBackPressed()
            }
        }
    }

    override fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }

    override fun hideFab() {
        fab?.visibility = View.GONE
    }

    override fun showFab() {
        fab?.visibility = View.VISIBLE
    }

    override fun setFabListener(listener: View.OnClickListener) {
        fab?.setOnClickListener(listener)
    }
}

interface InterfaceMainActivity{
    fun hideFab(){}
    fun showFab(){}
    fun setFabListener(listener: View.OnClickListener){}
    fun showToast(text: String)
}