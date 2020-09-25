package com.zpj.shouji.market.ui.fragment.manager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.felix.atoast.library.AToast;
import com.sunfusheng.ExpandableGroupRecyclerViewAdapter;
import com.sunfusheng.GroupRecyclerViewAdapter;
import com.sunfusheng.GroupViewHolder;
import com.sunfusheng.StickyHeaderDecoration;
import com.zpj.downloader.ZDownloader;
import com.zpj.downloader.constant.Error;
import com.zpj.downloader.core.DownloadManager;
import com.zpj.downloader.core.DownloadMission;
import com.zpj.downloader.util.FileUtil;
import com.zpj.fragmentation.dialog.impl.AttachListDialogFragment;
import com.zpj.fragmentation.dialog.impl.CheckDialogFragment;
import com.zpj.http.core.IHttp;
import com.zpj.http.core.ObservableTask;
import com.zpj.shouji.market.R;
import com.zpj.fragmentation.BaseFragment;
import com.zpj.shouji.market.constant.Keys;
import com.zpj.shouji.market.download.AppDownloadMission;
import com.zpj.shouji.market.event.StartFragmentEvent;
import com.zpj.shouji.market.glide.GlideApp;
import com.zpj.shouji.market.model.InstalledAppInfo;
import com.zpj.shouji.market.ui.fragment.detail.AppDetailFragment;
import com.zpj.utils.AppUtils;
import com.zpj.utils.ClickHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DownloadManagerFragment extends BaseFragment
        implements DownloadManager.DownloadManagerListener,
        GroupRecyclerViewAdapter.OnItemClickListener<DownloadManagerFragment.DownloadWrapper> {

    private final List<List<DownloadWrapper>> downloadTaskList = new ArrayList<>();

    private ExpandCollapseGroupAdapter expandableAdapter;

    public static DownloadManagerFragment newInstance(boolean showToolbar) {
        Bundle args = new Bundle();
        args.putBoolean(Keys.SHOW_TOOLBAR, showToolbar);
        DownloadManagerFragment fragment = new DownloadManagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void start(boolean showToolbar) {
        StartFragmentEvent.start(DownloadManagerFragment.newInstance(showToolbar));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_download;
    }

    @Override
    protected boolean supportSwipeBack() {
        return true;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null && getArguments().getBoolean(Keys.SHOW_TOOLBAR, false)) {
            toolbar.setVisibility(View.VISIBLE);
//            findViewById(R.id.shadow_view).setVisibility(View.VISIBLE);
            setToolbarTitle("下载管理");
        } else {
            setSwipeBackEnable(false);
        }

        ZDownloader.getDownloadManager().setDownloadManagerListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expandableAdapter = new ExpandCollapseGroupAdapter(getContext(), downloadTaskList);
        recyclerView.setAdapter(expandableAdapter);
        recyclerView.addItemDecoration(new StickyHeaderDecoration());
        expandableAdapter.setOnItemClickListener(this);
        postOnEnterAnimationEnd(this::loadDownloadMissions);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        ZDownloader.getDownloadManager().removeDownloadManagerListener(this);
        super.onDestroy();
    }

    @Override
    public void onItemClick(GroupRecyclerViewAdapter<DownloadWrapper> adapter, GroupViewHolder holder, DownloadWrapper data, int groupPosition, int childPosition) {
        if (adapter.isHeader(groupPosition, childPosition)) {
            List<DownloadWrapper> list = adapter.getGroupItemsWithoutHeaderFooter(groupPosition);
            if (list.size() == 1 && list.get(0).getMission() == null) {
                return;
            }
            if (expandableAdapter.isExpand(groupPosition)) {
                expandableAdapter.collapseGroup(groupPosition, true);
            } else {
                expandableAdapter.expandGroup(groupPosition, true);
            }
            expandableAdapter.updateItem(groupPosition, childPosition, expandableAdapter.getItem(groupPosition, childPosition));
        } else if (data.getMission() != null) {
            AppDownloadMission mission = data.getMission();
            if (mission.isFinished()) {
//                mission.openFile(getContext());
                mission.install();
            } else {
                AppDetailFragment.start(mission.getAppType(), mission.getAppId());
            }
        } else {
            // TODO onclick
            AToast.normal("TODO onClick");
        }
    }

    @Override
    public void onMissionAdd(DownloadMission mission) {
        loadDownloadMissions();
    }

    @Override
    public void onMissionDelete(DownloadMission mission) {
        loadDownloadMissions();
    }

    @Override
    public void onMissionFinished(DownloadMission mission) {
        loadDownloadMissions();
    }

    private void loadDownloadMissions() {
        new ObservableTask<>(
                emitter -> {
                    downloadTaskList.clear();
                    List<DownloadWrapper> downloadingList = new ArrayList<>();
                    List<DownloadWrapper> downloadedList = new ArrayList<>();
                    downloadTaskList.add(downloadingList);
                    downloadTaskList.add(downloadedList);
                    downloadingList.add(new DownloadWrapper("下载中"));
                    downloadedList.add(new DownloadWrapper("已完成"));
                    for (AppDownloadMission mission : ZDownloader.getAllMissions(true, AppDownloadMission.class)) {
                        downloadingList.add(new DownloadWrapper(mission));
                    }
                    for (AppDownloadMission mission : ZDownloader.getAllMissions(false, AppDownloadMission.class)) {
                        downloadedList.add(new DownloadWrapper(mission));
                    }
                    Log.d("DownloadFragment", "getAllMissions=" + ZDownloader.getAllMissions());
                    Log.d("DownloadFragment", "downloadingList.size=" + downloadingList.size());
                    Log.d("DownloadFragment", "downloadedList.size=" + downloadedList.size());
                    if (downloadingList.size() == 1) {
                        downloadingList.add(new DownloadWrapper());
                    }
                    if (downloadedList.size() == 1) {
                        downloadedList.add(new DownloadWrapper());
                    }
//                    emitter.onNext(new Object());
                    emitter.onComplete();
                })
//                .onSuccess(data -> expandableAdapter.notifyDataSetChanged())
                .onComplete(new IHttp.OnCompleteListener() {
                    @Override
                    public void onComplete() throws Exception {
                        expandableAdapter.notifyDataSetChanged();
                    }
                })
                .subscribe();
    }

    public static class DownloadWrapper {

        private AppDownloadMission mission;
        private String title;

        DownloadWrapper() {

        }

        DownloadWrapper(AppDownloadMission mission) {
            this.mission = mission;
        }

        DownloadWrapper(String title) {
            this.title = title;
        }

        public AppDownloadMission getMission() {
            return mission;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class ExpandCollapseGroupAdapter extends ExpandableGroupRecyclerViewAdapter<DownloadWrapper> {

        private static final int TYPE_CHILD = 111;
        private static final int TYPE_EMPTY = 222;
        private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

        private final Map<DownloadMission, MissionObserver> map = new HashMap<>();

        public ExpandCollapseGroupAdapter(Context context, List<List<DownloadWrapper>> groups) {
            super(context, groups);
        }

        @Override
        public int getChildItemViewType(int groupPosition, int childPosition) {
            List<DownloadWrapper> list = getGroupItemsWithoutHeaderFooter(groupPosition);
            if (list.size() == 1 && list.get(0).getMission() == null) {
                return TYPE_EMPTY;
            }
            return TYPE_CHILD;
        }

        @Override
        public boolean showHeader() {
            return true;
        }

        @Override
        public boolean showFooter() {
            return false;
        }

        @Override
        public int getHeaderLayoutId(int viewType) {
            return R.layout.item_download_header;
        }

        @Override
        public int getChildLayoutId(int viewType) {
            if (viewType == TYPE_CHILD) {
                return R.layout.item_download;
            } else if (viewType == TYPE_EMPTY) {
                return R.layout.item_empty;
            } else {
                return 0;
            }
        }

        @Override
        public int getFooterLayoutId(int viewType) {
            return 0;
        }

        @Override
        public void onBindHeaderViewHolder(GroupViewHolder holder, DownloadWrapper item, int groupPosition) {
            ((TextView) holder.get(R.id.text_title)).setText(item.getTitle());
            holder.setImageResource(R.id.img_arrow, isExpand(groupPosition) ? R.drawable.ic_keyboard_arrow_down_black_24dp : R.drawable.ic_keyboard_arrow_right_black_24dp);
            if (groupPosition == 0) {
                holder.setImageResource(R.id.img_icon, R.drawable.ic_downloading);
            } else {
                holder.setImageResource(R.id.img_icon, R.drawable.ic_downloaded);
            }
        }

        @Override
        public void onBindChildViewHolder(GroupViewHolder holder, DownloadWrapper item, int groupPosition, int childPosition) {
            int viewType = getChildItemViewType(groupPosition, childPosition);
            if (viewType == TYPE_CHILD) {
                AppDownloadMission mission = item.getMission();
                ImageView ivIcon = holder.get(R.id.item_icon);
                if (TextUtils.isEmpty(mission.getAppIcon())) {
                    if (AppUtils.isInstalled(context, mission.getPackageName())) {
                        InstalledAppInfo appInfo = new InstalledAppInfo();
                        appInfo.setTempInstalled(true);
                        appInfo.setPackageName(mission.getPackageName());
                        GlideApp.with(context).load(appInfo).into(ivIcon);
                    } else {
                        ivIcon.setImageResource(FileUtil.getFileTypeIconId(mission.getTaskName()));
                    }
                } else {
                    Glide.with(context).load(mission.getAppIcon()).into(ivIcon);
                }

                holder.setText(R.id.item_name, mission.getAppName());
                holder.get(R.id.btn_download).setOnClickListener(v -> {
                    if (mission.canStart()) {
                        mission.start();
                    } else if (mission.canPause()) {
                        mission.pause();
                    }
//                    if (mission.isPause() || mission.isError()) {
//                        mission.start();
//                    } else {
//                        mission.pause();
//                    }
                });
                updateStatus(holder, mission);

                ClickHelper.with(holder.itemView)
                        .setOnLongClickListener(new ClickHelper.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v, float x, float y) {
                                ArrayList<String> titleList = new ArrayList<>();
                                if (!mission.isFinished()) {
                                    if (mission.canPause()) {
                                        titleList.add("暂停");
                                    } else if (mission.canStart()) {
                                        titleList.add("开始");

                                    }
//                                    if (!mission.isRunning()) {
//                                        titleList.add("删除");
//                                    } else {
//                                        titleList.add("暂停");
//                                    }
                                    titleList.add("删除");
                                    titleList.add("复制链接");
                                    titleList.add("任务详情");
                                } else {
                                    titleList.add("打开");
                                    titleList.add("删除");
                                    titleList.add("复制链接");
                                    titleList.add("任务详情");
                                    titleList.add("分享");
                                }
                                new AttachListDialogFragment<String>()
                                        .addItems(titleList)
                                        .setOnSelectListener((position, text) -> {
                                            switch (text) {
                                                case "开始":
                                                    mission.start();
                                                    break;
                                                case "暂停":
                                                    mission.pause();
                                                    break;
                                                case "打开":
//                                                    mission.openFile(context);
                                                    mission.install();
                                                    break;
                                                case "删除":
                                                    new CheckDialogFragment()
                                                            .setChecked(true)
                                                            .setCheckTitle("删除已下载的文件")
                                                            .setTitle("确定删除？")
                                                            .setContent("你将删除下载任务：" + mission.getTaskName())
                                                            .setPositiveButton(fragment -> mission.delete())
                                                            .show(context);
                                                    break;
                                                case "复制链接":
                                                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                    cm.setPrimaryClip(ClipData.newPlainText(null, mission.getOriginUrl()));
                                                    AToast.success("已复制到粘贴板");
                                                    break;
                                                case "任务详情":
                                                    AToast.normal("TODO");
                                                    break;
                                                case "分享":
                                                    AToast.normal("分享");
                                                    break;
                                                case "检验":
                                                    AToast.normal("检验");
                                                    break;
                                            }
                                        })
                                        .setTouchPoint(x, y)
                                        .show(context);
                                return true;
                            }
                        });

            } else if (viewType == TYPE_EMPTY) {

            }
        }

        @Override
        public void onBindFooterViewHolder(GroupViewHolder holder, DownloadWrapper item, int groupPosition) {

        }

        private void updateStatus(GroupViewHolder holder, AppDownloadMission mission) {
            boolean isFinished = mission.isFinished();

            String size = mission.getFileSizeStr();
            if (isFinished) {
                size += (" " + formatter.format(mission.getCreateTime()));
            } else {
                size += ("/" + mission.getDownloadedSizeStr());
            }
            holder.setText(R.id.item_size, size);
            holder.setVisible(R.id.item_status, !isFinished);
            holder.setVisible(R.id.btn_download, !isFinished);
            ProgressBar progressBar = holder.get(R.id.progress_bar);
            progressBar.setMax(100);
            if (isFinished) {
                progressBar.setVisibility(View.GONE);
                holder.itemView.setBackground(new ColorDrawable(Color.WHITE));
                map.remove(mission);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress((int) mission.getProgress());

                if (mission.isPause()) {
                    holder.setText(R.id.item_status, mission.getStatus().toString());
                    holder.setImageResource(R.id.btn_download, R.drawable.download_item_resume_icon_style2);
                } else if (mission.isError()) {
                    holder.setText(R.id.item_status, mission.getStatus().toString() + ":" + mission.getErrCode());
                    holder.setImageResource(R.id.btn_download, R.drawable.download_item_retry_icon_style2);
                } else {
                    holder.setText(R.id.item_status, mission.getProgressStr());
                    holder.setImageResource(R.id.btn_download, R.drawable.download_item_pause_icon_style2);
                }

                MissionObserver missionListener = map.get(mission);
                if (missionListener == null) {
                    missionListener = new MissionObserver(holder, mission);
                    map.put(mission, missionListener);
                    mission.addListener(missionListener);
                } else {
                    missionListener.updateHolder(holder);
                }
            }
        }

        class MissionObserver implements DownloadMission.MissionListener {

            private GroupViewHolder holder;
            private AppDownloadMission mission;

            MissionObserver(GroupViewHolder holder, AppDownloadMission mission) {
                this.holder = holder;
                this.mission = mission;
            }

            public void updateHolder(GroupViewHolder holder) {
                this.holder = holder;
            }

            @Override
            public void onInit() {
                updateStatus(holder, mission);
            }

            @Override
            public void onStart() {
                updateStatus(holder, mission);
            }

            @Override
            public void onPause() {
                updateStatus(holder, mission);
            }

            @Override
            public void onWaiting() {
                updateStatus(holder, mission);
            }

            @Override
            public void onRetry() {
                updateStatus(holder, mission);
            }

            @Override
            public void onProgress(DownloadMission.UpdateInfo update) {
                holder.setText(R.id.item_size, update.getFileSizeStr() + "/" + update.getDownloadedSizeStr());
                holder.setText(R.id.item_status, mission.getProgressStr());
                ProgressBar progressBar = holder.get(R.id.progress_bar);
                progressBar.setProgress((int) mission.getProgress());
            }

            @Override
            public void onFinish() {
                updateStatus(holder, mission);
//                expandableAdapter.removeItem(0, 0, true);
//                expandableAdapter.insertItem(1, 0, new DownloadWrapper(mission), true);
            }

            @Override
            public void onError(Error error) {
                updateStatus(holder, mission);
            }

            @Override
            public void onDelete() {

            }

            @Override
            public void onClear() {

            }
        }

    }

}