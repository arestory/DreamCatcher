package ywq.ares.dreamcatcher.ui.activity

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import cn.waps.AppConnect
import kotlinx.android.synthetic.main.activity_setting.*
import ywq.ares.dreamcatcher.R
import ywq.ares.dreamcatcher.ui.adapter.SettingAdapter
import ywq.ares.dreamcatcher.ui.bean.SettingItem

class SettingActivity : BaseActivity() {
    override fun getLayoutId(): Int {

        return R.layout.activity_setting
    }

    override fun doMain() {

        val adapter = SettingAdapter()

        adapter.addData(SettingItem(R.drawable.ic_all_inclusive_black_24dp,"更多推荐应用"))
        adapter.addData(SettingItem(R.drawable.ic_system_update_alt_black_24dp,"检查更新"))
        adapter.addData(SettingItem(R.drawable.ic_delete_forever_black_48dp,"去广告"))

        rv.adapter =adapter
        rv.layoutManager = LinearLayoutManager(this)

        rv.addItemDecoration(DividerItemDecoration(this,VERTICAL))

        initToolbarSetting(toolbar)

        adapter.setOnItemClickListener { item, position ->


            AppConnect.getInstance(this).showAppOffers(this)
            Toast.makeText(this,"item = ${item.title}",Toast.LENGTH_SHORT).show()
        }

    }

    companion object {

        fun start(context: Context){


            context.startActivity(Intent(context,SettingActivity::class.java))

        }
    }

}