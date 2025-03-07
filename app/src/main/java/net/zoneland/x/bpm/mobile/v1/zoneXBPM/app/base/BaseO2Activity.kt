package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.TextView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.ImmersedStatusBarUtils

/**
 * Created by fancyLou on 20/06/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


abstract class BaseO2Activity : AppCompatActivity() {

    abstract fun layoutResId(): Int
    abstract fun afterSetContentView(savedInstanceState: Bundle?)
    /**
     * 可以在setContentView之前做一些事
     */
    open fun beforeSetContentView() {}

    /**
     * Toolbar
     */
    protected var toolbar: Toolbar? = null
    /**
     * Toolbar居中的标题
     */
    private var toolbarTitle: TextView? = null

    //固定字体比例 防止系统设置字体影响
    override fun getResources(): Resources {
        var res =  super.getResources()
        if (res.configuration != null && res.configuration.fontScale != 1f) {
            val config = Configuration()
            config.setToDefaults()
            config.fontScale = 1f
            val ct = createConfigurationContext(config)
            if (ct != null) {
                res = ct.resources
            }
        }
        return res
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beforeSetContentView()
        setContentView(layoutResId())
        //防止截屏
//        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        // 沉浸式状态栏
        ImmersedStatusBarUtils.setImmersedStatusBar(this)
        afterSetContentView(savedInstanceState)
    }


    fun setupToolBar(title: String = "", setupBackButton: Boolean = false, isCloseBackIcon: Boolean = false) {
        toolbar = findViewById(R.id.toolbar_snippet_top_bar)
        toolbar?.title = ""
        setSupportActionBar(toolbar)
        toolbarTitle = findViewById(R.id.tv_snippet_top_title)
        toolbarTitle?.text = title
        if (setupBackButton) {
            if (isCloseBackIcon) {
                setToolbarBackBtnWithCloseIcon()
            } else {
                setToolbarBackBtn()
            }
        }
    }

    fun updateToolbarTitle(title: String) {
        toolbarTitle?.text = title
    }

    private fun setToolbarBackBtn() {
        toolbar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
        toolbar?.setNavigationOnClickListener { finish() }
    }

    private fun setToolbarBackBtnWithCloseIcon() {
        toolbar?.setNavigationIcon(R.mipmap.icon_menu_window_close)
        toolbar?.setNavigationOnClickListener { finish() }
    }

}