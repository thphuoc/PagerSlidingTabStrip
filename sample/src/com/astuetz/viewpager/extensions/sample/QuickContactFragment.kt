package com.astuetz.viewpager.extensions.sample

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.util.TypedValue
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView

import com.astuetz.PagerSlidingTabStrip
import com.astuetz.PagerSlidingTabStrip.IconTabProvider

class QuickContactFragment : DialogFragment() {

    private var tabs: PagerSlidingTabStrip? = null
    private var pager: ViewPager? = null
    private var adapter: ContactPagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (dialog != null) {
            dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val root = inflater.inflate(R.layout.fragment_quick_contact, container, false)

        tabs = root.findViewById(R.id.tabs)
        pager = root.findViewById(R.id.pager)
        adapter = ContactPagerAdapter()

        pager!!.adapter = adapter

        tabs!!.setViewPager(pager!!)

        return root
    }

    override fun onStart() {
        super.onStart()

        // change dialog width
        if (dialog != null) {

            val fullWidth: Int

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                val display = activity!!.windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)
                fullWidth = size.x
            } else {
                val display = activity!!.windowManager.defaultDisplay
                fullWidth = display.width
            }

            val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources
                    .displayMetrics).toInt()

            val w = fullWidth - padding
            val h = dialog.window!!.attributes.height

            dialog.window!!.setLayout(w, h)
        }
    }

    inner class ContactPagerAdapter : PagerAdapter(), IconTabProvider {

        private val ICONS = intArrayOf(R.drawable.ic_launcher_gplus, R.drawable.ic_launcher_gmail, R.drawable.ic_launcher_gmaps, R.drawable.ic_launcher_chrome)

        override fun getCount(): Int {
            return ICONS.size
        }

        override fun getPageIconResId(position: Int): Int {
            return ICONS[position]
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            // looks a little bit messy here
            val v = TextView(activity)
            v.setBackgroundResource(R.color.background_window)
            v.text = "PAGE " + (position + 1)
            val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources
                    .displayMetrics).toInt()
            v.setPadding(padding, padding, padding, padding)
            v.gravity = Gravity.CENTER
            container.addView(v, 0)
            return v
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        override fun isViewFromObject(v: View, o: Any): Boolean {
            return v === o
        }

    }

    companion object {

        fun newInstance(): QuickContactFragment {
            return QuickContactFragment()
        }
    }

}
