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

package com.astuetz

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.astuetz.pagerslidingtabstrip.R
import java.util.*

class PagerSlidingTabStrip @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : HorizontalScrollView(context, attrs, defStyle) {
    // @formatter:on

    private val defaultTabLayoutParams: LinearLayout.LayoutParams
    private val expandedTabLayoutParams: LinearLayout.LayoutParams

    private val pageListener = PageListener()
    var delegatePageListener: OnPageChangeListener? = null

    private val tabsContainer: LinearLayout
    private var pager: ViewPager? = null

    private var tabCount: Int = 0

    private var currentPosition = 0
    private var currentPositionOffset = 0f

    private val rectPaint: Paint
    private val dividerPaint: Paint

    private var indicatorColor = -0x99999a
    private var underlineColor = 0x1A000000
    private var dividerColor = 0x1A000000

    private var shouldExpand = false
    var isTextAllCaps = true
        private set

    private var scrollOffset = 52
    private var indicatorHeight = 8
    private var underlineHeight = 2
    private var dividerPadding = 12
    private var tabPadding = 24
    private var dividerWidth = 1

    private var tabTextSize = 12
    private var tabTextColor = -0x99999a
    private var tabTypeface: Typeface? = null
    private var tabTypefaceStyle = Typeface.BOLD

    private var lastScrollX = 0

    var tabBackground = R.drawable.background_tab

    private var locale: Locale? = null

    var textSize: Int
        get() = tabTextSize
        set(textSizePx) {
            this.tabTextSize = textSizePx
            updateTabStyles()
        }

    var textColor: Int
        get() = tabTextColor
        set(textColor) {
            this.tabTextColor = textColor
            updateTabStyles()
        }

    var tabPaddingLeftRight: Int
        get() = tabPadding
        set(paddingPx) {
            this.tabPadding = paddingPx
            updateTabStyles()
        }

    interface IconTabProvider {
        fun getPageIconResId(position: Int): Int
    }

    interface CustomTabProvider {
        fun getTabViewResId(position: Int): View
    }

    init {

        isFillViewport = true
        setWillNotDraw(false)

        tabsContainer = LinearLayout(context)
        tabsContainer.orientation = LinearLayout.HORIZONTAL
        tabsContainer.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(tabsContainer)

        val dm = resources.displayMetrics

        scrollOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset.toFloat(), dm).toInt()
        indicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight.toFloat(), dm).toInt()
        underlineHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight.toFloat(), dm).toInt()
        dividerPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding.toFloat(), dm).toInt()
        tabPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding.toFloat(), dm).toInt()
        dividerWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth.toFloat(), dm).toInt()
        tabTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize.toFloat(), dm).toInt()

        // get system attrs (android:textSize and android:textColor)

        var a = context.obtainStyledAttributes(attrs, ATTRS)

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize)
        @SuppressLint("ResourceType")
        tabTextColor = a.getColor(1, tabTextColor)

        a.recycle()

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip)

        indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_indicatorColor, indicatorColor)
        underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_underlineColor, underlineColor)
        dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_dividerColor, dividerColor)
        indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_indicatorHeight, indicatorHeight)
        underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_underlineHeight, underlineHeight)
        dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_dividerPadding, dividerPadding)
        tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_tabPaddingLeftRight, tabPadding)
        tabBackground = a.getResourceId(R.styleable.PagerSlidingTabStrip_tabBackground, tabBackground)
        shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_shouldExpand, shouldExpand)
        scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_scrollOffset, scrollOffset)
        isTextAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_textAllCaps, isTextAllCaps)

        a.recycle()

        rectPaint = Paint()
        rectPaint.isAntiAlias = true
        rectPaint.style = Style.FILL

        dividerPaint = Paint()
        dividerPaint.isAntiAlias = true
        dividerPaint.strokeWidth = dividerWidth.toFloat()

        defaultTabLayoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        expandedTabLayoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f)

        if (locale == null) {
            locale = resources.configuration.locale
        }
    }

    fun setViewPager(pager: ViewPager) {
        this.pager = pager

        checkNotNull(pager.adapter) { "ViewPager does not have adapter instance." }

        pager.addOnPageChangeListener(pageListener)

        notifyDataSetChanged()
    }

    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        this.delegatePageListener = listener
    }

    fun notifyDataSetChanged() {

        tabsContainer.removeAllViews()

        tabCount = pager!!.adapter!!.count

        for (i in 0 until tabCount) {

            if (pager!!.adapter is CustomTabProvider) {
                addViewTab(i, (pager!!.adapter as CustomTabProvider).getTabViewResId(i))
            } else if (pager!!.adapter is IconTabProvider) {
                addIconTab(i, (pager!!.adapter as IconTabProvider).getPageIconResId(i))
            } else {
                addTextTab(i, pager!!.adapter!!.getPageTitle(i)!!.toString())
            }

        }

        updateTabStyles()

        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

            @SuppressLint("NewApi")
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                currentPosition = pager!!.currentItem
                scrollToChild(currentPosition, 0)
            }
        })

    }

    private fun addTextTab(position: Int, title: String) {

        val tab = TextView(context)
        tab.text = title
        tab.gravity = Gravity.CENTER
        tab.setSingleLine()

        addTab(position, tab)
    }

    private fun addViewTab(position: Int, view: View) {
        addTab(position, view)
    }

    private fun addIconTab(position: Int, resId: Int) {

        val tab = ImageButton(context)
        tab.setImageResource(resId)

        addTab(position, tab)

    }

    private fun addTab(position: Int, tab: View) {
        tab.isFocusable = true
        tab.setOnClickListener { pager!!.currentItem = position }

        tab.setPadding(tabPadding, 0, tabPadding, 0)
        tabsContainer.addView(tab, position, if (shouldExpand) expandedTabLayoutParams else defaultTabLayoutParams)
    }

    private fun updateTabStyles() {

        for (i in 0 until tabCount) {

            val v = tabsContainer.getChildAt(i)

            v.setBackgroundResource(tabBackground)

            if (v is TextView) {

                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize.toFloat())
                v.setTypeface(tabTypeface, tabTypefaceStyle)
                v.setTextColor(tabTextColor)

                // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
                // pre-ICS-build
                v.isAllCaps = isTextAllCaps
            }
        }

    }

    private fun scrollToChild(position: Int, offset: Int) {

        if (tabCount == 0) {
            return
        }

        var newScrollX = tabsContainer.getChildAt(position).left + offset

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX
            scrollTo(newScrollX, 0)
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isInEditMode || tabCount == 0) {
            return
        }

        val height = height

        // draw indicator line

        rectPaint.color = indicatorColor

        // default: line below current tab
        val currentTab = tabsContainer.getChildAt(currentPosition)
        var lineLeft = currentTab.left.toFloat()
        var lineRight = currentTab.right.toFloat()

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

            val nextTab = tabsContainer.getChildAt(currentPosition + 1)
            val nextTabLeft = nextTab.left.toFloat()
            val nextTabRight = nextTab.right.toFloat()

            lineLeft = currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft
            lineRight = currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight
        }

        canvas.drawRect(lineLeft, (height - indicatorHeight).toFloat(), lineRight, height.toFloat(), rectPaint)

        // draw underline

        rectPaint.color = underlineColor
        canvas.drawRect(0f, (height - underlineHeight).toFloat(), tabsContainer.width.toFloat(), height.toFloat(), rectPaint)

        // draw divider

        dividerPaint.color = dividerColor
        for (i in 0 until tabCount - 1) {
            val tab = tabsContainer.getChildAt(i)
            canvas.drawLine(tab.right.toFloat(), dividerPadding.toFloat(), tab.right.toFloat(), (height - dividerPadding).toFloat(), dividerPaint)
        }
    }

    private inner class PageListener : OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            currentPosition = position
            currentPositionOffset = positionOffset

            scrollToChild(position, (positionOffset * tabsContainer.getChildAt(position).width).toInt())

            invalidate()

            if (delegatePageListener != null) {
                delegatePageListener!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(pager!!.currentItem, 0)
            }

            if (delegatePageListener != null) {
                delegatePageListener!!.onPageScrollStateChanged(state)
            }
        }

        override fun onPageSelected(position: Int) {
            if (delegatePageListener != null) {
                delegatePageListener!!.onPageSelected(position)
            }
        }

    }

    fun setIndicatorColor(indicatorColor: Int) {
        this.indicatorColor = indicatorColor
        invalidate()
    }

    fun setIndicatorColorResource(resId: Int) {
        this.indicatorColor = resources.getColor(resId)
        invalidate()
    }

    fun getIndicatorColor(): Int {
        return this.indicatorColor
    }

    fun setIndicatorHeight(indicatorLineHeightPx: Int) {
        this.indicatorHeight = indicatorLineHeightPx
        invalidate()
    }

    fun getIndicatorHeight(): Int {
        return indicatorHeight
    }

    fun setUnderlineColor(underlineColor: Int) {
        this.underlineColor = underlineColor
        invalidate()
    }

    fun setUnderlineColorResource(resId: Int) {
        this.underlineColor = resources.getColor(resId)
        invalidate()
    }

    fun getUnderlineColor(): Int {
        return underlineColor
    }

    fun setDividerColor(dividerColor: Int) {
        this.dividerColor = dividerColor
        invalidate()
    }

    fun setDividerColorResource(resId: Int) {
        this.dividerColor = resources.getColor(resId)
        invalidate()
    }

    fun getDividerColor(): Int {
        return dividerColor
    }

    fun setUnderlineHeight(underlineHeightPx: Int) {
        this.underlineHeight = underlineHeightPx
        invalidate()
    }

    fun getUnderlineHeight(): Int {
        return underlineHeight
    }

    fun setDividerPadding(dividerPaddingPx: Int) {
        this.dividerPadding = dividerPaddingPx
        invalidate()
    }

    fun getDividerPadding(): Int {
        return dividerPadding
    }

    fun setScrollOffset(scrollOffsetPx: Int) {
        this.scrollOffset = scrollOffsetPx
        invalidate()
    }

    fun getScrollOffset(): Int {
        return scrollOffset
    }

    fun setShouldExpand(shouldExpand: Boolean) {
        this.shouldExpand = shouldExpand
        requestLayout()
    }

    fun getShouldExpand(): Boolean {
        return shouldExpand
    }

    fun setAllCaps(textAllCaps: Boolean) {
        this.isTextAllCaps = textAllCaps
    }

    fun setTextColorResource(resId: Int) {
        this.tabTextColor = resources.getColor(resId)
        updateTabStyles()
    }

    fun setTypeface(typeface: Typeface, style: Int) {
        this.tabTypeface = typeface
        this.tabTypefaceStyle = style
        updateTabStyles()
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        currentPosition = savedState.currentPosition
        requestLayout()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.currentPosition = currentPosition
        return savedState
    }

    internal class SavedState(superState: Parcelable?) : BaseSavedState(superState) {
        var currentPosition: Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPosition)
        }
    }

    companion object {
        // @formatter:off
        private val ATTRS = intArrayOf(android.R.attr.textSize, android.R.attr.textColor)
    }

}
