package com.llh.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import com.llh.service.db.DBHelper.Companion.DEFAULT_ID_VALUE
import com.llh.service.db.DBHelper.Companion.KEY_BASE_URL
import com.llh.service.db.DBHelper.Companion.KEY_PRIMARY_ID
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    var mUrl: String = APP_URL_Q

    val mContentValues: (String) -> ContentValues = {
        ContentValues().apply {
            put(KEY_PRIMARY_ID, DEFAULT_ID_VALUE)
            put(KEY_BASE_URL, it)
        }
    }
    private val prefs by lazy {
        getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }
    private val mRadioViews by lazy {
        arrayListOf<RadioButton>(mRelease, mDebug, mDevelop)
    }

    private val opUri = Uri.parse("content://com.llh.service.apphelper/appurl")

    companion object {
        //开发环境 URL
        const val APP_URL_DEBUG = "http://10.202.203.191:8003"
        // 测试环境 URL
        const val APP_URL_Q = "http://10.15.5.75:8003"
        // 预生产环境 URL
        const val APP_URL_RELEASE = "https://dfubapi.xdf.cn/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val isFirst = prefs.getBoolean("isFirst", true)
        if (isFirst) {
            prefs.edit().putBoolean("isFirst", false).apply()
            contentResolver.insert(opUri, mContentValues(mUrl))
        } else {
            val checkId = prefs.getInt("checkId", R.id.mDebug)
            mRadioViews.forEach { it.isChecked = it.id == checkId }
        }
        mRadioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        prefs.edit().putInt("checkId", checkedId).apply()
        mUrl = when (checkedId) {
            R.id.mRelease -> APP_URL_RELEASE
            R.id.mDebug -> APP_URL_Q
            R.id.mDevelop -> APP_URL_DEBUG
            else -> APP_URL_Q
        }
        contentResolver.update(opUri, mContentValues(mUrl), "$KEY_PRIMARY_ID=?", Array(1) { DEFAULT_ID_VALUE })
    }
}