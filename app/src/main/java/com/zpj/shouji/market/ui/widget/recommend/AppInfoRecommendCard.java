package com.zpj.shouji.market.ui.widget.recommend;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.felix.atoast.library.AToast;
import com.zpj.http.parser.html.nodes.Document;
import com.zpj.http.parser.html.nodes.Element;
import com.zpj.http.parser.html.select.Elements;
import com.zpj.recyclerview.EasyRecyclerView;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.shouji.market.R;
import com.zpj.shouji.market.api.HttpApi;
import com.zpj.shouji.market.api.HttpPreLoader;
import com.zpj.shouji.market.api.PreloadApi;
import com.zpj.shouji.market.model.AppInfo;
import com.zpj.shouji.market.ui.fragment.detail.AppDetailFragment;

import java.util.List;

public abstract class AppInfoRecommendCard extends RecommendCard<AppInfo> {

    public AppInfoRecommendCard(Context context) {
        this(context, null);
    }

    public AppInfoRecommendCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppInfoRecommendCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        if (getKey() != null) {
            if (HttpPreLoader.getInstance().hasKey(getKey())) {
                HttpPreLoader.getInstance().setLoadListener(getKey(), this::onGetDoc);
            } else {
                if (getKey() == PreloadApi.HOME_SOFT) {
                    HttpApi.homeRecommendSoftApi()
                            .onSuccess(this::onGetDoc)
                            .subscribe();
                } else if (getKey() == PreloadApi.HOME_GAME) {
                    HttpApi.homeRecommendGameApi()
                            .onSuccess(this::onGetDoc)
                            .subscribe();
                }
            }
        }
    }

    private void onGetDoc(Document document) {
        Elements elements = document.select("item");
        for (Element element : elements) {
            AppInfo info = AppInfo.parse(element);
            if (info == null) {
                continue;
            }
            list.add(info);
            if (list.size() == 8) {
                break;
            }
        }
        recyclerView.notifyDataSetChanged();
    }

    @Override
    protected void buildRecyclerView(EasyRecyclerView<AppInfo> recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(context, 4) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
    }

    @Override
    public void onBindViewHolder(EasyViewHolder holder, List<AppInfo> list, int position, List<Object> payloads) {
        AppInfo info = list.get(position);
        holder.getTextView(R.id.item_title).setText(info.getAppTitle());
        holder.getTextView(R.id.item_info).setText(info.getAppSize());
        Glide.with(context).load(info.getAppIcon()).into(holder.getImageView(R.id.item_icon));
        holder.getView(R.id.tv_download).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AToast.normal("TODO Download");
            }
        });
    }

    @Override
    public void onClick(EasyViewHolder holder, View view, AppInfo data) {
        AppDetailFragment.start(data);
    }

    public abstract PreloadApi getKey();

    public String getUrl() {
        return "";
    }

    @Override
    public int getItemRes() {
        return R.layout.item_app_grid;
    }
}
