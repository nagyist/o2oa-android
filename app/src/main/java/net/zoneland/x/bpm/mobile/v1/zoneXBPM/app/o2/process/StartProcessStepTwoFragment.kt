package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.os.Bundle
import android.text.TextUtils
import android.widget.RadioButton
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.fragment_start_process_step_two.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.main.identity.ProcessWOIdentityJson
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.ProcessDraftWorkData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.DateHelper
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.goThenKill
import org.jetbrains.anko.dip


class StartProcessStepTwoFragment : BaseMVPFragment<StartProcessStepTwoContract.View, StartProcessStepTwoContract.Presenter>(), StartProcessStepTwoContract.View {

    override var mPresenter: StartProcessStepTwoContract.Presenter = StartProcessStepTwoPresenter()

    override fun layoutResId(): Int = R.layout.fragment_start_process_step_two

    companion object {
        const val PROCESS_ID_KEY = "processId"
        const val PROCESS_NAME_KEY = "processName"
        const val PROCESS_START_MODE_KEY = "startMode"
        fun newInstance(processId:String, processName:String, defaultStartMode: String): StartProcessStepTwoFragment {
            val stepTwo = StartProcessStepTwoFragment()
            val bundle = Bundle()
            bundle.putString(PROCESS_ID_KEY, processId)
            bundle.putString(PROCESS_NAME_KEY, processName)
            bundle.putString(PROCESS_START_MODE_KEY, defaultStartMode)
            stepTwo.arguments = bundle
            return stepTwo
        }
    }
    private val identityList = ArrayList<ProcessWOIdentityJson>()
    private var processId = ""
    private var processName = ""
    private var processStartMode = ""
    private var identity = ""
    override fun initUI() {
        val startString = getString(R.string.title_activity_start_process_step_two)
        (activity as StartProcessActivity).setToolBarTitle(startString)
        processId = arguments?.getString(PROCESS_ID_KEY) ?: ""
        processName = arguments?.getString(PROCESS_NAME_KEY) ?: ""
        processStartMode = arguments?.getString(PROCESS_START_MODE_KEY) ?: ""
        tv_start_process_step_two_process_title.text = "$startString-$processName"
        tv_start_process_step_two_time.text = DateHelper.nowByFormate("yyyy-MM-dd HH:mm")
        btn_start_process_step_two_positive.setOnClickListener {
            if (processStartMode == O2.O2_Process_start_mode_draft){
                startDraft()
            }else {
                startProcess()
            }
        }
        btn_start_process_step_two_cancel.setOnClickListener { (activity as StartProcessActivity).finish() }
        mPresenter.loadCurrentPersonIdentityWithProcess(processId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        XLog.debug("StartProcessStepTwoFragment onDestroyView...............")
    }


    override fun loadCurrentPersonIdentity(list: List<ProcessWOIdentityJson>) {
        radio_group_process_step_two_department.removeAllViews()
        identityList.clear()
        //根据主身份排序
        val newList = list.sortedByDescending { id-> id.major }.toList()
        identityList.addAll(newList)
        if (identityList.size>0) {
            identityList.mapIndexed { index, it ->
                val radio = layoutInflater.inflate(R.layout.snippet_radio_button, null) as RadioButton
                radio.text = if (TextUtils.isEmpty(it.unitLevelName)) it.unitName else it.unitLevelName
                if (index==0) {
                    radio.isChecked = true
                    tv_start_process_step_two_identity.text = it.name + "("+it.unitName+")"
                    identity = it.distinguishedName
                }
                radio.id = 100 + index//这里必须添加id 否则后面获取选中Radio的时候 group.getCheckedRadioButtonId() 拿不到id 会有空指针异常
                val layoutParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, activity?.dip(10f) ?: 10, 0, 0)
                radio_group_process_step_two_department.addView(radio, layoutParams)
            }
        }
        radio_group_process_step_two_department.setOnCheckedChangeListener { _, checkedId ->
            val index = checkedId - 100
            tv_start_process_step_two_identity.text = identityList[index].name + "("+identityList[index].unitName+")"
            identity = identityList[index].distinguishedName

        }
    }

    override fun loadCurrentPersonIdentityFail() {
        XToast.toastShort(activity, getString(R.string.message_get_current_identity_fail))
        (activity as StartProcessActivity).removeFragment()
    }

    override fun startProcessSuccess(workId: String) {
        hideLoadingDialog()
        val name = if (!TextUtils.isEmpty(processName)){ processName}else{getString(R.string.create_manuscript)}
        (activity as StartProcessActivity).goThenKill<TaskWebViewActivity>(TaskWebViewActivity.start(workId, "", name))
    }

    override fun startProcessSuccessNoWork() {
        hideLoadingDialog()
        XToast.toastShort(activity, getString(R.string.message_start_process_success))
        (activity as StartProcessActivity).finish()
    }

    override fun startProcessFail(message:String) {
        hideLoadingDialog()
        XToast.toastShort(activity, getString(R.string.message_start_process_fail_with_error, message))
    }

    override fun startDraftSuccess(work: ProcessDraftWorkData) {
        hideLoadingDialog()
        (activity as StartProcessActivity).goThenKill<TaskWebViewActivity>(TaskWebViewActivity.startDraft(work))
    }

    override fun startDraftFail(message: String) {
        hideLoadingDialog()
        XToast.toastShort(activity, message)
    }

    private fun startProcess() {
        showLoadingDialog()
        mPresenter.startProcess("", identity, processId)
    }

    private fun startDraft() {
        showLoadingDialog()
        mPresenter.startDraft("", identity, processId)
    }

}
