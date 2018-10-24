package ywq.ares.dreamcatcher.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ares.datacontentlayout.DataContentLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_main.*
import ywq.ares.dreamcatcher.R
import ywq.ares.dreamcatcher.SoundApp
import ywq.ares.dreamcatcher.room.AppDatabase
import ywq.ares.dreamcatcher.room.dao.SoundDao
import ywq.ares.dreamcatcher.ui.adapter.RecordItemAdapter
import ywq.ares.dreamcatcher.ui.bean.SoundRecord
import ywq.ares.dreamcatcher.ui.bean.User
import ywq.ares.dreamcatcher.util.DeviceUtil
import ywq.ares.dreamcatcher.util.RxBus
import java.io.File
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class MineDreamFragment : Fragment() {
    private lateinit var database: AppDatabase
    private lateinit var adapter: RecordItemAdapter
    private lateinit var disposable: Disposable
    private lateinit var rv: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var loadingView: DataContentLayout

    private var dataSubscribe: Disposable? = null
    private var soundDao: SoundDao? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_mine_dream,container,false)
        rv = rootView.findViewById(R.id.rv)
        loadingView = rootView.findViewById(R.id.loadingView)
        swipeLayout = rootView.findViewById(R.id.swipeLayout)

        initDB()
        initRv()
        initUser()
        disposable = RxBus.get().toFlowable(SoundRecord::class.java).observeOn(AndroidSchedulers.mainThread()).subscribe {


            adapter.addItem(it)
            when {

                loadingView.getDataStatus() == DataContentLayout.DataStatus.EMPTY_CONTENT -> loadingView.showContent()
            }

        }
        return rootView

    }
    private fun initDB() {
        database = SoundApp.getDataBase()
    }
    private fun initUser() {


        val cacheUser = SoundApp.getUser()

        if (cacheUser == null) {
            val user = User()
            user.userId = DeviceUtil.getAndroidID(activity)
            user.isAdmin = false
            user.score = 0
            database.userDao().insertUser(user)

            println("new user = $user")
        }
        println("user = $cacheUser")
    }
    private fun initRv() {

        adapter = RecordItemAdapter()
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(activity)
        (rv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        adapter.recyclerView = rv

        adapter.setMoreClickListener {
            item, sound ->
            when (item.itemId) {
                R.id.action_delete -> {
                    database.soundDao().delete(sound)
                    adapter.removeItem(sound)
                    true
                }
                else -> {
                    val user = SoundApp.getUser()
//                    val shareFile = File(sound.url)
//                    if(shareFile.exists()){
//                        val intent = Intent()
//                        intent.action = Intent.ACTION_SEND
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        intent.addCategory("android.intent.category.DEFAULT")
////                        val comp =  ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareFileUI")
////                        intent.component = comp
//
//                        val uri = DeviceUtil.getFileUri(this,shareFile)
//                        intent.setDataAndType(uri,"audio/mp4a-latm")
//                        try {
//                            this.startActivity(Intent.createChooser(intent, shareFile.getName()))
//                        } catch ( e:Exception) {
//                            e.printStackTrace()
//                        }
//
//
//                    }

                    if (user != null && !user.isAdmin) {
                        showToast("权限不够")
                    } else {
                        showToast("文件路径:${sound.url}")
                    }
                    false
                }
            }
        }

        adapter.addPlayVoiceListener(object : RecordItemAdapter.PlayVoiceListener {

            override fun onPlaying(item: SoundRecord) {
            }

            override fun finish(item: SoundRecord) {
            }

            override fun fail(item: SoundRecord) {

                Toast.makeText(activity, getString(R.string.tips_play_fail), Toast.LENGTH_LONG).show()

                soundDao?.delete(item)
                File(item.url).delete()
            }
        })
        swipeLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent)
        swipeLayout.setOnRefreshListener {

            adapter.release()
            getData()

        }
        getData()
    }

    private fun getData(){
        val dis = database.soundDao().queryAll()?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe {

            if (it.size > 0) {
                val list = it
                //过滤掉无效的数据
                val validList = (0 until list.size).map {

                    pos ->
                    list[pos]
                }.filter {

                    soundRecord ->
                    File(soundRecord.url).exists()

                }

                if (validList.isNotEmpty()) {
                    adapter.itemList =(validList as ArrayList<SoundRecord>)
                    adapter.notifyDataSetChanged()
                    swipeLayout.isRefreshing = false
                    loadingView.showContent()
                } else {
                    loadingView.showEmptyContent()
                }

            } else {
                loadingView.showEmptyContent()
                swipeLayout.isRefreshing = false

            }
        }
    }
    private fun showToast(msg: String) {


        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        this.adapter.release()
        disposable.dispose()
        dataSubscribe?.dispose()
    }
    companion object {


        fun newInstance(): MineDreamFragment {

            val args = Bundle()

            val fragment = MineDreamFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
