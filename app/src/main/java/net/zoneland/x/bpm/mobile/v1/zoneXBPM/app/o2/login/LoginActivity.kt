package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.login

import android.content.Context
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.bind.BindPhoneActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.main.MainActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.CaptchaImgData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.LoginModeData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.LoginWithCaptchaForm
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.AuthenticationInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.biometric.BioConstants
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.biometric.BiometryManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.biometric.OnBiometryAuthCallback
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.goThenKill
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderOptions
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CountDownButtonHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import java.io.IOException


/**
 * Created by fancy o7/6/8.
 */

class LoginActivity: BaseMVPActivity<LoginContract.View, LoginContract.Presenter>(),
        LoginContract.View, View.OnClickListener {

    override var mPresenter: LoginContract.Presenter = LoginPresenter()

    override fun beforeSetContentView() {
        super.beforeSetContentView()
        setTheme(R.style.XBPMTheme_NoActionBar)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)//去掉信息栏
    }

    companion object {
        const val REQUEST_PHONE_KEY = "REQUEST_PHONE_KEY"
        fun startBundleData(phone: String): Bundle {
            val bundle = Bundle()
            bundle.putString(REQUEST_PHONE_KEY, phone)
            return bundle
        }
    }

    private val countDownHelper: CountDownButtonHelper by lazy {
        CountDownButtonHelper(button_login_phone_code,
                getString(R.string.login_button_code),
                60,
                1)
    }

    //翻转动画
//    private val scale0 by lazy {
//        ScaleAnimation(1f,0f,1f,1f,
//            Animation.RELATIVE_TO_PARENT,0.5f,
//                Animation.RELATIVE_TO_PARENT,0.5f)
//    }
//    private val scale1 by lazy {
//        ScaleAnimation(0f,1f,1f,1f,
//                Animation.RELATIVE_TO_PARENT,0.5f,
//                Animation.RELATIVE_TO_PARENT,0.5f)
//    }
    private var receivePhone = ""

    //播放声音
    private var mediaPlayer: MediaPlayer? = null
    private var playBeep: Boolean = false

    private var loginType = 0 // 0默认的用户名验证码登录 1用户名密码登录
    private var canBioAuth = false //是否有指纹认证

    //验证码
    private var useCaptcha = true
    private var captcha : CaptchaImgData? = null



    override fun afterSetContentView(savedInstanceState: Bundle?) {
        receivePhone = intent.extras?.getString(REQUEST_PHONE_KEY) ?: ""
        setDefaultLogo()
        login_edit_username_id.setText(receivePhone)
        tv_login_copyright.text = getString(R.string.copy_right).plus(" ")
                .plus(DateHelper.nowByFormate("yyyy")).plus(" ")
                .plus(getString(R.string.app_name_about)).plus(" ")
                .plus(getString(R.string.reserved))
        login_edit_username_id.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                image_login_icon_name.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_user_focus))
                view_login_username_bottom.setBackgroundColor(FancySkinManager.instance().getColor(this, R.color.z_color_input_line_focus))
            }else {
                image_login_icon_name.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_user_normal))
                view_login_username_bottom.setBackgroundColor(FancySkinManager.instance().getColor(this, R.color.z_color_input_line_blur))
            }
        }
        login_edit_password_id.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                image_login_icon_password.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_verification_code_focus))
                view_login_password_bottom.setBackgroundColor(FancySkinManager.instance().getColor(this, R.color.z_color_input_line_focus))
            }else {
                image_login_icon_password.setImageDrawable(FancySkinManager.instance().getDrawable(this, R.mipmap.icon_verification_code_normal))
                view_login_password_bottom.setBackgroundColor(FancySkinManager.instance().getColor(this, R.color.z_color_input_line_blur))
            }
        }
        edit_login_captcha_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view_login_captcha_input_bottom.setBackgroundColor(FancySkinManager.instance().getColor(this, R.color.z_color_input_line_focus))
            }else {
                view_login_captcha_input_bottom.setBackgroundColor(FancySkinManager.instance().getColor(this, R.color.z_color_input_line_blur))
            }
        }

        btn_login_submit.setOnClickListener(this)
        btn_bio_auth_login.setOnClickListener(this)
        tv_user_fallback_btn.setOnClickListener(this)
        tv_bioauth_btn.setOnClickListener(this)
        image_login_captcha.setOnClickListener(this)

        //是否开启了指纹识别登录
        checkBioAuthLogin()
        if (BuildConfig.InnerServer) {
            loginType = 1 // 内部连接 默认用密码登录
            login_edit_password_id.setHint(R.string.activity_login_password)
            login_edit_password_id.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button_login_phone_code.gone()
            tv_rebind_btn.gone()
            tv_bioauth_btn.visible()
            ll_login_captcha.visible()
        }else {
            tv_bioauth_btn.visible()
            login_edit_password_id.setHint(R.string.login_code)
//            login_edit_password_id.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            login_edit_password_id.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD // 不限制只有数字
            button_login_phone_code.visible()
            button_login_phone_code.setOnClickListener(this)
            tv_rebind_btn.visible()
            tv_rebind_btn.setOnClickListener(this)
            //不需要图片验证码
            ll_login_captcha.gone()
        }

        //获取加密key
        mPresenter.getRSAPublicKey()
        //获取图片验证码
        mPresenter.getCaptcha()
        // 登录模式检查 是否有验证码登录 短信登录这些
        mPresenter.getLoginMode()
    }


    override fun layoutResId(): Int = R.layout.activity_login


    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        event?.let {
            if (it.keyCode == KeyEvent.KEYCODE_ENTER && it.action == KeyEvent.ACTION_DOWN) {
                if (login_edit_password_id.hasFocus()) {
                    /*隐藏软键盘*/
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (inputMethodManager.isActive) {
                        inputMethodManager.hideSoftInputFromWindow(this@LoginActivity.currentFocus!!.windowToken, 0)
                    }
                    submitLogin()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        //是否是sample服务器
        checkShowSampleAlert()
        //清除用户名密码
        login_edit_username_id.setText("")
        login_edit_password_id.setText("")
        edit_login_captcha_input.setText("")

        playBeep = true
        val audioService = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false
        }
        initBeepSound()
    }

    override fun onStop() {
        super.onStop()

        //Activity反劫持检测工具
        Thread(Runnable { // 白名单
            val safe = AntiHijackingUtil.checkActivity(applicationContext)
            // 系统桌面
            val isHome = AntiHijackingUtil.isHome(applicationContext)
            // 锁屏操作
            val isReflectScreen = AntiHijackingUtil.isReflectScreen(applicationContext)
            // 判断程序是否当前显示
            if (!safe && !isHome && !isReflectScreen) {
                Looper.prepare()
                XToast.toastLong(R.string.activity_safe_warning)
//                Toast.makeText(applicationContext, R.string.activity_safe_warning,
//                        Toast.LENGTH_LONG).show()
                Looper.loop()
            }
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownHelper.destroy()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.tv_user_fallback_btn -> {
                userFallback()
            }
            R.id.tv_bioauth_btn -> {
                showChangeLoginTypeMenu()
            }
            R.id.btn_bio_auth_login -> {
                bioAuthLogin()
            }
            R.id.btn_login_submit -> {
                if (CheckButtonDoubleClick.isFastDoubleClick(R.id.btn_login_submit)) {
                    XLog.debug("重复点了。。。。。。。。。。。。")
                    return
                }
                submitLogin()
            }
            R.id.button_login_phone_code -> {
                getVerificationCode()
            }
            R.id.tv_rebind_btn -> {
                O2DialogSupport.openConfirmDialog(this@LoginActivity, getString(R.string.dialog_msg_rebind_phone), { _ ->
                    reBindService()
                })
            }
            R.id.image_login_captcha -> {
                showLoadingDialog()
                mPresenter.getCaptcha()
            }
        }
    }

    private fun checkShowSampleAlert() {
        val unit = O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HOST_KEY, "")
        if (!TextUtils.isEmpty(unit) && unit == "sample.o2oa.net") {
            login_sample_alert.visible()
        }
    }

    //切换登录方式
    private fun showChangeLoginTypeMenu() {
        val listItems = ArrayList<String>()
        val title = if(loginType == 0) getString(R.string.login_type_password) else getString(R.string.login_type_code)
        listItems.add(title)
        if (canBioAuth) {
            listItems.add(getString(R.string.login_type_fingerprint))
        }
        BottomSheetMenu(this)
                .setTitle(getString(R.string.login_change_type))
                .setItems(listItems, ContextCompat.getColor(this, R.color.z_color_text_primary)) { index ->
                    if (index == 0) {
                        changeLoginType()
                    }else if (index == 1) {
                        showBiometryAuthUI()
                    }
                }
                .show()


    }
    private  fun changeLoginType() {
        if (loginType == 0) {
            if (useCaptcha) {
                ll_login_captcha.visible()
            }else {
                ll_login_captcha.gone()
            }
            login_edit_password_id.setHint(R.string.activity_login_password)
            login_edit_password_id.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button_login_phone_code.gone()
            loginType = 1
        }else {
            ll_login_captcha.gone()
            login_edit_password_id.setHint(R.string.login_code)
//            login_edit_password_id.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            login_edit_password_id.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD // 不限制只有数字
            button_login_phone_code.visible()
            button_login_phone_code.setOnClickListener(this)
            loginType = 0
        }
    }


    override fun loginMode(mode: LoginModeData?) {
        if (mode != null) {
            XLog.debug(mode.toString())
            // 图片验证码
            useCaptcha = mode.captchaLogin
            if (useCaptcha) {
                ll_login_captcha.visible()
            }else {
                ll_login_captcha.gone()
            }
            // 短信验证码登录
            if (mode.codeLogin) {
                loginType = 1
                changeLoginType()
            } else { // 用户密码登录
                loginType = 0
                tv_bioauth_btn.gone()
                changeLoginType()
            }
        } else { // 用户密码登录
            loginType = 0
            tv_bioauth_btn.gone()
            changeLoginType()
            // loginMode返回null 一般是服务器连接不上 alert提示
            O2DialogSupport.openAlertDialog(this, "服务器连接失败，请确认app是否连接服务器的地址是否正确！")
        }
    }

    override fun showCaptcha(data: CaptchaImgData) {
        hideLoadingDialog()
        captcha = data
         val stream = Base64ImageUtil.generateBase642Inputstream(data.image)
        if (stream != null) {
            image_login_captcha.setImageBitmap(BitmapFactory.decodeStream(stream))
        }
    }

    override fun getCaptchaError(err: String) {
        hideLoadingDialog()
        //todo 图片验证码没有
        XLog.error(err)
    }

    override fun loginSuccess(data: AuthenticationInfoJson) {
        if (login_main_biometry.visibility == View.VISIBLE) {
            playBeepSound()
        }
        hideLoadingDialog()
        O2SDKManager.instance().setCurrentPersonData(data)
        goThenKill<MainActivity>()
    }

    override fun loginFail() {
       // XToast.toastShort(this, getString(R.string.message_login_fail))
        if (useCaptcha && ll_login_captcha.visibility == View.VISIBLE) { // 如果有验证码 刷新
            mPresenter.getCaptcha()
        } else {
            hideLoadingDialog()
        }
    }

    override fun getCodeError() {
        XToast.toastShort(this, getString(R.string.message_login_validate_code_fail))
    }

    private fun submitLogin() {
        val credential = login_edit_username_id.text.toString()
        val code = login_edit_password_id.text.toString()
        if (TextUtils.isEmpty(credential)) {
            XToast.toastShort(this, getString(R.string.message_login_username_can_not_empty))
            return
        }
        if (TextUtils.isEmpty(code)) {
            val label = if(BuildConfig.InnerServer){
                getString(R.string.activity_login_password)
            }else {
                if (loginType == 0) {
                    getString(R.string.login_code)
                }else {
                    getString(R.string.activity_login_password)
                }
            }
            XToast.toastShort(this, getString(R.string.message_login_something_can_not_empty, label))
            return
        }
        if (useCaptcha) {
            if (loginType == 0) {
                showLoadingDialog()
                mPresenter.login(credential, code)
            }else {
//                    mPresenter.loginByPassword(credential, code)
                val captchaCode = edit_login_captcha_input.text.toString()
                if (TextUtils.isEmpty(captchaCode)) {
                    XToast.toastShort(this, getString(R.string.message_login_image_code_can_not_empty))
                    return
                }
                if (captcha == null) {
                    XToast.toastShort(this, getString(R.string.message_login_image_code_can_not_empty))
                    return
                }
                val form = LoginWithCaptchaForm()
                form.credential = credential
                form.password = code
                form.captcha = captcha!!.id
                form.captchaAnswer = captchaCode
                showLoadingDialog()
                mPresenter.loginWithCaptcha(form)
            }
        }else {
            if (loginType == 0) {
                showLoadingDialog()
                mPresenter.login(credential, code)
            }else {
//                    mPresenter.loginByPassword(credential, code)
                val form = LoginWithCaptchaForm()
                form.credential = credential
                form.password = code
                showLoadingDialog()
                mPresenter.loginWithCaptcha(form)
            }
        }

    }


    private fun getVerificationCode() {
        val credential = login_edit_username_id.text.toString()
        if (TextUtils.isEmpty(credential)) {
            XToast.toastShort(this, getString(R.string.message_login_input_username))
            return
        }
        // 发送验证码
        mPresenter.getVerificationCode(credential)
        countDownHelper.start()
        //焦点跳转到验证码上面
        login_edit_password_id.isFocusable = true
        login_edit_password_id.isFocusableInTouchMode = true
        login_edit_password_id.requestFocus()
        login_edit_password_id.requestFocusFromTouch()
    }

    private fun reBindService() {
        O2SDKManager.instance().clearBindUnit()
        goThenKill<BindPhoneActivity>()
    }


    /**
     * 是否开启了指纹识别登录
     */
    private fun checkBioAuthLogin() {
        val bioAuthUser = O2SDKManager.instance().prefs().getString(BioConstants.O2_bio_auth_user_id_prefs_key, "") ?: ""
        var userId = ""
        //判断是否当前绑定的服务器的
        if (bioAuthUser.isNotBlank()) {
            val array = bioAuthUser.split("^^")
            if (array.isNotEmpty() && array.size == 2) {
                val unitId = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_UNIT_ID_KEY, "") ?: ""
                if (array[0] == unitId) {
                    userId = array[1]
                }
            }
        }
        if (userId.isNotEmpty()) {
            canBioAuth = true
            showBiometryAuthUI()
        }else {
            login_form_scroll_id.visible()
            login_main_biometry.gone()
        }
    }

    /**
     * 其他方式登录
     */
    private fun userFallback() {
        login_form_scroll_id.visible()
        login_main_biometry.gone()
    }

    /**
     * 指纹识别登录
     */
    private fun showBiometryAuthUI() {
        login_form_scroll_id.gone()
        login_main_biometry.visible()
    }

    private val bioManager: BiometryManager by lazy { BiometryManager(this) }
    /**
     * 指纹识别登录
     */
    private fun bioAuthLogin() {
        if(!bioManager.isBiometricPromptEnable()){
            XToast.toastShort(this, getString(R.string.message_login_fingerprint_disabled))
        }else {


            bioManager.authenticate(object : OnBiometryAuthCallback {
                override fun onUseFallBack() {
                    XLog.error("点击了《其他方式》按钮。。。。。")
                    userFallback()
                }

                override fun onSucceeded() {
                    showLoadingDialog()
                    val bioAuthUser = O2SDKManager.instance().prefs().getString(BioConstants.O2_bio_auth_user_id_prefs_key, "")
                            ?: ""
                    var userId = ""
                    //判断是否当前绑定的服务器的
                    if (bioAuthUser.isNotBlank()) {
                        val array = bioAuthUser.split("^^")
                        if (array.isNotEmpty() && array.size == 2) {
                            val unitId = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_UNIT_ID_KEY, "")
                                    ?: ""
                            if (array[0] == unitId) {
                                userId = array[1]
                            }
                        }
                    }
                    if (userId.isBlank()) {
                        XLog.error("用户名为空 无法登录。。。。")
                        XToast.toastShort(this@LoginActivity, getString(R.string.message_login_server_error))
                    } else {
                        mPresenter.ssoLogin(userId)
                    }
                }

                override fun onFailed() {
                    XLog.error("指纹识别验证失败了。。。。。")
                    //XToast.toastShort(this@LoginActivity, "验证失败")
                }

                override fun onError(code: Int, reason: String) {
                    XLog.error("指纹识别验证出错，code:$code , reason:$reason")
                    //XToast.toastShort(this@LoginActivity, "验证失败")
                }

                override fun onCancel() {
                    XLog.info("指纹识别取消了。。。。。")
                }

            })
        }
    }

    /**
     * 设置logo
     */
    private fun setDefaultLogo() {
        val newUrl = O2CustomStyle.loginAvatarImageNewUrl()
        if (newUrl.isNullOrEmpty()) {
            val path = O2CustomStyle.loginAvatarImagePath(this@LoginActivity)
            if (!TextUtils.isEmpty(path)) {
                BitmapUtil.setImageFromFile(path!!, image_login_logo)
            }
        } else {
            O2ImageLoaderManager.instance().showImage(image_login_logo, newUrl, O2ImageLoaderOptions(isSkipCache = true))
        }

    }



    ///////////////play media////////////
    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private val beepListener = MediaPlayer.OnCompletionListener { mediaPlayer -> mediaPlayer.seekTo(0) }

    private fun initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.setOnCompletionListener(beepListener)
            val file = resources.openRawResourceFd(
                    R.raw.beep)
            try {
                mediaPlayer?.setDataSource(file.fileDescriptor,
                        file.startOffset, file.length)
                file.close()
                mediaPlayer?.setVolume(0.90f, 0.90f)
                mediaPlayer?.prepare()
            } catch (e: IOException) {
                mediaPlayer = null
            }
        }
    }

    private fun playBeepSound() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer?.start()
        }
    }



}