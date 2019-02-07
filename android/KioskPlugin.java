package jk.cordova.plugin.kiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import org.apache.cordova.*;
import android.widget.*;
import android.view.Window;
import android.view.View;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import jk.cordova.plugin.kiosk.KioskActivity;
import jk.cordova.plugin.kiosk.FakeHome;
import org.json.JSONObject;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.List;

public class KioskPlugin extends CordovaPlugin {
    
    public static final String EXIT_KIOSK = "exitKiosk";
    
    public static final String KILL_APP = "killApp";
    
    public static final String IS_IN_KIOSK = "isInKiosk";
    
    public static final String INIT_KIOSK = "initKiosk";

    private static final String PREF_KIOSK_MODE = "pref_kiosk_mode";
    
    /**
     * method checks to see if app is currently set as default launcher
     * @return boolean true means currently set as default, otherwise false
     */
    private boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = this.cordova.getActivity().getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        PackageManager packageManager = (PackageManager) this.cordova.getActivity().getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        if(activities.size() == 0)
            return true;

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (INIT_KIOSK.equals(action)) {
                if(!isMyLauncherDefault()) {
                  KioskActivity.toAndroidLog("InitKiosk -> not launcher default");

                  PackageManager p = this.cordova.getActivity().getPackageManager();
                  ComponentName cN = new ComponentName(this.cordova.getActivity().getApplicationContext(), FakeHome.class);
                  p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                  Intent selector = new Intent(Intent.ACTION_MAIN);
                  selector.addCategory(Intent.CATEGORY_HOME);
                  selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  this.cordova.getActivity().startActivity(selector);

                  p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                  android.os.Process.killProcess(android.os.Process.myPid());

                  KioskActivity.toAndroidLog("InitKiosk -> Kill PROCESS: " + android.os.Process.myPid());
                }

                callbackContext.success(Boolean.toString(KioskActivity.running));
                return true;

            } else if (IS_IN_KIOSK.equals(action)) {
                if(!isMyLauncherDefault()) {
                  PackageManager p = this.cordova.getActivity().getPackageManager();
                  ComponentName cN = new ComponentName(this.cordova.getActivity().getApplicationContext(), FakeHome.class);
                  p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                  Intent selector = new Intent(Intent.ACTION_MAIN);
                  selector.addCategory(Intent.CATEGORY_HOME);
                  selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  this.cordova.getActivity().startActivity(selector);

                  p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                
                callbackContext.success(Boolean.toString(KioskActivity.running));
                return true;
                
            } else if (EXIT_KIOSK.equals(action)) {
                PackageManager packageManager = this.cordova.getActivity().getPackageManager();
                packageManager.clearPackagePreferredActivities(this.cordova.getActivity().getPackageName());
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity().getApplicationContext());
                sp.edit().putBoolean(PREF_KIOSK_MODE, false).commit();
                if (intent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
                    cordova.getActivity().startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                KioskActivity.toAndroidLog("ExitKiosk");
                
                callbackContext.success();
                return true;
            } else if (KILL_APP.equals(action)) {
              android.os.Process.killProcess(android.os.Process.myPid());
              KioskActivity.toAndroidLog("KillApp: " + android.os.Process.myPid());

              callbackContext.success();
              return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            KioskActivity.toAndroidLog("KioskPlugin Execute exception: " + e.getMessage(), 0);
            callbackContext.error(e.getMessage());
            return false;
        }
    }
}

