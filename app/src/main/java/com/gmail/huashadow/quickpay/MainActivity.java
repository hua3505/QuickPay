package com.gmail.huashadow.quickpay;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.DataOutputStream;
import java.io.IOException;

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
        init();
    }

    private void init() {
        setWechatBtnClickAction();
    }

    private void setWechatBtnClickAction() {
        Button btnWechat = (Button) findViewById(R.id.btn_pay_by_wechat);
        btnWechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityWithRoot("com.tencent.mm/.plugin.offline.ui.WalletOfflineCoinPurseUI");
            }
        });
    }

    /**
     *
     * @param activity com.package.name/com.package.name.ActivityName
     */
    private void startActivityWithRoot(String activity) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            String command = "am start -n " + activity +  "\n";
            outputStream.writeBytes(command);
            outputStream.writeBytes("exit\n");
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "startActivityWithRoot", e);
        }
    }
}
