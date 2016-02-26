package com.gmail.huashadow.quickpay;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Wolf Xu on 2016/2/26.
 */
public class MainActivity extends Activity {

    private static final String TAG = Constants.APP_TAG + MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
