package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.service.PictureLoaderService
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ApplicationOrCategory
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ApplicationWithProcessData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessInfoData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.ImmersedStatusBarUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.replaceFragmentSafely

class StartProcessActivity : AppCompatActivity() {

    companion object {
        const val chooseApplicationResultKey = "chooseApplicationResultKey"
        const val chooseProcessResultKey = "chooseProcessResultKey"
        const val chooseModeKey = "chooseModeKey"
        fun startChooseApplication(): Bundle {
            val bundle = Bundle()
            bundle.putString(chooseModeKey, "2")
           return bundle
        }
        fun startChooseProcess(): Bundle {
            val bundle = Bundle()
            bundle.putString(chooseModeKey, "3")
            return bundle
        }
    }

    var toolbar: Toolbar? = null
    var toolbarTitle: TextView? = null
    var pictureLoaderService: PictureLoaderService? = null

    var chooseMode = "1" // 1 默认的 启动流程  2 应用选择器 3 流程选择器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_process)
        // 沉浸式状态栏
        ImmersedStatusBarUtils.setImmersedStatusBar(this)
        toolbar = findViewById(R.id.toolbar_snippet_top_bar)
        toolbar?.title = ""
        setSupportActionBar(toolbar)
        toolbarTitle = findViewById(R.id.tv_snippet_top_title)
        toolbarTitle?.text = ""
        toolbar?.setNavigationIcon(R.mipmap.ic_back_mtrl_white_alpha)
        toolbar?.setNavigationOnClickListener { removeFragment() }

        chooseMode = intent.getStringExtra(chooseModeKey) ?: "1"

        if (supportFragmentManager.fragments.isEmpty()) {
            addFragment(StartProcessStepOneFragment())
        }

    }

    override fun onResume() {
        super.onResume()
        pictureLoaderService = PictureLoaderService(this)
    }

    override fun onPause() {
        super.onPause()
        pictureLoaderService?.close()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (supportFragmentManager.backStackEntryCount == 1) {
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun setToolBarTitle(title:String) {
        toolbarTitle?.text = title
    }

    fun loadProcessApplicationIcon(convertView:View, appId:String) {
        pictureLoaderService?.loadProcessAppIcon(convertView, appId)
    }

    fun addFragment(fragment: Fragment){
        replaceFragmentSafely(fragment, fragment.javaClass.simpleName, R.id.fragment_container_start_process, allowState = true, isAddBackStack = true)
    }

    fun removeFragment() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        }else {
            finish()
        }
    }

    fun chooseModeResult(app: ApplicationWithProcessData?, process: ProcessInfoData?) {
        intent.putExtra(chooseApplicationResultKey, app)
        intent.putExtra(chooseProcessResultKey, process)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
