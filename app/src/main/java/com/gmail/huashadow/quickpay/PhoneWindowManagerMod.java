package com.gmail.huashadow.quickpay;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;

import com.gmail.huashadow.quickpay.utils.ReflectTools;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by Administrator on 2016/2/25.
 */
public class PhoneWindowManagerMod implements IXposedHookZygoteInit {
    private static final String TAG = Constants.APP_TAG + "." + PhoneWindowManagerMod.class.getSimpleName();

    private Context mContext;
    private UserHandle mUserHandle;
    private Map<String, Method> mMethods = new HashMap<>();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.d(TAG, "initZygote");

        String phoneWindowManagerClassName = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
            ? "com.android.server.policy.PhoneWindowManager"
            :"com.android.internal.policy.impl.PhoneWindowManager";

        try {
            Class<?> clazz = XposedHelpers.findClass(phoneWindowManagerClassName, null);
            if (clazz != null) {
                hookInit(clazz);
                hookInterceptKeyBeforeDispatching(clazz);
            } else {
                Log.w(TAG, "PhoneWindowManager not found");
            }
        } catch (XposedHelpers.ClassNotFoundError e) {
            Log.e(TAG, "PhoneWindowManager class not found", e);
        }
    }

    private void hookInit(Class<?> clazz) {
        try {
            Method m = ReflectTools.findMethodWithoutOverLoaded(clazz, "init");
            XposedBridge.hookMethod(m, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    init(param);
                }
            });
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hookInit", e);
        } catch (ReflectTools.HasOverLoadedMethodException e) {
            Log.e(TAG, "hookInit", e);
        }
    }

    private void init(XC_MethodHook.MethodHookParam param) {
        mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
        try {
            Method methodStartActivityAsUser = XposedHelpers.findMethodExact(mContext.getClass(),
                    "startActivityAsUser", Intent.class, UserHandle.class);
            mMethods.put("startActivityAsUser", methodStartActivityAsUser);
            mUserHandle = (UserHandle) XposedHelpers.getStaticObjectField(UserHandle.class, "CURRENT");
        } catch (XposedHelpers.ClassNotFoundError e) {
            Log.e(TAG, "init", e);
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "init", e);
        } catch (Throwable tr) {
            Log.e(TAG, "init", tr);
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

                        handleKeyEvent(keyEvent);

                    }
                });
                Log.d(TAG, "hook method interceptKeyBeforeDispatching");
            } else {
                Log.w(TAG, "method interceptKeyBeforeDispatching not found");
            }
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "hookInterceptKeyBeforeDispatching", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "hookInterceptKeyBeforeDispatching", e);
        } catch (Exception e) {
            Log.e(TAG, "hookInterceptKeyBeforeDispatching", e);
        }
    }

    private void handleKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                Log.v(TAG, "handleKeyEvent key VOLUME_DOWN aciton ACTION_UP");
                try {
                    //wakeUp();
                    //unlock();
                    Intent intent = new Intent(Constants.ACTION_START_MAIN_ACTIVITY);
                    Method method = mMethods.get("startActivityAsUser");
                    method.invoke(mContext, intent, mUserHandle);
                } catch (Throwable tr) {
                    Log.e(TAG, "handleKeyEvent", tr);
                }
            }
        }
    }

    private void wakeUp() {
        PowerManager powerManager =(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        wl.acquire();
        wl.release();
    }

    private void unlock() {
        KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        kl.disableKeyguard();
    }
}
