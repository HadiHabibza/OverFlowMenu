package com.hadi.menu.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hadi.menu.overflow.OverFlowMenu

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            OverFlowMenu.createDefaultMenu(this, R.menu.menu).show(view)
        }

        fab2.setOnClickListener { view ->
            OverFlowMenu.createDefaultMenu(this, R.menu.menu).show(view)
        }
    }
}