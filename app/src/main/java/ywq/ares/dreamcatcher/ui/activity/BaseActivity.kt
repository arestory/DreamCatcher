package ywq.ares.dreamcatcher.ui.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

abstract class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        doMain()
    }


    abstract fun getLayoutId():Int


    abstract fun doMain()


    /**
     * 设置toolbar
     *
     * @param toolbar
     * @param title   标题
     */
    protected fun initToolbarSetting(@NonNull toolbar: Toolbar, title: String) {
        toolbar.title = title
        initToolbarSetting(toolbar)
    }

    /**
     * 设置toolbar，显示返回按钮，点击可结束当前activity
     *
     * @param toolbar
     */
    protected fun initToolbarSetting(@NonNull toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener{

            finish()
        }


    }


    /**
     * 设置 toolbar 显示返回按钮
     *
     * @param toolbar
     * @param backClickListener 返回按钮回调
     */
    protected fun initToolbarSetting(@NonNull toolbar: Toolbar, backClickListener: View.OnClickListener) {
        initToolbarSetting(toolbar)
        toolbar.setNavigationOnClickListener(backClickListener)
    }

    /**
     * 设置 toolbar
     *
     * @param toolbar
     * @param title             标题
     * @param backClickListener 返回按钮回调
     */
    protected fun initToolbarSetting(@NonNull toolbar: Toolbar, title: String, backClickListener: View.OnClickListener) {
        initToolbarSetting(toolbar, title)
        toolbar.setNavigationOnClickListener(backClickListener)
    }
}