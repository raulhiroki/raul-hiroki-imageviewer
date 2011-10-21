package com.hero.imageviewer;

import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class SwapViews implements Runnable {
    private final int mPosition;
    private ViewGroup mContainer;

    public SwapViews(ViewGroup container,int position) {
    	mContainer = container;
        mPosition = position;
    }

    public void run() {
        final float centerX = mContainer.getWidth() / 2.0f;
        final float centerY = mContainer.getHeight() / 2.0f;
        Rotate3dAnimation rotation;

        if (mPosition > -1) {
            //mPhotosList.setVisibility(View.GONE);
            //mImageView.setVisibility(View.VISIBLE);
            //mImageView.requestFocus();

            rotation = new Rotate3dAnimation(90, 180, centerX, centerY, 310.0f, false);
        } else {
            //mImageView.setVisibility(View.GONE);
            //mPhotosList.setVisibility(View.VISIBLE);
            //mPhotosList.requestFocus();

            rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
        }

        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new DecelerateInterpolator());

        mContainer.startAnimation(rotation);
    }

}
