package ywq.ares.dreamcatcher.ui.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "sound_record")
class SoundRecord {


    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "url")
    var url: String? = null


    @ColumnInfo(name = "time_stamp")
    var timeStamp = System.currentTimeMillis()

    @ColumnInfo(name = "duration")
    var duration: Long = 0

    @Ignore
    var progress = 0


    @Ignore
    var playStatus = STATUS.END

    constructor(id: Int, url: String, timeStamp: Long, duration: Long, progress: Int, playStatus: STATUS) {
        this.id = id
        this.url = url
        this.timeStamp = timeStamp
        this.duration = duration
        this.progress = progress
        this.playStatus = playStatus
    }

    constructor()

    @Ignore
    constructor(id: Int, url: String, timeStamp: Long, duration: Long) {
        this.id = id
        this.url = url
        this.timeStamp = timeStamp
        this.duration = duration
    }

    enum class STATUS {

        PLAYING, PAUSE, END,DRAG

    }


}
