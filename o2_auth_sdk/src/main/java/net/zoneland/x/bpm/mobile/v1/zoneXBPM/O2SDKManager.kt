package net.zoneland.x.bpm.mobile.v1.zoneXBPM

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.TextureView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2.SECURITY_IS_UPDATE
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.RetrofitClient
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.LaunchState
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.exception.NoLoginException
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiResponse
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.bbs.BBSMuteInfo
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.AuthenticationInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.CollectUnitData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.portal.PortalData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.SharedPreferencesHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.security.SecuritySharedPreference
import org.w3c.dom.Text
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancyLou on 2018/11/22.
 * Copyright © 2018 O2. All rights reserved.
 */


class O2SDKManager private constructor()  {

    val TAG = "O2SDKManager"

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: O2SDKManager? = null

        fun instance(): O2SDKManager {
            if (INSTANCE == null) {
                synchronized(O2SDKManager::class) {
                    if (INSTANCE == null) {
                        INSTANCE = O2SDKManager()
                    }
                }
            }
            return INSTANCE!!
        }

    }

    private val CURRENT_PERSON_ID_KEY = "CURRENT_PERSON_ID_KEY"//用户 id
    private val CURRENT_PERSON_DISTINGUISHED_KEY = "CURRENT_PERSON_DISTINGUISHED_KEY"//用户 唯一标识
    private val CURRENT_PERSON_UPDATETIME_KEY = "CURRENT_PERSON_UPDATETIME_KEY"//用户最后更新时间
    private val CURRENT_PERSON_GENDERTYPE_KEY = "CURRENT_PERSON_GENDERTYPE_KEY"//性别
    private val CURRENT_PERSON_PINYIN_KEY = "CURRENT_PERSON_PINYIN_KEY"//拼音
    private val CURRENT_PERSON_PINYININITIAL_KEY = "CURRENT_PERSON_PINYININITIAL_KEY"//拼音简写
    private val CURRENT_PERSON_NAME_KEY = "CURRENT_PERSON_NAME_KEY"//姓名
    private val CURRENT_PERSON_EMPLOYEE_KEY = "CURRENT_PERSON_EMPLOYEE_KEY"//员工号
    private val CURRENT_PERSON_UNIQUE_KEY = "CURRENT_PERSON_UNIQUE_KEY"//
    private val CURRENT_PERSON_CONTROLLERLIST_KEY = "CURRENT_PERSON_CONTROLLERLIST_KEY"//
    private val CURRENT_PERSON_MAIL_KEY = "CURRENT_PERSON_MAIL_KEY"//邮箱地址
    private val CURRENT_PERSON_QQ_KEY = "CURRENT_PERSON_QQ_KEY"//我的qq
    private val CURRENT_PERSON_WEIXIN_KEY = "CURRENT_PERSON_WEIXIN_KEY"//微信号
    private val CURRENT_PERSON_MOBILE_KEY = "CURRENT_PERSON_MOBILE_KEY"//手机号
    private val CURRENT_PERSON_DEVICELIST_KEY = "CURRENT_PERSON_DEVICELIST_KEY"//
    private val CURRENT_PERSON_SIGNATURE_KEY = "CURRENT_PERSON_SIGNATURE_KEY"//
    private val CURRENT_PERSON_TOKEN_KEY = "CURRENT_PERSON_TOKEN_KEY"//登录的token
    private val CURRENT_PERSON_ROLELIST_KEY = "CURRENT_PERSON_ROLELIST_KEY"//角色

    /***********************当前登录的用户信息 */
    var cId: String = ""//用户唯一标识
    var distinguishedName: String = "" //用户唯一标识
    var cUnique: String = "" //用户唯一标识
    var cUpdateTime: String = ""
    var cGenderType: String = ""
    var cPinyin: String = ""
    var cPinyinInitial: String = ""
    var cName: String = ""
    var cEmployee: String = ""
    var cControllerList: String = ""
    var cMail: String = ""
    var cQq: String = ""
    var cWeixin: String = ""
    var cMobile: String = ""
    var cDeviceList: String = ""
    var cSignature: String = ""
    var cRoleList: String = ""//角色
    //扩展信息
    var zToken: String = ""//用户登录的token

    var bbsMuteInfo: BBSMuteInfo? = null // 论坛禁言对象



    private lateinit var context: Context
    private lateinit var spHelper: SharedPreferencesHelper
    val gson: Gson by lazy { GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create() }

    /**
     * Application onCreate中初始化 context = ApplicationContext
     */
    fun init(context: Context) {
        //初始化RetrofitClient
        this.context = context
        spHelper = SharedPreferencesHelper(context)
        //检查老的sp 是否要更新
        val isUpdate = prefs().getBoolean(SECURITY_IS_UPDATE, false)
        if (!isUpdate) {
            Log.i(TAG, "过渡老的sp文件！")
            prefs().handleTransition() //执行过渡程序把老的sp文件读取覆盖一下
            prefs().edit().putBoolean(SECURITY_IS_UPDATE, true).apply()
        }

        RetrofitClient.instance().init(context)
        cId = prefs().getString(CURRENT_PERSON_ID_KEY, "") ?: ""
        distinguishedName = prefs().getString(CURRENT_PERSON_DISTINGUISHED_KEY, "") ?: ""
        cUpdateTime = prefs().getString(CURRENT_PERSON_UPDATETIME_KEY, "") ?: ""
        cGenderType = prefs().getString(CURRENT_PERSON_GENDERTYPE_KEY, "") ?: ""
        cPinyin = prefs().getString(CURRENT_PERSON_PINYIN_KEY, "") ?: ""
        cPinyinInitial = prefs().getString(CURRENT_PERSON_PINYININITIAL_KEY, "") ?: ""
        cName = prefs().getString(CURRENT_PERSON_NAME_KEY, "") ?: ""
        cEmployee = prefs().getString(CURRENT_PERSON_EMPLOYEE_KEY, "") ?: ""
        cUnique = prefs().getString(CURRENT_PERSON_UNIQUE_KEY, "") ?: ""
        cControllerList = prefs().getString(CURRENT_PERSON_CONTROLLERLIST_KEY, "") ?: ""
        cMail = prefs().getString(CURRENT_PERSON_MAIL_KEY, "") ?: ""
        cQq = prefs().getString(CURRENT_PERSON_QQ_KEY, "") ?: ""
        cWeixin = prefs().getString(CURRENT_PERSON_WEIXIN_KEY, "") ?: ""
        cMobile = prefs().getString(CURRENT_PERSON_MOBILE_KEY, "") ?: ""
        cDeviceList = prefs().getString(CURRENT_PERSON_DEVICELIST_KEY, "") ?: ""
        cSignature = prefs().getString(CURRENT_PERSON_SIGNATURE_KEY, "") ?: ""
        cRoleList = prefs().getString(CURRENT_PERSON_ROLELIST_KEY, "") ?: ""
        //扩展信息
        zToken = prefs().getString(CURRENT_PERSON_TOKEN_KEY, "") ?: ""//TOKEN


    }

//    fun prefs(): SharedPreferences = spHelper.prefs()

    fun prefs(): SecuritySharedPreference = spHelper.securityPrefs()

    /**
     * 获取tokenName
     */
    fun tokenName(): String {
        return prefs().getString(O2.PRE_TOKEN_NAME_KEY, "x-token") ?: "x-token"
    }

    /**
     * 启动  整个启动过程，检查绑定 连接中心服务器 下载配置 登录
     */
    fun launch(deviceToken: String, showState:(state: LaunchState)->Unit) {

        if (TextUtils.isEmpty(deviceToken)) {
            Log.e(TAG,"没有deviceToken！")
            showState(LaunchState.NoBindError)
            return
        }
        val phone = prefs().getString(O2.PRE_BIND_PHONE_KEY, "") ?: ""
        val unit = prefs().getString(O2.PRE_BIND_UNIT_ID_KEY, "") ?: ""
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(unit)) {
            Log.e(TAG,"没有绑定手机号码。。。。")
            showState(LaunchState.NoBindError)
            return
        }
        try {
//            val client = RetrofitClient.instance()
            showState(LaunchState.ConnectO2Collect)
            val demoKey = prefs().getBoolean(O2.PRE_DEMO_O2_KEY, false)
            if (demoKey) { // 不验证
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .flatMap {
                            val demoUnit = CollectUnitData()
                            demoUnit.id = prefs().getString(O2.PRE_BIND_UNIT_ID_KEY, "")
                            demoUnit.httpProtocol = prefs().getString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, "")
                            demoUnit.centerHost = prefs().getString(O2.PRE_CENTER_HOST_KEY, "")
                            demoUnit.centerContext = prefs().getString(O2.PRE_CENTER_CONTEXT_KEY, "")
                            demoUnit.centerPort = prefs().getInt(O2.PRE_CENTER_PORT_KEY, -1)
                            demoUnit.name = prefs().getString(O2.PRE_BIND_UNIT_KEY, "")
                            Observable.just(demoUnit)
                        }.observeOn(AndroidSchedulers.mainThread())
                        .o2Subscribe {
                            onNext {
                                saveCollectInfo(it, showState)
                            }
                            onError { e, _ ->
                                Log.e(TAG, "未知异常", e)
                                showState(LaunchState.NoBindError)
                            }
                        }
            }else {
                // 检查绑定设备取消 绑定后直接读取本地存储的服务器信息
//                client.collectApi().checkBindDeviceNew(deviceToken, phone, unit, O2.DEVICE_TYPE)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .o2Subscribe {
//                            onNext { collectUnitRes ->
                                saveCollectInfo(null, showState)
//                            }
//                            onError { e, _ ->
//                                Log.e(TAG, "检查绑定异常", e)
//                                showState(LaunchState.NoBindError)
//                            }
//                        }
            }
        }catch (e: RuntimeException) {
            Log.e(TAG, "catch到的异常", e)
            showState(LaunchState.UnknownError)
        }

    }

    /**
     * 启动 内网使用版本
     */
    fun launchInner(serverJson: String, showState:(state: LaunchState)->Unit) {
        if (TextUtils.isEmpty(serverJson)) {
            showState(LaunchState.UnknownError)
            return
        }
        try {
            Observable.just(true)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        val server = gson.fromJson<CollectUnitData>(serverJson, CollectUnitData::class.java)
                        Observable.just(server)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .o2Subscribe {
                        onNext { unit->
                            saveCollectInfo(unit, showState)
                        }
                        onError { e, _ ->
                            Log.e(TAG, "未知异常", e)
                            showState(LaunchState.UnknownError)
                        }
                    }
        }catch (e: Exception) {
            Log.e(TAG, "catch到的异常", e)
            showState(LaunchState.UnknownError)
        }

    }

    /**
     * 转化成urlMapping后的地址
     * @param url 转化前的地址
     * @param urlMapping 默认情况下读取prefs中PRE_BIND_UNIT_URLMAPPING_KEY的值，特殊情况下（暂时没有存储）使用这个值解析
     */
    fun urlTransfer2Mapping(url: String, urlMapping: String? = null): String {
        var mapping = if (!TextUtils.isEmpty(urlMapping)) {
            urlMapping
        } else {
            prefs().getString(O2.PRE_BIND_UNIT_URLMAPPING_KEY, "")
        }
        if (TextUtils.isEmpty(mapping)) { // 没有值 直接返回原地址
            return url
        } else {
            try {
                val map = gson.fromJson<HashMap<String, String>>(mapping, HashMap::class.java)
                if (map != null) {
                    var newUrl = ""
                    map.keys.forEach { key->
                        val value = map[key]
                        if (url.contains(key) && value != null) {
                            newUrl = url.replace(key, value, false)
                        }
                    }
                    return if (TextUtils.isEmpty(newUrl)) {
                        url
                    } else {
                        newUrl
                    }
                }else {
                    return url
                }
            } catch (e: Exception) {
                Log.e(TAG, "urlMapping 解析失败", e)
                return url
            }
        }
    }

    /**
     * 绑定信息存储
     */
    fun bindUnit(unit: CollectUnitData, phone: String, deviceToken: String) {
        val url = APIAddressHelper.instance().getCenterUrl(unit.centerHost, unit.centerContext, unit.centerPort)
        prefs().edit {
            putString(O2.PRE_CENTER_URL_KEY, url)
            putString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, unit.httpProtocol)
            putString(O2.PRE_CENTER_HOST_KEY, unit.centerHost)
            putString(O2.PRE_CENTER_CONTEXT_KEY, unit.centerContext)
            putInt(O2.PRE_CENTER_PORT_KEY, unit.centerPort)
            putString(O2.PRE_BIND_UNIT_ID_KEY, unit.id)
            putString(O2.PRE_BIND_UNIT_KEY, unit.name)
            putString(O2.PRE_BIND_UNIT_URLMAPPING_KEY, unit.urlMapping)
            putString(O2.PRE_BIND_PHONE_KEY, phone)
            putString(O2.PRE_BIND_PHONE_TOKEN_KEY, deviceToken)
        }
    }

    /**
     * 清除绑定信息
     */
    fun clearBindUnit() {
        prefs().edit {
            putString(O2.PRE_CENTER_URL_KEY, "")
            putString(O2.PRE_CENTER_HOST_KEY, "")
            putString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, "")
            putString(O2.PRE_CENTER_CONTEXT_KEY, "")
            putInt(O2.PRE_CENTER_PORT_KEY, 0)
            putString(O2.PRE_BIND_UNIT_ID_KEY, "")
            putString(O2.PRE_BIND_UNIT_KEY, "")
            putString(O2.PRE_BIND_UNIT_URLMAPPING_KEY, "")
            putString(O2.PRE_BIND_PHONE_KEY, "")
            putString(O2.PRE_BIND_PHONE_TOKEN_KEY, "")
        }
    }




    private fun saveCollectInfo(unit: CollectUnitData?, showState:(state: LaunchState)->Unit) {
        var newUrl = ""
        var host = ""
        if (unit != null) {
            Log.d(TAG, "unit: ${unit.centerHost}, port: ${unit.centerPort} , id: ${unit.id}")
            //更新http协议
            RetrofitClient.instance().setO2ServerHttpProtocol(unit.httpProtocol)
            APIAddressHelper.instance().setHttpProtocol(unit.httpProtocol)
            host = unit.centerHost
            newUrl = APIAddressHelper.instance().getCenterUrl(unit.centerHost, unit.centerContext, unit.centerPort)
            prefs().edit {
                putString(O2.PRE_BIND_UNIT_ID_KEY, unit.id)
                putString(O2.PRE_CENTER_URL_KEY, newUrl)
                putString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, unit.httpProtocol)
                putString(O2.PRE_CENTER_HOST_KEY, unit.centerHost)
                putString(O2.PRE_CENTER_CONTEXT_KEY, unit.centerContext)
                putInt(O2.PRE_CENTER_PORT_KEY, unit.centerPort)
                putString(O2.PRE_BIND_UNIT_KEY, unit.name)
                putString(O2.PRE_BIND_UNIT_URLMAPPING_KEY, unit.urlMapping)
            }
            Log.d(TAG, "保存 服务器信息成功！！！！newUrl：$newUrl")
            Log.d(TAG, "httpProtocol:${unit.httpProtocol}")
            Log.d(TAG, "host:$host")
        } else {
            Log.d(TAG, "没有单位信息，读取本地存储信息")
            host = prefs().getString(O2.PRE_CENTER_HOST_KEY, null) ?: ""
            newUrl = prefs().getString(O2.PRE_CENTER_URL_KEY, null) ?: ""
            if (TextUtils.isEmpty(host) || TextUtils.isEmpty(newUrl)) {
                Log.e(TAG, "本地检查异常， 没有获取到本地存储的服务器信息")
                showState(LaunchState.NoBindError)
                return
            }
        }


        /////////////////////////// 开始业务逻辑  ////////////////////////////////////

        Log.d(TAG, "开始连接center......$newUrl")
        showState(LaunchState.ConnectO2Server)
        val client = RetrofitClient.instance()
        val api = client.api(newUrl)
        api.getWebserverDistributeWithSource(host)
                .subscribeOn(Schedulers.io())
                .flatMap { response->
                    Log.d(TAG, "开始检查配置.....")
                    showState(LaunchState.CheckMobileConfig)
                    APIAddressHelper.instance().setDistributeData(response.data)
                    api.getCustomStyleUpdateDate()
                }
                .flatMap { response->
                    val hash = prefs().getString(O2CustomStyle.CUSTOM_STYLE_UPDATE_HASH_KEY, "")
                            ?: ""
                    val result = response.data.value
                    Log.d(TAG, "检查配置newHash：$result ， oldHash：$hash")
                    if (hash == result) {
                        Observable.just(false)
                    } else {
                        prefs().edit {
                            putString(O2CustomStyle.CUSTOM_STYLE_UPDATE_HASH_KEY, result)
                        }
                        Observable.just(true)
                    }
                }.flatMap { flag->
                    if (flag) {
                        Log.d(TAG, "开始下载配置.....")
                        showState(LaunchState.DownloadMobileConfig)
                        var excep = false
                        api.getCustomStyle()
                                .subscribeOn(Schedulers.immediate())
                                .o2Subscribe {
                                    onNext {res->
                                        val style = res.data
                                        // 去除不需要显示的门户
                                        val portalList = style.portalList
                                        val newlist: ArrayList<PortalData> = ArrayList()
                                        if (!portalList.isEmpty()) {
                                            for (portal in portalList) {
                                                if (portal.mobileClient) {
                                                    newlist.add(portal)
                                                }
                                            }
                                            style.portalList = newlist
                                        }

                                        val styleJson = gson.toJson(style)
                                        prefs().edit {
                                            putString(O2CustomStyle.CUSTOM_STYLE_JSON_KEY, styleJson)
                                        }
                                    }
                                    onError { e, _ ->
                                        Log.e(TAG, "下载配置文件出错", e)
                                        excep = true
                                    }
                                }
                        if (excep) {
                            Observable.error<Boolean>(RuntimeException("下载配置文件出错"))
                        }else {
                            Observable.just(true)
                        }
                    }else {
                        Observable.just(true)
                    }
                }.flatMap { flag ->
                    Log.d(TAG, "开始登录......$flag")
                    showState(LaunchState.AutoLogin)
                    if (TextUtils.isEmpty(zToken)) {
                        Observable.error<ApiResponse<AuthenticationInfoJson>>(NoLoginException("没有登录！"))
                    }else {
                        client.assembleAuthenticationApi().who()
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { who->
                        val authentication = who.data
                        if (authentication.name != O2.TOKEN_TYPE_ANONYMOUS) {
                            if (TextUtils.isEmpty(authentication.token)) {
                                Log.d(TAG, "开始登录过期了......")
                                logoutCleanCurrentPerson()
                                showState(LaunchState.NoLoginError)
                            } else {
                                setCurrentPersonData(authentication)
                                showState(LaunchState.Success)
                            }
                        }else{
                            Log.d(TAG, "开始登录过期了......")
                            logoutCleanCurrentPerson()
                            showState(LaunchState.NoLoginError)
                        }
                    }
                    onError { e, _ ->
                        Log.e(TAG, "", e)
                        showState(LaunchState.NoLoginError)
                    }
                }
    }


    /**
     * 是否系统管理员
     */
    fun isAdministrator(): Boolean {
        val roleList = this.cRoleList.split(",").map {
            if (it.contains("@")) {
                it.substring(0, it.indexOf("@")).toLowerCase()
            } else {
                it.toLowerCase()
            }
        }
        fun isAdminRole(): Boolean = roleList.any { it == "manager" }
        return this.cName == "xadmin" || isAdminRole()
    }

    /**
     * 是否会议管理员
     */
    fun isMeetingAdministrator(): Boolean {
        if (isAdministrator()) return true
        val roleList = this.cRoleList.split(",").map {
            if (it.contains("@")) {
                it.substring(0, it.indexOf("@")).toLowerCase()
            } else {
                it.toLowerCase()
            }
        }
        return roleList.any { it == "meetingmanager" }
    }

    /**
     * 登录 加载用户信息
     */
    fun setCurrentPersonData(data: AuthenticationInfoJson) {
        storagecId(data.id)
        storageDistinguishedName(data.distinguishedName)
        storagecUpdateTime(data.updateTime)
        storagezToken(data.token)
        storagecGenderType(data.genderType)
        storagecPinyin(data.pinyin)
        storagecPinyinInitial(data.pinyinInitial)
        storagecName(data.name)
        storagecEmployee(data.employee)
        storagecUnique(data.unique)
        storagecControllerList(data.controllerList.joinToString(","))
        storagecMail(data.mail)
        storagecQq(data.qq)
        storagecWeixin(data.weixin)
        storagecMobile(data.mobile)
        storagecDeviceList(data.deviceList.joinToString(","))
        storagecSignature(data.signature)
        storagecRoleList(data.roleList.joinToString(","))
    }

    /**
     * 登出 清空用户数据
     */
    fun logoutCleanCurrentPerson() {
        storagecId("")
        storageDistinguishedName("")
        storagecUnique("")
        storagecUpdateTime("")
        storagezToken("")
        storagecGenderType("")
        storagecPinyin("")
        storagecPinyinInitial("")
        storagecName("")
        storagecEmployee("")
        storagecControllerList("")
        storagecMail("")
        storagecQq("")
        storagecWeixin("")
        storagecMobile("")
        storagecDeviceList("")
//        storagecIcon("")
        storagecSignature("")
        storagecRoleList("")
    }


    fun storagecRoleList(cRoleList: String) {
        if (this.cRoleList == cRoleList) {
            return
        }
        this.cRoleList = cRoleList
        prefs().edit().putString(CURRENT_PERSON_ROLELIST_KEY, cRoleList).apply()
    }

    fun storagezToken(zToken: String) {
        if (this.zToken == zToken) {
            return
        }
        this.zToken = zToken
        prefs().edit().putString(CURRENT_PERSON_TOKEN_KEY, zToken).apply()
    }

    fun storagecId(cId: String) {
        if (this.cId == cId) {
            return
        }
        this.cId = cId
        prefs().edit().putString(CURRENT_PERSON_ID_KEY, cId).apply()
    }

    fun storageDistinguishedName(distinguishedName: String) {
        if (this.distinguishedName == distinguishedName) {
            return
        }
        this.distinguishedName = distinguishedName
        prefs().edit().putString(CURRENT_PERSON_DISTINGUISHED_KEY, distinguishedName).apply()
    }

    fun storagecUpdateTime(cUpdateTime: String) {
        if (this.cUpdateTime == cUpdateTime) {
            return
        }
        this.cUpdateTime = cUpdateTime
        prefs().edit().putString(CURRENT_PERSON_UPDATETIME_KEY, cUpdateTime).apply()
    }

    fun storagecGenderType(cGenderType: String) {
        if (this.cGenderType == cGenderType) {
            return
        }
        this.cGenderType = cGenderType
        prefs().edit().putString(CURRENT_PERSON_GENDERTYPE_KEY, cGenderType).apply()
    }

    fun storagecPinyin(cPinyin: String) {
        if (this.cPinyin == cPinyin) {
            return
        }
        this.cPinyin = cPinyin
        prefs().edit().putString(CURRENT_PERSON_PINYIN_KEY, cPinyin).apply()
    }

    fun storagecPinyinInitial(cPinyinInitial: String) {
        if (this.cPinyinInitial == cPinyinInitial) {
            return
        }
        this.cPinyinInitial = cPinyinInitial
        prefs().edit().putString(CURRENT_PERSON_PINYININITIAL_KEY, cPinyinInitial).apply()
    }

    fun storagecName(cName: String) {
        if (this.cName == cName) {
            return
        }
        this.cName = cName
        prefs().edit().putString(CURRENT_PERSON_NAME_KEY, cName).apply()
    }

    fun storagecEmployee(cEmployee: String) {
        if (this.cEmployee == cEmployee) {
            return
        }
        this.cEmployee = cEmployee
        prefs().edit().putString(CURRENT_PERSON_EMPLOYEE_KEY, cEmployee).apply()
    }

    fun storagecUnique(cUnique: String) {
        if (this.cUnique == cUnique) {
            return
        }
        this.cUnique = cUnique
        prefs().edit().putString(CURRENT_PERSON_UNIQUE_KEY, cUnique).apply()
    }

    fun storagecControllerList(cControllerList: String) {
        if (this.cControllerList == cControllerList) {
            return
        }
        this.cControllerList = cControllerList
        prefs().edit().putString(CURRENT_PERSON_CONTROLLERLIST_KEY, cControllerList).apply()
    }

    fun storagecMail(cMail: String) {
        if (this.cMail == cMail) {
            return
        }
        this.cMail = cMail
        prefs().edit().putString(CURRENT_PERSON_MAIL_KEY, cMail).apply()
    }

    fun storagecQq(cQq: String) {
        if (this.cQq == cQq) {
            return
        }
        this.cQq = cQq
        prefs().edit().putString(CURRENT_PERSON_QQ_KEY, cQq).apply()
    }

    fun storagecWeixin(cWeixin: String) {
        if (this.cWeixin == cWeixin) {
            return
        }
        this.cWeixin = cWeixin
        prefs().edit().putString(CURRENT_PERSON_WEIXIN_KEY, cWeixin).apply()
    }

    fun storagecMobile(cMobile: String) {
        if (this.cMobile == cMobile) {
            return
        }
        this.cMobile = cMobile
        prefs().edit().putString(CURRENT_PERSON_MOBILE_KEY, cMobile).apply()
    }

    fun storagecDeviceList(cDeviceList: String) {
        if (this.cDeviceList == cDeviceList) {
            return
        }
        this.cDeviceList = cDeviceList
        prefs().edit().putString(CURRENT_PERSON_DEVICELIST_KEY, cDeviceList).apply()
    }

    fun storagecSignature(cSignature: String) {
        if (this.cSignature == cSignature) {
            return
        }
        this.cSignature = cSignature
        prefs().edit().putString(CURRENT_PERSON_SIGNATURE_KEY, cSignature).apply()
    }

    /**
     * 网盘是否是V3版本
     * V3 版本 使用CloudFileV3ControlService
     * x_pan_assemble_control
     */
    fun appCloudDiskIsV3(): Boolean {
        val cloudFileV3 =  prefs().getString(O2.PRE_CLOUD_FILE_VERSION_KEY, "")
        return !(TextUtils.isEmpty(cloudFileV3) || cloudFileV3 != "1")
    }

    /**
     * 当前用户是否禁言
     */
    fun isBBSMute(): Boolean {
        if (bbsMuteInfo == null){
            return false
        }
        val expiredDate = bbsMuteInfo?.unmuteDate ?: return false
        return try {
            DateHelper.lsOrEq(DateHelper.nowByFormate("yyyy-MM-dd"), expiredDate, "yyyy-MM-dd")
        } catch (e: Exception) {
            false
        }
    }
}