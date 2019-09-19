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

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.TextView

class SuperAwesomeCardFragment : Fragment() {

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        position = arguments!!.getInt(ARG_POSITION)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        val fl = FrameLayout(activity!!)
        fl.layoutParams = params

        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources
                .displayMetrics).toInt()

        val v = TextView(activity)
        params.setMargins(margin, margin, margin, margin)
        v.layoutParams = params
        v.layoutParams = params
        v.gravity = Gravity.CENTER
        v.setBackgroundResource(R.drawable.background_card)
        v.text = "CARD " + (position + 1)

        fl.addView(v)
        return fl
    }

    companion object {

        private val ARG_POSITION = "position"

        fun newInstance(position: Int): SuperAwesomeCardFragment {
            val f = SuperAwesomeCardFragment()
            val b = Bundle()
            b.putInt(ARG_POSITION, position)
            f.arguments = b
            return f
        }
    }

}