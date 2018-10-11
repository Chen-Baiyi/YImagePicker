package com.ypx.imagepicker.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ypx.imagepicker.R;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.config.IImgPickerUIConfig;
import com.ypx.imagepicker.config.ImgPickerSelectConfig;
import com.ypx.imagepicker.data.ImagePickerData;
import com.ypx.imagepicker.interf.ImageSelectMode;
import com.ypx.imagepicker.ui.activity.YPXImageGridActivity;
import com.ypx.imagepicker.widget.ShowTypeImageView;
import com.ypx.imagepicker.widget.SuperCheckBox;

import java.util.List;

/**
 * 作者：yangpeixing on 2018/4/6 10:32
 * 功能：gridview适配器
 * 产权：南京婚尚信息技术
 */
public class ImageGridAdapter extends BaseAdapter {
    private static final int ITEM_TYPE_CAMERA = 0;
    private static final int ITEM_TYPE_NORMAL = 1;
    private List<ImageItem> images;
    private Context mContext;
    private ImgPickerSelectConfig selectConfig;
    private IImgPickerUIConfig uiConfig;

    public ImageGridAdapter(Context ctx, List<ImageItem> images, ImgPickerSelectConfig selectConfig, IImgPickerUIConfig uiConfig) {
        this.images = images;
        this.mContext = ctx;
        this.selectConfig = selectConfig;
        this.uiConfig = uiConfig;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (selectConfig.isShowCamera()) {
            return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        }
        return ITEM_TYPE_NORMAL;
    }


    @Override
    public int getCount() {
        return selectConfig.isShowCamera() ? images.size() + 1 : images.size();
    }

    @Override
    public ImageItem getItem(int position) {
        if (selectConfig.isShowCamera()) {
            if (position == 0) {
                return null;
            }
            return images.get(position - 1);
        } else {
            return images.get(position);
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == ITEM_TYPE_CAMERA) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ypx_grid_item_camera, parent, false);
            TextView tv_camera = (TextView) convertView.findViewById(R.id.tv_camera);
            tv_camera.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources().getDrawable(uiConfig.getCameraIconID()), null, null);
            convertView.setTag(null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext instanceof YPXImageGridActivity) {
                        ((YPXImageGridActivity) mContext).takePhoto();
                    }
                }
            });
        } else {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.ypx_image_grid_item, null);
                holder = new ViewHolder();
                holder.ivPic = (ShowTypeImageView) convertView.findViewById(R.id.iv_thumb);
                holder.cbSelected = (SuperCheckBox) convertView.findViewById(R.id.iv_thumb_check);
                holder.cbPanel = convertView.findViewById(R.id.thumb_check_panel);
                holder.v_masker = convertView.findViewById(R.id.v_masker);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (selectConfig.getSelectMode() == ImageSelectMode.MODE_MULTI) {
                holder.cbSelected.setVisibility(View.VISIBLE);
            } else {
                holder.cbSelected.setVisibility(View.GONE);
            }

            if (uiConfig.getImageItemBackgroundColor() != 0) {
                holder.ivPic.setBackgroundColor(uiConfig.getImageItemBackgroundColor());
            }
            holder.cbSelected.setRightDrawable(mContext.getResources().getDrawable(uiConfig.getSelectedIconID())
                    , mContext.getResources().getDrawable(uiConfig.getUnSelectIconID()));
            //  int index = selectConfig.isShowCamera() ? position + 1 : position;
            final ImageItem item = getItem(position);
            holder.cbSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ImagePickerData.isOverLimit(selectConfig.getSelectLimit())) {
                        if (holder.cbSelected.isChecked()) {
                            holder.cbSelected.toggle();
                            String toast = mContext.getResources().getString(R.string.you_have_a_select_limit, selectConfig.getSelectLimit() + "");
                            uiConfig.tip(mContext, toast);
                        }
                    }
                }
            });

            holder.cbSelected.setOnCheckedChangeListener(null);
            if (ImagePickerData.getSelectImgs().contains(item)) {
                holder.cbSelected.setChecked(true);
                holder.ivPic.setSelected(true);
                holder.v_masker.setVisibility(View.VISIBLE);
            } else {
                holder.cbSelected.setChecked(false);
                holder.v_masker.setVisibility(View.GONE);
            }

            ViewGroup.LayoutParams params = holder.ivPic.getLayoutParams();
            params.width = params.height = (getScreenWidth() - dp_2() * 2) / selectConfig.getColumnCount();

            holder.ivPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext instanceof YPXImageGridActivity) {
                        ((YPXImageGridActivity) mContext).onImageClickListener(item, selectConfig.isShowCamera() ? position - 1 : position);
                    }
                }
            });

            holder.cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mContext instanceof YPXImageGridActivity) {
                        ((YPXImageGridActivity) mContext).imageSelectChange(item, isChecked);
                        notifyDataSetChanged();
                    }
                }

            });
            holder.ivPic.setTypeWithUrlAndSize(item.path, item.width, item.height);
            if (uiConfig != null) {
                uiConfig.displayListImage(holder.ivPic, item.path, params.width);
            }
        }
        return convertView;
    }

    public void refreshData(List<ImageItem> items) {
        if (items != null && items.size() > 0) {
            images = items;
        }
        notifyDataSetChanged();
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private int dp_2() {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 2, mContext.getResources().getDisplayMetrics());
    }

    class ViewHolder {
        ShowTypeImageView ivPic;
        SuperCheckBox cbSelected;
        View cbPanel, v_masker;
    }
}