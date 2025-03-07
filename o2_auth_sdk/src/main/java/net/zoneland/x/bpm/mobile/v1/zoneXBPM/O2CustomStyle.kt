package net.zoneland.x.bpm.mobile.v1.zoneXBPM

import android.content.Context
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileUtil
import java.io.File
import com.bumptech.glide.Glide
import android.os.Looper



/**
 * 服务器自定义图片
 * Created by fancyLou on 16/04/2018.
 * Copyright © 2018 O2. All rights reserved.
 */


object O2CustomStyle {

    const val CUSTOM_STYLE_JSON_KEY = "customStyleJsonKey"
    //MARK - 自定义信息 更新hash
    const val CUSTOM_STYLE_UPDATE_HASH_KEY = "customStyleUpdateHashKey"

    //MARK - 移动端首页展现是默认的native 还是配置的portal门户页面
    const val INDEX_TYPE_DEFAULT = "default"
    const val INDEX_TYPE_PORTAL = "portal"

    //MARK - 移动端首页展现类型的key
    const val INDEX_TYPE_PREF_KEY = "customStyleIndexTypeKey"
    const val INDEX_ID_PREF_KEY = "customStyleIndexIdKey"
    // 移动端简易模式 key
    const val CUSTOM_STYLE_SIMPLE_MODE_PREF_KEY = "customStyleSimpleModeKey"
    // 首页展现的 tab 列表的 key
    const val CUSTOM_STYLE_INDEX_PAGES_KEY = "customStyleIndexPagesKey"

    const val CUSTOM_STYLE_INDEX_FILTER_PROCESS_KEY = "customStyleIndexFilterProcessKey"
    const val CUSTOM_STYLE_INDEX_FILTER_CATEGORY_KEY = "customStyleIndexFilterCategoryKey"
    // 默哀
    const val CUSTOM_STYLE_SILENCE_GRAY_PREF_KEY = "customStyleSilenceGrayKey"
    // 系统消息是否可点击
    const val CUSTOM_STYLE_SYSTEM_MESSAGE_CAN_CLICK_KEY = "customStyleSystemMessageCanClickKey"
    // app退出提示
    const val CUSTOM_STYLE_APP_EXIT_ALERT_KEY = "customStyleAppExitAlertKey"

    // 通讯录权限查询视图key
    const val CUSTOM_STYLE_CONTACT_PERMISSION_PREF_KEY = "customStyleContactPermissionViewKey"
    const val CUSTOM_STYLE_CONTACT_PERMISSION_DEFAULT = "addressPowerView"

    const val extension_png = ".png"

    //MARK - 缓存图片key
    const val IMAGE_KEY_LAUNCH_LOGO = "launch_logo" //启动页logo图  关于页面用的也是这个图 195px    65dp
    const val IMAGE_KEY_LAUNCH_LOGO_URL = "launch_logo_URL" //启动页logo图  关于页面用的也是这个图 195px    65dp
    //首页底部菜单home按钮  114px   38dp
    const val IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_FOCUS = "index_bottom_menu_logo_focus"
    const val IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_FOCUS_URL = "index_bottom_menu_logo_focus_URL"
    const val IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_BLUR = "index_bottom_menu_logo_blur"
    const val IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_BLUR_URL = "index_bottom_menu_logo_blur_URL"

    const val IMAGE_KEY_LOGIN_AVATAR = "login_avatar" //登录页默认头像  225px  75dp
    const val IMAGE_KEY_LOGIN_AVATAR_URL = "login_avatar_URL" //登录页默认头像  225px  75dp

    const val IMAGE_KEY_PEOPLE_AVATAR_DEFAULT = "people_avatar_default" //人员默认头像  120px  40dp
    const val IMAGE_KEY_PEOPLE_AVATAR_DEFAULT_URL = "people_avatar_default_URL" //人员默认头像  120px  40dp

    const val IMAGE_KEY_PROCESS_DEFAULT = "process_default"  //流程默认图标   90px  30dp
    const val IMAGE_KEY_PROCESS_DEFAULT_URL = "process_default_URL"  //流程默认图标   90px  30dp

    const val IMAGE_KEY_SETUP_ABOUT_LOGO = "setup_about_logo" //设置页 关于按钮 logo    66px  22dp
    const val IMAGE_KEY_SETUP_ABOUT_LOGO_URL = "setup_about_logo_URL" //设置页 关于按钮 logo    66px  22dp

    const val IMAGE_KEY_APPLICATION_TOP = "application_top" //应用页面 顶部图片    730  390
    const val IMAGE_KEY_APPLICATION_TOP_URL = "application_top_URL" //应用页面 顶部图片    730  390


    /**
     * 启动 logo图地址
     */
    fun launchLogoImagePath(context: Context?): String? {
        return if (context != null) {
            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_LAUNCH_LOGO + extension_png
        } else {
            null
        }
    }
    fun launchLogoImageNewUrl(): String? {
       return O2SDKManager.instance().prefs().getString(IMAGE_KEY_LAUNCH_LOGO_URL, "")
    }

    /**
     * 首页底部Home focus 图地址
     */
    fun indexMenuLogoFocusImagePath(context: Context?): String? {
        return if (context != null) {
            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_FOCUS + extension_png
        } else {
            null
        }
    }
    fun indexMenuLogoFocusImageNewUrl(): String? {
        return O2SDKManager.instance().prefs().getString(IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_FOCUS_URL, "")
    }

    /**
     * 首页底部Home blur 图地址
     */
    fun indexMenuLogoBlurImagePath(context: Context?): String? {
        return if (context != null) {
            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_BLUR + extension_png
        } else {
            null
        }
    }
    fun indexMenuLogoBlurImageNewUrl(): String? {
        return O2SDKManager.instance().prefs().getString(IMAGE_KEY_INDEX_BOTTOM_MENU_LOGO_BLUR_URL, "")
    }

    /**
     * 登录页头像 图地址
     */
    fun loginAvatarImagePath(context: Context?): String? {
        return if (context != null) {
            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_LOGIN_AVATAR + extension_png
        } else {
            null
        }
    }
    fun loginAvatarImageNewUrl(): String? {
        return O2SDKManager.instance().prefs().getString(IMAGE_KEY_LOGIN_AVATAR_URL, "")
    }

    /**
     * 人员头像默认 图地址
     */
//    fun peopleAvatarImagePath(context: Context?): String? {
//        return if (context != null) {
//            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_PEOPLE_AVATAR_DEFAULT + extension_png
//        } else {
//            null
//        }
//    }
//    fun peopleAvatarImageUrl(): String? {
//        return O2SDKManager.instance().prefs().getString(IMAGE_KEY_PEOPLE_AVATAR_DEFAULT_URL, "")
//    }

    /**
     * 流程默认 图地址
     */
//    fun processDefaultImagePath(context: Context?): String? {
//        return if (context != null) {
//            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_PROCESS_DEFAULT + extension_png
//        } else {
//            null
//        }
//    }
//    fun processDefaultImageUrl(): String? {
//        return O2SDKManager.instance().prefs().getString(IMAGE_KEY_PROCESS_DEFAULT_URL, "")
//    }

    /**
     * 设置页关于logo 图地址
     */
    fun setupAboutImagePath(context: Context?): String? {
        return if (context != null) {
            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_SETUP_ABOUT_LOGO + extension_png
        } else {
            null
        }
    }
    fun setupAboutImageUrl(): String? {
        return O2SDKManager.instance().prefs().getString(IMAGE_KEY_SETUP_ABOUT_LOGO_URL, "")
    }

    /**
     * 应用页面 顶部大图
     */
    fun applicationTopImagePath(context: Context?): String? {
        return if (context != null) {
            FileUtil.appExternalImageDir(context)?.absolutePath + File.separator + IMAGE_KEY_APPLICATION_TOP + extension_png
        } else {
            null
        }
    }
    fun applicationTopImageUrl(): String? {
        return O2SDKManager.instance().prefs().getString(IMAGE_KEY_APPLICATION_TOP_URL, "")
    }


    // 切换更新服务器资源后需要清除缓存。。。。。。。。。

    /**
     * 清除图片磁盘缓存
     */
    fun clearImageDiskCache(context: Context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Thread(Runnable {
                    Glide.get(context).clearDiskCache()
                }).start()
            } else {
                Glide.get(context).clearDiskCache()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 清除图片内存缓存
     */
    fun clearImageMemoryCache(context: Context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}