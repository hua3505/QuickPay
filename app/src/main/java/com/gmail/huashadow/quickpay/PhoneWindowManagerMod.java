package com.gmail.huashadow.quickpay;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import com.gmail.huashadow.quickpay.utils.ReflectTools;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by Administrator on 2016/2/25.
 */
public class PhoneWindowManagerMod implements IXposedHookZygoteInit {
    private static final String TAG = Constants.APP_TAG + "." + PhoneWindowManagerMod.class.getSimpleName();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.d(TAG, "initZygote");

        String phoneWindowManagerClassName = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
            ? "com.android.server.policy.PhoneWindowManager"
            :"com.android.internal.policy.impl.PhoneWindowManager";

        try {
            Class<?> clazz = XposedHelpers.findClass(phoneWindowManagerClassName, null);
            if (clazz != null) {
                hookInterceptKeyBeforeDispatching(clazz);
            } else {
                Log.w(TAG, "PhoneWindowManager not found");
            }
        } catch (XposedHelpers.ClassNotFoundError e) {
            Log.e(TAG, "PhoneWindowManager class not found", e);
        }
    }

    private void hookInterceptKeyBeforeDispatching(Class<?> clazz) {

        /**
         *
         * Original Arguments
         * 		- Gingerbread: PhoneWindowManager.interceptKeyBeforeDispatching(WindowState win, Integer action, Integer flags, Integer keyCode, Integer scanCode, Integer metaState, Integer repeatCount, Integer policyFlags)
         * 		- ICS & Above: PhoneWindowManager.interceptKeyBeforeDispatching(WindowState win, KeyEvent event, Integer policyFlags)
         * Only care about ICS & Above
         */
        try {
            Method m = ReflectTools.findMethodWithoutOverLoaded(clazz, "interceptKeyBeforeDispatching");
            if (m != null) {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        KeyEvent keyEvent = (KeyEvent) param.args[1];
                        Log.v(TAG, "hookInterceptKeyBeforeDispatching: " + keyEvent.toString());
                        Context context = (Context) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mContext");
                        handleKeyEvent(context, keyEvent);

                    }
                });
                Log.d(TAG, "hook method interceptKeyBeforeDispatching");
            } else {
                Log.w(TAG, "method interceptKeyBeforeDispatching not found");
            }
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "", e);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }


    private void handleKeyEvent(Context context, KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                Log.d(TAG, "handleKeyEvent key VOLUME_DOWN aciton ACTION_UP");
                try {
                    Log.d(TAG, "handleKeyEvent start MainActivity -2");
                    Intent intent = new Intent(context, MainActivity.class);
                    Log.d(TAG, "handleKeyEvent start MainActivity -1");
                    context.startActivity(intent);
                    Log.d(TAG, "handleKeyEvent start MainActivity");
                } catch (Throwable tr) {
                    Log.e(TAG, "", tr);
                }
            }
        }
    }
}
