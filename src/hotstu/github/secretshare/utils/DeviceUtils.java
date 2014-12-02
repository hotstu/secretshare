package hotstu.github.secretshare.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class DeviceUtils {

    public static String uniqueId(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String tmDevice, tmSerial, androidId;
        tmDevice = tm.getDeviceId();
        if (tmDevice != null)
            return tmDevice;
        tmSerial = tm.getSubscriberId();
        if (tmSerial != null)
            return tmSerial;
        androidId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        if (androidId != null)
            return androidId;
        else
            return "--nounidqueid--";
        
    }
    
    public static String getMacAddress(Context context) {
        WifiManager wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            macAddress = "--nomacaddr--";
        }
        return macAddress;
    }

}
