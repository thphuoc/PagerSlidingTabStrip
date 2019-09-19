/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.astuetz.viewpager.extensions.sample

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.astuetz.PagerSlidingTabStrip

class MainActivity : FragmentActivity() {

    private val handler = Handler()

    private var tabs: PagerSlidingTabStrip? = null
    private var pager: ViewPager? = null
    private var adapter: MyPagerAdapter? = null

    private var oldBackground: Drawable? = null
    private var currentColor = -0x99999a

    private val drawableCallback = object : Drawable.Callback {
        override fun invalidateDrawable(who: Drawable) {
            actionBar!!.setBackgroundDrawable(who)
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
            handler.postAtTime(what, `when`)
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            handler.removeCallbacks(what)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabs = findViewById<View>(R.id.tabs) as PagerSlidingTabStrip
        pager = findViewById<View>(R.id.pager) as ViewPager
        adapter = MyPagerAdapter(supportFragmentManager)

        pager!!.adapter = adapter

        val pageMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources
                .displayMetrics).toInt()
        pager!!.pageMargin = pageMargin

        tabs!!.setViewPager(pager!!)

        changeColor(currentColor)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_contact -> {
                val dialog = QuickContactFragment()
                dialog.show(supportFragmentManager, "QuickContactFragment")
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun changeColor(newColor: Int) {

        tabs!!.setIndicatorColor(newColor)

        // change ActionBar color just if an ActionBar is available
        val colorDrawable = ColorDrawable(newColor)
        val bottomDrawable = resources.getDrawable(R.drawable.actionbar_bottom)
        val ld = LayerDrawable(arrayOf(colorDrawable, bottomDrawable))

        if (oldBackground == null) {
            ld.callback = drawableCallback
        } else {

            val td = TransitionDrawable(arrayOf(oldBackground!!, ld))

            // workaround for broken ActionBarContainer drawable handling on
            // pre-API 17 builds
            // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
            actionBar!!.setBackgroundDrawable(td)

            td.startTransition(200)

        }

        oldBackground = ld

        // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
        actionBar!!.setDisplayShowTitleEnabled(false)
        actionBar!!.setDisplayShowTitleEnabled(true)

        currentColor = newColor

    }

    fun onColorClicked(v: View) {

        val color = Color.parseColor(v.tag.toString())
        changeColor(color)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentColor", currentColor)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentColor = savedInstanceState.getInt("currentColor")
        changeColor(currentColor)
    }

    inner class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val TITLES = arrayOf("Categories", "Home", "Top Paid", "Top Free", "Top Grossing", "Top New Paid", "Top New Free", "Trending")

        override fun getPageTitle(position: Int): CharSequence? {
            return TITLES[position]
        }

        override fun getCount(): Int {
            return TITLES.size
        }

        override fun getItem(position: Int): Fragment {
            return SuperAwesomeCardFragment.newInstance(position)
        }

    }

}