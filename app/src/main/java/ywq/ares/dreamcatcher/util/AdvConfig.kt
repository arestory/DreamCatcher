package ywq.ares.dreamcatcher.util

import android.content.Context
import cn.waps.AppConnect

object  AdvConfig {


    fun getValue(mContext:Context):String{

        return AppConnect.getInstance(mContext).getConfig("openAdv", "false")
    }
}