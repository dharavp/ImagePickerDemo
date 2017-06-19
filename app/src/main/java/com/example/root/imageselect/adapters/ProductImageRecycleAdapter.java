package com.example.root.imageselect.adapters;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.root.imageselect.R;
import com.example.root.imageselect.listeners.DragListener;
import com.example.root.imageselect.listeners.ItemTouchHelperAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by root on 6/1/17.
 */

public class ProductImageRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemTouchHelperAdapter {
    private ArrayList<String> img;
    private int selectedPosition = -1;
    private ImageRecycleClick imageRecycleClick;
    private DragListener mDragListener;
    private int oldPosition;
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;

    public interface ImageRecycleClick {
        void onItemClick(View view, int position);

        void onDeleteClick(View view, int position);

        void itemMove(ArrayList<String> image, int toPos);

        void onFooterClick();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(img, i, i + 1);

            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(img, i, i - 1);
            }
        }
     //   Collections.swap(img, fromPosition, toPosition);
        imageRecycleClick.itemMove(img, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
//    @Override
//    public void onItemDismiss(int position) {
//        img.remove(position);
//        notifyItemRemoved(position);
//    }


    public void addData(String imagePath) {
        img.add(imagePath);
        notifyItemInserted(img.size() - 1);
    }

    public void deleteImage(int position) {
        img.remove(position);
        notifyItemRemoved(position);
    }

    public ProductImageRecycleAdapter(DragListener mDragListener) {
        this.mDragListener = mDragListener;
        img = new ArrayList<>();
    }

    public void setImageRecycleClick(ImageRecycleClick imageRecycleClick) {
        this.imageRecycleClick = imageRecycleClick;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public ImageView image, imageDelete;
        public RelativeLayout relativeLayout;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.img_recycle);
            imageDelete = (ImageView) view.findViewById(R.id.img_delete);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.selected_img_bg);
        }

        public void bind(String imagePath) {
            if (!TextUtils.isEmpty(imagePath)) {
                image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            }
            if (selectedPosition == getAdapterPosition()) {
               relativeLayout.setVisibility(View.VISIBLE);
            } else {
                relativeLayout.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePosition(getAdapterPosition());
                    imageRecycleClick.onItemClick(v, getAdapterPosition());

                }
            });
            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageRecycleClick.onDeleteClick(v, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            mDragListener.onStartDrag(this);
            return false;
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageEdit;
        public RelativeLayout footerLayout;

        public FooterViewHolder(View view) {
            super(view);
            imageEdit = (ImageView) view.findViewById(R.id.img_edit);
            footerLayout = (RelativeLayout) view.findViewById(R.id.layout_footer);
        }

        public void bind() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageRecycleClick.onFooterClick();
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_footer_layout,
                    parent, false);
            return new FooterViewHolder(v);
        } else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_image_item,
                    parent, false);
            return new MyViewHolder(v);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).bind(img.get(position));
        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).bind();
        }

    }

    @Override
    public int getItemCount() {
        return img.size() + 1;
    }

    public void changePosition(int position) {
        oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(selectedPosition);
        notifyItemChanged(oldPosition);
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return position == getItemCount() - 1;
    }
}
