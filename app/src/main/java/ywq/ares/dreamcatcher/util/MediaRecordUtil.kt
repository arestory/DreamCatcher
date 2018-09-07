package ywq.ares.dreamcatcher.util

import android.app.Activity
import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

open class MediaRecordUtil() {


    protected var mediaRecorder: MediaRecorder? = null


    var isRecord = false
    private var duration: Long = 0L

    private var filePath: String? = null

    fun startRecord(filePath: String) {


        this.filePath = filePath
        mediaRecorder.run {

            this?.release()
            mediaRecorder = null
        }

        mediaRecorder = MediaRecorder()

        // 设置麦克风为音频源
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        // 设置音频文件的编码
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        // 设置输出文件的格式
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)//可以设置成 MediaRecorder.AudioEncoder.AMR_NB

        mediaRecorder?.setOutputFile(filePath)

        mediaRecorder?.prepare()
        mediaRecorder?.start()

        isRecord = true

        duration = System.currentTimeMillis()
    }


    protected var timer: Timer? = null
    protected var timeTask: TimerTask? = null

    protected val tempVoiceFilePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "DreamCatcher" + File.separator
    open fun addDecibelListener(decibelListener: ((currentDb: Double) -> Unit), refreshTime: Long) {

        mediaRecorder = MediaRecorder()

        // 设置麦克风为音频源
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        // 设置音频文件的编码
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        // 设置输出文件的格式
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)//可以设置成 MediaRecorder.AudioEncoder.AMR_NB

        File(tempVoiceFilePath).mkdir()
        File(tempVoiceFilePath + "tmp").delete()
        mediaRecorder?.setOutputFile(tempVoiceFilePath + "tmp")

        mediaRecorder?.prepare()
        mediaRecorder?.start()


        timer = Timer()

        timeTask = timerTask {

            val ratio: Int = if (mediaRecorder == null) 0 else mediaRecorder!!.maxAmplitude

            if (ratio > 1) {


                val db = 20 * Math.log10(ratio * 1.0)


                decibelListener.invoke(db)
            }

        }


        timer?.schedule(timeTask, 1000, refreshTime)
    }

    fun endDecibelListener() {

        mediaRecorder.run {

            this?.stop()
            this?.reset()
            this?.release()
            mediaRecorder = null
        }
        timer?.cancel()
        timeTask?.cancel()
        timer = null
        timeTask = null
        File(tempVoiceFilePath).delete()


    }



    fun endRecord(listener: RecordListener) {


        isRecord = false
        mediaRecorder.run {

            this?.stop()
            this?.reset()
            this?.release()
            mediaRecorder = null
            listener.onStop(File(filePath), System.currentTimeMillis() - duration)

            filePath = ""
        }


        duration = 0

    }

    fun release() {

        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
        timer?.cancel()
        timeTask?.cancel()
        timer = null
        timeTask = null
        filePath = null

        isRecord = false
    }

    interface RecordListener {

        fun onStop(file: File, duration: Long)

    }

}
