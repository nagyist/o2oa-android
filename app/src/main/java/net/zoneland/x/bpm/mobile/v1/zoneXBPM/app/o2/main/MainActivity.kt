package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.Manifest
import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_bottom_bar_image.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.O2IM
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.im.fm.O2IMConversationFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.DownloadAPKFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.MainActivityFragmentAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.service.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.O2AppUpdateBean
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im.IMMessage
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.BitmapUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.O2DoubleClickExit
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.tbs.WordReadHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.GrayFrameLayout
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2AlertIconEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import org.jetbrains.anko.doAsync


/**
 * Created by fancy on 2017/6/8.
 */


class MainActivity : BaseMVPActivity<MainContract.View, MainContract.Presenter>(), MainContract.View, View.OnClickListener {


    override var mPresenter: MainContract.Presenter = MainPresenter()

    private val fragmentList: ArrayList<Fragment> = ArrayList()
    private val fragmentTitles: ArrayList<String> = ArrayList()
    private val mCurrentSelectIndexKey = "mCurrentSelectIndexKey"
    private var mCurrentSelectIndex = 2
    private var simpleMode = false
    private var appExitAlert = ""


    var pictureLoaderService: PictureLoaderService? = null
    private val doubleClickExitHelper: O2DoubleClickExit by lazy { O2DoubleClickExit(this) }
    val adapter: MainActivityFragmentAdapter by lazy { MainActivityFragmentAdapter(fragmentList, fragmentTitles, supportFragmentManager) }

    override fun layoutResId(): Int {
        return R.layout.activity_main
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        // 默哀灰色处理
        try {
           val silence = O2SDKManager.instance().prefs().getBoolean(O2CustomStyle.CUSTOM_STYLE_SILENCE_GRAY_PREF_KEY, false)
            if (silence) {
                if ("FrameLayout" == name) {
                    val count = attrs.attributeCount
                    for (i in 0..count) {
                        val attributeName = attrs.getAttributeName(i)
                        val attributeValue = attrs.getAttributeValue(i)
                        if (attributeName == "id") {
                            val id = attributeValue.substring(1).toInt()
                            val idVal = resources.getResourceName(id)
                            if ("android:id/content" == idVal) {
                                return GrayFrameLayout(context, attrs)
                            }
                        }
                    }
                }
            }
        } catch ( e: Exception) {
            e.printStackTrace()
        }
        return super.onCreateView(name, context, attrs)
    }

    override fun beforeSetContentView() {
        super.beforeSetContentView()
        setTheme(R.style.XBPMTheme_NoActionBar)
    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        mCurrentSelectIndex = savedInstanceState?.getInt(mCurrentSelectIndexKey, 2) ?: 2
        setupToolBar(getString(R.string.app_name))

        XLog.info("main activity init..............")
        val indexType = O2SDKManager.instance().prefs().getString(O2CustomStyle.INDEX_TYPE_PREF_KEY, O2CustomStyle.INDEX_TYPE_DEFAULT)
        val indexId = O2SDKManager.instance().prefs().getString(O2CustomStyle.INDEX_ID_PREF_KEY, "") ?: ""
        simpleMode = O2SDKManager.instance().prefs().getBoolean(O2CustomStyle.CUSTOM_STYLE_SIMPLE_MODE_PREF_KEY, false)
        appExitAlert = O2SDKManager.instance().prefs().getString(O2CustomStyle.CUSTOM_STYLE_APP_EXIT_ALERT_KEY, "") ?: ""
        XLog.info("main activity isIndex $indexType..............simpleMode: $simpleMode")
        // 简易模式 只有首页和设置页面
        if (simpleMode) {
            val indexName = getString(R.string.tab_todo)
            if (indexType == O2CustomStyle.INDEX_TYPE_DEFAULT || TextUtils.isEmpty(indexId)) {
                val indexFragment = IndexFragment()
                fragmentList.add(indexFragment)
                fragmentTitles.add(indexName)
            } else {
                val indexFragment = IndexPortalFragment.instance(indexId)
                fragmentList.add(indexFragment)
                fragmentTitles.add(indexName)
            }
            val settingFragment = SettingsFragment()
            fragmentList.add(settingFragment)
            fragmentTitles.add(getString(R.string.tab_settings))
            icon_main_bottom_news.gone()
            icon_main_bottom_contact.gone()
            icon_main_bottom_app.gone()
            icon_main_bottom_index.setOnClickListener(this)
            icon_main_bottom_setting.setOnClickListener(this)
        } else {
            val newsFragment = O2IMConversationFragment()
            fragmentList.add(newsFragment)
            fragmentTitles.add(getString(R.string.tab_message))

            val contactFragment = NewContactFragment()
            fragmentList.add(contactFragment)
            fragmentTitles.add(getString(R.string.tab_contact))

            val indexName = getString(R.string.tab_todo)
            if (indexType == O2CustomStyle.INDEX_TYPE_DEFAULT || TextUtils.isEmpty(indexId)) {
                val indexFragment = IndexFragment()
                fragmentList.add(indexFragment)
                fragmentTitles.add(indexName)
            } else {
                val indexFragment = IndexPortalFragment.instance(indexId)
                fragmentList.add(indexFragment)
                fragmentTitles.add(indexName)
            }

            val appFragment = AppFragment()
            fragmentList.add(appFragment)
            fragmentTitles.add(getString(R.string.tab_app))

            val settingFragment = SettingsFragment()
            fragmentList.add(settingFragment)
            fragmentTitles.add(getString(R.string.tab_settings))
            icon_main_bottom_news.visible()
            icon_main_bottom_contact.visible()
            icon_main_bottom_app.visible()
            icon_main_bottom_news.setOnClickListener(this)
            icon_main_bottom_app.setOnClickListener(this)
            icon_main_bottom_index.setOnClickListener(this)
            icon_main_bottom_contact.setOnClickListener(this)
            icon_main_bottom_setting.setOnClickListener(this)

            // 通讯录权限加载
//            val data = OrganizationPermissionData(excludePerson = "楼国栋@237@P", excludeUnit = "产品运营组@320789019@U", hideMobilePerson = "周睿@233@P", limitQueryAll = "蔡艳红@204@P", limitQueryOuter = "金飞@207@P")
//            OrganizationPermissionManager.instance().initData(data)
            mPresenter.loadOrganizationPermission()
        }
        content_fragmentView_id.adapter = adapter
        content_fragmentView_id.offscreenPageLimit = if(simpleMode){2}else{5}
        content_fragmentView_id.addOnPageChangeListener {
            onPageSelected { position ->
                var index = position
                if (simpleMode) {
                    index = when(position) {
                        0 -> 2
                        else -> 4
                    }
                }
                selectTab(index)
            }
        }

        selectTab(mCurrentSelectIndex)

        //register scheduler job
        registerSchedulerJob()
        //注册设备号 推送消息用
        mPresenter.jPushBindDevice()

        //绑定启动webSocket 服务
        val webSocketServiceIntent = Intent(this, WebSocketService::class.java)
        bindService(webSocketServiceIntent, serviceConnect, BIND_AUTO_CREATE)

        // IM 消息接收广播
        registerBroadcast()

        // 检查考勤版本
        mPresenter.checkAttendanceFeature()

        // 检测网盘是否存在V3版本
        mPresenter.checkCloudFileV3()

        // 权限
        PermissionRequester(this)
            .request(Manifest.permission.READ_EXTERNAL_STORAGE).o2Subscribe {
                onNext { (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
                    Log.d("LaunchActivity", "granted:$granted, show:$shouldShowRequestPermissionRationale, deniedList:$deniedPermissions")
                }
                onError { e, _ ->
                    Log.e("LaunchActivity", "检查权限出错", e)
                }
            }
    }


    override fun onResume() {
        super.onResume()
        pictureLoaderService = PictureLoaderService(this)
        changeBottomIcon(mCurrentSelectIndex)
        calDpi()
        val unit = O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HOST_KEY, "")
        if (!TextUtils.isEmpty(unit) && unit == "sample.o2oa.net") {
            val day = O2SDKManager.instance().prefs().getString(O2.PRE_DEMO_ALERT_REMIND_DAY, "")
            val today = DateHelper.nowByFormate("yyyy-MM-dd")
            if (day != today) {
                val demoDialog = DemoAlertFragment()
                demoDialog.isCancelable = true
                demoDialog.show(supportFragmentManager, "demo")
                O2SDKManager.instance().prefs().edit {
                    putString(O2.PRE_DEMO_ALERT_REMIND_DAY, today)
                }
            }
        }
        //退出重新登录的情况下 重连webSocket
        if (webSocketService != null) {
            if (webSocketService?.isWebSocketOpen() == false) {
                webSocketService?.webSocketOpen()
            }
        }
        // 清除通知
        XLog.info("onResume ... 清除通知！！")
        O2App.instance.clearAllNotification()

        // 触发一次 tbs 内核下载
        val isX5Init = WordReadHelper.getInstance().initFinish()
        XLog.info("x5内核是否已经完成，$isX5Init")

        // 检查更新
        if (BuildConfig.InnerServer) {
            checkAppUpdateInner()
        }
    }


    override fun onPause() {
        super.onPause()
        pictureLoaderService?.close()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(mCurrentSelectIndexKey, mCurrentSelectIndex)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mCurrentSelectIndex = savedInstanceState.getInt(mCurrentSelectIndexKey)
    }

    override fun onDestroy() {
        unbindService(serviceConnect)
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
        }
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val indexFragment =  if (simpleMode) {
                fragmentList[0]
            }else {
                fragmentList[2]
            }
            return if (mCurrentSelectIndex == 2 && indexFragment is IndexPortalFragment) {
                if (indexFragment.previousPage()) {
                    true
                } else {
                    onBackPressExitApp(keyCode, event)
                }
            } else {
                onBackPressExitApp(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onBackPressExitApp(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode != KeyEvent.KEYCODE_BACK) {
            false
        } else {
            if (!TextUtils.isEmpty(appExitAlert)) {
                O2DialogSupport.openConfirmDialog(this, appExitAlert, { d ->
                    finish()
                })
                true
            } else {
                doubleClickExitHelper.onKeyDown(keyCode, event)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.icon_main_bottom_news -> selectTab(0)
            R.id.icon_main_bottom_contact -> selectTab(1)
            R.id.icon_main_bottom_index -> {
                val indexFragment = if (simpleMode) {
                    fragmentList[0]
                }else {
                    fragmentList[2]
                }
                if (indexFragment is IndexPortalFragment) {
                    // 点击返回上一页
                    if (!indexFragment.previousPage()) {
                        // 否则 刷新
                        indexFragment.windowReload()
                    }
                }
                selectTab(2)
            }
            R.id.icon_main_bottom_app -> selectTab(3)
            R.id.icon_main_bottom_setting -> selectTab(4)
        }
    }

    //刷新ActionBar的菜单按钮 应用页面使用
    fun refreshMenu() {
        invalidateOptionsMenu()
    }

    //跳转到应用页面 首页使用
    fun gotoApp() {
        if (!simpleMode) {
            selectTab(3)
        }
    }

    // 给IndexFragment使用
    fun isSimpleMode(): Boolean = simpleMode

    private fun selectTab(i: Int) {
        changePageView(i)
        changeBottomIcon(i)
        mCurrentSelectIndex = i
    }


    private fun changeBottomIcon(i: Int) {
        resetBottomBtnAlpha()
        when (i) {

            0 -> {
                image_icon_main_bottom_news.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_news_red))
                tv_icon_main_bottom_news.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
            }
            1 -> {
                image_icon_main_bottom_contact.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_contact_red))
                tv_icon_main_bottom_contact.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
            }
            2 -> {
                val path = O2CustomStyle.indexMenuLogoFocusImagePath(this)
                if (!TextUtils.isEmpty(path)) {
                    BitmapUtil.setImageFromFile(path!!, icon_main_bottom_index)
                } else {
                    icon_main_bottom_index.setImageResource(R.mipmap.index_bottom_menu_logo_focus)
                }
            }
            3 -> {
                image_icon_main_bottom_app.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_app_red))
                tv_icon_main_bottom_app.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
            }
            4 -> {
                image_icon_main_bottom_setting.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_setting_red))
                tv_icon_main_bottom_setting.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
            }
        }

    }

    private fun changePageView(position: Int) {
        val pageIndex = if (simpleMode) {
            if (position == 4) {
                1
            }else {
                0
            }
        }else {
            position
        }
        content_fragmentView_id.setCurrentItem(pageIndex, false)
        when (position) {
            0 -> resetToolBar(getString(R.string.tab_message))
            1 -> resetToolBar(getString(R.string.tab_contact))
            2 -> setIndexToolBar()
            3 -> resetToolBar(getString(R.string.tab_app))
            4 -> resetToolBar(getString(R.string.tab_settings))
        }

    }

    private fun resetToolBar(string: String?) {
        app_bar_layout_main_head.visible()
        toolbar?.navigationIcon = null
        toolbarTitle?.text = string
    }

    private fun setIndexToolBar() {
        app_bar_layout_main_head.gone()
    }

    private fun resetBottomBtnAlpha() {
        image_icon_main_bottom_news.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_news))
        tv_icon_main_bottom_news.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary))
        image_icon_main_bottom_contact.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_contact))
        tv_icon_main_bottom_contact.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary))
        val path = O2CustomStyle.indexMenuLogoBlurImagePath(this)
        if (!TextUtils.isEmpty(path)) {
            BitmapUtil.setImageFromFile(path!!, icon_main_bottom_index)
        } else {
            icon_main_bottom_index.setImageResource(R.mipmap.index_bottom_menu_logo_blur)
        }
        image_icon_main_bottom_app.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_app))
        tv_icon_main_bottom_app.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary))
        image_icon_main_bottom_setting.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_main_setting))
        tv_icon_main_bottom_setting.setTextColor(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary))
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerSchedulerJob() {
        val componentName = ComponentName(this, ClearTempFileJobService::class.java)
        val jobInfo = JobInfo.Builder(O2.O2_CLEAR_TEMP_FILE_JOB_ID, componentName)
                .setPersisted(true)//手机重启之后是否继续
                .setRequiresCharging(true)//充电的时候才执行
                .setPeriodic(24 * 60 * 60 * 1000)
                .build()

        val jobScheduler = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val result = jobScheduler.schedule(jobInfo)
//        val result2 = jobScheduler.schedule(jobCollectLog)
//        XLog.info("jobScheduler result:$result, result2:$result2")
        XLog.info("jobScheduler result:$result")
    }



    /*************检查应用是否需要更新*********/


    private var downloadFragment: DownloadAPKFragment? = null
    private var versionName = ""
    private var downloadUrl = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 10086) { //  下载安装更新
            if (downloadFragment == null) {
                downloadFragment = DownloadAPKFragment()
            }
            downloadFragment?.isCancelable = false
            if (downloadFragment?.isAdded == true) {
            }else {
                downloadFragment?.show(supportFragmentManager, DownloadAPKFragment.DOWNLOAD_FRAGMENT_TAG)
                downloadServiceStart()
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        startActivityForResult(intent, 10086)
    }

    private fun checkAppUpdateInner() {
        val isOpen = O2SDKManager.instance().prefs()
            .getBoolean(O2.PRE_APP_AUTO_CHECK_UPDATE_KEY, true)
        // 用户自行开关检查
        if (isOpen) {
            checkAppUpdate(callbackContinue = { result ->
                if (result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {// 8.0需要判断安装未知来源的权限
                        startInstallPermissionSettingActivity()
                    }else { // 下载安装更新
                        if (downloadFragment == null) {
                            downloadFragment = DownloadAPKFragment()
                        }
                        downloadFragment?.isCancelable = false
                        if (downloadFragment?.isAdded == true) {
                        }else {
                            downloadFragment?.show(supportFragmentManager, DownloadAPKFragment.DOWNLOAD_FRAGMENT_TAG)
                            downloadServiceStart()
                        }

                    }
                }
            })
        }
    }

    private fun downloadServiceStart() {
        val intent = Intent(this, DownloadAPKService::class.java)
        intent.action = packageName + DownloadAPKService.DOWNLOAD_SERVICE_ACTION
        intent.putExtra(DownloadAPKService.VERSIN_NAME_EXTRA_NAME, versionName)
        intent.putExtra(DownloadAPKService.DOWNLOAD_URL_EXTRA_NAME, downloadUrl)
        startService(intent)
    }

    private fun checkAppUpdate(callbackContinue:((flag: Boolean)->Unit)? = null) {
        O2AppUpdateManager.instance().checkUpdateInner(this, object : O2AppUpdateCallback {
            override fun onUpdate(version: O2AppUpdateBean) {
                XLog.debug("onUpdateAvailable $version")
                versionName = version.versionName
                downloadUrl = version.downloadUrl
                XLog.info("versionName:$versionName, downloadUrl:$downloadUrl")
                val tips = getString(R.string.message_update_tips, versionName)
                O2DialogSupport.openConfirmDialog(this@MainActivity,tips + version.content, listener = { _ ->
                    XLog.info("notification is true..........")
                    callbackContinue?.invoke(true)
                }, icon = O2AlertIconEnum.UPDATE, negativeListener = { _->
                    callbackContinue?.invoke(false)
                })
            }

            override fun onNoneUpdate(error: String) {
                XLog.info(error)
                callbackContinue?.invoke(false)
            }
        })
    }

    /**
     * 存储下手机分辨率
     */
    private fun calDpi() {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels
        doAsync {
            O2SDKManager.instance().prefs().edit {
                putString(O2.PRE_DEVICE_DPI_KEY, "$width*$height")
            }
            XLog.debug("storage success, width:$width, height:$height")
        }
    }

    /**
     * 重启应用
     */
    private fun restartAppSelf(context: Context) {
        val intent = Intent(context, RestartSelfService::class.java)
        intent.putExtra(RestartSelfService.RESTART_PACKAGE_NAME_EXTRA_NAME, context.packageName)
        context.startService(intent)

        android.os.Process.killProcess(android.os.Process.myPid())
    }

    /*************websocket service*********/

    private var webSocketService: WebSocketService? = null
    private val serviceConnect: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                XLog.debug("onServiceDisconnected...............name:$name")
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WebSocketService.WebSocketBinder
                webSocketService = binder.service
                XLog.debug("onServiceConnected............webSocketService.")
                webSocketService?.webSocketOpen()
            }
        }
    }

    /**
     * 登出的时候调用
     */
    fun webSocketClose() {
        webSocketService?.webSocketClose()
    }

    /**************im 消息接收器***************/

    var mReceiver: IMMessageReceiver? = null
    private var unreadMsgNumber = 0

    fun refreshUnreadNumber(number: Int) {
        unreadMsgNumber = number
        when {
            unreadMsgNumber in 1..99 -> {
                circle_tv_icon_main_bottom_news.visible()
                circle_tv_icon_main_bottom_news.setText("$unreadMsgNumber")
            }
            unreadMsgNumber >= 100 -> {
                circle_tv_icon_main_bottom_news.visible()
                circle_tv_icon_main_bottom_news.setText("99..")
            }
            else -> circle_tv_icon_main_bottom_news.gone()
        }
    }

    fun addUnreadMsg() {
        unreadMsgNumber += 1
        when {
            unreadMsgNumber in 1..99 -> {
                circle_tv_icon_main_bottom_news.visible()
                circle_tv_icon_main_bottom_news.setText("$unreadMsgNumber")
            }
            unreadMsgNumber >= 100 -> {
                circle_tv_icon_main_bottom_news.visible()
                circle_tv_icon_main_bottom_news.setText("99..")
            }
            else -> circle_tv_icon_main_bottom_news.gone()
        }
    }

    private fun registerBroadcast() {
        mReceiver = IMMessageReceiver()
        val filter = IntentFilter(O2IM.IM_Message_Receiver_Action)
        registerReceiver(mReceiver, filter)
    }

    private fun receiveIMMessage(message: IMMessage) {
        val newsFragment = fragmentList[0]
        if (newsFragment is O2IMConversationFragment) {
            newsFragment.receiveMessageFromWebsocket(message)
        }
        addUnreadMsg()
    }

    inner class IMMessageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val body = intent?.getStringExtra(O2IM.IM_Message_Receiver_name)
            if (body != null && body.isNotEmpty()) {
                XLog.debug("接收到im消息, $body")
                try {
                    val message = O2SDKManager.instance().gson.fromJson<IMMessage>(body, IMMessage::class.java)
                    receiveIMMessage(message)
                } catch (e: Exception) {
                    XLog.error("", e)
                }

            }
        }

    }

}