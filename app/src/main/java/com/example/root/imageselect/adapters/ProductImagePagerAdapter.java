package com.example.root.imageselect.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.root.imageselect.R;

import java.util.ArrayList;

/**
 * Created by root on 6/1/17.
 */

public class ProductImagePagerAdapter extends PagerAdapter {
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> images;

    public ProductImagePagerAdapter(Context mContext) {
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        images = new ArrayList<>();
    }

    public void addImage(String image) {
        images.add(image);
        notifyDataSetChanged();
    }

    public void deleteImagePage(int position) {
        images.remove(position);
        notifyDataSetChanged();
    }

    public void changeItemPosition(ArrayList<String> listImages) {
        images.clear();
        images.addAll(listImages);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return images.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.image_pager_item,
                container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.img_product);
        if (images.size() != 0 && images.get(position) != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(images.get(position)));
        } else {
            // imageView.setImageResource(R.drawable.ic_action);
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getItemPosition(Object object) {
        int index = images.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }
}
