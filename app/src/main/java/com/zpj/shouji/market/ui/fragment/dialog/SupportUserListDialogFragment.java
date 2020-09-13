package com.zpj.shouji.market.ui.fragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.bumptech.glide.Glide;
import com.zpj.fragmentation.dialog.base.BottomDialogFragment;
import com.zpj.http.core.IHttp;
import com.zpj.http.parser.html.nodes.Element;
import com.zpj.recyclerview.EasyRecyclerView;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.IEasy;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpApi;
import com.zpj.shouji.market.model.SupportUserInfo;
import com.zpj.shouji.market.ui.fragment.profile.ProfileFragment;
import com.zpj.utils.ScreenUtils;
import com.zpj.widget.statelayout.StateLayout;

import java.util.ArrayList;
import java.util.List;

public class SupportUserListDialogFragment extends BottomDialogFragment
         implements IEasy.OnBindViewHolderListener<SupportUserInfo> {

    private final List<SupportUserInfo> userInfoList = new ArrayList<>();

    private StateLayout stateLayout;
    private EasyRecyclerView<SupportUserInfo> recyclerView;

    private String themeId;


    private boolean isShow;
    private boolean hasInit;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            stateLayout.showContentView();
            recyclerView.notifyDataSetChanged();
        }
    };

    public static SupportUserListDialogFragment with(Context context) {
        return new SupportUserListDialogFragment();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.layout_popup_support_user_list;
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);

        getContentView().setMinimumHeight(ScreenUtils.getScreenHeight(context) / 3);

        findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());

        stateLayout = findViewById(R.id.state_layout);
        recyclerView = new EasyRecyclerView<>(findViewById(R.id.recycler_view));
        recyclerView.setData(userInfoList)
                .setItemRes(R.layout.item_menu)
                .setLayoutManager(new LinearLayoutManager(getContext()))
                .onBindViewHolder(this)
                .onItemClick((holder, view1, data) -> {
                    dismiss();
                    ProfileFragment.start(data.getUserId(), false);
                })
                .build();
        stateLayout.showLoadingView();
        getSupportUserList();

    }

    @Override
    public void onBindViewHolder(EasyViewHolder holder, List<SupportUserInfo> list, int position, List<Object> payloads) {
        SupportUserInfo userInfo = list.get(position);
        Glide.with(context).load(userInfo.getUserLogo()).into(holder.getImageView(R.id.iv_icon));
        holder.setText(R.id.tv_title, userInfo.getNickName());
    }

    @Override
    protected int getMaxHeight() {
        return ScreenUtils.getScreenHeight(context) - ScreenUtils.getStatusBarHeight(context) - ScreenUtils.dp2pxInt(context, 56);
    }

//    @Override
//    protected void onShow() {
//        super.onShow();
//        isShow = true;
//        if (hasInit) {
//            post(runnable);
//        }
//    }

    private void getSupportUserList() {
        HttpApi.getSupportUserListApi(themeId)
                .onSuccess(data -> {
                    userInfoList.clear();
                    for (Element element : data.select("fuser")) {
                        SupportUserInfo userInfo = new SupportUserInfo();
                        userInfo.setNickName(element.selectFirst("fname").text());
                        userInfo.setUserId(element.selectFirst("fid").text());
                        userInfo.setUserLogo(element.selectFirst("avatar").text());
                        userInfoList.add(userInfo);
                    }
//                    recyclerView.notifyDataSetChanged();
//                    stateLayout.showContentView();
                    postDelayed(runnable, 250);
//                    if (isShow) {
//                        hasInit = false;
//                        post(runnable);
//                    } else {
//                        hasInit = true;
//                    }
                })
                .onError(new IHttp.OnErrorListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        stateLayout.showErrorView(throwable.getMessage());
                    }
                })
                .subscribe();
    }


    public SupportUserListDialogFragment setThemeId(String id) {
        this.themeId = id;
        return this;
    }

}