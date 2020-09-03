package com.zpj.shouji.market.ui.fragment.subject;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.donkingliang.consecutivescroller.ConsecutiveScrollerLayout;
import com.felix.atoast.library.AToast;
import com.zpj.fragmentation.BaseFragment;
import com.zpj.http.core.IHttp;
import com.zpj.http.parser.html.nodes.Document;
import com.zpj.http.parser.html.nodes.Element;
import com.zpj.recyclerview.EasyRecyclerView;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.IEasy;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpApi;
import com.zpj.shouji.market.constant.Keys;
import com.zpj.shouji.market.event.StartFragmentEvent;
import com.zpj.shouji.market.glide.blur.CropBlurTransformation;
import com.zpj.shouji.market.model.AppInfo;
import com.zpj.shouji.market.model.SubjectInfo;
import com.zpj.shouji.market.ui.fragment.AppListFragment;
import com.zpj.shouji.market.ui.fragment.detail.AppDetailFragment;
import com.zpj.widget.statelayout.StateLayout;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SubjectDetailFragment extends BaseFragment
        implements IEasy.OnBindViewHolderListener<AppInfo>,
        IEasy.OnItemClickListener<AppInfo> {

    private final List<AppInfo> appInfoList = new ArrayList<>();


    private StateLayout stateLayout;
    private EasyRecyclerView<AppInfo> recyclerView;

    private String id;
    private String title;

    private boolean isLightStyle = true;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_subject_detail;
    }

    @Override
    protected boolean supportSwipeBack() {
        return true;
    }

//    public static void start(String url, String title) {
//        Bundle args = new Bundle();
//        args.putString(Keys.DEFAULT_URL, url);
//        args.putString(Keys.TITLE, title);
//        SubjectDetailFragment fragment = new SubjectDetailFragment();
//        fragment.setArguments(args);
//        StartFragmentEvent.start(fragment);
//    }

    public static void start(SubjectInfo subjectInfo) {
        Bundle args = new Bundle();
        args.putString(Keys.ID, subjectInfo.getId());
        args.putString(Keys.TITLE, subjectInfo.getTitle());
        args.putString(Keys.INFO, subjectInfo.getM());
        args.putString(Keys.CONTENT, subjectInfo.getComment());
        args.putString(Keys.ICON, subjectInfo.getIcon());
        SubjectDetailFragment fragment = new SubjectDetailFragment();
        fragment.setArguments(args);
        StartFragmentEvent.start(fragment);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {

        stateLayout = findViewById(R.id.state_layout);
        if (getArguments() != null) {
            id = getArguments().getString(Keys.ID, "");
            stateLayout.showLoadingView();


            TextView tvTitle = findViewById(R.id.tv_title);
            TextView tvInfo = findViewById(R.id.tv_info);
            TextView tvDesc = findViewById(R.id.tv_desc);
            ImageView ivBg = findViewById(R.id.iv_bg);
            ImageView ivIcon = findViewById(R.id.iv_icon);

            title = getArguments().getString(Keys.TITLE, "Title");
            setToolbarTitle(title);
            toolbar.getCenterTextView().setAlpha(0);
            tvTitle.setText(title);

            tvInfo.setText(getArguments().getString(Keys.INFO, ""));
            tvDesc.setText(getArguments().getString(Keys.CONTENT, ""));

            Glide.with(context).load(getArguments().getString(Keys.ICON)).into(ivIcon);
            Glide.with(context)
                    .load(getArguments().getString(Keys.ICON))
                    .apply(
                            RequestOptions
                                    .bitmapTransform(new CropBlurTransformation(25, 0.1f))
                                    .error(R.drawable.bg_member_default)
                                    .placeholder(R.drawable.bg_member_default)
                    )
                    .into(ivBg);
        } else {
            pop();
            AToast.error("查看专题失败！");
            return;
        }

        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setBackground(new ColorDrawable(Color.TRANSPARENT), true);

        ConsecutiveScrollerLayout scrollerLayout = findViewById(R.id.layout_scroller);
        scrollerLayout.setOnVerticalScrollChangeListener(new ConsecutiveScrollerLayout.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollY, int oldScrollY, int scrollState) {
                if (scrollY < toolbar.getMeasuredHeight()) {
                    float alpha = (float) scrollY / toolbar.getMeasuredHeight();
                    int color = alphaColor(Color.WHITE, alpha * 0.95f);
                    toolbar.setBackgroundColor(color);
                    isLightStyle = alpha <= 0.5;
                    toolbar.setLightStyle(isLightStyle);
                    toolbar.getCenterTextView().setAlpha(alpha);

                    if (isLightStyle) {
                        lightStatusBar();
//                        setToolbarTitle("");
                    } else {
                        darkStatusBar();
//                        setToolbarTitle(title);
                    }
                } else {
                    toolbar.setLightStyle(false);
                    darkStatusBar();
//                    setToolbarTitle(title);
                    toolbar.getCenterTextView().setAlpha(1);
                }
            }
        });

        recyclerView = new EasyRecyclerView<>(findViewById(R.id.recycler_view));
        recyclerView.setData(appInfoList)
                .setItemRes(R.layout.item_app_linear)
                .onBindViewHolder(this)
                .onItemClick(this)
                .build();
        getData();
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        if (isLightStyle) {
            lightStatusBar();
        } else {
            darkStatusBar();
        }
    }

    @Override
    public void onBindViewHolder(EasyViewHolder holder, List<AppInfo> list, int position, List<Object> payloads) {
        final AppInfo appInfo = list.get(position);
        holder.getTextView(R.id.tv_title).setText(appInfo.getAppTitle());
        holder.getTextView(R.id.tv_info).setText(appInfo.getAppSize() + " | " + appInfo.getAppInfo());
        holder.getTextView(R.id.tv_desc).setText(appInfo.getAppComment());
        Glide.with(context).load(appInfo.getAppIcon()).into(holder.getImageView(R.id.iv_icon));
    }

    @Override
    public void onClick(EasyViewHolder holder, View view, AppInfo data) {
        AppDetailFragment.start(data);
    }

    private void getData() {
        HttpApi.get("http://tt.shouji.com.cn/androidv3/special_list_xml.jsp?id=" + id)
                .onSuccess(new IHttp.OnSuccessListener<Document>() {
                    @Override
                    public void onSuccess(Document data) throws Exception {
                        for (Element element : data.select("item")) {
                            appInfoList.add(AppInfo.parse(element));
                        }
                        postOnEnterAnimationEnd(() -> {
                            recyclerView.notifyDataSetChanged();
                            stateLayout.showContentView();
                        });
                    }
                })
                .onError(new IHttp.OnErrorListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        AToast.error("出错了！" + throwable.getMessage());
                    }
                })
                .subscribe();
    }

    public static int alphaColor(int color, float alpha) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & color;
        return a + rgb;
    }


}