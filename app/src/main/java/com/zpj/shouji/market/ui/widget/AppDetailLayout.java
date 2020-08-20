package com.zpj.shouji.market.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.shehuan.niv.NiceImageView;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.glide.blur.CropBlurTransformation;
import com.zpj.shouji.market.model.AppDetailInfo;
import com.zpj.widget.toolbar.BaseToolBar;
import com.zpj.widget.toolbar.ZToolBar;

import net.lucode.hackware.magicindicator.MagicIndicator;

import top.defaults.drawabletoolbox.DrawableBuilder;


/**
 * 处理 header + tab + viewPager + recyclerView
 * Description:NestedScrolling2机制下的嵌套滑动，实现NestedScrollingParent2接口下，处理fling效果的区别
 * @author hufeiyang
 */
public class AppDetailLayout extends FrameLayout {

    private CollapsingToolbarLayout toolbarLayout;
    private final LinearLayout headerLayout;
    private final MagicIndicator magicIndicator;
    private final ViewPager mViewPager;

    private final NiceImageView icon;
    private final TextView title;
    private final TextView tvVersion;
    private final TextView tvSize;
    private final TextView shortInfo;
    private final TextView shortIntroduce;

    private BaseToolBar toolBar;
    private View buttonBarLayout;
    private NiceImageView ivToolbarAvater;
    private TextView tvToolbarName;

    private int mTopViewHeight;


    public AppDetailLayout(Context context) {
        this(context, null);
    }

    public AppDetailLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppDetailLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.WHITE);
        LayoutInflater.from(context).inflate(R.layout.layout_app_detail2, this, true);
        toolbarLayout = findViewById(R.id.collapsingToolbar);
        headerLayout = findViewById(R.id.layout_header);
        mViewPager = findViewById(R.id.view_pager);
        magicIndicator = findViewById(R.id.magic_indicator);

        icon = findViewById(R.id.iv_icon);
        title = findViewById(R.id.tv_title);
        tvVersion = findViewById(R.id.tv_version);
        tvSize = findViewById(R.id.tv_size);
        shortInfo = findViewById(R.id.tv_info);
        shortIntroduce = findViewById(R.id.tv_detail);

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, i) -> {
            float alpha = (float) Math.abs(i) / appBarLayout1.getTotalScrollRange();
            alpha = Math.min(1f, alpha);
            buttonBarLayout.setAlpha(alpha);
            if (alpha >= 1f) {
                headerLayout.setAlpha(0f);
            } else {
                headerLayout.setAlpha(1f);
            }
        });
    }

    public void loadInfo(AppDetailInfo info) {
        Glide.with(getContext())
                .load(info.getIconUrl())
                .into(icon);
        Glide.with(getContext())
                .load(info.getIconUrl())
                .into(ivToolbarAvater);

        Glide.with(getContext())
                .asDrawable()
                .load(info.getIconUrl())
                .apply(
                        RequestOptions
                                .bitmapTransform(new CropBlurTransformation(25, 1f))
                                .error(R.drawable.bg_member_default)
                                .placeholder(R.drawable.bg_member_default)
                )
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        toolbarLayout.setBackground(resource);
                    }
                });

        title.setText(info.getName());
        tvVersion.setText(info.getVersion());
        tvSize.setText(info.getSize());
        tvToolbarName.setText(info.getName());
        shortInfo.setText(info.getLanguage() + " | " + info.getFee()
                + " | " + info.getAds() + " | " + info.getFirmware());
        shortIntroduce.setText(info.getLineInfo());

        tvVersion.setBackground(new DrawableBuilder()
                .rectangle()
                .rounded()
                .strokeColor(getResources().getColor(R.color.colorPrimary))
                .solidColor(getResources().getColor(R.color.colorPrimary))
                .build());
        tvSize.setBackground(new DrawableBuilder()
                .rectangle()
                .rounded()
                .strokeColor(getResources().getColor(R.color.light_blue1))
                .solidColor(getResources().getColor(R.color.light_blue1))
                .build());
        int color = Color.WHITE;
        title.setTextColor(color);
        tvVersion.setTextColor(color);
        tvSize.setTextColor(color);
        tvToolbarName.setTextColor(color);
        shortInfo.setTextColor(color);
        shortIntroduce.setTextColor(color);
    }

    public void bindToolbar(ZToolBar toolBar) {
        this.toolBar = toolBar;
        buttonBarLayout = toolBar.getCenterCustomView();
        buttonBarLayout.setAlpha(0);
        ivToolbarAvater = toolBar.findViewById(R.id.toolbar_avatar);
        tvToolbarName = toolBar.findViewById(R.id.toolbar_name);
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public MagicIndicator getMagicIndicator() {
        return magicIndicator;
    }

}