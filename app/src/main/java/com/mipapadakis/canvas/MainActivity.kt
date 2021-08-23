package com.mipapadakis.canvas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.mipapadakis.canvas.model.CvImage
import com.mipapadakis.canvas.tools.CvFileHelper
import com.mipapadakis.canvas.ui.create_canvas.CreateCanvasFragment
import java.io.File


private const val ON_BACK_WAIT_TIME_SHORT = 2000L // FYI: Toast.LENGTH_SHORT = 2000ms
//private const val ON_BACK_WAIT_TIME_LONG = 3500L // Toast.LENGTH_LONG = 3500ms
private const val BACK_NOT_PRESSED = 200
private const val BACK_PRESSED_ONCE = 201

class MainActivity : AppCompatActivity(), InterfaceMainActivity{
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var backIsPressed = BACK_NOT_PRESSED
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar)) // In app_bar_main.xml
        toast = Toast(this)
        drawerLayout = findViewById(R.id.drawer_layout) // In activity_main.xml
        navView = findViewById(R.id.nav_view) // In activity_main.xml
        navView.itemIconTintList = null
        val navController = findNavController(R.id.nav_host_fragment) // In content_main.xml
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration( setOf( //In menu activity_main_drawer
            R.id.nav_gallery,
            R.id.nav_canvas,
            R.id.nav_about,
            R.id.nav_feedback,
            R.id.nav_rate),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        //User wants to open an image using this app
        if(intent.action == Intent.ACTION_VIEW){
            //Receive the image and go to CanvasActivity:
            val cvImageUri = intent.data
            val cvImage = CvFileHelper(this).loadExternalCvImage(cvImageUri)
            if(cvImage==null) exitApp()
            else{
                CanvasViewModel.cvImage = CvImage(cvImage)
                launchCanvasActivity()
            }
        }
    }

    private fun launchCanvasActivity(){
        val intent = Intent(this, CanvasActivity::class.java)
        intent.putExtra(CreateCanvasFragment.IMPORT_CV_IMAGE_INTENT_KEY, "CanvasViewModel.cvImage contains the required cvImage.")
        startActivity(intent)
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
            if(drawerLayout[0].isSelected) showToast("Gallery is selected")
            else if(drawerLayout[1].isSelected) showToast("CreateCanvas is selected")
        }
        else{
            //If createCanvasFragment or AboutFragment are open, pressing back button opens Gallery:
            if(!navView.menu[0].isChecked) {
                super.onBackPressed()
                return
            }

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
                if (backIsPressed == BACK_PRESSED_ONCE)  toast.cancel()
//                val navController = findNavController(R.id.nav_host_fragment)
//                navController.popBackStack()
//                super.onBackPressed()
                exitApp()
            }
        }
    }

    private fun exitApp(){
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask()
        else this.finishAffinity()
    }

    override fun showToast(text: String){
        if(text.length>30) toast.duration = Toast.LENGTH_LONG
        else toast.duration = Toast.LENGTH_SHORT
        toast.cancel()
        toast = Toast.makeText(this, text, toast.duration)
        toast.show()
    }
}

interface InterfaceMainActivity{
    fun showToast(text: String)
}