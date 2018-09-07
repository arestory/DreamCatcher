package ywq.ares.dreamcatcher.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import ywq.ares.dreamcatcher.R
import kotlinx.android.synthetic.main.activity_record.*
import ywq.ares.dreamcatcher.SoundApp
import ywq.ares.dreamcatcher.ui.bean.RecordParams
import ywq.ares.dreamcatcher.ui.bean.SoundRecord
import ywq.ares.dreamcatcher.ui.fragment.NumPickerFragment
import ywq.ares.dreamcatcher.ui.service.RecordVoiceService
import ywq.ares.dreamcatcher.util.MediaRecordUtil
import ywq.ares.dreamcatcher.util.MediaRecordUtilExtension
import ywq.ares.dreamcatcher.util.RxBus
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask


class RecordVoiceActivity : BaseActivity() {


    override fun getLayoutId(): Int {


        return R.layout.activity_record
    }


    //seekbar进度和实际分贝的差值
    private val diffValue = 30
    private val voiceUtil = MediaRecordUtilExtension(this)


    private var mMinute = 300
    private var dreamTime = 15
    override fun doMain() {

        initToolbarSetting(toolbar)

        dreamEt.addTextChangedListener(object :TextWatcher{

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            }

            override fun afterTextChanged(s: Editable?) {


                if (!s.isNullOrEmpty() && s.toString().toInt() > 30) {

                    dreamEt.setText(30.toString())
                    dreamEt.setSelection(2)
                    dreamTime=30
                    Toast.makeText(this@RecordVoiceActivity, getString(R.string.sleep_suggestion), Toast.LENGTH_SHORT).show()
                }else if(!s.isNullOrEmpty()){


                    dreamTime=s.toString().toInt()
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

        })
        dreamEt.setText("$dreamTime")
        dreamBtn.setOnClickListener {

            val dialogFragment = NumPickerFragment.newInstance(dreamTime,29,getString(R.string.action_select_sleep_time))

            dialogFragment.show(supportFragmentManager,"dreamTime")

            dialogFragment.setOnMinuteChangeListener {


                dreamTime = it
                dreamEt.setText("$dreamTime")

            }

        }

        deadlineEt.addTextChangedListener(object :TextWatcher{

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            }

            override fun afterTextChanged(s: Editable?) {


                if (!s.isNullOrEmpty() && s.toString().toInt() > 360) {

                    deadlineEt.setText(360.toString())
                    deadlineEt.setSelection(3)
                    mMinute=360
                    Toast.makeText(this@RecordVoiceActivity, getString(R.string.end_suggestion), Toast.LENGTH_SHORT).show()
                }else if(!s.isNullOrEmpty()){


                    mMinute=s.toString().toInt()
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

        })
        deadlineEt.setText("$mMinute")

        dbSeekBar.progress = 50
        tvVoice.text = getString(R.string.db_setting, dbSeekBar.progress + diffValue)
        dbSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                tvVoice.text = getString(R.string.db_setting, progress + diffValue)


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {


            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {


            }
        })


        listenerBtn.setOnClickListener {


            listenerBtn.isSelected = !listenerBtn.isSelected
            toggleCheckVoice(listenerBtn.isSelected)
            when (listenerBtn.isSelected) {

                true -> listenerBtn.text = getString(R.string.str_stop_listen_voice)
                false -> listenerBtn.text =getString(R.string.str_listener_voice)
            }
        }

        deadlineBtn.setOnClickListener {

            val dialogFragment = NumPickerFragment.newInstance(mMinute,359,getString(R.string.action_select_deadline))

            dialogFragment.show(supportFragmentManager,"deadline")

            dialogFragment.setOnMinuteChangeListener {


                mMinute = it
               deadlineEt.setText("$mMinute")

            }
        }

        rg.setOnCheckedChangeListener { _, checkedId ->



            when (checkedId) {

                R.id.conditionRb -> intervalLayout.visibility = View.VISIBLE
                else -> intervalLayout.visibility = View.GONE
            }

        }
        intervalBtn.setOnClickListener {

            val dialog = NumPickerFragment.newInstance(intervalEt.text.toString().toInt(),30,getString(R.string.action_select_recording_length))
            dialog.show(supportFragmentManager, "NumPickerFragment")
            dialog.setOnMinuteChangeListener {

                intervalEt.setText(it.toString())
            }
        }
        intervalEt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }

            override fun afterTextChanged(s: Editable?) {


                if (!s.isNullOrEmpty() && s.toString().toInt() > 30) {

                    intervalEt.setText(30.toString())
                    intervalEt.setSelection(2)
                    Toast.makeText(this@RecordVoiceActivity, getString(R.string.record_suggestion), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            }

        })

        beginBtn.setOnClickListener {


            showJobDialog()
        }

    }


    private fun showJobDialog() {


        toggleCheckVoice(false)
        val currentDBvalue = dbSeekBar.progress + diffValue
        val intervalTime = intervalEt.text.toString().toInt()
        val dialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_catcher_dialog))
                .setMessage(getString(R.string.content_catcher_dialog,mMinute,currentDBvalue,intervalTime))
                .setPositiveButton(getString(R.string.action_confirm)) { _, which ->

                    val params = RecordParams()
                    params.db=currentDBvalue
//                    params.hour=mHour
                    params.minute=mMinute
                    params.dreamTime=dreamTime
                    params.intervalTime=intervalTime
                    RecordVoiceService.start(this@RecordVoiceActivity,params)
                    finish()

                }
                .setNegativeButton(getString(R.string.action_cancel)) { _, which -> RecordVoiceService.stop(this) }
                .create()
        dialog.show()


    }


    private fun toggleCheckVoice(startOrStop: Boolean) {

        if (startOrStop) {
            voiceUtil.addDecibelListener(decibelListener = {


                dbSeekBar.progress = it.toInt() - diffValue


            }, refreshTime = 1000)

        } else {
            voiceUtil.endDecibelListener()
        }

    }

    private val testFilePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "DreamCatcher" + File.separator

    private var recordTimer: Timer? = null
    private var recordTask: TimerTask? = null

    private var stop = false
    private fun startDreamCatcher() {

        if (!stop) {
            voiceUtil.addDecibelListener(decibelListener = {

                println("当前分贝：$it")
                if (it > 80) {


                    println("超过设定分贝，开始录音")
                    voiceUtil.endDecibelListener()
                    voiceUtil.startRecord(testFilePath + System.currentTimeMillis() + ".m4a")

                    recordTask = timerTask {

                        endRecord()
                    }
                    recordTimer = Timer()
                    recordTimer?.schedule(recordTask, 20000, 20000)
                }

            }, refreshTime = 3000)
        }

    }

    private fun endRecord() {

        voiceUtil.endRecord(object : MediaRecordUtil.RecordListener {

            override fun onStop(file: File, duration: Long) {

                println("结束录音，file = ${file.path} duration = $duration")

                val sound = SoundRecord()
                sound.url = file.path
                sound.duration = duration
                sound.timeStamp = System.currentTimeMillis()

                SoundApp.getDataBase().soundDao().insert(sound)

                RxBus.get().post(sound)

                voiceUtil.release()
                recordTimer?.cancel()
                recordTask?.cancel()
                startDreamCatcher()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {


    }


    override fun onDestroy() {
        super.onDestroy()

        recordTimer?.cancel()
        recordTask?.cancel()
        stop = false
        if (voiceUtil.isRecord) {

            endRecord()
        }
        voiceUtil.release()


    }

    companion object {

        fun start(context: Context) {


            context.startActivity(Intent(context, RecordVoiceActivity::class.java))

        }
    }

}