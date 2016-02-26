package com.gmail.huashadow.quickpay.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.gmail.huashadow.quickpay.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wolf  on 2016/2/26.Xu
 */
public class ReflectTools {

    private static final String TAG = Constants.APP_TAG + "." + ReflectTools.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static class HasOverLoadedMethodException extends ReflectiveOperationException {
    }

    public static final List<Method> findMethod(Class<?> clazz, String methodName) {
        if (clazz != null && !TextUtils.isEmpty(methodName)) {
            Method[] methods = clazz.getDeclaredMethods();
            List<Method> matchMethods = new ArrayList<>();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    matchMethods.add(method);
                }
            }
            return matchMethods;
        }
        return null;
    }

    public static final Method findMethodWithoutOverLoaded(Class<?> clazz, String methodName)
            throws NoSuchMethodException, HasOverLoadedMethodException {
        List<Method> methods = findMethod(clazz, methodName);
        if (methods == null || methods.size() <= 0) {
            throw new NoSuchMethodException();
        } else if (methods.size() >= 2) {
            throw new HasOverLoadedMethodException();
        }
        return methods.get(0);
    };
}
