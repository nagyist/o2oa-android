package net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.process

import android.graphics.BitmapFactory
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import kotlinx.android.synthetic.main.fragment_todo_task.*
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2CustomStyle
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.base.BaseMVPViewPagerFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.app.o2.webview.TaskWebViewActivity
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.CommonRecyclerViewHolder
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.core.component.adapter.SwipeRefreshCommonRecyclerViewAdapter
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2.TaskData
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XToast
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.go
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.gone
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.extension.visible
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.widgets.CircleImageView
import org.jetbrains.anko.dip


class TaskFragment : BaseMVPViewPagerFragment<TaskContract.View, TaskContract.Presenter>(), TaskContract.View {

    override var mPresenter: TaskContract.Presenter = TaskPresenter()

    override fun layoutResId(): Int = R.layout.fragment_todo_task

    companion object {
        val APPLICATION_ID_KEY = "APPLICATION_ID_KEY_TASK_FRAGMENT"
    }

    var application: String = ""
    var lastTaskId: String = ""
    var isRefresh = false
    var isLoading = false
    val taskDatas = ArrayList<TaskData>()
    val adapter: SwipeRefreshCommonRecyclerViewAdapter<TaskData> by lazy {
        object : SwipeRefreshCommonRecyclerViewAdapter<TaskData>(activity, taskDatas, R.layout.item_todo_list) {
            override fun convert(holder: CommonRecyclerViewHolder?, data: TaskData?) {
                val time = data?.startTime?.substring(0, 10) ?: ""
                val title = if (TextUtils.isEmpty(data?.title)) { getString(R.string.no_title) } else { data?.title }
                holder?.setText(R.id.todo_card_view_title_id, title)
                        ?.setText(R.id.todo_card_view_content_id, "【${data?.processName}】")
                        ?.setText(R.id.todo_card_view_node_id, data?.activityName)
                        ?.setText(R.id.todo_card_view_time_id, time)
                val icon = holder?.getView<CircleImageView>(R.id.todo_card_view_icon_id)
                icon?.tag = data?.application
                (activity as TaskListActivity).loadApplicationIcon(holder?.convertView, data?.application)
            }
        }
    }

    override fun initUI() {
        application = arguments?.getString(APPLICATION_ID_KEY) ?: ""
        todo_task_refresh_layout_id.touchSlop = activity?.dip(70f) ?: 70
        todo_task_refresh_layout_id.setColorSchemeResources(R.color.z_color_refresh_scuba_blue,
                R.color.z_color_refresh_red, R.color.z_color_refresh_purple, R.color.z_color_refresh_orange)
        todo_task_refresh_layout_id.recyclerViewPageNumber = O2.DEFAULT_PAGE_NUMBER
        todo_task_refresh_layout_id.setOnRefreshListener {
            if (!isLoading && !isRefresh) {
                getDatas(true)
                isRefresh = true
            }
        }
        todo_task_refresh_layout_id.setOnLoadMoreListener {
            if (!isLoading && !isRefresh) {
                if (TextUtils.isEmpty(lastTaskId)) {
                    getDatas(true)
                } else {
                    getDatas(false)
                }
                isLoading = true
            }
        }

        todo_task_list_id.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        todo_task_list_id.adapter = adapter
        adapter.setOnItemClickListener { _, position ->
            (activity as TaskListActivity).go<TaskWebViewActivity>(TaskWebViewActivity.start(taskDatas[position].work, "", taskDatas[position].title))
        }
    }


    override fun lazyLoad() {
        getDatas(true)
        isRefresh = true
    }

    override fun findTaskList(list: List<TaskData>) {
        if (isRefresh) {
            taskDatas.clear()
            taskDatas.addAll(list)
            if (taskDatas.size > 0) {
                lastTaskId = taskDatas[taskDatas.size - 1].id
                tv_no_data.gone()
                todo_task_refresh_layout_id.visible()
                adapter.notifyDataSetChanged()
            } else {
                tv_no_data.visible()
                todo_task_refresh_layout_id.gone()
            }
        } else if (isLoading) {
            taskDatas.addAll(list)
            if (taskDatas.size > 0) {
                lastTaskId = taskDatas[taskDatas.size - 1].id
                tv_no_data.gone()
                todo_task_refresh_layout_id.visible()
                adapter.notifyDataSetChanged()
            } else {
                tv_no_data.visible()
                todo_task_refresh_layout_id.gone()
            }
        }
        finishAnimation()
    }

    override fun findTaskListFail() {
        XToast.toastShort(activity, getString(R.string.message_get_todo_list_error))
        taskDatas.clear()
        adapter.notifyDataSetChanged()
        tv_no_data.visible()
        todo_task_refresh_layout_id.gone()
        finishAnimation()
    }

    private fun finishAnimation() {
        if (isRefresh) {
            todo_task_refresh_layout_id.isRefreshing = false
            isRefresh = false
        }
        if (isLoading) {
            todo_task_refresh_layout_id.setLoading(false)
            isLoading = false
        }
    }

    private fun getDatas(flag: Boolean) {
        if (flag) {
            mPresenter.findTaskList(application, O2.FIRST_PAGE_TAG, O2.DEFAULT_PAGE_NUMBER)
        } else {
            mPresenter.findTaskList(application, lastTaskId, O2.DEFAULT_PAGE_NUMBER)
        }
    }
}
