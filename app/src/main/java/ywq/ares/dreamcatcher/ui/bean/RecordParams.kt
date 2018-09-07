package ywq.ares.dreamcatcher.ui.bean

import java.io.Serializable

class RecordParams : Serializable {


    constructor()
    constructor(intervalTime: Int , minute: Int,delayTime: Int, db: Int) {
        this.intervalTime = intervalTime
        this.minute = minute
        this.db = db
    }
    constructor(intervalTime: Int , minute: Int, db: Int) {
        this.intervalTime = intervalTime
        this.minute = minute
        this.db = db
    }


    var intervalTime: Int = 5
    var minute: Int = 30
    var dreamTime: Int = 30
    var db: Int = 80
    override fun toString(): String {
        return "RecordParams(intervalTime=$intervalTime, minute=$minute, db=$db) dreamTime=$dreamTime"
    }


}
