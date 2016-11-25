package com.anyonavinfo.bluetoothphone.bpservice.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Drive on 2016/11/25.
 */

public class ReflectUtil {
    private static Class<?> mClassType = null;
    private static Method mSetRecentTasksMethod = null;
    private static Method mGetRecentTasksMethod = null;

    static {
        init();
    }

    private static void init() {

        try {
            if (mClassType == null) {
                mClassType = Class.forName("android.os.SystemProperties");
                mSetRecentTasksMethod = mClassType.getDeclaredMethod("set", String.class, String.class);
                mGetRecentTasksMethod = mClassType.getDeclaredMethod("get", String.class);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    public static void setRecentTaskAvaliable(String avaliable) {
        try {
            mSetRecentTasksMethod.invoke(mClassType, "persist.sys.allowrecents", avaliable);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static String getRecentTaskAvaliable() {
        String avaliable = "false";
        try {
            avaliable = (String) mGetRecentTasksMethod.invoke(mClassType, "persist.sys.allowrecents");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return avaliable;
    }
}
