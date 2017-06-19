package com.example.root.imageselect.listeners;

/**
 * Created by root on 6/16/17.
 */

public interface ImageListener {
    void onImagePick(int reqCode, String path);

    void onError(String s);
}
