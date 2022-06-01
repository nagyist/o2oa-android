package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.meeting.edit


import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_meeting_edit_form.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.organization.ContactPickerActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecycleViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.api.APIAddressHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.process.ProcessDataJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.meeting.MeetingFileInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.meeting.MeetingInfoJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.FileExtensionHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.imageloader.O2ImageLoaderManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PickTypeMode
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.pick.PicturePickUtil
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.BottomSheetMenu
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleImageView
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.dialog.O2DialogSupport


class MeetingEditActivity : BaseMVPActivity<MeetingEditContract.View, MeetingEditContract.Presenter>(), MeetingEditContract.View {

    override var mPresenter: MeetingEditContract.Presenter = MeetingEditPresenter()
    override fun layoutResId(): Int = R.layout.activity_meeting_edit_form

    val invitePersonAdd = "添加"
    val invitePersonList = ArrayList<String>()
    val inviteOldPersonList = ArrayList<String>() //被删除的人员需要记录一下 这个是记录老的列表 计算用
    val meetingFileList = ArrayList<MeetingFileInfoJson>()

    lateinit var meeting:MeetingInfoJson
    private lateinit var roomName:String

    private val typeList = ArrayList<String>()
    private var type: String = ""  // 会议类型
    private var hostPerson: String = "" // 主持人 默认当前用户
    private var hostUnit: String = "" // 承办部门

    companion object {
        val MEETING_INFO_KEY = "xbpm.meeting.edit.info"
        val MEETING_INFO_ROOM_NAME_KEY = "xbpm.meeting.edit.room.name"
        val MEETING_FILE_CODE = 1003

        fun startBundleData(info: MeetingInfoJson, roomName:String): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(MEETING_INFO_KEY, info)
            bundle.putString(MEETING_INFO_ROOM_NAME_KEY, roomName)
            return bundle
        }
    }

    //软键盘
    override fun beforeSetContentView() {
        super.beforeSetContentView()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun afterSetContentView(savedInstanceState: Bundle?) {
        meeting = intent.extras?.getSerializable(MEETING_INFO_KEY) as MeetingInfoJson
        roomName = intent.extras?.getString(MEETING_INFO_ROOM_NAME_KEY) ?: getString(R.string.title_activity_meeting_edit_form)

        if (TextUtils.isEmpty(meeting.id)){
            XToast.toastShort(this, "错误：没有会议对象")
            finish()
            return
        }

        setupToolBar(meeting.subject, true)
        tv_meeting_edit_form_room_name.text = roomName
        edit_meeting_edit_form_name.setText(meeting.subject)
        val startDay = meeting.startTime.substring(0, 10)
        val startTime = meeting.startTime.substring(11, 16)
        val completeTime = meeting.completedTime.substring(11, 16)
        edit_meeting_edit_form_start_day.text = startDay
        edit_meeting_edit_form_start_time.text = startTime
        edit_meeting_edit_form_end_time.text = completeTime
        edit_meeting_edit_form_desc.setText(meeting.summary)
        type = meeting.type
        edit_meeting_type.text = type
        refreshHostPerson(meeting.hostPerson)
        refreshHostUnit(meeting.hostUnit)

        meetingFileList.addAll(meeting.attachmentList)
        recycler_meeting_edit_form_file_list.layoutManager = LinearLayoutManager(this)
        recycler_meeting_edit_form_file_list.adapter = meetingFileAdapter
        meetingFileAdapter.notifyDataSetChanged()
        inviteOldPersonList.addAll(meeting.inviteMemberList)
        invitePersonList.addAll(meeting.inviteMemberList)
        invitePersonList.add(invitePersonAdd)
        invitePersonAdapter.setOnItemClickListener { _, position ->
            when(position) {
                invitePersonList.size-1 -> {
                    val bundle = ContactPickerActivity.startPickerBundle(
                            arrayListOf("personPicker"),
                            multiple = true)
                    contactPicker(bundle) { result ->
                        if (result != null) {
                            val users = result.users.map { it.distinguishedName }
                            XLog.debug("choose invite person, list:$users,")
                            chooseInvitePersonCallback(users)
                        }
                    }
                }
                else -> {
                    invitePersonList.removeAt(position)
                    invitePersonAdapter.notifyDataSetChanged()
                }
            }
        }
        recycler_meeting_edit_form_invite_person_list.layoutManager = GridLayoutManager(this, 5)
        recycler_meeting_edit_form_invite_person_list.adapter = invitePersonAdapter
        invitePersonAdapter.notifyDataSetChanged()

        //tv_bottom_button_first.setOnClickListener { finish() }
        button_submit_meeting.setOnClickListener {
            O2DialogSupport.openConfirmDialog(this, getString(R.string.meeting_delete_message_confirm), { _ ->
                mPresenter.deleteMeeting(meeting.id)
            })
        }
        iv_meeting_file_add.setOnClickListener {
            _ ->
            PicturePickUtil().withAction(this)
                .setMode(PickTypeMode.File)
                .forResult { files ->
                    if (files !=null && files.isNotEmpty()) {
                        XLog.debug("uri path:" + files[0])
                        showLoadingDialog()
                        mPresenter.saveMeetingFile(files[0], meeting.id)
                    }
                }
        }
        meetingFileAdapter.setOnItemClickListener { _, position ->
            showLoadingDialog()
            mPresenter.deleteMeetingFile(meetingFileList[position].id,position)
        }

        // 读取配置 获取会议类型
        val config = O2SDKManager.instance().prefs().getString(O2.PRE_MEETING_CONFIG_KEY, "")
        if (!TextUtils.isEmpty(config)) {
            val meetingConfig = O2SDKManager.instance().gson.fromJson<ProcessDataJson>(config, ProcessDataJson::class.java)
            if (meetingConfig.typeList?.isNotEmpty() == true) {
                typeList.clear()
                typeList.addAll(meetingConfig.typeList!!)
            }
        }
        rl_choose_meeting_type.setOnClickListener { chooseMeetingType() }
        rl_choose_meeting_hostPerson.setOnClickListener { chooseHostPerson() }
        rl_choose_meeting_hostUnit.setOnClickListener { chooseHostUnit() }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_meeting_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_meeting_edit_save -> {
                submitForm()

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode == Activity.RESULT_OK) {
//            when(requestCode){
//
//                MEETING_FILE_CODE -> {
//                    val result = data?.getStringExtra(FilePicker.FANCY_FILE_PICKER_SINGLE_RESULT_KEY)
//                    if (!TextUtils.isEmpty(result)) {
//                        XLog.debug("uri path:" + result)
//                        showLoadingDialog()
//                        mPresenter.saveMeetingFile(result!!,meeting.id)
//                    } else {
//                        XLog.error("FilePicker 没有返回值！")
//                    }
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }

    override fun onError(message: String) {
        XToast.toastShort(this, message)
        hideLoadingDialog()
    }

    override fun deleteMeetingFile(position: Int) {
        meetingFileList.removeAt(position)
        meetingFileAdapter.notifyDataSetChanged()
        hideLoadingDialog()
    }

    override fun updateMeetingSuccess() {
        finish()
    }

    override fun deleteMeetingSuccess() {
        finish()
    }

    override fun saveMeetingFileSuccess(fileName: String,fileId: String) {
        val meetingFile = MeetingFileInfoJson()
        meetingFile.name = fileName
        meetingFile.extension = fileName.substringAfterLast('.', "")
        meetingFile.id = fileId
        meetingFileList.add(meetingFile)
        meetingFileAdapter.notifyDataSetChanged()
        hideLoadingDialog()
    }
    /**
     * 选择会议类型
     */
    private fun chooseMeetingType() {
        if (typeList.isNotEmpty()) {
            BottomSheetMenu(this)
                .setTitle(getString(R.string.meeting_form_type))
                .setItems(typeList, ContextCompat.getColor(this, R.color.z_color_text_primary)) { index ->
                    setMeetingType(index)
                }.show()
        }
    }

    private fun setMeetingType(index: Int) {
        type = typeList[index]
        edit_meeting_type.text = type
    }
    /**
     * 选择主持人
     */
    private fun chooseHostPerson() {
        val bundle = ContactPickerActivity.startPickerBundle(
            arrayListOf(ContactPickerActivity.personPicker),
            multiple = false)
        contactPicker(bundle) { result ->
            if (result != null) {
                val users = result.users.map { it.distinguishedName }
                XLog.debug("选择了主持人, list:$users ")
                if (users.isNotEmpty()) {
                    refreshHostPerson(users[0])
                }
            }
        }
    }

    /**
     * 选择承办部门
     */
    private fun chooseHostUnit() {
        val bundle = ContactPickerActivity.startPickerBundle(
            arrayListOf(ContactPickerActivity.departmentPicker),
            multiple = false)
        contactPicker(bundle) { result ->
            if (result != null) {
                val depts = result.departments.map { it.distinguishedName }
                XLog.debug("选择了承办部门, list:$depts ")
                if (depts.isNotEmpty()) {
                    refreshHostUnit(depts[0])
                }
            }
        }
    }


    /**
     * 设置主持人
     */
    private fun refreshHostPerson(person: String) {
        hostPerson = person
        if (!TextUtils.isEmpty(hostPerson) && hostPerson.contains("@")) {
            edit_meeting_hostPerson.text = hostPerson.split("@")[0]
        }
    }

    private fun refreshHostUnit(unit: String) {
        hostUnit = unit
        if (!TextUtils.isEmpty(hostUnit) && hostUnit.contains("@")) {
            edit_meeting_hostUnit.text = hostUnit.split("@")[0]
        }
    }

    private fun chooseInvitePersonCallback(result: List<String>) {
        val allList = ArrayList<String>()
        invitePersonList.remove(invitePersonAdd)
        if (invitePersonList.isNotEmpty()){
            allList.addAll(invitePersonList)
        }
        allList.addAll(result)
        invitePersonList.clear()
        invitePersonList.addAll(allList.distinct())
        invitePersonList.add(invitePersonAdd)
        invitePersonAdapter.notifyDataSetChanged()
    }


    private fun submitForm() {
        val subject = edit_meeting_edit_form_name.text.toString()
        if (TextUtils.isEmpty(subject)) {
            XToast.toastShort(this, "会议名称不能为空！")
            return
        }
        if (invitePersonList.isEmpty()) {
            XToast.toastShort(this, "请选择与会人员！")
            return
        }
        meeting.subject = subject
        meeting.summary = edit_meeting_edit_form_desc.text.toString()
        val savePersonList = invitePersonList
        savePersonList.remove(invitePersonAdd)
        meeting.invitePersonList = savePersonList
        meeting.inviteMemberList = savePersonList
        val inviteDelPersonList = ArrayList<String>()
        if (inviteOldPersonList.isNotEmpty()) {
            inviteOldPersonList.forEach { old ->
                if (!savePersonList.contains(old)) {
                    inviteDelPersonList.add(old)
                }
            }
        }
        meeting.inviteDelPersonList = inviteDelPersonList
        meeting.attachmentList = meetingFileList
        meeting.type = type
        meeting.hostPerson = hostPerson
        meeting.hostUnit = hostUnit
        mPresenter.updateMeetingInfo(meeting)
    }

    private val invitePersonAdapter: CommonRecycleViewAdapter<String> by lazy {
        object : CommonRecycleViewAdapter<String>(this, invitePersonList, R.layout.item_person_avatar_name) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: String?) {
                if (TextUtils.isEmpty(t)){
                    XLog.error("person id is null!!!!!!")
                    return
                }
                val avatar = holder?.getView<CircleImageView>(R.id.circle_image_avatar)
                avatar?.setImageResource(R.mipmap.contact_icon_avatar)
                val delete = holder?.getView<ImageView>(R.id.delete_people_iv)
                delete?.visibility = View.VISIBLE
                if (avatar!=null) {
                    if (invitePersonAdd==t){
                        avatar.setImageResource(R.mipmap.icon_add_people)
                        delete?.visibility = View.GONE
                    }else {
                        val url = APIAddressHelper.instance().getPersonAvatarUrlWithId(t!!)
                        O2ImageLoaderManager.instance().showImage(avatar, url)
                    }
                }
                val nameTv = holder?.getView<TextView>(R.id.tv_name)
                if (nameTv!=null) {
                    if(invitePersonAdd==t){
                        nameTv.text = t
                    }else{
                        if (t != null && t.contains("@")) {
                            nameTv.text = t.split("@").first()
                        }else {
                            nameTv.text = t
                        }
//                        mPresenter.asyncLoadPersonName(nameTv, t!!)
                    }
                }
            }
        }
    }

    private val meetingFileAdapter: CommonRecycleViewAdapter<MeetingFileInfoJson> by lazy {
        object : CommonRecycleViewAdapter<MeetingFileInfoJson>(this, meetingFileList, R.layout.item_meeting_file_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, t: MeetingFileInfoJson?) {
                holder?.setText(R.id.meeting_file_list_name_id,t?.name)
                val avatar = holder?.getView<ImageView>(R.id.meeting_file_list_icon_id)
                val id = FileExtensionHelper.getImageResourceByFileExtension(t?.extension)
                avatar?.setImageResource(id)
            }
        }
    }

}
