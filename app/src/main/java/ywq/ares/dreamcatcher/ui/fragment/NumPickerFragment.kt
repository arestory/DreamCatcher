package ywq.ares.dreamcatcher.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView

import ywq.ares.dreamcatcher.R
import androidx.fragment.app.DialogFragment

class NumPickerFragment : DialogFragment() {


    private var seekBar: SeekBar? = null
    private var minuteTv: TextView? = null
    private var titleTv: TextView? = null
    private var minute = 5
    private var max = 29
    private var diff = 1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.minute = arguments!!.getInt("minute")
        this.max = arguments!!.getInt("max")
        println("onCreateView")
        val params = dialog.window!!
                .attributes
        params.gravity = Gravity.CENTER
        dialog.window!!.attributes = params
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val root = inflater.inflate(R.layout.num_picker_dialog_layout, container, false)

        minuteTv = root.findViewById(R.id.minuteTv)
        titleTv = root.findViewById(R.id.titleTv)
        seekBar = root.findViewById(R.id.seekBar)
        titleTv!!.text = arguments!!.getString("title")
        seekBar!!.max=max
        seekBar!!.progress = minute - diff

        minuteTv?.text = getString(R.string.minute, minute)
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                minuteChangeListener?.invoke(progress + diff)


                minuteTv?.text = getString(R.string.minute, progress + diff)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {


            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {


            }

        })


        return root
    }

    private var minuteChangeListener: ((num: Int) -> Unit)? = null


    fun setOnMinuteChangeListener(minuteChangeListener: ((num: Int) -> Unit)) {

        this.minuteChangeListener = minuteChangeListener
    }


    override fun onResume() {
        super.onResume()

        val dm = DisplayMetrics()


        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dialog.window!!.setLayout(dm.widthPixels * 4 / 5, dm.heightPixels / 6)
    }

    companion object {

        @JvmOverloads
        fun newInstance(minute: Int = 5,max:Int = 29,title:String ="设置"): NumPickerFragment {

            val args = Bundle()

            val fragment = NumPickerFragment()
            args.putInt("minute", minute)
            args.putInt("max", max)
            args.putString("title", title)
            fragment.arguments = args

            return fragment
        }
    }
}
