package ywq.ares.dreamcatcher

import android.app.Application
import androidx.room.Room
import com.google.android.gms.ads.MobileAds
import com.tencent.bugly.Bugly
import com.tencent.bugly.crashreport.CrashReport
import ywq.ares.dreamcatcher.room.AppDatabase
import ywq.ares.dreamcatcher.ui.bean.User
import ywq.ares.dreamcatcher.util.CrashHandler
import ywq.ares.dreamcatcher.util.DeviceUtil

class SoundApp : Application() {


    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, Constants.GOOGLE_ADV_KEY)

        app = this
        CrashHandler.getInstance().init(this)

        CrashReport.initCrashReport(this,"0e0b08c792",BuildConfig.DEBUG)
    }

    companion object {


        private var app: SoundApp? = null
        private var appDatabase: AppDatabase? = null
        fun getDataBase(): AppDatabase {

            appDatabase = Room.databaseBuilder(app!!.applicationContext, AppDatabase::class.java, "soundDb.db").fallbackToDestructiveMigration().allowMainThreadQueries().build()
            return appDatabase!!
        }

        fun isZhLanguage(): Boolean {

            val locale = app?.resources?.configuration?.locale
            val language = locale?.language
            return language!!.endsWith("zh")

        }

        fun getUser(): User? {

            val list = getDataBase().userDao().getUser(DeviceUtil.getAndroidID(app))


            if (!list.isEmpty()) {

                return list[0]
            }



            return null

        }

    }


}