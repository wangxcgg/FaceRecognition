package com.moons.wangxc.popupmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class MyWrapContentView extends FrameLayout {
    public MyWrapContentView(Context context) {
        super(context);
    }

    public MyWrapContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWrapContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.AT_MOST,0);//核心代码
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}