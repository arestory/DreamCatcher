package ywq.ares.dreamcatcher.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ywq.ares.dreamcatcher.R
import ywq.ares.dreamcatcher.SoundApp

import ywq.ares.dreamcatcher.ui.bean.RecordParams
import ywq.ares.dreamcatcher.ui.bean.SoundRecord
import ywq.ares.dreamcatcher.util.MediaRecordUtil
import ywq.ares.dreamcatcher.util.RxBus
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

class RecordVoiceService : Service() {

    val tag = RecordVoiceService::class.java.name
    private val channelId = "1"
    private var params = RecordParams(1, 300,30, 80)

    private val mediaRecordUtil = MediaRecordUtil()

    private var createTime: Long = System.currentTimeMillis()

    val soundDao = SoundApp.getDataBase().soundDao()
    private var notificationChannel: NotificationChannel? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(tag,"onStartCommand")

        val serializableObj = intent?.getSerializableExtra("params")
        if (serializableObj != null) {
            params = serializableObj as RecordParams

        }
        Log.i(tag,params.toString())

        //到达入睡时间后再进行检测
        Timer().schedule(timerTask {


            checkVoice()

        },params.dreamTime*60*1000L)

        return super.onStartCommand(intent, flags, startId)
    }

 

    //检查是否到截止时间
    private fun isDeadLine(): Boolean {


        val currentTime = System.currentTimeMillis()

        val deadlineMinute = params.minute
        Log.i(tag,"duration = ${currentTime-createTime}")
        return when (currentTime - createTime) {

            in 0..deadlineMinute * 60 * 1000 -> false
            else -> true


        }
    }


    override fun onCreate() {
        super.onCreate()

        //创建的当前时间:小时
//        createOnHourOfDay = java.util.Calendar.getInstance(Locale.CHINESE).get(java.util.Calendar.HOUR_OF_DAY)
        createTime = System.currentTimeMillis()
        Log.i(tag,"on create !!! time = $createTime")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, "DreamCatcher", NotificationManager.IMPORTANCE_HIGH)

            notificationChannel?.enableLights(true)
            notificationChannel?.setShowBadge(true)
            notificationChannel?.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            val notification = NotificationCompat.Builder(this, notificationChannel!!.id)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("正在监听环境声音")
                    .build()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
            startForeground(1, notification)
        } else {
            val notification = Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("正在监听环境声音")
                    .setContentText("test")
                    .build()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(1, notification)

        }






    }

    fun checkVoice() {

        mediaRecordUtil.addDecibelListener(decibelListener = {


            Log.i(tag,"current db = $it")

            if(!isDeadLine()){
                if (it > params.db) {

                    Log.i(tag,"超过设定阈值,开始录音")

                    startRecordVoice()

                }
            }else{

                //到达截止时间时释放资源
                Log.i(tag,"到达截止时间时释放资源")
                releaseVoiceUtil()
                stop(this)
            }


        }, refreshTime = 5000)
    }

    private var recordTimer: Timer? = null
    private var recordTimeTask: TimerTask? = null




    private fun startRecordVoice() {


        //释放资源
        releaseVoiceUtil()
        val voiceFile = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "dreamCatcher" + File.separator + System.currentTimeMillis() + ".m4a")

        mediaRecordUtil.startRecord(voiceFile.path)
        recordTimer = Timer()
        recordTimeTask = timerTask {

          endRecord()
        }
        recordTimer?.schedule(recordTimeTask, params.intervalTime * 60 * 1000L, params.intervalTime * 60 * 1000L)


    }

    private fun endRecord(){


        if(mediaRecordUtil.isRecord){


            mediaRecordUtil.endRecord(object : MediaRecordUtil.RecordListener {


                override fun onStop(file: File, duration: Long) {


                    val soundRecord = SoundRecord()
                    soundRecord.timeStamp = System.currentTimeMillis()
                    soundRecord.url = file.path
                    soundRecord.duration = duration
                    //插入数据库
                    soundDao.insert(soundRecord)
                    RxBus.get().post(soundRecord)
                    Log.i(tag,"endRecord file = ${file.path}")
                    //继续监听环境分贝
                    checkVoice()
                }
            })
        }
    }

    private fun releaseVoiceUtil() {


        mediaRecordUtil.endDecibelListener()
        mediaRecordUtil.release()
        recordTimer?.cancel()
        recordTimer = null
        recordTimeTask?.cancel()
        recordTimeTask = null
    }


    companion object {

        fun start(context: Context, params: RecordParams) {
            val starter = Intent(context, RecordVoiceService::class.java)
            starter.putExtra("params", params)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(starter)
            } else {
                context.startService(starter)
            }
        }

        fun stop(context: Context) {
            val stoper = Intent(context, RecordVoiceService::class.java)
            context.stopService(stoper)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i(tag,"onDestory")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.deleteNotificationChannel(notificationChannel?.id)

        }

        mediaRecordUtil.endDecibelListener()
        mediaRecordUtil.release()
    }
}
