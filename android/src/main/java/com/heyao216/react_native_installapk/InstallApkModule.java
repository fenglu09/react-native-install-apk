package com.heyao216.react_native_installapk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.FileProvider;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;

/**
 * Created by heyao on 2016/11/4.
 */
public class InstallApkModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private ReactApplicationContext _context = null;
    public static int INSTALL_APK_REQUESTCODE = 111;
    public static int GET_UNKNOWN_APP_SOURCES = 112;
    public static String apk_path;

    public InstallApkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _context = reactContext;
        _context.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "InstallApk";
    }

    @ReactMethod
    public void install(String path) {
        String cmd = "chmod 777 " + path;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(Uri.parse("file://" + path),"application/vnd.android.package-archive");
//        _context.startActivity(intent);

        /** add by david  at 2019-04-18  start */
        // android 8.0 安装apk 问题
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            apk_path = path;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Build.VERSION.SDK_INT >= 26) {
                    boolean installAllowed = getCurrentActivity().getPackageManager().canRequestPackageInstalls();
                    if (installAllowed) {
                        install_APK(intent, path);
                    } else {
                        //无权限 申请权限
                        Uri packageURI = Uri.parse("package:" + getCurrentActivity().getPackageName());
                        Intent intent_Permissions = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                        getCurrentActivity().startActivityForResult(intent_Permissions, GET_UNKNOWN_APP_SOURCES);
                    }
                } else {
                    install_APK(intent, path);
                }

            } else {
                /* Android N之前的老版本写法*/
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
                _context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /** add by david  at 2019-04-18  start */
    }

    /**
     * add by david  at 2019-04-18  start
     */
    // android 8.0 安装apk 问题
    public void install_APK(Intent intent, String path) {
        try {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            /** modify by david at 2019-8-6 start */
            // authorities 更改为当前包名
            String authorities = AppUtils.getAppProcessName(getReactApplicationContext());
            // Uri contentUri = FileProvider.getUriForFile(_context, "com.mglink.mgcircle",
            // new File(path));
            Uri contentUri = FileProvider.getUriForFile(_context, authorities, new File(path));
            /** modify by david at 2019-8-6 end */
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//add by david android 9.0 更新问题

            _context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == GET_UNKNOWN_APP_SOURCES) {
                if (Build.VERSION.SDK_INT >= 26) {
                    boolean installAllowed = getCurrentActivity().getPackageManager().canRequestPackageInstalls();
                    if (installAllowed) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        install_APK(intent, apk_path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
    }
    /**
     * add by david  at 2019-04-18  end
     */
}
