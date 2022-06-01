package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.my


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_my_info.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.login.LoginActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.GenderTypeEnums
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.person.PersonJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderOptions
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.permission.PermissionRequester
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.IOException


class MyInfoActivity : BaseMVPActivity<MyInfoContract.View, MyInfoContract.Presenter>(), MyInfoContract.View, View.OnClickListener {

    companion object {
        val TAKE_FROM_PICTURES_CODE = 1
        val TAKE_FROM_CAMERA_CODE = 2
        val CLIP_AVATAR_ACTIVITY_CODE = 3
        val REQUEST_CODE_ASK_PERMISSIONS = 123
    }

    override var mPresenter: MyInfoContract.Presenter = MyInfoPresenter()

//    private val avatarMenuList: ArrayList<String> by lazy { arrayListOf(getString(R.string.take_photo), getString(R.string.take_from_album)) }
//    val avatarMenu: CommonMenuPopupWindow by lazy { CommonMenuPopupWindow(avatarMenuList, this) }

    var person: PersonJson? = null
//    private val cameraImageUri: Uri by lazy { FileUtil.getUriFromFile(this, File(FileExtensionHelper.getCameraCacheFilePath(this))) }
    private var cameraImageUri:Uri? = null
    var isEdit = false
    private val backgroundList = arrayListOf(R.mipmap.pic_person_bg_1, R.mipmap.pic_person_bg_2, R.mipmap.pic_person_bg_3, R.mipmap.pic_person_bg_4, R.mipmap.pic_person_bg_5, R.mipmap.pic_person_bg_6)

    //软键盘
    override fun beforeSetContentView() {
        super.beforeSetContentView()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        //透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0 全透明实现
            //getWindow.setStatusBarColor(Color.TRANSPARENT)
            val window: Window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4 全透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        randomBg()

        image_myInfo_back.setOnClickListener(this)
        image_myInfo_avatar.setOnClickListener(this)
//        myInfo_logout_btn_id.setOnClickListener(this)
        tv_myInfo_edit_save_btn.setOnClickListener(this)
        linear_myInfo_gender_men_button.setOnClickListener(this)
        linear_myInfo_gender_women_button.setOnClickListener(this)
//        avatarMenu.setOnDismissListener { ZoneUtil.lightOn(this@MyInfoActivity) }
//        avatarMenu.onMenuItemClickListener = object : CommonMenuPopupWindow.OnMenuItemClickListener {
//            override fun itemClick(position: Int) {
//                when (position) {
//                    0 -> takeFromCamera()
//                    1 -> takeFromPictures()
//                }
//                avatarMenu.dismiss()
//            }
//        }

        showLoadingDialog()
        mPresenter.loadMyInfo()
        showAvatar()
    }

    // 随机背景图
    private fun randomBg() {
        val index = (0..5).random()
        XLog.debug("背景图片 $index")
        rl_my_info_outer.setBackgroundResource(backgroundList[index])
    }

    override fun layoutResId(): Int {
        return R.layout.activity_my_info
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isEdit) {
                switchStatus(false)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                TAKE_FROM_CAMERA_CODE -> cameraImageUri?.also { startClipAvatar(it) }

                CLIP_AVATAR_ACTIVITY_CODE -> {
                    data?.let {
                        val url = it.extras?.getString("clipAvatarFilePath")
                        XLog.debug("back Myinfo avatar uri : $url ")
                        modifyAvatar2Remote(url)
                    }
                }
            }
        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_myInfo_edit_save_btn -> switchTheStatusAndUpdateTheData()
            R.id.image_myInfo_back -> {
                if (isEdit) {
                    switchStatus(false)
                } else {
                    finish()
                }
            }
            R.id.image_myInfo_avatar -> modifyAvatarMenuShow()
//            R.id.myInfo_logout_btn_id -> logout()
            R.id.linear_myInfo_gender_men_button -> {
                if (isEdit) {
                    image_myInfo_gender_men_edit.visibility = View.VISIBLE
                    image_myInfo_gender_women_edit.visibility = View.GONE
                }
            }
            R.id.linear_myInfo_gender_women_button -> {
                if (isEdit) {
                    image_myInfo_gender_men_edit.visibility = View.GONE
                    image_myInfo_gender_women_edit.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun loadMyInfoSuccess(personal: PersonJson) {
        hideLoadingDialog()
        person = personal
        switchStatus(false)
        tv_myInfo_name.text = personal.name
        myInfo_officePhone_value_id.text = personal.officePhone
        edit_myInfo_officePhone.setText(personal.officePhone)
        tv_myInfo_sign_edit.text = personal.signature
        tv_myInfo_sign.text = personal.signature
        edit_myInfo_sign.setText(personal.signature)
        myInfo_mobile_value_id.text = personal.mobile
        edit_myInfo_mobile.setText(personal.mobile)
        myInfo_email_value_id.text = personal.mail
        edit_myInfo_email.setText(personal.mail)
        myInfo_qq_value_id.text = personal.qq
        edit_myInfo_qq.setText(personal.qq)
        myInfo_weixin_value_id.text = personal.weixin
        edit_myInfo_weixin.setText(personal.weixin)
        if (GenderTypeEnums.MALE.key == personal.genderType) {
            image_myInfo_gender_men.setImageResource(R.mipmap.icon_gender_men_enable_50dp)
            image_myInfo_gender_women.setImageResource(R.mipmap.icon_gender_women_disable_50dp)
        } else {
            image_myInfo_gender_men.setImageResource(R.mipmap.icon_gender_men_disable_50dp)
            image_myInfo_gender_women.setImageResource(R.mipmap.icon_gender_women_enable_50dp)
        }
        scroll_myInfo.scrollTo(0, 0)
    }

    override fun loadMyInfoFail() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_my_info_get_fail))
        finish()
    }

    override fun updateMyInfoFail() {
        hideLoadingDialog()
        XToast.toastShort(this, getString(R.string.message_my_info_update_fail))
        switchStatus(false)
    }

    override fun updateMyIcon(f: Boolean) {
        hideLoadingDialog()
        if (f) {
            doAsync {
                //休眠1秒
                XLog.debug("sleep 1 second")
                Thread.sleep(1000L)
                uiThread {
                    showAvatar()
                }
            }
        }
    }

    override fun logoutSuccess() {
        O2SDKManager.instance().logoutCleanCurrentPerson()
        goThenKill<LoginActivity>()
    }

    override fun logoutFail() {
        O2SDKManager.instance().logoutCleanCurrentPerson()
        goThenKill<LoginActivity>()
    }

    private fun switchStatus(flag: Boolean) {
        if (flag) {
            tv_myInfo_sign_edit.visibility = View.GONE
            edit_myInfo_sign.visibility = View.VISIBLE
            myInfo_officePhone_value_id.visibility = View.GONE
            edit_myInfo_officePhone.visibility = View.VISIBLE
            myInfo_mobile_value_id.visibility = View.GONE
            edit_myInfo_mobile.visibility = View.VISIBLE
            myInfo_email_value_id.visibility = View.GONE
            edit_myInfo_email.visibility = View.VISIBLE
            myInfo_qq_value_id.visibility = View.GONE
            edit_myInfo_qq.visibility = View.VISIBLE
            myInfo_weixin_value_id.gone()
            edit_myInfo_weixin.visible()
            if (GenderTypeEnums.MALE.key == person?.genderType) {
                image_myInfo_gender_men_edit.visibility = View.VISIBLE
                image_myInfo_gender_women_edit.visibility = View.GONE
            } else {
                image_myInfo_gender_men_edit.visibility = View.GONE
                image_myInfo_gender_women_edit.visibility = View.VISIBLE
            }
            image_myInfo_gender_men.setImageResource(R.mipmap.icon_gender_men_enable_50dp)
            image_myInfo_gender_women.setImageResource(R.mipmap.icon_gender_women_enable_50dp)
            image_myInfo_edit_avatar.visibility = View.VISIBLE
//            myInfo_logout_btn_id.visibility = View.INVISIBLE
        } else {
            tv_myInfo_sign_edit.visibility = View.VISIBLE
            edit_myInfo_sign.visibility = View.GONE
            myInfo_officePhone_value_id.visibility = View.VISIBLE
            edit_myInfo_officePhone.visibility = View.GONE
            myInfo_mobile_value_id.visibility = View.VISIBLE
            edit_myInfo_mobile.visibility = View.GONE
            myInfo_email_value_id.visibility = View.VISIBLE
            edit_myInfo_email.visibility = View.GONE
            myInfo_qq_value_id.visibility = View.VISIBLE
            edit_myInfo_qq.visibility = View.GONE
            myInfo_weixin_value_id.visible()
            edit_myInfo_weixin.gone()
            image_myInfo_gender_men_edit.visibility = View.GONE
            image_myInfo_gender_women_edit.visibility = View.GONE
            image_myInfo_edit_avatar.visibility = View.GONE
//            myInfo_logout_btn_id.visibility = View.VISIBLE
        }
        isEdit = flag
        refreshMenu()
    }


    private fun modifyAvatar2Remote(filePath: String?) {
        try {
            XLog.debug("in modifyAvatar2Remote...... $filePath")
            showLoadingDialog()
            mPresenter.updateMyIcon(File(filePath))
        } catch (e: Exception) {
            XLog.error("更新头像失败", e)
            hideLoadingDialog()
        }
    }

    private fun startClipAvatar(pictureUri: Uri) {
//        val intent = Intent(this, ClipAvatarActivity::class.java)
//        intent.putExtra("avatarUri", pictureUri)
//        startActivityForResult(intent, CLIP_AVATAR_ACTIVITY_CODE)//启动裁剪头像activity 返回的时候更新头像
        goWithRequestCode<ClipAvatarActivity>(ClipAvatarActivity.startWithBundle(pictureUri), CLIP_AVATAR_ACTIVITY_CODE)
    }


    private fun takeFromPictures() {
        PicturePickUtil().withAction(this)
            .forResult { files ->
                if (files!=null && files.isNotEmpty()) {
                    val photoURI = FileUtil.getUriFromFile(this, File(files[0]))
                    startClipAvatar(photoURI)
                }
            }
    }

    private fun takeFromCamera() {
        PermissionRequester(this).request(Manifest.permission.CAMERA)
                .o2Subscribe {
                    onNext { (granted, shouldShowRequestPermissionRationale, deniedPermissions) ->
                        XLog.info("granted:$granted , shouldShowRequest:$shouldShowRequestPermissionRationale, denied:$deniedPermissions")
                        if (!granted) {
                            O2DialogSupport.openAlertDialog(this@MyInfoActivity, getString(R.string.message_my_no_camera_permission))
                        } else {
                            openCamera()
                        }
                    }
                }
    }


    private fun openCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        //return-data false 不是直接返回拍照后的照片Bitmap 因为照片太大会传输失败
//        intent.putExtra("return-data", false)
//        //改用Uri 传递
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
//        intent.putExtra("noFaceDetection", true)
//        startActivityForResult(intent, TAKE_FROM_CAMERA_CODE)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    FileExtensionHelper.createImageFile(this)
                } catch (ex: IOException) {
                    XToast.toastShort(this, getString(R.string.message_camera_file_create_error))
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    cameraImageUri = FileUtil.getUriFromFile(this, it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                    startActivityForResult(takePictureIntent, TAKE_FROM_CAMERA_CODE)
                }
            }
        }
    }


    private fun refreshMenu() {
        if (isEdit) {
            tv_myInfo_edit_save_btn.text = getString(R.string.menu_save)
        } else {
            tv_myInfo_edit_save_btn.text = getString(R.string.menu_edit)
        }
    }

    private fun logout() {
        O2DialogSupport.openConfirmDialog(this, getString(R.string.dialog_msg_logout), {
            mPresenter.logout()
        })
    }

    private fun modifyAvatarMenuShow() {
        if (isEdit) {
            BottomSheetMenu(this).setItems(arrayListOf(getString(R.string.take_photo), getString(R.string.take_from_album)), ContextCompat.getColor(this, R.color.text_primary)) { i ->
                when (i) {
                    0 -> takeFromCamera()
                    1 -> takeFromPictures()
                }
            }.show()
        }

//        avatarMenu.showAtLocation(image_myInfo_avatar, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
//        ZoneUtil.lightOff(this)

    }

    private fun switchTheStatusAndUpdateTheData() {
        if (isEdit) {

            if (person != null) {
                person?.name = tv_myInfo_name.text.toString()
                person?.signature = edit_myInfo_sign.text.toString()
                person?.mobile = edit_myInfo_mobile.text.toString()
                person?.mail = edit_myInfo_email.text.toString()
                person?.qq = edit_myInfo_qq.text.toString()
                person?.officePhone = edit_myInfo_officePhone.text.toString()
                person?.weixin = edit_myInfo_weixin.text.toString()
                if (View.VISIBLE == image_myInfo_gender_men_edit.visibility) {
                    person?.genderType = GenderTypeEnums.MALE.key
                } else {
                    person?.genderType = GenderTypeEnums.FEMALE.key
                }
                showLoadingDialog()
                mPresenter.updateMyInfo(person!!)
            } else {
                XLog.error("my info  is null.......")
                XToast.toastShort(this, getString(R.string.message_my_info_obj_is_null))
            }

        } else {
            switchStatus(true)
        }

    }

    private fun showAvatar() {
        //头像
        val url = APIAddressHelper.instance().getPersonAvatarUrlWithId(O2SDKManager.instance().distinguishedName)
        O2ImageLoaderManager.instance().showImage(image_myInfo_avatar, url, O2ImageLoaderOptions(isSkipCache = true))
    }
}
