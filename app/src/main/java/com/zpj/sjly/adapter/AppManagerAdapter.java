package com.zpj.sjly.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zpj.sjly.R;
import com.zpj.sjly.bean.InstalledAppInfo;
import com.zpj.sjly.utils.AppUtil;
import com.zpj.sjly.utils.AppUpdateHelper;
import com.zpj.sjly.utils.ExecutorHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.bingoogolapple.photopicker.widget.BGAImageView;
import cn.refactor.library.SmoothCheckBox;

public class AppManagerAdapter extends RecyclerView.Adapter<AppManagerAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ViewHolder holder, int position, InstalledAppInfo updateInfo);
        void onMenuClicked(View view, InstalledAppInfo updateInfo);
        void onCheckBoxClicked(int allCount, int selectCount);
    }

    private Context context;
    private List<InstalledAppInfo> installedAppInfoList;
    private OnItemClickListener onItemClickListener;

    private Set<Integer> selectedSet = new HashSet<>();

    public static class ViewHolder extends RecyclerView.ViewHolder{

        BGAImageView appIcon;
        TextView appName;
        TextView appInfo;
        ImageView moreBtn;
        LinearLayout checkBoxWrapper;
        SmoothCheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.icon_app);
            appName = itemView.findViewById(R.id.text_name);
            appInfo = itemView.findViewById(R.id.text_info);
            moreBtn = itemView.findViewById(R.id.btn_more);
            checkBoxWrapper = itemView.findViewById(R.id.checkbox_wrapper);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    public AppManagerAdapter(List<InstalledAppInfo> installedAppInfoList){
        this.installedAppInfoList = installedAppInfoList;
    }


    @NonNull
    @Override
    public AppManagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_installed_app, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AppManagerAdapter.ViewHolder holder, int position) {
        InstalledAppInfo appInfo = installedAppInfoList.get(position);
        Log.d("onBindViewHolder", "name=" + appInfo.getName());
        Log.d("onBindViewHolder", "size=" + appInfo.getFileLength());
        if (appInfo.getIconDrawable() == null) {
            holder.appIcon.setImageResource(R.mipmap.ic_launcher);
            ExecutorHelper.submit(() -> {
                final Drawable drawable;
                if (appInfo.isTempInstalled()) {
                    drawable = AppUtil.getAppIcon(context, appInfo.getPackageName());
                } else if (appInfo.isTempXPK()){
                    drawable = AppUtil.readApkIcon(context, appInfo.getApkFilePath());
                } else {
                    return;
                }
                appInfo.setIconDrawable(drawable);
                holder.appIcon.post(() -> holder.appIcon.setImageDrawable(drawable));
            });
        } else {
            holder.appIcon.setImageDrawable(appInfo.getIconDrawable());
        }

        holder.appName.setText(appInfo.getName());
        String idStr = AppUpdateHelper.getInstance().getAppIdAndType(appInfo.getPackageName());
        String info;
        if (idStr == null) {
            info = "未收录";
        } else {
            info = "已收录";
        }
        holder.appInfo.setText(appInfo.getVersionName() + " | " + appInfo.getFormattedAppSize() + " | " + info);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(holder, holder.getAdapterPosition(), appInfo);
            }
        });

        boolean select = selectedSet.contains(position);
        holder.checkBox.setChecked(select, select);
        holder.checkBox.setClickable(false);
        holder.checkBoxWrapper.setOnClickListener(v -> {
            boolean isChecked = holder.checkBox.isChecked();
            holder.checkBox.setChecked(!isChecked, !isChecked);
        });
        holder.checkBox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                    selectedSet.add(holder.getAdapterPosition());
                } else {
                    selectedSet.remove(holder.getAdapterPosition());
                }
                if (onItemClickListener != null) {
                    onItemClickListener.onCheckBoxClicked(installedAppInfoList.size(), selectedSet.size());
                }
            }
        });
        holder.moreBtn.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onMenuClicked(v, appInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return installedAppInfoList.size();
    }

    public void setItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void selectAll() {
        selectedSet.clear();
        for (int i = 0; i < installedAppInfoList.size(); i++) {
            selectedSet.add(i);
        }
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        selectedSet.clear();
        notifyDataSetChanged();
    }

}
