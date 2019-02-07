package jk.cordova.plugin.kiosk;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ComponentName;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import org.apache.cordova.*;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class KioskActivity extends CordovaActivity {

    private static final String PREF_KIOSK_MODE = "pref_kiosk_mode";
    private static final int REQUEST_CODE = 123467;
    public static boolean running = false;
    public static boolean enabledLog = true;
    public static Integer LOG_DEBUG = 1;
    public static Integer LOG_ERROR = 0;
    Object statusBarService;
    ActivityManager am;
    private static String TAG = "KioskActivity";

    protected void onStart() {
        super.onStart();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        if(Build.VERSION.SDK_INT >= 23) {
            sp.edit().putBoolean(PREF_KIOSK_MODE, false).commit();
            checkDrawOverlayPermission();

            KioskActivity.toAndroidLog("onStart # checkDrawOverlay - build version: " + Build.VERSION.SDK_INT);
        } else {
            sp.edit().putBoolean(PREF_KIOSK_MODE, true).commit();
            addOverlay();

            KioskActivity.toAndroidLog("onStart # addOverlay - build version: " + Build.VERSION.SDK_INT);
        }
        running = true;
    }
    //http://stackoverflow.com/questions/7569937/unable-to-add-window-android-view-viewrootw44da9bc0-permission-denied-for-t
    @TargetApi(Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        KioskActivity.toAndroidLog("checkDrawOverlayPermission");

        if (!Settings.canDrawOverlays(this.getApplicationContext())) {
            KioskActivity.toAndroidLog("checkDrawOverlayPermission - dentro if");

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        KioskActivity.toAndroidLog("onActivityResult: " + REQUEST_CODE);

        if (requestCode == REQUEST_CODE) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            sp.edit().putBoolean(PREF_KIOSK_MODE, true).commit();
            if (Settings.canDrawOverlays(this)) {
                addOverlay();
            }
        }
    }
    //http://stackoverflow.com/questions/25284233/prevent-status-bar-for-appearing-android-modified?answertab=active#tab-top
    public void addOverlay() {
        KioskActivity.toAndroidLog("addOverlay - context: " + Context.WINDOW_SERVICE);

        WindowManager manager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (25 * getResources()
                .getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;

        CustomViewGroup view = new CustomViewGroup(this);

        manager.addView(view, localLayoutParams);
    }

    protected void onStop() {
        KioskActivity.toAndroidLog("onStop with trick for Recent Apps soft button");

        super.onStop();
        running = false;
        
        // Trick to avoid Recent App soft button touch
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        sp.edit().putBoolean(PREF_KIOSK_MODE, true).commit();
        if(!sp.getBoolean(PREF_KIOSK_MODE, false)) {
            return;
        }

        if(am == null) {
            am = ((ActivityManager)getSystemService("activity"));
        }
        am.moveTaskToFront(getTaskId(), 1);
        sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        //collapseNotifications();
    }

    public void onCreate(Bundle savedInstanceState) {
        KioskActivity.toAndroidLog("onCreate");

        super.onCreate(savedInstanceState);
        super.init();
        loadUrl(launchUrl);
    }

    private void collapseNotifications()
    {
        try
        {
            KioskActivity.toAndroidLog("collapseNotifications");

            if(statusBarService == null) {
                statusBarService = getSystemService("statusbar");
            }

            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");

            if (Build.VERSION.SDK_INT <= 16)
            {
                Method collapseStatusBar = statusBarManager.getMethod("collapse");
                collapseStatusBar.setAccessible(true);
                collapseStatusBar.invoke(statusBarService);
                return;
            }
            Method collapseStatusBar = statusBarManager.getMethod("collapsePanels");
            collapseStatusBar.setAccessible(true);
            collapseStatusBar.invoke(statusBarService);
        }
        catch (Exception e)
        {
            KioskActivity.toAndroidLog(e.toString(), 0);
        }
    }

    public void onPause()
    {
        KioskActivity.toAndroidLog("onPause -> super.onPause()");
        super.onPause();
        return;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        KioskActivity.toAndroidLog("KeyDown with keycode: " + keyCode);

        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        KioskActivity.toAndroidLog("onWindowFocusChanged - HASFOCUS: " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        return;
    }

    public static void toAndroidLog(String str, Integer... t) {
        Integer type = t.length > 0 ? t[0] : KioskActivity.LOG_DEBUG;

        if(KioskActivity.enabledLog) {
            if(type == KioskActivity.LOG_DEBUG) {
                Log.d(KioskActivity.TAG, str);
            } else {
                Log.e(KioskActivity.TAG, str);
            }
        }
    }

    //http://stackoverflow.com/questions/25284233/prevent-status-bar-for-appearing-android-modified?answertab=active#tab-top
    public class CustomViewGroup extends ViewGroup {

        public CustomViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }
    }
}

