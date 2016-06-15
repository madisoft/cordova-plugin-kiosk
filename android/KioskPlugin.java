package jk.cordova.plugin.kiosk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import org.json.JSONObject;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import 	android.content.pm.PackageManager;

public class KioskPlugin extends CordovaPlugin {
    
    public static final String EXIT_KIOSK = "exitKiosk";
    
    public static final String IS_IN_KIOSK = "isInKiosk";

    private static final String PREF_KIOSK_MODE = "pref_kiosk_mode";
    
    /**
     * method checks to see if app is currently set as default launcher
     * @return boolean true means currently set as default, otherwise false
     */ 
     /*
    private boolean isMyAppLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
    
        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);
    
        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) getPackageManager();
    
        packageManager.getPreferredActivities(filters, activities, null);
    
        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }
    */
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (IS_IN_KIOSK.equals(action)) {
                
                callbackContext.success(Boolean.toString(KioskActivity.running));
                return true;
                
            } else if (EXIT_KIOSK.equals(action)) {
                /*
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity().getApplicationContext());
                sp.edit().putBoolean(PREF_KIOSK_MODE, false).commit();

                Intent chooser = Intent.createChooser(intent, "Select destination...");
                if (intent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
                    cordova.getActivity().startActivity(chooser);
                }*/
                
                PackageManager packageManager = this.cordova.getActivity().getPackageManager();
                packageManager.clearPackagePreferredActivities(this.cordova.getActivity().getPackageName());
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity().getApplicationContext());
                sp.edit().putBoolean(PREF_KIOSK_MODE, false).commit();
                if (intent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
                    cordova.getActivity().startActivity(intent);
                }
                
                callbackContext.success();
                return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        }
    }
}

