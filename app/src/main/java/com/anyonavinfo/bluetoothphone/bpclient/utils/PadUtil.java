package com.anyonavinfo.bluetoothphone.bpclient.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Drive on 2016/11/8.
 */

public class PadUtil {
    public static void releaseKeyguard(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!powerManager.isScreenOn()) {
            KeyguardManager.KeyguardLock kl = keyguardManager.newKeyguardLock("unLock");
            kl.disableKeyguard(); // 解锁
            PowerManager.WakeLock pm_wl = powerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            pm_wl.acquire();
            pm_wl.release();
        }
    }
}
