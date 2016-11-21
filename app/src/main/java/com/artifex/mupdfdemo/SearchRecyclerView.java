package com.artifex.mupdfdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by p1025 on 18.11.2016.
 */

public class SearchRecyclerView extends RecyclerView {
    public SearchRecyclerView(Context context) {
        super(context);
    }

    public SearchRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
