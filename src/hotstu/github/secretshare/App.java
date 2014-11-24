package hotstu.github.secretshare;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class App extends Application {
    public static String SESSION;
    public static File APP_FILES_DIR;
    public static File APP_CACHE_DIR;
    public static File APP_EXTERNAL_FILES_DIR;
    public static File APP_EXTERNAL_CACHE_DIR;
    public static String VERSION_NAME;
    public static int VERSION_CODE;
    public static String USER_DISPLAY_NAME;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Context CONTEXT = this;
        String PACKAGE = getClass().getPackage().getName();
        try {
            VERSION_NAME = getPackageManager().getPackageInfo(PACKAGE, 0).versionName;
            VERSION_CODE = getPackageManager().getPackageInfo(PACKAGE, 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            Log.e("App","Fail to get version code.");
        }
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CONTEXT);
        SESSION = prefs.getString("session", null);
        
        APP_FILES_DIR = getFilesDir();
        APP_CACHE_DIR = getCacheDir();
        
        APP_EXTERNAL_FILES_DIR = CONTEXT.getExternalFilesDir(null);
        APP_EXTERNAL_CACHE_DIR = CONTEXT.getExternalCacheDir();
        Log.d("App", "APP_FILES_DIR:"+APP_FILES_DIR);
        Log.d("App", "APP_CACHE_DIR:"+APP_CACHE_DIR);
        Log.d("App", "APP_EXTERNAL_FILES_DIR:"+APP_EXTERNAL_FILES_DIR);
        Log.d("App", "APP_EXTERNAL_CACHE_DIR:"+APP_EXTERNAL_CACHE_DIR);
    } 
    
}
