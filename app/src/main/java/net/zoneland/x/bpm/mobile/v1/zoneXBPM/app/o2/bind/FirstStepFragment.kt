package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.bind

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.internal.ContextUtils
import com.google.gson.reflect.TypeToken
import com.xiaomi.push.it
import kotlinx.android.synthetic.main.fragment_fluid_login_phone.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.login.LoginActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main.MainActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.O2WebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.RetrofitClient
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.APIDistributeData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.AuthenticationInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.CollectUnitData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.edit
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.goThenKill
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.hideSoftInput
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CountDownButtonHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport

/**
 * Created by fancy on 2017/6/8.
 */


class FirstStepFragment : BaseMVPFragment<FirstStepContract.View, FirstStepContract.Presenter>(), FirstStepContract.View, View.OnClickListener {

    var phone = ""
    var code = ""
    val countDownHelper: CountDownButtonHelper by lazy { CountDownButtonHelper(button_login_phone_code, getString(R.string.login_button_code), 60, 1) }
    private var isDemoAccount = false // 上架测试账号

    override var mPresenter: FirstStepContract.Presenter = FirstStepPresenter()
    override fun layoutResId(): Int = R.layout.fragment_fluid_login_phone

    override fun initUI() {
        countDownHelper.setOnFinishListener { XLog.debug("CountDownButtonHelper, finish.................."); }
        button_login_phone_next.setOnClickListener(this)
        button_login_phone_code.setOnClickListener(this)
        tv_secret_login.setOnClickListener(this)
        tv_user_service_login.setOnClickListener(this)
        edit_login_phone.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view_bind_phone_step_one_number_bottom.setBackgroundColor(FancySkinManager.instance().getColor(activity!!, R.color.z_color_input_line_focus))
                image_login_phone_icon.setImageDrawable(FancySkinManager.instance().getDrawable(activity!!, R.mipmap.icon_phone_focus))
            } else {
                view_bind_phone_step_one_number_bottom.setBackgroundColor(FancySkinManager.instance().getColor(activity!!, R.color.z_color_input_line_blur))
                image_login_phone_icon.setImageDrawable(FancySkinManager.instance().getDrawable(activity!!, R.mipmap.icon_phone_normal))
            }
        }
        edit_login_code.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view_bind_phone_step_one_code_bottom.setBackgroundColor(FancySkinManager.instance().getColor(activity!!, R.color.z_color_input_line_focus))
                image_login_phone_code_icon.setImageDrawable(FancySkinManager.instance().getDrawable(activity!!, R.mipmap.icon_verification_code_focus))
            } else {
                view_bind_phone_step_one_code_bottom.setBackgroundColor(FancySkinManager.instance().getColor(activity!!, R.color.z_color_input_line_blur))
                image_login_phone_code_icon.setImageDrawable(FancySkinManager.instance().getDrawable(activity!!, R.mipmap.icon_verification_code_normal))
            }
        }
        // 华为需要同意协议
        if (AndroidUtils.isHuaweiChannel(activity)) {
            ll_fluid_login_agree_bar.visible()
            openPrivacyDialog()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        countDownHelper.destroy()
    }

    override fun receiveUnitList(list: List<CollectUnitData>) {
        if (list.isEmpty()) {
            XToast.toastShort(activity, getString(R.string.message_phone_no_bind))
            return
        }
        if (list.size == 1) {
            autoBind(list[0])
        }else {
            val json = O2SDKManager.instance().gson.toJson(list, object : TypeToken<List<CollectUnitData>>(){}.type)
            redirectToSecondStep(json)
        }
    }

    override fun receiveUnitFail() {
//        XToast.toastLong(activity, getString(R.string.message_get_o2collect_unit_list_fail))
        O2DialogSupport.openConfirmDialog(activity, getString(R.string.dialog_msg_get_o2collect_unit_list_fail), { _ ->
            bind2SampleServer()
        }, positiveText = getString(R.string.dialog_title_sample_server))
    }

    override fun bindSuccess(distributeData: APIDistributeData) {
        APIAddressHelper.instance().setDistributeData(distributeData)
        O2SDKManager.instance().prefs().edit {
            putBoolean(O2.PRE_DEMO_O2_KEY, false)
        }
        gotoLogin()
    }

    override fun bindFail() {
        hideLoadingDialog()
//        XToast.toastShort(activity, "绑定服务器失败！")
        O2DialogSupport.openConfirmDialog(activity, getString(R.string.dialog_msg_bind_to_server_fail), { _ ->
            bind2SampleServer()
        }, positiveText = getString(R.string.dialog_title_sample_server))
    }


    private fun openPrivacyDialog() {
        activity?.let {
            val dialog = O2DialogSupport.openCustomViewDialog(it, getString(R.string.user_privacy_dialog_title),getString(R.string.user_privacy_dialog_agree_btn), getString(R.string.user_privacy_dialog_disagree_btn), R.layout.dialog_user_privacy_secret, { _ ->
                radio_fluid_login_agree.isChecked = true
            }, { _ ->
                XLog.error("不同意隐私政策！！！！！")
            })
            val f = dialog.findViewById<TextView>(R.id.tv_dialog_user_privacy_second)
            val style = SpannableStringBuilder()
            //设置文字
            val text = getString(R.string.user_privacy_dialog_2)
            style.append(text)
            // 《用户协议》 点击
            val clickableSpan = object :ClickableSpan() {
                override fun onClick(widget: View) {
                    XLog.debug("点击了 用户协议")
                    openUserPrivacy()
                }
            }
            style.setSpan(clickableSpan, 10, 16, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            val foregroundColorSpan =  ForegroundColorSpan(FancySkinManager.instance().getColor(it, R.color.z_color_primary_blue))
            style.setSpan(foregroundColorSpan, 10, 16, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            // 《隐私政策》
            val clickableSpan2 = object :ClickableSpan() {
                override fun onClick(widget: View) {
                    XLog.debug("点击了 隐私政策")
                    openSecret()
                }
            }
            style.setSpan(clickableSpan2, 17, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            style.setSpan(foregroundColorSpan, 17, 23, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            f.text = style
            f.movementMethod = LinkMovementMethod.getInstance()
        }

    }

    /**
     * 绑定到sample服务器
     */
    private fun bind2SampleServer() {
        val unit = CollectUnitData()
        unit.id = "61a4d035-81ee-44a6-af3b-ab3d374ee24d"
        unit.name = "演示站点"
        unit.pinyin = "yanshizhandian"
        unit.pinyinInitial = "yszd"
        unit.centerHost = "sample.o2oa.net"
        unit.centerPort = 443
        unit.centerContext = "/x_program_center"
        unit.httpProtocol = "https"
        //绑定成功写入本地存储
        O2SDKManager.instance().bindUnit(unit, phone, (activity as BindPhoneActivity).loadDeviceId())
        APIAddressHelper.instance().setHttpProtocol(unit.httpProtocol)
        val url = APIAddressHelper.instance().getCenterUrl(unit.centerHost,
                unit.centerContext, unit.centerPort)
        XLog.debug(url)
        showLoadingDialog()
        mPresenter.getDistribute(url, unit.centerHost)
    }

    override fun distribute(distributeData: APIDistributeData) {
        hideLoadingDialog()
        APIAddressHelper.instance().setDistributeData(distributeData)
        O2SDKManager.instance().prefs().edit {
            putBoolean(O2.PRE_DEMO_O2_KEY, true)
        }
        // 上架测试账号 直接登录
        if (isDemoAccount && phone == "13912345678") {
            mPresenter.loginWithPwd(phone, "345678") // 自动登录到演示服务器
        } else {
            activity?.goThenKill<LoginActivity>()
        }
    }

    override fun err(msg: String) {
        hideLoadingDialog()
        XToast.toastShort(activity, msg)
    }

    override fun noDeviceId() {
        hideLoadingDialog()
        XToast.toastShort(activity, getString(R.string.message_can_not_get_device_number))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_login_phone_next -> {
                if (CheckButtonDoubleClick.isFastDoubleClick(R.id.button_login_phone_next)) {
                    XLog.debug("重复点了。。。。。。。。。。。。")
                    return
                }

                // 必须同意协议
                if (AndroidUtils.isHuaweiChannel(activity)) {
                    val isCheck = radio_fluid_login_agree.isChecked
                    if (!isCheck) {
                        XToast.toastShort(activity, getString(R.string.agree_login_privacy_alert_message))
                        return
                    }
                }

                val phone = edit_login_phone.text.toString()
                val code = edit_login_code.text.toString()
                if (TextUtils.isEmpty(phone)) {
                    XToast.toastShort(activity, getString(R.string.message_need_input_right_cellphone))
                    return
                }
                if (!StringUtil.isPhoneWithHKandMACAO(phone)) {
                    XToast.toastShort(activity, getString(R.string.message_need_input_right_cellphone))
                    return
                }
                if (TextUtils.isEmpty(code)) {
                    XToast.toastShort(activity, getString(R.string.message_code_can_not_empty))
                    return
                }
                activity?.hideSoftInput()
                this.phone = phone
                this.code = code
                // 应用上架用的测试账户 手机号码：13912345678 验证码：5678
                if ("13912345678" == phone && "5678" == code) {
                    isDemoAccount = true
                    bind2SampleServer()
                } else {
                    mPresenter.getUnitList(phone, code)
                }
            }
            R.id.button_login_phone_code -> {
                if (CheckButtonDoubleClick.isFastDoubleClick(R.id.button_login_phone_code)) {
                    XLog.debug("重复点了。。。。。。。。。。。。")
                    return
                }
                val phone = edit_login_phone.text.toString()
                if (TextUtils.isEmpty(phone)) {
                    XToast.toastShort(activity, getString(R.string.message_need_input_right_cellphone))
                    return
                }
                if (!StringUtil.isPhoneWithHKandMACAO(phone)) {
                    XToast.toastShort(activity, getString(R.string.message_need_input_right_cellphone))
                    return
                }
                // 发送验证码
                mPresenter.getVerificationCode(phone)
                countDownHelper.destroy()
                countDownHelper.start()

                //焦点跳转到验证码上面
                edit_login_code.setText("")//先清空
                edit_login_code.isFocusable = true
                edit_login_code.isFocusableInTouchMode = true
                edit_login_code.requestFocus()
                edit_login_code.requestFocusFromTouch()
            }
            R.id.tv_secret_login -> {
                openSecret()
            }
            R.id.tv_user_service_login -> {
               openUserPrivacy()
            }
            else -> XLog.error("no implements this view ,id:${v?.id}")
        }

    }


    override fun loginSuccess(data: AuthenticationInfoJson) {
        O2SDKManager.instance().setCurrentPersonData(data)
        hideLoadingDialog()
        (activity as BindPhoneActivity).startInstallCustomStyle(true)
    }

    override fun loginFail() {
        hideLoadingDialog()
        //自动登陆失败 跳转过去手动登陆
        (activity as BindPhoneActivity).startInstallCustomStyle(false, phone)
    }

    private fun autoBind(collectUnitData: CollectUnitData) {
        XLog.debug("autoBind......${collectUnitData.name}")
        showLoadingDialog()
        //更新http协议
        RetrofitClient.instance().setO2ServerHttpProtocol(collectUnitData.httpProtocol)
        APIAddressHelper.instance().setHttpProtocol(collectUnitData.httpProtocol)
        mPresenter.bindDevice((activity as BindPhoneActivity).loadDeviceId(), phone, code, collectUnitData)
    }

    private fun redirectToSecondStep(json: String?) {
        if (TextUtils.isEmpty(json)){
            XToast.toastShort(activity, getString(R.string.message_bind_list_data_error))
        }else {
            val fragment = SecondStepFragment.newInstance(json!!, phone, code)
            (activity as BindPhoneActivity).addFragment(fragment)
        }
    }

    private fun openSecret() {
        activity?.let {
            O2WebViewActivity.openWebView(it, getString(R.string.secret), "https://www.o2oa.net/secret.html")
        }
    }
    private fun openUserPrivacy() {
        activity?.let {
            O2WebViewActivity.openWebView(it, getString(R.string.user_service), "https://www.o2oa.net/userService.html")
        }
    }
    private fun gotoLogin() {
        mPresenter.login(phone, code)//@date 2018-03-20 绑定成功 直接登陆
    }





}