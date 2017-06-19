package com.example.root.imageselect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.example.root.imageselect.listeners.ImageListener;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.soundcloud.android.crop.Crop;

import java.io.File;

/**
 * Created by dsk-221 on 4/5/17.
 */

public class ImagePicker implements ImageChooserListener {
    private static final String TAG = "ImagePicker";
    private final String KEY_FILE_PATH = "filePath";
    private final String KEY_CHOOSER_TYPE = "chooserType";
    private final String KEY_PICK_REQUEST_CODE = "imageReq";
    private final String FOLDER_NAME = "CultureTruckImages";


    private String filePath;
    private int chooserType;
    private int imageReq = -1;
    private ImageChooserManager imageChooserManager;
    private Activity mActivity;
    private ImageListener imageChooserListener;
    private Fragment mFragment;
    private android.support.v4.app.Fragment mSupportFragment;
    private Uri outputUri;
    private boolean isCrop;
    private int x, y;
    private Uri inputUri;


    private ImagePicker(Builder builder) {
        this.mActivity = builder.activity;
        this.mFragment = builder.fragment;
        this.mSupportFragment = builder.supportFragment;
        this.imageChooserListener = builder.listener;
        this.x = -1;
        this.y = -1;
        this.isCrop = false;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putInt(KEY_PICK_REQUEST_CODE, imageReq);
            outState.putInt(KEY_CHOOSER_TYPE, chooserType);
            outState.putString(KEY_FILE_PATH, filePath);
            Log.d(TAG, "onSaveInstanceState() called with: " + "outState = [" + outState + "]");
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            imageReq = savedInstanceState.getInt(KEY_PICK_REQUEST_CODE);
            chooserType = savedInstanceState.getInt(KEY_CHOOSER_TYPE);
            filePath = savedInstanceState.getString(KEY_FILE_PATH);
        }
    }

    public void openDialog(final int reqCode) {
        String str[] = new String[]{"Camara",
                "Gallary"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performImgPicAction(reqCode, which);
            }
        }).show();
    }

    private Activity getActivity() {
        if (mActivity != null) {
            return mActivity;
        } else if (mFragment != null) {
            return mFragment.getActivity();
        } else if (mSupportFragment != null) {
            return mSupportFragment.getActivity();
        }
        return null;
    }

//    public void performImgPicAction(int reqCode, int which) {
//        performImgPicAction(null, reqCode, which);
//    }

    public void performImgPicAction(int reqCode, int which) {
//        if (cropConfig != null) {
//            isCrop = true;
//            x = cropConfig.getX();
//            y = cropConfig.getY();
//        }
        imageReq = reqCode;
        if (which == 1) {
            chooserType = ChooserType.REQUEST_PICK_PICTURE;
        } else {
            chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        }
        initializeImageChooser(chooserType);
        try {
            filePath = imageChooserManager.choose();
        } catch (Exception e) {
            imageChooserListener.onError(e.getMessage());
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "OnActivityResult");
        Log.i(TAG, "File Path : " + filePath);
        Log.i(TAG, "Chooser Type: " + chooserType);
        Log.d(TAG, "onActivityResult() called with: "
                + "requestCode = ["
                + requestCode
                + "], resultCode = ["
                + resultCode
                + "], data = ["
                + data
                + "]");
        if (resultCode == Activity.RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE
                || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageReq == -1) {
                if (imageChooserListener != null) {
                    imageChooserListener.onError("Request code is diff");
                }
                return;
            }

            if (imageChooserManager == null) {
                initializeImageChooser(chooserType);
                imageChooserManager.reinitialize(filePath);
            }
            imageChooserManager.submit(requestCode, data);
        }
        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            File file=new File(inputUri.getPath());
            file.delete();
            returnImage(imageReq, outputUri.getPath());
            imageReq = -1;
        }
    }

    private void returnImage(final int imageReq, final String path) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (imageChooserListener != null) {
                    imageChooserListener.onImagePick(imageReq, path);
                }
            }
        });
    }


    private void initializeImageChooser(int type) {
        if (mActivity != null) {
            imageChooserManager = new ImageChooserManager(mActivity, type, FOLDER_NAME, false);
        } else if (mFragment != null) {
            imageChooserManager = new ImageChooserManager(mFragment, type, FOLDER_NAME, false);
        } else {
            imageChooserManager = new ImageChooserManager(mSupportFragment, type, FOLDER_NAME, false);
        }
        imageChooserManager.setImageChooserListener(this);
    }

    @Override
    public void onImageChosen(final ChosenImage chosenImage) {
        inputUri = Uri.fromFile(new File(chosenImage.getFilePathOriginal()));
        File imageDir = new File(Environment.getExternalStorageDirectory(), "/" + FOLDER_NAME);
        Log.d(TAG, "onImageChosen: "+imageDir);
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }
        if (isCrop) {
            File croppedOutputFile = new File(imageDir, System.currentTimeMillis() + ".jpeg");
            outputUri = Uri.fromFile(croppedOutputFile);
            Crop crop = Crop.of(inputUri, outputUri);
            if (x != y) {
                crop = crop.withAspect(x, y);
            } else if (x != -1) {
                crop = crop.asSquare();
            }
            if (mActivity != null) {
                crop.start(mActivity);
            } else if (mFragment != null) {
                crop.start(getActivity(), mFragment);
            } else {
                crop.start(getActivity(), mSupportFragment);
            }

        } else {

            Activity activity=getActivity();
            if(activity!=null){

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        returnImage(imageReq, chosenImage.getFilePathOriginal());
                    }
                });

            }

        }
    }

    @Override
    public void onError(String s) {
        imageReq = -1;
        if (imageChooserListener != null) {
            imageChooserListener.onError(s);
        }
    }

    public static class Builder {
        private Activity activity;
        private Fragment fragment;
        private android.support.v4.app.Fragment supportFragment;
        private ImageListener listener;

        private Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder(Fragment fragment) {
            this.fragment = fragment;
        }

        public Builder(android.support.v4.app.Fragment supportFragment) {
            this.supportFragment = supportFragment;
        }

        public Builder setListener(ImageListener imageChooserListener) {
            this.listener = imageChooserListener;
            return this;
        }

        public ImagePicker build() {
            return new ImagePicker(this);
        }
    }
}
