package ywq.ares.dreamcatcher.ui.activity

import android.content.Context
import android.content.Intent
import kotlinx.android.synthetic.main.activity_about.*
import ywq.ares.dreamcatcher.R

class AboutActivity:BaseActivity() {


    override fun getLayoutId(): Int {

        return R.layout.activity_about
    }

    override fun doMain() {

         initToolbarSetting(toolbar)


    }


    companion object {

        fun start(context: Context){


            context.startActivity(Intent(context,AboutActivity::class.java))

        }
    }

}