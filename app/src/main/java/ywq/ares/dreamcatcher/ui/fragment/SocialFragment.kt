package ywq.ares.dreamcatcher.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SocialFragment :Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {


        fun newInstance(): SocialFragment {

            val args = Bundle()

            val fragment = SocialFragment()
            fragment.arguments = args
            return fragment
        }
    }
}