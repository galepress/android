package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.R;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 26.04.2015.
 */
public class ThumnailHorizontalLayout extends HorizontalScrollView {

    private Context context;
    public ArrayList<TextView> pageNumberList;
    private String mPath;
    private ArrayList<PointF> itemSizeList;
    public PointF mPreviewSize;
    private PointF mPageSize;
    private final SparseArray<Bitmap> mBitmapCache = new SparseArray<Bitmap>();
    private static final String TAG = ThumnailHorizontalLayout.class
            .getSimpleName();
    private LinearLayout rootLayout;
    private int prevIndex = 0;
    private CustomThumnailAdapter adapter;
    private Animation thumnailAnimVisible;
    private Animation thumnailAnimInvisible;
    private int thumnailAnimStartPoint;
    private int thumnailAnimEndPoint;
    private AnimationSet sInVisible;
    private AnimationSet sVisible;

    public ThumnailHorizontalLayout(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;

        setHorizontalScrollBarEnabled(false);
        rootLayout = new LinearLayout(context);
        rootLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(rootLayout);

        pageNumberList = new ArrayList<TextView>();
    }

    private void addSubViews(){

        rootLayout.removeAllViews();
        for(int i = 0; i < adapter.getCount(); i++){
            View view = adapter.getView(i, null, rootLayout);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(v.getId() > adapter.getmDocView().getDisplayedViewIndex())
                        startThumnailRightAnim(v.getId());
                    else if(v.getId() < adapter.getmDocView().getDisplayedViewIndex())
                        startThumnailLeftAnim(v.getId());

                }
            });
            rootLayout.addView(view);
        }
    }

    private void setInvisibleAllTextView(int selectedIndex){
        for(int i = selectedIndex-10; i < selectedIndex+10; i++){
            if(i >= 0 && i < adapter.getCount())
                adapter.pageNumberList.get(i).setVisibility(View.INVISIBLE);
        }
    }

    public void startThumnailLeftAnim(final int id){

        final int currentIndex = adapter.getmDocView().getDisplayedViewIndex();
        final int selectedIndex = (int) id;
        int durationTime = 100;

        try{

            sInVisible = new AnimationSet(false);
            thumnailAnimStartPoint = (((RelativeLayout)adapter.pageNumberList.get(currentIndex).getParent()).getWidth()
                    -adapter.pageNumberList.get(currentIndex).getWidth()) / 2;
            thumnailAnimEndPoint = -(thumnailAnimStartPoint + adapter.pageNumberList.get(currentIndex).getWidth());

            thumnailAnimInvisible = new TranslateAnimation(0, thumnailAnimEndPoint, 0, 0);
            thumnailAnimInvisible.setDuration(100);
            thumnailAnimInvisible.setInterpolator(new AccelerateInterpolator());
            thumnailAnimInvisible.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {}
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationEnd(Animation animation) {
                    adapter.pageNumberList.get(currentIndex).setVisibility(View.INVISIBLE);

                }
            });

            AlphaAnimation alphaAmin = new AlphaAnimation(1, 0);
            alphaAmin.setDuration(50);

            sInVisible.addAnimation(alphaAmin);
            sInVisible.addAnimation(thumnailAnimInvisible);

            adapter.pageNumberList.get(currentIndex).startAnimation(sInVisible);
        } catch (Exception e){
            durationTime = 0;
        }

        try{
            sVisible = new AnimationSet(false);

            thumnailAnimStartPoint = ((RelativeLayout)adapter.pageNumberList.get(selectedIndex).getParent()).getWidth();
            thumnailAnimEndPoint  = ((RelativeLayout)adapter.pageNumberList.get(selectedIndex).getParent()).getWidth() / 2;

            thumnailAnimVisible = new TranslateAnimation(thumnailAnimStartPoint, 0, 0 ,0);
            thumnailAnimVisible.setDuration(100);
            thumnailAnimVisible.setStartOffset(durationTime);
            thumnailAnimVisible.setInterpolator(new DecelerateInterpolator());

            AlphaAnimation alphaAmin = new AlphaAnimation(0, 1);
            alphaAmin.setDuration(150);
            alphaAmin.setStartOffset(thumnailAnimVisible.getStartOffset());
            alphaAmin.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    adapter.getmDocView().setDisplayedViewIndex(selectedIndex);
                    setInvisibleAllTextView(selectedIndex);
                    adapter.pageNumberList.get(selectedIndex).setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            sVisible.addAnimation(alphaAmin);
            sVisible.addAnimation(thumnailAnimVisible);

            adapter.pageNumberList.get(selectedIndex).startAnimation(sVisible);
        } catch (Exception e){
            Log.e("ThumnailleftAnim", e.toString());
        }
    }

    public void startThumnailRightAnim(final int id){

        final int currentIndex = adapter.getmDocView().getDisplayedViewIndex();
        final int selectedIndex = (int) id;
        int durationTime = 100;

        try{

            AnimationSet sInVisible = new AnimationSet(false);

            thumnailAnimStartPoint = (((RelativeLayout)adapter.pageNumberList.get(currentIndex).getParent()).getWidth()
                    -adapter.pageNumberList.get(currentIndex).getWidth()) / 2;
            thumnailAnimEndPoint  = ((RelativeLayout)adapter.pageNumberList.get(currentIndex).getParent()).getWidth();


            thumnailAnimInvisible = new TranslateAnimation(0, thumnailAnimEndPoint, 0, 0);
            thumnailAnimInvisible.setDuration(100);
            thumnailAnimInvisible.setInterpolator(new AccelerateInterpolator());
            thumnailAnimInvisible.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {}
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationEnd(Animation animation) {
                    adapter.pageNumberList.get(currentIndex).setVisibility(View.INVISIBLE);
                }
            });

            AlphaAnimation alphaAmin = new AlphaAnimation(1, 0);
            alphaAmin.setDuration(50);

            sInVisible.addAnimation(alphaAmin);
            sInVisible.addAnimation(thumnailAnimInvisible);

            adapter.pageNumberList.get(currentIndex).clearAnimation();
            adapter.pageNumberList.get(currentIndex).startAnimation(sInVisible);
        } catch (Exception e){
            durationTime = 0;
        }

        try{
            AnimationSet sVisible = new AnimationSet(false);

            thumnailAnimStartPoint = -((((RelativeLayout)adapter.pageNumberList.get(selectedIndex).getParent()).getWidth()
                    - adapter.pageNumberList.get(selectedIndex).getWidth()) / 2
                    +adapter.pageNumberList.get(selectedIndex).getWidth());
            thumnailAnimEndPoint = (((RelativeLayout)adapter.pageNumberList.get(selectedIndex).getParent()).getWidth()
                    - adapter.pageNumberList.get(selectedIndex).getWidth()) / 2;

            thumnailAnimVisible = new TranslateAnimation(thumnailAnimStartPoint, 0, 0 ,0);
            thumnailAnimVisible.setDuration(100);
            thumnailAnimVisible.setStartOffset(durationTime);
            thumnailAnimVisible.setInterpolator(new DecelerateInterpolator());

            AlphaAnimation alphaAmin = new AlphaAnimation(0, 1);
            alphaAmin.setDuration(150);
            alphaAmin.setStartOffset(durationTime);
            alphaAmin.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    adapter.getmDocView().setDisplayedViewIndex(selectedIndex);
                    setInvisibleAllTextView(selectedIndex);
                    adapter.pageNumberList.get(selectedIndex).setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            sVisible.addAnimation(alphaAmin);
            sVisible.addAnimation(thumnailAnimVisible);

            adapter.pageNumberList.get(selectedIndex).clearAnimation();
            adapter.pageNumberList.get(selectedIndex).startAnimation(sVisible);
        } catch (Exception e){
            Log.e("ThumnailrightAnim", e.toString());
        }

    }

    public void setCenter(int index) {

        View view = rootLayout.getChildAt(index);

        int screenWidth = ((MuPDFActivity) context).getWindowManager()
                .getDefaultDisplay().getWidth();

        int scrollX = (view.getLeft() - (screenWidth / 2))
                + (view.getWidth() / 2);
        this.scrollTo(scrollX, 0);
        prevIndex = index;
    }

    public void setAdapter(CustomThumnailAdapter adapter) {
        this.adapter = adapter;
        addSubViews();
    }

    public CustomThumnailAdapter getAdapter(){
        return adapter;
    }
}
