package ywq.ares.dreamcatcher.ui.adapter

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ywq.ares.dreamcatcher.R
import ywq.ares.dreamcatcher.SoundApp
import ywq.ares.dreamcatcher.ui.bean.SoundRecord
import ywq.ares.dreamcatcher.util.ExMediaPlayer
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RecordItemAdapter : RecyclerView.Adapter<RecordItemAdapter.ViewHolder>() {


    var itemList: ArrayList<SoundRecord> = ArrayList()

    var recyclerView: RecyclerView? = null

    private var lastPlayIndex: Int = -1

    private var context: Context? = null


    private val mpForSound = HashMap<SoundRecord, ExMediaPlayer>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        context = parent.context
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        val rootViewHolder = ViewHolder(rootView)
        rootViewHolder.playBtn = rootView.findViewById(R.id.playBtn)
        rootViewHolder.moreBtn = rootView.findViewById(R.id.moreBtn)
        rootViewHolder.seekBar = rootView.findViewById(R.id.seekBar)
        rootViewHolder.timeTv = rootView.findViewById(R.id.timeTv)
        rootViewHolder.dateTv = rootView.findViewById(R.id.dateTv)
        rootViewHolder.cardView = rootView.findViewById(R.id.cardView)

        return rootViewHolder
    }

    private var listener: ((view: View, item: SoundRecord) -> Unit)? = null
    fun setItemLongClickListener(listener: (view: View, item: SoundRecord) -> Unit) {
        this.listener = listener
    }

    private var menuListener: ((item: MenuItem, sound: SoundRecord) -> Boolean)? = null
    fun setMoreClickListener(listener: (item: MenuItem, sound: SoundRecord) -> Boolean) {
        this.menuListener = listener
    }

    interface ItemLongClickListener {

        fun onLongClick(view: View, item: SoundRecord)
    }

    interface MoreClickListener {

        fun onMenuClick(item: MenuItem, sound: SoundRecord)
    }


    private var playListener: PlayVoiceListener? = null
    fun addPlayVoiceListener(listener: PlayVoiceListener) {
        this.playListener = listener
    }

    interface PlayVoiceListener {

        fun onPlaying(item: SoundRecord)

        fun finish(item: SoundRecord)

        fun fail(item: SoundRecord)
    }

    fun addList(itemList: ArrayList<SoundRecord>) {

        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    fun addItem(sound: SoundRecord) {

        addItem(0, sound)
    }

    fun addItem(position: Int, sound: SoundRecord) {

        this.itemList.add(position, sound)
        notifyDataSetChanged()
    }

    fun removeItem(sound: SoundRecord) {


        if(sound.playStatus == SoundRecord.STATUS.PLAYING){
            sound.playStatus = SoundRecord.STATUS.PAUSE
            val mediaPlayer = mpForSound[sound]

            mediaPlayer?.pause()
            lastPlayIndex = -1
        }


        File(sound.url).delete()
        this.itemList.remove(sound)

        notifyDataSetChanged()
    }


    private fun compareData(date: Long): String {
        val template = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE)
        val duration = (System.currentTimeMillis() - date) / 1000

        if (SoundApp.isZhLanguage()) {
            return when (duration) {

                in 0..60 * 2 -> "刚刚"
                in 60 * 2..3600 -> "${duration / 60}分钟前"
                in 3600..3600 *3-> "${duration / 3600}小时前"
                else -> template.format(date)

            }
        } else {
            return when (duration) {

                in 0..60 * 2 -> "just now"
                in 60 * 2..3600 -> "before ${duration / 60}minutes"
                in 3600..3600 * 3 -> "before ${duration / 3600}hours"
                else -> template.format(date)

            }
        }


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val template = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE)

        val item = itemList[position]

        holder.cardView.setOnLongClickListener {

            listener?.invoke(it, item)
            false

        }
        holder.dateTv.text = template.format(item.timeStamp)
        holder.dateTv.text = compareData(item.timeStamp)
        val duration = item.duration * 1.0f / 1000
        if (duration > 60) {

            val minute = if (SoundApp.isZhLanguage()) {
                when (duration / 60) {



                    in 0..10 -> String.format("%.0f", duration / 60)
                    else -> String.format("%.0f", duration % 60)


                }
            } else {
                when (duration / 60) {

                    in 0..10 -> "0".plus(String.format("%.0f", duration / 60))
                    else -> String.format("%.0f", duration % 60)


                }
            }
            val seconds = if(SoundApp.isZhLanguage()){
                when (duration % 60) {

                    in 0..10 -> String.format("%.0f", duration / 60)
                    else -> String.format("%.0f", duration % 60)


                }
            }else{
                when (duration % 60) {

                    in 0..10 -> "0".plus(String.format("%.0f", duration / 60))
                    else -> String.format("%.0f", duration % 60)


                }
            }
            holder.timeTv.text = holder.itemView.context.getString(R.string.duration, minute, seconds)

        } else {
            holder.timeTv.text = holder.itemView.context.getString(R.string.duration_seconds, String.format("%.0f", duration))

        }

        holder.seekBar.progress = item.progress
        holder.playBtn.isSelected = when (item.playStatus) {

            SoundRecord.STATUS.PLAYING -> true
            SoundRecord.STATUS.END -> false
            SoundRecord.STATUS.PAUSE -> false
            SoundRecord.STATUS.DRAG -> true


        }

        holder.moreBtn.setOnClickListener {


            val menu = PopupMenu(it.context, it)

            menu.inflate(R.menu.more_menu)
            menu.show()
            menu.setOnMenuItemClickListener {


                menuListener?.invoke(it, item)!!


            }

        }

        holder.playBtn.setOnClickListener {


            when (item.playStatus) {

                SoundRecord.STATUS.END -> playVoice(item, position)
                SoundRecord.STATUS.PLAYING -> pauseVoice()
                SoundRecord.STATUS.PAUSE -> replayVoice(item, position)
                else -> println("drag")

            }

        }

        holder.seekBar.isEnabled = when (item.playStatus) {

            SoundRecord.STATUS.PLAYING -> true
            SoundRecord.STATUS.END -> false
            SoundRecord.STATUS.PAUSE -> false
            SoundRecord.STATUS.DRAG -> true


        }
        holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {


            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

                item.playStatus = SoundRecord.STATUS.DRAG
                item.progress = p0!!.progress
                notifyItemChanged(position)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                item.progress = p0!!.progress

                val mediaPlayer = mpForSound[item]
                item.playStatus = SoundRecord.STATUS.PLAYING

                val pos: Int = (item.progress / 100.0f * mediaPlayer!!.duration).toInt()
                mediaPlayer.seekTo(pos)
                //进度条到末尾，停止播放
                if (item.progress >= 100) {
                    item.playStatus = SoundRecord.STATUS.END
                    notifyItemChanged(position)
                    mediaPlayer.stop()
                    playListener?.finish(item)
                }


            }


        })

    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var playBtn: Button
        lateinit var moreBtn: Button
        lateinit var seekBar: SeekBar
        lateinit var timeTv: TextView
        lateinit var cardView: CardView
        lateinit var dateTv: TextView
    }


    private var playThread: Handler? = null

    private fun playVoice(sound: SoundRecord, position: Int) {

        //暂停上一次的录音播放
        if (lastPlayIndex != position && lastPlayIndex != -1) {

            val lastItem = itemList[lastPlayIndex]
            if (lastItem.playStatus == SoundRecord.STATUS.PLAYING) {

                pauseVoice()
            }
        }

        playThread = PlayHandler(sound, position)

        playThread?.sendEmptyMessage(START_PLAY_VOICE)

    }


    companion object {


        const val START_PLAY_VOICE = 0
        const val PAUSE_VOICE = 1
        const val REPLAY_VOICE = 2
        const val UPDATE_PROGRESS = 3
        const val PLAY_VOICE_FAIL = 4

    }

    private inner class PlayHandler(var sound: SoundRecord, var position: Int) : Handler() {

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            when (msg?.what) {



                UPDATE_PROGRESS -> {
                    sound.progress = msg.arg1
                    playListener?.onPlaying(sound)
                    notifyItemChanged(position)

                }
                PLAY_VOICE_FAIL -> {
                    playListener?.fail(sound)
                    notifyItemChanged(position)
                }
                START_PLAY_VOICE -> {

                    try {
                        var mediaPlayer = mpForSound[sound]

                        if (mediaPlayer == null) {
                            //设置声音文件
                            mediaPlayer = ExMediaPlayer()
                            mediaPlayer.setDataSource(sound.url)
                            //配置音量,中等音量
                            mediaPlayer.setVolume(1.0f, 1.0f)
                            //播放是否循环
                            mediaPlayer.isLooping = false
                            mpForSound[sound] = mediaPlayer
                            mediaPlayer.prepare()

                        }


                        mediaPlayer.start()

                        mediaPlayer.setProgressListener(object : ExMediaPlayer.ProgressListener {

                            override fun onProgress(progress: Int, currentPosition: Int, duration: Int) {

                                if (mediaPlayer.isPlaying && (sound.playStatus == SoundRecord.STATUS.PLAYING)) {

                                    val newMsg = Message.obtain()
                                    newMsg.what = UPDATE_PROGRESS
                                    newMsg.arg1 = progress
                                    sendMessage(newMsg)


                                }
                            }

                        })
                        mediaPlayer.setOnCompletionListener {


                            sound.playStatus = SoundRecord.STATUS.END
                            sound.progress = 100
                            notifyItemChanged(lastPlayIndex)
                            playListener?.finish(sound)

                        }
                        lastPlayIndex = position
                        sound.playStatus = SoundRecord.STATUS.PLAYING
                        notifyItemChanged(position)
                    } catch (e: IOException) {
                        sendEmptyMessage(PLAY_VOICE_FAIL)
                        playListener?.fail(sound)
                    }


                }

                PAUSE_VOICE -> {
                    val lastSound = itemList[lastPlayIndex]
                    lastSound.playStatus = SoundRecord.STATUS.PAUSE
                    val mediaPlayer = mpForSound[lastSound]

                    mediaPlayer?.pause()
                    notifyItemChanged(lastPlayIndex)

                }

                REPLAY_VOICE -> {

                    lastPlayIndex = position
                    val lastItem = itemList[lastPlayIndex]
                    lastItem.playStatus = SoundRecord.STATUS.PLAYING
                    val mediaPlayer = mpForSound[lastItem]

                    mediaPlayer?.seekTo(mediaPlayer.currentPosition)


                    mediaPlayer?.start()


                    notifyItemChanged(position)

                }

            }
        }

    }


    /**
     * 释放资源
     */
    fun release() {


        for (obj in mpForSound.values) {

            obj.stop()
            obj.releaseTimeTask()
        }
        mpForSound.clear()


        playThread = null

    }

    private fun createMediaPlayer(source: String): MediaPlayer {

        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(source)
        //配置音量,中等音量
        mediaPlayer.setVolume(1.0f, 1.0f)
        //播放是否循环
        mediaPlayer.isLooping = false
        mediaPlayer.prepare()

        return mediaPlayer
    }

    private fun pauseVoice() {


        playThread?.sendEmptyMessage(PAUSE_VOICE)


    }

    private fun replayVoice(sound: SoundRecord, position: Int) {
        //暂停上一次的录音播放
        if (lastPlayIndex != position && lastPlayIndex != -1) {

            pauseVoice()
        }
        playThread = PlayHandler(sound, position)
        playThread?.sendEmptyMessage(REPLAY_VOICE)
    }
}


