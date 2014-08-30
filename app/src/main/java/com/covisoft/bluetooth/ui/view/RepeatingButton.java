package com.covisoft.bluetooth.ui.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by USER on 8/28/2014.
 */
public class RepeatingButton extends Button{

    private long mStartTime;
    private int mRepeatCount = 0;
    private RepeatingButtonListener mListener = null;
    private long mInterval = 500;

    public RepeatingButton(Context context)	{
        this(context, null);
    }

    public RepeatingButton(Context context, AttributeSet attrs)	{
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public RepeatingButton(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        setFocusable(true);
        setLongClickable(true);
    }

    public void bindListener(RepeatingButtonListener listener, long longHold){
        this.mListener = listener;
        this.mInterval = longHold;
    }

    public void setRepeatFreq(long interval){
        this.mInterval = interval;
    }


    @Override
    public boolean performLongClick(){
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mRepeatCount = 0;
        post(this.mRepeater);
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_UP){
            removeCallbacks(this.mRepeater);
            this.doUp();
        }else if (event.getAction() == MotionEvent.ACTION_DOWN ){
            this.doDown();
        }
        return super.onTouchEvent(event);
    }

    private Runnable mRepeater = new Runnable(){
        private long lFirstRunTime = mStartTime + mInterval;
        public void run(){
            if (SystemClock.elapsedRealtime() > lFirstRunTime)
                doRepeat(false);
            if (isPressed())
                postDelayed(this, mInterval);
        }
    };


    public void doRepeat(boolean last){
        if (this.mListener != null)
            this.mListener.onRepeat(
                    this,
                    SystemClock.elapsedRealtime() - this.mStartTime,
                    last ? -1 : this.mRepeatCount++
            );
    }


    public void doUp(){
        if (this.mListener != null)
            this.mListener.onUp(this);
    }


    public void doDown(){
        if (this.mListener != null)
            this.mListener.onDown(this);
    }
}
