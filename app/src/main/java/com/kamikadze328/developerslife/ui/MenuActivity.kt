package com.kamikadze328.developerslife.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

open class MenuActivity(@LayoutRes layoutId: Int) : AppCompatActivity(layoutId) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}