package ywq.ares.dreamcatcher.util

import android.app.Activity
import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

class MediaRecordUtilExtension(var context: Context) :MediaRecordUtil(){


    override fun addDecibelListener(decibelListener: ((currentDb: Double) -> Unit), refreshTime: Long) {

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


                println("db = $db")
                if (context is Activity) {

                    (context as Activity).runOnUiThread {

                        decibelListener.invoke(db)

                    }

                } else {
                    decibelListener.invoke(db)
                }
            }

        }


        timer?.schedule(timeTask, 1000, refreshTime)
    }











}
