package ywq.ares.dreamcatcher.ui.activity

import android.Manifest
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle


import androidx.appcompat.app.AppCompatActivity

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import cn.waps.AppConnect
import cn.waps.AppListener
import com.ares.datacontentlayout.DataContentLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayout
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.tableLayout
import org.jetbrains.anko.toast
import ywq.ares.dreamcatcher.R
import ywq.ares.dreamcatcher.SoundApp
import ywq.ares.dreamcatcher.room.AppDatabase
import ywq.ares.dreamcatcher.room.dao.SoundDao
import ywq.ares.dreamcatcher.ui.adapter.RecordItemAdapter
import ywq.ares.dreamcatcher.ui.adapter.TabAdapter
import ywq.ares.dreamcatcher.ui.bean.SoundRecord
import ywq.ares.dreamcatcher.ui.bean.User
import ywq.ares.dreamcatcher.ui.fragment.MineDreamFragment
import ywq.ares.dreamcatcher.ui.fragment.SocialFragment
import ywq.ares.dreamcatcher.util.DeviceUtil
import ywq.ares.dreamcatcher.util.PermissionUtils
import ywq.ares.dreamcatcher.util.RxBus
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var adapter: RecordItemAdapter
    private lateinit var disposable: Disposable

    private var pauseAndResume = false
    private var clickJump = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        checkPermission()

        fab.setOnClickListener {

            RecordVoiceActivity.start(this)
        }

    }
    companion object {

        const val REQUEST_PERMISSION = 1000
    }
    override fun onResume() {
        super.onResume()

        println("------- onresume -------")
        if(pauseAndResume&&clickJump){
            checkPermission()
        }
        pauseAndResume = false
        clickJump=false
    }

    override fun onPause() {
        super.onPause()
        println("------- onPause -------")
        pauseAndResume=true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isPermissionPass = grantResults.any { it == -1 }
        println("权限申请通过 ${!isPermissionPass}")
        if (!isPermissionPass) {
            initView()
        } else {
            val ignorePer= permissions.map {
                //检测用户是否点击了不再询问
                val flag =  ActivityCompat.shouldShowRequestPermissionRationale(this,it)

                println(" it per = $flag")
                 flag
            }.any {

                false
            }
            if(!ignorePer){

                showAlertDialog()


            }else{
                showRequestPermissionDialog()
            }
        }


    }

    private fun showAlertDialog() {
        val dialog = AlertDialog.Builder(this).setMessage(getString(R.string.title_alert_dialog)).setPositiveButton(getString(R.string.action_jump)) { p0, p1 ->
            clickJump= true
            PermissionUtils(this@MainActivity).startPermissionSetting()
        }.setNegativeButton(R.string.action_cancel) { p0, p1 -> finish() }.create()

        dialog.setOnCancelListener {

            toast("请同意这些权限，否则无法使用")
            finish()
        }

        dialog.show()
    }



    private fun showRequestPermissionDialog() {

        val dialog = AlertDialog.Builder(this).setTitle(getString(R.string.miss_permission)).setPositiveButton(getString(R.string.str_retry_apply)) { p0, p1 -> checkPermission() }.setMessage(getString(R.string.tips_authority)).setNegativeButton(R.string.action_cancel) { p0, p1 -> finish() }.create()

        dialog.setOnCancelListener {

            toast("请同意这些权限，否则无法使用")
            finish()
        }

        dialog.show()
    }

    private fun checkPermission() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE), REQUEST_PERMISSION)
        }else{
            initView()
        }
    }

    private fun initView(){
        val list  = ArrayList<Fragment>()
        list.add(MineDreamFragment.newInstance())
        list.add(SocialFragment.newInstance())
        val titles  = ArrayList<String>()
        titles.add("我的")
        titles.add("社区")

        val pagerAdapter = TabAdapter(supportFragmentManager,list,titles)
        viewPager.adapter = pagerAdapter

        tabLayout.tabMode = TabLayout.MODE_FIXED
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE)
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {


            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

            }


        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }



    private fun showAdv() {


        val adRequest = AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()


        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {

            override fun onAdClosed() {

                println("onAdClosed ")

            }

            override fun onAdLoaded() {
                println("onAdLoaded ")

                adView.visibility = View.GONE

            }

            override fun onAdFailedToLoad(p0: Int) {

                println("onAdFailedToLoad $p0")
            }
        }

        val timer = Timer()

        timer.schedule(timerTask {


            AppConnect.getInstance(this@MainActivity).showBannerAd(this@MainActivity, advLayout, object : AppListener() {


                override fun onBannerClose() {
                    super.onBannerClose()
                    runOnUiThread {

                        advLayout.visibility = View.GONE

                    }
                }
            })
            timer.schedule(timerTask {


                runOnUiThread {
                    advLayout.visibility = View.VISIBLE

                }


            }, 1000)
        }, 10000)
    }





    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId




        return when (id) {
            R.id.action_settings -> {

                SettingActivity.start(this)
                true
            }
            R.id.action_about -> {

                AboutActivity.start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }


}

