package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.clouddrive


import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_yunpan.*
import net.muliba.changeskin.FancySkinManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonFragmentPagerAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.APIDistributeTypeEnum
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.enums.FileOperateType
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PickTypeMode
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File


class CloudDriveActivity : BaseMVPActivity<CloudDriveContract.View, CloudDriveContract.Presenter>(), CloudDriveContract.View {
    override var mPresenter: CloudDriveContract.Presenter = CloudDrivePresenter()

    override fun layoutResId(): Int = R.layout.activity_yunpan

    companion object {
        val YUNPAN_UPLOAD_FILE_REQUEST_CODE = 1
        val YUNPAN_FROM_SHARE = "YUNPAN_FROM_SHARE"
        val YUNPAN_FROM_SEND = "YUNPAN_FROM_SEND"

    }

    val fragmentList = ArrayList<Fragment>(3)
    val fragmentTitles = ArrayList<String>(3)
    val adapter:CommonFragmentPagerAdapter by lazy { CommonFragmentPagerAdapter(supportFragmentManager, fragmentList, fragmentTitles) }
    var back = ""

    override fun afterSetContentView(savedInstanceState: Bundle?) {

        fragmentList.add(CloudDriveMyFileFragment())
        val shareFragment = CloudDriveCooperationFileFragment()
        val bundle = Bundle()
        bundle.putInt(CloudDriveCooperationFileFragment.COOPERATION_TYPE_KEY, CloudDriveCooperationFileFragment.COOPERATION_TYPE_SHARE_FILE)
        shareFragment.arguments = bundle
        fragmentList.add(shareFragment)
        val receiveFragment = CloudDriveCooperationFileFragment()
        val receiveBundle = Bundle()
        receiveBundle.putInt(CloudDriveCooperationFileFragment.COOPERATION_TYPE_KEY, CloudDriveCooperationFileFragment.COOPERATION_TYPE_RECEIVE_FILE)
        receiveFragment.arguments = receiveBundle
        fragmentList.add(receiveFragment)
        fragmentTitles.add(getString(R.string.tab_yunpan_my_file))
        fragmentTitles.add(getString(R.string.tab_yunpan_share_file))
        fragmentTitles.add(getString(R.string.tab_yunpan_recive_file))

        setupToolBar(getString(R.string.title_activity_yunpan), true, true)

        yunpan_viewPager_id.adapter = adapter
        yunpan_viewPager_id.offscreenPageLimit = 3
        tab_yunpan.setupWithViewPager(yunpan_viewPager_id)
        tab_yunpan.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabStyle(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tabStyle(tab, false)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabStyle(tab)
            }
        })
        tab_yunpan.getTabAt(0)?.select()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_scale_in, R.anim.activity_scale_out)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //有下载文件的 取消下载
            if (downloadTaskMap.isNotEmpty()) {
                downloadTaskMap.map {
                    it.value.unsubscribe()
                }
                downloadTaskMap.clear()
                yunpan_download_file_id.gone()
                return true
            }
            val currentIndex = yunpan_viewPager_id.currentItem
            var isOperate = true
            when(currentIndex) {
                0 -> {
                    isOperate = (fragmentList[0] as CloudDriveMyFileFragment).onClickBackBtn()
                }
                1 -> {
                    isOperate = (fragmentList[1] as CloudDriveCooperationFileFragment).onClickBackBtn()
                }
                2 -> {
                    isOperate = (fragmentList[2] as CloudDriveCooperationFileFragment).onClickBackBtn()
                }
            }
            if (!isOperate) {
                finish()
                return true
            }else {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    val downloadTaskMap = HashMap<String, Subscription>()
    fun openYunPanFile(id: String, fileName: String) {
        XLog.debug("download id:$id, , file:$fileName")
        if (!downloadTaskMap.containsKey(id)) {
            yunpan_download_file_id.visible()
            val path = FileExtensionHelper.getXBPMTempFolder(this) + File.separator + id + File.separator + fileName
            XLog.debug("file path $path")
            val downloadUrl = APIAddressHelper.instance()
                    .getCommonDownloadUrl(APIDistributeTypeEnum.x_file_assemble_control, "jaxrs/attachment/$id/download/stream")
            val subscription = O2FileDownloadHelper.download(downloadUrl, path)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Boolean>{
                        override fun onError(e: Throwable?) {
                            yunpan_download_file_id.gone()
                            if (downloadTaskMap.containsKey(id)){
                                downloadTaskMap.remove(id)
                            }
                            XToast.toastShort(this@CloudDriveActivity, "下载附件失败！")
                        }

                        override fun onNext(t: Boolean?) {
                            yunpan_download_file_id.gone()
                            if (downloadTaskMap.containsKey(id)){
                                downloadTaskMap.remove(id)
                            }
                            AndroidUtils.openFileWithDefaultApp(this@CloudDriveActivity, File(path))
                        }

                        override fun onCompleted() {

                        }

                    })
            downloadTaskMap[id] = subscription

//            downloadTaskMap.put(id, doAsync {
//                var downfile = true
//                val path = FileExtensionHelper.getXBPMTempFolder()+ File.separator + fileName
//                XLog.debug("file path $path")
//                val file = File(path)
//                if (!file.exists()) {
//                    XLog.debug("file not exist, ${file.path}")
//                    try {
//                        val call = RetrofitClient.instance().fileAssembleControlApi()
//                                .downloadFile(id)
//                        val response = call.execute()
//                        val input  = DataInputStream(response.body()?.byteStream())
//                        val output = DataOutputStream(FileOutputStream(file))
//                        val buffer = ByteArray(4096)
//                        var count = 0
//                        do {
//                            count = input.read(buffer)
//                            if (count > 0) {
//                                output.write(buffer, 0, count)
//                            }
//                        } while (count > 0)
//                        output.close()
//                        input.close()
//                        downfile = true
//                    }catch (e: Exception){
//                        XLog.error("download file fail", e)
//                        downfile = false
//                    }
//                }
//                uiThread {
//                    if (downloadTaskMap.containsKey(id)){
//                        downloadTaskMap.remove(id)
//                    }
//                    yunpan_download_file_id.gone()
//                    if (downfile) {
//                        AndroidUtils.openFileWithDefaultApp(this@CloudDriveActivity, file)
//                    }else {
//                        if (file.exists()){
//                            file.delete()
//                        }
//                        XToast.toastShort(this@CloudDriveActivity, "下载附件失败！")
//                    }
//                }
//            })

        }

    }

    fun clickUploadFile() {

        PicturePickUtil().withAction(this)
            .setMode(PickTypeMode.File)
            .forResult { files ->
                if (files !=null && files.isNotEmpty()) {
                    (fragmentList[0] as CloudDriveMyFileFragment).menuUploadFile(files[0])
                }
            }
    }

    fun menuShareOrSend(from: String) {
        back = from
        if (!TextUtils.isEmpty(back)) {
            val bundle = ContactPickerActivity.startPickerBundle(
                    arrayListOf("personPicker"),
                    multiple = true)
            contactPicker(bundle) { result ->
                if (result != null) {
                    val users = ArrayList<String>()
                    result.users.forEach {
                        users.add(it.distinguishedName)
                    }
                    when (back) {
                        YUNPAN_FROM_SHARE -> (fragmentList[0] as CloudDriveMyFileFragment).menuSendResult(users, FileOperateType.SHARE)
                        YUNPAN_FROM_SEND -> (fragmentList[0] as CloudDriveMyFileFragment).menuSendResult(users, FileOperateType.SEND)
                        else -> XLog.error("error back , back:$back")
                    }
                }
            }
        }
    }


    private fun tabStyle(tab: TabLayout.Tab?, isSelected: Boolean = true) {
        val colorSpan = when (isSelected) {
            true -> ForegroundColorSpan(FancySkinManager.instance().getColor(this, R.color.z_color_primary))
            false -> ForegroundColorSpan(FancySkinManager.instance().getColor(this, R.color.z_color_text_primary))
        }
        val title = tab?.text.toString()
        val spannableString = SpannableString(title)
        spannableString.setSpan(colorSpan, 0, title.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        tab?.text = spannableString
    }
}
