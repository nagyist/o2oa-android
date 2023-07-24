package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base

import android.content.Context
import android.text.TextUtils
import android.widget.TextView
import cn.jpush.android.api.JPushInterface
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.BuildConfig
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.RetrofitClient
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.service.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.JPushDeviceForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.PushType
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.o2Subscribe
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * Created by fancy on 2017/6/5.
 */

open class BasePresenterImpl<V : BaseView> : BasePresenter<V> {

    protected var mView: V? = null

    override fun attachView(view: V) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

    fun getApiService(context: Context?, url: String): ApiService? {
        return try {
            RetrofitClient.instance().api(url)
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "中心服务异常，请联系管理员！！")
            }
            null
        }
    }

    fun getCollectService(context: Context?): CollectService? {
        return try {
            RetrofitClient.instance().collectApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "O2注册中心服务异常，请联系管理员！！")
            }
            null
        }
    }

    /**
     * 人员组织服务
     */
    fun getOrganizationAssembleControlApi(context: Context?): OrganizationAssembleControlAlphaService? {
        return try {
            RetrofitClient.instance().organizationAssembleControlApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "人员组织模块服务异常，请联系管理员！！")
            }
            null
        }
    }

    /**
     * 查询服务
     */
    fun getQueryAssembleSurfaceServiceAPI(context: Context?): QueryAssembleSurfaceService? {
        return try {
            RetrofitClient.instance().queryAssembleSurfaceServiceAPI()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "查询模块异常，请联系管理员！！")
            }
            null
        }
    }


    /**
     * 认证服务
     */
    fun getAssembleAuthenticationService(context: Context?): OrgAssembleAuthenticationService? {
        return try {
            RetrofitClient.instance().assembleAuthenticationApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "权限认证模块服务异常，请联系管理员！！")
            }
            null
        }
    }

    /**
     * 热图服务
     */
    fun getHotPicAssembleControlServiceApi(context: Context?): HotpicAssembleControlService? {
        return try {
            RetrofitClient.instance().hotpicAssembleControlServiceApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "热点图片新闻服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 人员服务
     */
    fun getAssemblePersonalApi(context: Context?): OrgAssemblePersonalService? {
        return try {
            RetrofitClient.instance().assemblePersonalApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "个人信息服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 组织服务
     */
    fun getAssembleExpressApi(context: Context?): OrgAssembleExpressService? {
        return try {
            RetrofitClient.instance().assembleExpressApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "组织管理服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 流程服务
     */
    fun getProcessAssembleSurfaceServiceAPI(context: Context?): ProcessAssembleSurfaceService? {
        return try {
            RetrofitClient.instance().processAssembleSurfaceServiceAPI()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "流程服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 云盘服务
     */
    fun getFileAssembleControlService(context: Context?): FileAssembleControlService? {
        return try {
            RetrofitClient.instance().fileAssembleControlApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "云盘服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 新版云盘服务
     */
    fun getCloudFileControlService(context: Context?): CloudFileControlService? {
        return try {
            RetrofitClient.instance().cloudFileControlApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "云盘服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 新版云盘服务
     */
    fun getCloudFileV3ControlService(context: Context?): CloudFileV3ControlService? {
        return try {
            RetrofitClient.instance().cloudFileV3ControlApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "云盘服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 会议管理服务
     */
    fun getMeetingAssembleControlService(context: Context?): MeetingAssembleControlService? {
        return try {
            RetrofitClient.instance().meetingAssembleControlApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "会议管理模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 考勤管理服务
     */
    fun getAttendanceAssembleControlService(context: Context?): AttendanceAssembleControlService? {
        return try {
            RetrofitClient.instance().attendanceAssembleControlApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "考勤管理模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 论坛服务
     */
    fun getBBSAssembleControlService(context: Context?): BBSAssembleControlService? {
        return try {
            RetrofitClient.instance().bbsAssembleControlServiceApi()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "论坛服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 信息中心
     */
    fun getCMSAssembleControlService(context: Context?): CMSAssembleControlService? {
        return try {
            RetrofitClient.instance().cmsAssembleControlService()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "信息中心服务模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 公共配置服务
     */
    fun getOrganizationAssembleCustomService(context: Context?): OrganizationAssembleCustomService? {
        return try {
            RetrofitClient.instance().organizationAssembleCustomService()
        } catch (e: Exception) {
            XLog.error("", e)
//            if (context!=null){
//                XToast.toastLong(context, "公共配置服务模块异常，请联系管理员！")
//            }
            null
        }
    }

    /**
     * 门户模块
     */
    fun getPortalAssembleSurfaceService(context: Context?): PortalAssembleSurfaceService? {
        return try {
            RetrofitClient.instance().portalAssembleSurfaceService()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "门户模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 日程管理
     */
    fun getCalendarAssembleService(context: Context?): CalendarAssembleControlService? {
        return try {
            RetrofitClient.instance().calendarAssembleControlService()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "日程管理模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 消息服务
     */
    fun getMessageCommunicateService(context: Context?): MessageCommunicateService? {
        return try {
            RetrofitClient.instance().messageCommunicateService()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "消息服务器异常， 请联系管理员！")
            }
            null
        }
    }


    /**
     * 极光消息管理
     */
    private fun getJPushControlService(): JPushControlService? {
        return try {
            RetrofitClient.instance().jPushControlService()
        } catch (e: Exception) {
            XLog.error("极光消息推送模块异常，请联系管理员！", e)
            null
        }
    }

    /**
     * 绑定设备
     * 根据 后台配置 绑定 设备号
     */
    override fun jPushBindDevice() {
        XLog.info("绑定设备号")
        val service = getJPushControlService()
        if (service != null) {
            service.deviceConfigPushType().subscribeOn(Schedulers.io())
                .flatMap { res ->
                    val config = res.data
                    if (config != null && !TextUtils.isEmpty(config.pushType)) {
                        val deviceToken = if (config.pushType == PushType.HUAWEI_TYPE) {
                            O2SDKManager.instance().prefs()
                                .getString(O2.PRE_PUSH_HUAWEI_DEVICE_ID_KEY, "") ?: ""
                        } else {
                            O2SDKManager.instance().prefs()
                                .getString(O2.PRE_PUSH_JPUSH_DEVICE_ID_KEY, "") ?: ""
                        }
                        val form = JPushDeviceForm(deviceToken, config.pushType)
                        service.deviceBind(form)
                    } else {
                        val deviceToken = O2SDKManager.instance().prefs()
                            .getString(O2.PRE_PUSH_JPUSH_DEVICE_ID_KEY, "") ?: ""
                        val form = JPushDeviceForm(deviceToken, PushType.JPUSH_TYPE)
                        service.deviceBind(form)
                    }
                }.observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { res ->
                        XLog.info("绑定设备，结果：${res.data.isValue}")
                    }
                    onError { e, _ ->
                        XLog.error("绑定设备出错，", e)
                        jpushBindOldRetry()
                    }
                }
        } else {
            XLog.error("没有极光推送模块")
        }
    }

    /**
     * 适配老版本 重试一下
     */
    private fun jpushBindOldRetry() {
        val service = getJPushControlService()
        if (service != null) {
            val deviceToken =
                O2SDKManager.instance().prefs().getString(O2.PRE_PUSH_JPUSH_DEVICE_ID_KEY, "") ?: ""
            val form = JPushDeviceForm(deviceToken, PushType.JPUSH_TYPE)
            service.deviceBind(form).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { res ->
                        XLog.info("老版接口： 绑定设备，结果：${res.data.isValue}")
                        XLog.info("老版接口： 绑定设备，message：${res.message}")
                    }
                    onError { e, _ ->
                        XLog.error("老版接口： 绑定设备出错，", e)
                    }
                }
        }
    }

    /**
     * 解除绑定设备 内部直连版本使用
     */
    override fun jPushUnBindDevice() {
        XLog.info("解除绑定设备号")
        val service = getJPushControlService()
        if (service != null) {
            service.deviceConfigPushType().subscribeOn(Schedulers.io())
                .flatMap { res ->
                    val config = res.data
                    if (config != null && !TextUtils.isEmpty(config.pushType)) {
                        val deviceToken = if (config.pushType == PushType.HUAWEI_TYPE) {
                            O2SDKManager.instance().prefs()
                                .getString(O2.PRE_PUSH_HUAWEI_DEVICE_ID_KEY, "") ?: ""
                        } else {
                            O2SDKManager.instance().prefs()
                                .getString(O2.PRE_PUSH_JPUSH_DEVICE_ID_KEY, "") ?: ""
                        }
                        service.deviceUnBindNew(deviceToken, config.pushType)
                    } else {
                        val deviceToken = O2SDKManager.instance().prefs()
                            .getString(O2.PRE_PUSH_JPUSH_DEVICE_ID_KEY, "") ?: ""
                        service.deviceUnBindNew(deviceToken, PushType.JPUSH_TYPE)
                    }
                }.observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { res ->
                        XLog.info("解除绑定，结果：${res.data.isValue}")
                    }
                    onError { e, _ ->
                        XLog.error("解除绑定出错，", e)
                        jpushUnbindOldRetry()
                    }
                }


        } else {
            XLog.error("没有极光推送模块")
        }
    }

    private fun jpushUnbindOldRetry() {
        val service = getJPushControlService()
        if (service != null) {
            val deviceToken =
                O2SDKManager.instance().prefs().getString(O2.PRE_PUSH_JPUSH_DEVICE_ID_KEY, "") ?: ""
            service.deviceUnBind(deviceToken).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .o2Subscribe {
                    onNext { res ->
                        XLog.info("解除绑定，结果：${res.data.isValue}")
                    }
                    onError { e, _ ->
                        XLog.error("解除绑定出错，", e)
                    }
                }
        }
    }


    override fun getTaskNumber(context: Context?, tv: TextView, tvTag: String) {
        XLog.debug("getTaskNumber 。。。。。。。。。。。。。。")
        val service = getProcessAssembleSurfaceServiceAPI(context)
        service?.countWithPerson(O2SDKManager.instance().distinguishedName)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())?.o2Subscribe {
                onNext { res ->
                    XLog.debug("getTaskNumber 。。。。。。。。。。。。。。1111")
                    if (res.data != null) {
                        val taskNumber = res.data.task ?: 0
                        if (taskNumber > 0 && tv.tag != null) {
                            val tag = tv.tag as String
                            if (tag == tvTag) {
                                tv.text = "$taskNumber"
                                tv.visible()
                            }
                        }
                    }
                }
                onError { e, isNetworkError ->
                    XLog.error("", e)
                }
            }
    }

    override fun getPortalNumber(context: Context?, tv: TextView, portalId: String) {
        XLog.debug("getPortalNumber ..................")
        getPortalAssembleSurfaceService(context)
            ?.cornerMarkNumber(portalId)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.o2Subscribe {
                onNext {
                    if (it.data != null && it.data.count > 0 && tv.tag != null) {
                        val tag = tv.tag as String
                        if (tag == portalId) {
                            tv.text = "${it.data.count}"
                            tv.visible()
                        }
                    }
                }
                onError { e, _ ->
                    XLog.error("", e)
                }
            }
    }

    override fun getReadNumber(context: Context?, tv: TextView, tvTag: String) {
        XLog.debug("getReadNumber 。。。。。。。。。。。。。。")
        val service = getProcessAssembleSurfaceServiceAPI(context)
        service?.countWithPerson(O2SDKManager.instance().distinguishedName)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())?.o2Subscribe {
                onNext { res ->
                    XLog.debug("getReadNumber 。。。。。。。。。。。1111。。。")
                    if (res.data != null) {
                        val readNumber = res.data.read ?: 0
                        if (readNumber > 0 && tv.tag != null) {
                            val tag = tv.tag as String
                            if (tag == tvTag) {
                                tv.text = "$readNumber"
                                tv.visible()
                            }
                        }
                    }
                }
                onError { e, isNetworkError ->
                    XLog.error("", e)
                }
            }
    }

    /**
     * 图灵 v1 服务
     */
    fun getTuling123Service(context: Context?): Tuling123Service? {
        return try {
            RetrofitClient.instance().tuling123Service()
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "AI查询模块异常，请联系管理员！")
            }
            null
        }
    }

    /**
     * 人脸识别服务
     */
    fun getFaceppService(baseUrl: String, context: Context?): FaceppApiService? {
        return try {
            RetrofitClient.instance().faceppApiService(baseUrl)
        } catch (e: Exception) {
            XLog.error("", e)
            if (context != null) {
                XToast.toastLong(context, "人脸识别模块异常，请联系管理员！")
            }
            null
        }
    }
}