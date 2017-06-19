package com.example.root.imageselect.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.root.imageselect.ImagePicker;
import com.example.root.imageselect.R;
import com.example.root.imageselect.SimpleItemTouchHelperCallback;
import com.example.root.imageselect.adapters.ProductImagePagerAdapter;
import com.example.root.imageselect.adapters.ProductImageRecycleAdapter;
import com.example.root.imageselect.databinding.AddImagesBinding;
import com.example.root.imageselect.listeners.DragListener;
import com.example.root.imageselect.listeners.ImageListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddImagesFragment extends Fragment implements DragListener {
    public static final int REQ_IMAGE = 1;
    public static final int REQ_GALLARY = 2;
    private ProductImageRecycleAdapter productImageRecycleAdapter;
    private ImagePicker imagePicker;
    private ProductImagePagerAdapter productImagePagerAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ViewPager mViewPager;
    private AddImagesBinding mBinding;

    public AddImagesFragment() {
        // Required empty public constructor
    }

    public static AddImagesFragment newInstance() {
        AddImagesFragment fragment = new AddImagesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_add_images, container, false);
        imagePicker = new ImagePicker.Builder(this)
                .setListener(imageListener)
                .build();

        RecyclerView.LayoutManager recylerViewLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        mBinding.recycleProductImage.setLayoutManager(recylerViewLayoutManager);
        productImageRecycleAdapter = new ProductImageRecycleAdapter(this);
        mBinding.recycleProductImage.setAdapter(productImageRecycleAdapter);
        productImagePagerAdapter = new ProductImagePagerAdapter(getActivity());
        mViewPager = mBinding.viewPagerProductImage;
        mViewPager.setAdapter(productImagePagerAdapter);
        mViewPager.setClipToPadding(false);
        mViewPager.setPadding(100, 0, 100, 0);
        mViewPager.setPageMargin(20);
        mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                float normalizedPosition = Math.abs(Math.abs(position) - 1);
                float scaleY = normalizedPosition / 2 + 0.7f;
                Log.d("test", "transformPage: " + scaleY);
                page.setScaleY(scaleY);
                int pageWidth = mViewPager.getMeasuredWidth() -
                        mViewPager.getPaddingLeft() - mViewPager.getPaddingRight();
                int paddingLeft = mViewPager.getPaddingLeft();
                float transformPos = (float) (page.getLeft() -
                        (mViewPager.getScrollX() + paddingLeft)) / pageWidth;
                if (transformPos < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    page.setAlpha(normalizedPosition);// to make left transparent
                } else if (transformPos <= 1) { // [-1,1]
//                    page.setScaleY(1f);
                    page.setAlpha(1f);// t
                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    page.setAlpha(normalizedPosition);// to make right transparent
                }
            }
        });

        mBinding.imgCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePicker.performImgPicAction(REQ_IMAGE, 2);
            }
        });
        mBinding.imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePicker.performImgPicAction(REQ_GALLARY, 1);
            }
        });

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(productImageRecycleAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mBinding.recycleProductImage);
        productImageRecycleAdapter.setImageRecycleClick
                (new ProductImageRecycleAdapter.ImageRecycleClick() {
                    @Override
                    public void onItemClick(View view, int position) {
                        viewPagerPositionChange(position);
                    }

                    @Override
                    public void onDeleteClick(View view, int position) {
                        productImageRecycleAdapter.deleteImage(position);
                        productImagePagerAdapter.deleteImagePage(position);
                        if ((productImageRecycleAdapter.getItemCount() - 1) == 0) {
                            mViewPager.setVisibility(View.GONE);
                            mBinding.recycleProductImage.setVisibility(View.GONE);
                            mBinding.addProductLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void itemMove(ArrayList<String> images, int toPos) {
                        productImagePagerAdapter.changeItemPosition(images);
                        recycleViewPositionChange(toPos);
                        viewPagerPositionChange(toPos);
                    }

                    @Override
                    public void onFooterClick() {
                        openImagePickerDialog();
                    }
                });

        mViewPager.addOnPageChangeListener
                (new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                                               int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        recycleViewPositionChange(position);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });


        return mBinding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(requestCode, resultCode, data);
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void onImagePick(final int reqCode, final String path) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBinding.addProductLayout.getVisibility() == View.VISIBLE) {
                        mBinding.addProductLayout.setVisibility(View.GONE);
                        mViewPager.setVisibility(View.VISIBLE);
                        mBinding.recycleProductImage.setVisibility(View.VISIBLE);
                    }
                    productImageRecycleAdapter.addData(path);
                    productImagePagerAdapter.addImage(path);
                    mBinding.recycleProductImage.getLayoutManager().scrollToPosition(productImageRecycleAdapter.getItemCount());
                    recycleViewPositionChange(productImageRecycleAdapter.getItemCount() - 2);
                    viewPagerPositionChange(productImagePagerAdapter.getCount() - 1);
                }
            });
        }

        @Override
        public void onError(final String s) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    private void viewPagerPositionChange(int position) {
        mViewPager.setCurrentItem(position);
    }

    private void recycleViewPositionChange(int position) {
        productImageRecycleAdapter.changePosition(position);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {

        mItemTouchHelper.startDrag(viewHolder);
    }

    public void openImagePickerDialog() {
        if ((productImageRecycleAdapter.getItemCount() - 1) < 5) {
            AlertDialog.Builder alBuilder=new AlertDialog.Builder(getActivity());
            alBuilder.setPositiveButton("Camara", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    imagePicker.performImgPicAction(REQ_IMAGE, 2);
                }
            });
            alBuilder.setNegativeButton("Gallary", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    imagePicker.performImgPicAction(REQ_GALLARY, 1);
                }
            });
            alBuilder.show();
        }

//            ImagePickerDialog customImagePickerDialog =
//                    new ImagePickerDialog(getActivity());
//            customImagePickerDialog.setCameraButton
//                    (new ImagePickerDialog.OnImagePickerButtonClick() {
//                        @Override
//                        public void onClick() {
//                            imagePicker.performImgPicAction(REQ_IMAGE, 2);
//                        }
//                    });
//            customImagePickerDialog.setGalleryButton(new ImagePickerDialog.OnImagePickerButtonClick() {
//                @Override
//                public void onClick() {
//                    imagePicker.performImgPicAction(REQ_GALLARY, 1);
//                }
//            });
//            customImagePickerDialog.show();
//        }
      else {
            Toast.makeText(getActivity(), "You can add only 5 photos", Toast.LENGTH_SHORT).show();
        }

    }


}
