package ywq.ares.dreamcatcher.ui.activity

import android.Manifest
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle


import androidx.appcompat.app.AppCompatActivity

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import cn.waps.AppConnect
import cn.waps.AppListener
import com.ares.datacontentlayout.DataContentLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.toast
import ywq.ares.dreamcatcher.R
import ywq.ares.dreamcatcher.SoundApp
import ywq.ares.dreamcatcher.room.AppDatabase
import ywq.ares.dreamcatcher.room.dao.SoundDao
import ywq.ares.dreamcatcher.ui.adapter.RecordItemAdapter
import ywq.ares.dreamcatcher.ui.bean.SoundRecord
import ywq.ares.dreamcatcher.ui.bean.User
import ywq.ares.dreamcatcher.util.DeviceUtil
import ywq.ares.dreamcatcher.util.PermissionUtils
import ywq.ares.dreamcatcher.util.RxBus
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
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
        initRv()
        initDB()
        initUser()
        checkPermission()



        disposable = RxBus.get().toFlowable(SoundRecord::class.java).observeOn(AndroidSchedulers.mainThread()).subscribe {

            adapter.addItem(it)

            when {

                loadingView.getDataStatus() == DataContentLayout.DataStatus.EMPTY_CONTENT -> loadingView.showContent()
            }

        }
        fab.setOnClickListener {

            RecordVoiceActivity.start(this)
        }


    }

    companion object {

        const val REQUEST_PERMISSION = 1000
    }

    private fun initUser() {


        val cacheUser = SoundApp.getUser()

        if (cacheUser == null) {
            val user = User()
            user.userId = DeviceUtil.getAndroidID(this)
            user.isAdmin = false
            user.score = 0
            database.userDao().insertUser(user)

            println("new user = $user")
        }
        println("user = $cacheUser")


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

            getDataList()


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

        val dialog = AlertDialog.Builder(this).setMessage(getString(R.string.title_alert_dialog)).setPositiveButton(getString(R.string.action_jump), object : DialogInterface.OnClickListener {

            override fun onClick(p0: DialogInterface?, p1: Int) {
                clickJump= true
                PermissionUtils(this@MainActivity).startPermissionSetting()

            }
        }).setNegativeButton(R.string.action_cancel, object : DialogInterface.OnClickListener {

            override fun onClick(p0: DialogInterface?, p1: Int) {


                finish()
            }
        }).create()

        dialog.setOnCancelListener {

            toast("请同意这些权限，否则无法使用")
            finish()
        }

        dialog.show()
    }



    private fun showRequestPermissionDialog() {

        val dialog = AlertDialog.Builder(this).setTitle(getString(R.string.miss_permission)).setPositiveButton(getString(R.string.str_retry_apply), object : DialogInterface.OnClickListener {

            override fun onClick(p0: DialogInterface?, p1: Int) {
                checkPermission()
            }
        }).setMessage(getString(R.string.tips_authority)).setNegativeButton(R.string.action_cancel, object : DialogInterface.OnClickListener {

            override fun onClick(p0: DialogInterface?, p1: Int) {


                finish()
            }
        }).create()

        dialog.setOnCancelListener {

            toast("请同意这些权限，否则无法使用")
            finish()
        }

        dialog.show()
    }

    private fun checkPermission() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.FOREGROUND_SERVICE), REQUEST_PERMISSION)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    private fun initDB() {

        database = SoundApp.getDataBase()


    }

    private var dataSubscribe: Disposable? = null
    private var soundDao: SoundDao? = null

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

    private fun getDataList() {


        soundDao = database.soundDao()

        loadingView.showLoading()
        dataSubscribe = soundDao?.queryAll()?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe {


            when {


                it.size > 0 -> {


                    val list = it
                    val validList = (0 until list.size).map {

                        list[it]
                    }.filter {

                        File(it.url).exists()
                    }
                    println("存在有效记录数：${validList.size}")
                    list.removeAll(validList)

                    println("存在无效记录数：${list.size}")


                    Flowable.just(list).observeOn(Schedulers.io())
                            .subscribe {


                                soundDao?.deleteRecords(it)

                                it.forEach {

                                    File(it.url).delete()
                                }
                            }
                    if (validList.isNotEmpty()) {
                        adapter.itemList = (validList as ArrayList<SoundRecord>)
                        adapter.notifyDataSetChanged()
                        loadingView.showContent()
                        if(!SoundApp.getUser()!!.isAdmin){

                            showAdv()
                        }
                    } else {
                        loadingView.showEmptyContent()
                    }

                }
                else -> loadingView.showEmptyContent()
            }

        }

    }


    private fun initRv() {

        adapter = RecordItemAdapter()
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        adapter.recyclerView = rv

        adapter.setMoreClickListener {


            item, sound ->
            when (item.itemId) {


                R.id.action_delete -> {

                    database.soundDao().delete(sound)
                    adapter.removeItem(sound)
                    true
                }
                else -> {

                    val user = SoundApp.getUser()
//                    val shareFile = File(sound.url)
//                    if(shareFile.exists()){
//                        val intent = Intent()
//                        intent.action = Intent.ACTION_SEND
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        intent.addCategory("android.intent.category.DEFAULT")
////                        val comp =  ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareFileUI")
////                        intent.component = comp
//
//                        val uri = DeviceUtil.getFileUri(this,shareFile)
//                        intent.setDataAndType(uri,"audio/mp4a-latm")
//                        try {
//                            this.startActivity(Intent.createChooser(intent, shareFile.getName()))
//                        } catch ( e:Exception) {
//                            e.printStackTrace()
//                        }
//
//
//                    }

                    if (user != null && !user.isAdmin) {


                        showToast("权限不够")

                    } else {
                        showToast("文件路径:${sound.url}")

                    }
                    false
                }
            }

        }

        adapter.addPlayVoiceListener(object : RecordItemAdapter.PlayVoiceListener {


            override fun onPlaying(item: SoundRecord) {


            }

            override fun finish(item: SoundRecord) {


            }

            override fun fail(item: SoundRecord) {

                Toast.makeText(this@MainActivity, getString(R.string.tips_play_fail), Toast.LENGTH_LONG).show()

                soundDao?.delete(item)
                File(item.url).delete()
            }
        })
        swipeLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        swipeLayout.setOnRefreshListener {

            adapter.release()
            database.soundDao().queryAll()?.delay(2, TimeUnit.SECONDS)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe {

                if (it.size > 0) {


                    val list = it
                    //过滤掉无效的数据
                    val validList = (0 until list.size).map {

                        pos ->
                        list[pos]
                    }.filter {

                        soundRecord ->
                        File(soundRecord.url).exists()

                    }
                    if (validList.isNotEmpty()) {
                        adapter.itemList = (validList as ArrayList<SoundRecord>)
                        adapter.notifyDataSetChanged()
                        swipeLayout.isRefreshing = false
                        loadingView.showContent()
                    } else {
                        loadingView.showEmptyContent()
                    }

                } else {
                    loadingView.showEmptyContent()
                    swipeLayout.isRefreshing = false

                }
            }

        }
    }

    private fun showToast(msg: String) {


        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        this.adapter.release()
        disposable.dispose()
        dataSubscribe?.dispose()
    }
}

