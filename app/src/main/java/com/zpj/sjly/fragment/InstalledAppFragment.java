package com.zpj.sjly.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.felix.atoast.library.AToast;
import com.hmy.popwindow.PopWindow;
import com.zpj.popupmenuview.OptionMenu;
import com.zpj.popupmenuview.OptionMenuView;
import com.zpj.popupmenuview.PopupMenuView;
import com.zpj.sjly.DetailActivity;
import com.zpj.sjly.R;
import com.zpj.sjly.adapter.AppManagerAdapter;
import com.zpj.sjly.bean.InstalledAppInfo;
import com.zpj.sjly.utils.AppUtil;
import com.zpj.sjly.utils.LoadAppsTask;
import com.zpj.sjly.view.recyclerview.LoadMoreAdapter;
import com.zpj.sjly.view.recyclerview.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.library.SmoothCheckBox;

public class InstalledAppFragment extends BaseFragment implements AppManagerAdapter.OnItemClickListener {

    private static final List<OptionMenu> optionMenus = new ArrayList<>();
    static {
//        optionMenus.add(new OptionMenu("忽略更新"));
        optionMenus.add(new OptionMenu("详细信息"));
        optionMenus.add(new OptionMenu("分享"));
        optionMenus.add(new OptionMenu("卸载"));
        optionMenus.add(new OptionMenu("打开"));
    }

    private LoadAppsTask loadAppsTask;

    private final List<InstalledAppInfo> installedAppInfos = new ArrayList<>();
    private static final List<InstalledAppInfo> USER_APP_LIST = new ArrayList<>();
    private static final List<InstalledAppInfo> SYSTEM_APP_LIST = new ArrayList<>();
    private AppManagerAdapter adapter;
    private RecyclerView recyclerView;
    private SmoothCheckBox checkBox;

    private TextView infoTextView;
    private TextView titleTextView;

    @Nullable
    @Override
    public View onBuildView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_installed_app, null, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppUtil.UNINSTALL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                AToast.success("应用卸载成功！");
                loadInstallApps();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                AToast.normal("应用卸载取消！");
            }
        }
    }

    @Override
    public void lazyLoadData() {

    }

    private void initView(View view) {

        infoTextView = view.findViewById(R.id.text_info);
        infoTextView.setText("扫描中...");
        titleTextView = view.findViewById(R.id.text_title);
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View appFilterLayout = LayoutInflater.from(getContext()).inflate(R.layout.layout_app_filter, null, false);
                PopWindow popWindow = new PopWindow.Builder(getActivity())
                        .setStyle(PopWindow.PopWindowStyle.PopDown)
                        .setView(appFilterLayout)
                        .show(titleTextView);
                appFilterLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popWindow.dismiss();
                        installedAppInfos.clear();
                        if (titleTextView.getText().toString().equals("用户应用")) {
                            installedAppInfos.addAll(SYSTEM_APP_LIST);
                            titleTextView.setText("系统应用");
                        } else {
                            installedAppInfos.addAll(USER_APP_LIST);
                            titleTextView.setText("用户应用");
                        }
                        infoTextView.setText("共计：" + installedAppInfos.size() + " | 已选：0");
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        checkBox = view.findViewById(R.id.checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "" + checkBox.isChecked(), Toast.LENGTH_SHORT).show();
                if (checkBox.isChecked()) {
                    adapter.unSelectAll();
                } else {
                    adapter.selectAll();
                }
            }
        });

        adapter = new AppManagerAdapter(installedAppInfos);
        adapter.setItemClickListener(this);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        LoadMoreWrapper.with(adapter)
                .setLoadMoreEnabled(false)
                .setListener(new LoadMoreAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore(LoadMoreAdapter.Enabled enabled) {
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 获取数据
                                loadInstallApps();
                            }
                        }, 1);
                    }
                })
                .into(recyclerView);
    }

    private void loadInstallApps() {
        loadAppsTask = LoadAppsTask.with(InstalledAppFragment.this)
                .setCallBack(new LoadAppsTask.CallBack() {
                    @Override
                    public void onPostExecute(List<InstalledAppInfo> userAppList, List<InstalledAppInfo> systemAppList) {

                        installedAppInfos.clear();
                        installedAppInfos.addAll(userAppList);
                        titleTextView.setText("用户应用");
                        infoTextView.setText("共计：" + installedAppInfos.size() + " | 已选：0");
                        adapter.notifyDataSetChanged();
                        USER_APP_LIST.clear();
                        USER_APP_LIST.addAll(userAppList);

                        SYSTEM_APP_LIST.clear();
                        SYSTEM_APP_LIST.addAll(systemAppList);
                    }
                });
        loadAppsTask.execute();
    }

    @Override
    public void onItemClick(AppManagerAdapter.ViewHolder holder, int position, InstalledAppInfo updateInfo) {
        if (TextUtils.isEmpty(updateInfo.getId()) || TextUtils.isEmpty(updateInfo.getAppType())) {
            return;
        }
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        if ("game".equals(updateInfo.getAppType())) {
            intent.putExtra("app_site", "sjly:http://tt.shouji.com.cn/androidv3/game_show.jsp?id=" + updateInfo.getId());
        } else {
            intent.putExtra("app_site", "sjly:http://tt.shouji.com.cn/androidv3/soft_show.jsp?id=" + updateInfo.getId());
        }
        getActivity().startActivity(intent);
    }

    @Override
    public void onMenuClicked(View view, InstalledAppInfo appInfo) {
        PopupMenuView popupMenuView = new PopupMenuView(getContext());
        popupMenuView.setOrientation(LinearLayout.HORIZONTAL)
                .setMenuItems(optionMenus)
                .setBackgroundAlpha(getActivity(), 0.9f, 500)
                .setBackgroundColor(Color.WHITE)
                .setOnMenuClickListener(new OptionMenuView.OnOptionMenuClickListener() {
                    @Override
                    public boolean onOptionMenuClick(int position, OptionMenu menu) {
                        popupMenuView.dismiss();
                        switch (position) {
                            case 0:
                                AToast.normal("详细信息");
                                break;
                            case 1:
                                AToast.normal(appInfo.getApkFilePath());
                                AppUtil.shareApk(getContext(), appInfo.getApkFilePath());
                                break;
                            case 2:
                                AppUtil.uninstallApp(getActivity(), appInfo.getPackageName());
                                break;
                            case 3:
                                AppUtil.openApp(getContext(), appInfo.getPackageName());
                                break;
                            default:
                                AToast.warning("未知操作！");
                                break;
                        }
                        return true;
                    }
                }).show(view);
    }

    @Override
    public void onCheckBoxClicked(int allCount, int selectCount) {
        boolean isSelectAll = selectCount == allCount;
//        installedInfo.setText("总计：" + allCount);
        infoTextView.setText("共计：" + installedAppInfos.size() + " | 已选：" + selectCount);
        if (checkBox.isChecked() == isSelectAll) {
            return;
        }
        checkBox.setChecked(isSelectAll, true);
    }

    @Override
    public void onDestroy() {
        if (loadAppsTask != null) {
            loadAppsTask.onDestroy();
        }
        super.onDestroy();
    }

}
