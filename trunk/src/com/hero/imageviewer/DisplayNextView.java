package com.hero.imageviewer;

import android.view.ViewGroup;
import android.view.animation.Animation;


public class DisplayNextView implements Animation.AnimationListener {
    private final int mPosition;
    private ViewGroup mContainer;

    public DisplayNextView(ViewGroup container,int position) {
    	mContainer = container;
        mPosition = position;
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        mContainer.post(new SwapViews(mContainer,mPosition));
    }

    public void onAnimationRepeat(Animation animation) {
    }

}
