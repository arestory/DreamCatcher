package ywq.ares.dreamcatcher.util

import android.app.Activity
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VoiceUtil(val context: Context) {

    private var mMediaRecorder: MediaRecorder? = null
    private val SAMPLE_RATE_IN_HZ = 8000
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)
    private var mAudioRecord: AudioRecord ?= null
    private var voiceStatus: Status = Status.NO_READY
    private var db: Double? = null
    private var maxDb: Double = 0.0

    private var recordThread: Thread? = null

    private val threadpool: ExecutorService = Executors.newCachedThreadPool()
    private var refreshTime: Long = 1000

    private var duration: Long = 0
    private var currentVoiceFile: File? = null


    fun startRecordVoice() {


        voiceStatus = Status.RECORD_VOICE

        threadpool.submit {

            mMediaRecorder = MediaRecorder()
            val voiceFile = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "sleepTalker" + File.separator + System.currentTimeMillis() + ".m4a")
            if (!voiceFile.exists()) {
                voiceFile.parentFile.mkdirs()
                voiceFile.createNewFile()
            }
            currentVoiceFile = voiceFile
            //从麦克风采集
            mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            //保存文件为MP4格式
            mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            //所有android系统都支持的适中采样的频率
            mMediaRecorder?.setAudioSamplingRate(44100);

            //通用的AAC编码格式
            mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            //设置音质频率
            mMediaRecorder?.setAudioEncodingBitRate(96000)
            //设置文件录音的位置
            mMediaRecorder?.setOutputFile(voiceFile.absolutePath)
            //开始录音
            mMediaRecorder?.prepare()
            mMediaRecorder?.start()
            duration = System.currentTimeMillis()

        }


    }


    interface RecordListener {

        fun onStop(file: File, duration: Long?)

        fun onCancel()

    }

    init {

        mAudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE)
    }

    private fun createAudio():AudioRecord{


        return  AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE)
    }

    fun startDecibelListener(timeRefresh: Long, listener: DecibelListener) {


        when(mAudioRecord){

            null -> {

                mAudioRecord = createAudio()
            }
        }

        mAudioRecord?.startRecording()

        recordThread = Thread(Runnable {

            val buffer = ShortArray(BUFFER_SIZE)

            voiceStatus = Status.COLLECT_VOICE

            while (voiceStatus != Status.NO_READY) {
                var r = 0
                r = mAudioRecord?.read(buffer, 0, BUFFER_SIZE)!!

                var v: Long = 0
                for (i in 0 until buffer.size) {
                    v += buffer[i] * buffer[i]
                }
                val mean = v / r.toDouble()
                val volume = 10 * Math.log10(mean)

                if (!volume.isNaN()) {
                    println("printlndb value= $volume ThreadName = ${Thread.currentThread().name}")

                    db = volume
                    if (volume > maxDb) {
                        maxDb = volume
                    }

                }
                Thread.sleep(timeRefresh)


                if (context is Activity) {


                    context.runOnUiThread {

                        listener.on(db!!, maxDb)

                    }
                }else{
                    listener.on(db!!, maxDb)

                }

            }
        })

        recordThread?.start()
    }

    fun startDecibelListener(listener: DecibelListener) {


        startDecibelListener(refreshTime, listener)


    }

    fun isRecord(): Boolean {


        return when (voiceStatus) {

            Status.NO_READY -> false
            Status.COLLECT_VOICE -> true
            Status.RECORD_VOICE -> true

            else -> false


        }
    }

    fun stopCollect() {


        voiceStatus = Status.NO_READY
        mAudioRecord?.stop()
        mAudioRecord?.release()
        mAudioRecord = null
        recordThread = null

    }

    private fun releaseLastRecord() {


        mMediaRecorder?.setOnErrorListener(null)
        mMediaRecorder?.setOnInfoListener(null)
        mMediaRecorder?.setPreviewDisplay(null)
        mMediaRecorder?.stop()
        mMediaRecorder = null
        currentVoiceFile = null
        voiceStatus = Status.NO_READY

    }

    fun stopRecord(recordListener: RecordListener) {

        if (currentVoiceFile != null && currentVoiceFile!!.exists()) {
            recordListener.onStop(currentVoiceFile!!, (System.currentTimeMillis() - duration))
        }

        releaseLastRecord()
    }


    enum class Status {

        NO_READY,
        COLLECT_VOICE,
        PAUSE,
        RECORD_VOICE


    }


    interface DecibelListener {


        fun on(currentDb: Double, maxDb: Double)
    }


}