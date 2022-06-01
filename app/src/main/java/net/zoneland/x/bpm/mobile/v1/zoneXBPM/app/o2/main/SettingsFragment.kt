package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main

import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.fragment_main_settings.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPViewPagerFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.about.AboutActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.login.LoginActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.logs.LogsActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.my.MyInfoActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.notice.NoticeSettingActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.security.AccountSecurityActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.skin.SkinManagerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.BitmapUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.HttpCacheUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderOptions
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.AndroidShareDialog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2AlertIconEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport


/**
 * Created by fancy on 2017/6/9.
 */


class SettingsFragment : BaseMVPViewPagerFragment<SettingsContract.View, SettingsContract.Presenter>(), SettingsContract.View, View.OnClickListener {

    override var mPresenter: SettingsContract.Presenter = SettingsPresenter()

    override fun lazyLoad() {

    }

    override fun layoutResId(): Int {
        return R.layout.fragment_main_settings
    }

    override fun initUI() {
        rl_settings_button_my_info.setOnClickListener(this)
        setting_button_account_security_id.setOnClickListener(this)
        setting_button_skin.setOnClickListener(this)
        setting_button_about_id.setOnClickListener(this)
        setting_button_remind_setting_id.setOnClickListener(this)
        setting_button_logs_id.setOnClickListener(this)
        setting_button_common_set_id.setOnClickListener(this)
        setting_button_common_set_id.setOnLongClickListener {
            XLog.info("长按清除缓存按钮！！！")
            longClickClearCache()
            true
        }
        if (BuildConfig.InnerServer) {
            id_setting_button_customer_service_split.gone()
            setting_button_customer_service_id.gone()
        }else {
            id_setting_button_customer_service_split.visible()
            setting_button_customer_service_id.visible()
            setting_button_customer_service_id.setOnClickListener(this)
        }

//        setting_button_feedback_id.setOnClickListener(this)
        myInfo_logout_btn_id.setOnClickListener(this)


        //用户头像等信息
        val url = APIAddressHelper.instance().getPersonAvatarUrlWithId(O2SDKManager.instance().distinguishedName)
        O2ImageLoaderManager.instance().showImage(image_settings_my_avatar, url, O2ImageLoaderOptions(isSkipCache = true))
        tv_settings_my_name.text = O2SDKManager.instance().cName
        tv_settings_my_sign.text = O2SDKManager.instance().cSignature


        val path = O2CustomStyle.setupAboutImagePath(activity)
        if (!TextUtils.isEmpty(path)) {
            BitmapUtil.setImageFromFile(path!!, setting_image_about_icon)
        }

    }

    val shareDialog: AndroidShareDialog by lazy { AndroidShareDialog(activity, O2.O2_DOWNLOAD_URL, null) }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rl_settings_button_my_info -> activity?.go<MyInfoActivity>()
            R.id.setting_button_account_security_id -> activity?.go<AccountSecurityActivity>()
            R.id.setting_button_skin -> activity?.go<SkinManagerActivity>()
            R.id.setting_button_remind_setting_id -> activity?.go<NoticeSettingActivity>()
            R.id.setting_button_common_set_id -> {
                O2DialogSupport.openConfirmDialog(activity, getString(R.string.dialog_msg_clean_cache), {
                    HttpCacheUtil.clearCache(activity, 0)
                }, icon = O2AlertIconEnum.CLEAR)
            }
            R.id.setting_button_logs_id -> activity?.go<LogsActivity>()
            R.id.setting_button_customer_service_id -> shareDialog.show()
//            R.id.setting_button_feedback_id -> startFeedBack()
            R.id.setting_button_about_id -> activity?.go<AboutActivity>()
            R.id.myInfo_logout_btn_id -> logout()


        }
    }

    override fun logoutSuccess() {
        logoutThenJump2Login()
    }

    override fun logoutFail() {
        logoutThenJump2Login()
    }

    override fun cleanOver() {
        XToast.toastShort(activity, "clean Ok!")
    }

    private fun logout() {
        O2DialogSupport.openConfirmDialog(activity, getString(R.string.dialog_msg_logout), {
            mPresenter.jPushUnBindDevice()
            if (activity is MainActivity) {
                (activity as MainActivity).webSocketClose()
            }
            mPresenter.logout()
        })
    }

    private fun longClickClearCache() {
        O2DialogSupport.openConfirmDialog(activity, "确认要深度清除缓存吗，深度清除后，请清除当前app进程重新再打开？", {
            HttpCacheUtil.clearCache(activity, 0)
            // 删除本地sp数据
            O2SDKManager.instance().prefs().edit {
                clear()
            }
            logoutThenJump2Login()
        }, icon = O2AlertIconEnum.CLEAR)
    }

//    private fun startFeedBack() {
//        PermissionRequester(activity)
//                .request(Manifest.permission.RECORD_AUDIO)
//                .o2Subscribe {
//                    onNext { (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
//                        XLog.info("granted:$granted , shouldShowRequest:$shouldShowRequestPermissionRationale, denied:$deniedPermissions")
//                        if (!granted) {
//                            O2DialogSupport.openAlertDialog(activity, "需要麦克风权限才能进行语音反馈!")
//                        } else {
//                            PgyerDialog.setDialogTitleBackgroundColor("#FB4747")
//                            PgyFeedback.getInstance().showDialog(activity)
//                        }
//                    }
//                    onError { e, _ ->
//                        XLog.error("麦克风权限验证异常", e)
//                    }
//                }
//    }

    private fun logoutThenJump2Login() {
        O2SDKManager.instance().logoutCleanCurrentPerson()
        activity?.goAndClearBefore<LoginActivity>()
    }
}