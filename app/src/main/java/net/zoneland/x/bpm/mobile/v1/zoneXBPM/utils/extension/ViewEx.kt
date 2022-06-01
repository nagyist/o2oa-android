package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.Base64ImageUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import org.jetbrains.anko.ctx
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by fancy on 2017/4/10.
 */
inline fun Activity.makeCallDial(number: String): Boolean = ctx.makeCallDial(number)
inline fun Fragment.makeCallDial(number: String): Boolean = activity?.makeCallDial(number) ?: false

fun Context.makeCallDial(number: String): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

inline fun <reified T : Activity> Activity.go(bundle: Bundle? = null) {
    val intent = Intent(this, T::class.java)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
}

inline fun <reified T:Activity> Activity.goWithRequestCode(bundle: Bundle? = null, requestCode: Int = 0) {
    val intent = Intent(this, T::class.java)
    if (bundle !=null) {
        intent.putExtras(bundle)
    }
    startActivityForResult(intent, requestCode)
}

inline fun <reified T: Activity> Activity.goThenKill(bundle: Bundle? = null) {
    val intent = Intent(this, T::class.java)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
    finish()
}

/**
 * 启动T 并清除堆栈中的所有Activity
 */
inline fun <reified T:Activity> Activity.goAndClearBefore(bundle: Bundle? = null) {
    val intent = Intent(this, T::class.java)
    if (bundle!=null){
        intent.putExtras(bundle)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    finish()
}

fun Activity.hideSoftInput() {
    var view: View? = this.currentFocus
    if (view !=null && view.windowToken !=null) {
        val manager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (manager.isActive) {
            manager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}
fun View.gone() {
    visibility = View.GONE
}
fun View.inVisible() {
    visibility = View.INVISIBLE
}

fun ImageView.setImageBase64(base64Str: String? , tag: String) {
    Observable.create<Bitmap> { subscriber ->
        try {
            if (TextUtils.isEmpty(base64Str) || TextUtils.isEmpty(tag)) {
                subscriber.onError(NullPointerException("参数不正确，没有base64或者tag"))
            }else {
                val inputStream = Base64ImageUtil.generateBase642Inputstream(base64Str)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                subscriber.onNext(bitmap)
            }

        }catch (e: Exception){
            subscriber.onError(e)
        }
        subscriber.onCompleted()
    }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({b->
                if (tag == this.tag){
                    this.setImageBitmap(b)
                }
            },{e-> XLog.error("base 64 转化图片失败", e)},{})

}


fun TextView.text2String(): String = text.toString()


fun ContextCompat.getColor(context: Context?, @ColorRes colorRes: Int): Int {
    return if (context != null) {
        if (Build.VERSION.SDK_INT >= 23) {
            context.getColor(colorRes)
        } else {
            context.resources.getColor(colorRes)
        }
    }else {
        colorRes
    }
}

/*
 * 获取控件宽
 */
fun View.getSelfWidth(): Int {
    val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    this.measure(w, h)
    return this.measuredWidth
}
/*
 * 获取控件高
 */
fun View.getSelfHeight(): Int {
    val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    this.measure(w, h)
    return this.measuredHeight
}
/*
 * 设置控件所在的位置YY，并且不改变宽高，
 * XY为绝对位置
 */
fun View.layoutSelf(x: Int, y: Int) {
    val margin = ViewGroup.MarginLayoutParams(this.layoutParams)
    margin.setMargins(x, y, 0, 0)
    val layoutParams = FrameLayout.LayoutParams(margin)
    this.layoutParams = layoutParams
}

/**
 * o2oa 自定义刷新loading样式
 */
fun SwipeRefreshLayout.o2oaColorScheme() {
    this.setColorSchemeResources(
        R.color.z_color_refresh_scuba_blue,
        R.color.z_color_refresh_red, R.color.z_color_refresh_purple, R.color.z_color_refresh_orange)
}