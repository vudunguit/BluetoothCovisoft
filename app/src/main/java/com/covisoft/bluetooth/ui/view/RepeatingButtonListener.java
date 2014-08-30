package com.covisoft.bluetooth.ui.view;

import android.view.View;

/**
 * Created by USER on 8/28/2014.
 */
public interface RepeatingButtonListener {
    void onRepeat(View v, long duration, int repeatcount);

    void onUp(View v);

    void onDown(View v);
}
