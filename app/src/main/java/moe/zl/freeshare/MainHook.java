package moe.zl.freeshare;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.app.ActivityOptions;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MainHook implements IXposedHookLoadPackage {

  private String DISPLAY_RESOLVE_INFO = "com.android.intentresolver.chooser.DisplayResolveInfo";
  private String CHOOSER_TARGET_INFO = "com.android.intentresolver.chooser.ChooserTargetInfo";
  private String TARGET_INFO = "com.android.intentresolver.chooser.TargetInfo";
  private String TAG = "Free Share";

  private XC_MethodHook hookmethod =
      new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
          // ComponentName是唯一标识符
          ComponentName resolvedComponentName =
              (ComponentName)
                  XposedHelpers.callMethod(param.thisObject, "getResolvedComponentName");

          XposedBridge.log(TAG + ": User selected: " + resolvedComponentName.flattenToString());

          param.args[1] = freeBundle((Bundle) param.args[1]);

          XposedBridge.log(TAG + ": Successfully hooked ");
        }
      };

  private Bundle freeBundle(Bundle options) {
    Bundle newOptionsBundle;

    if (options == null) {
      ActivityOptions activityOptions = ActivityOptions.makeBasic();
      newOptionsBundle = activityOptions.toBundle();
    } else {
      newOptionsBundle = new Bundle(options);
    }

    newOptionsBundle.putInt("android.activity.windowingMode", 5);
    return newOptionsBundle;
  }

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    XposedBridge.log(TAG + ": Found target package: " + lpparam.packageName);

    try {
            //安卓13剥离了负责处理的应用，弃用了ChooserTargetInfo
      if (Build.VERSION.SDK_INT < 33) {
        DISPLAY_RESOLVE_INFO = "com.android.internal.app.chooser.TargetInfo";
        XposedHelpers.findAndHookMethod(
            DISPLAY_RESOLVE_INFO,
            lpparam.classLoader,
            "startAsCaller",
            Activity.class,
            UserHandle.class,
            int.class,
            new XC_MethodHook() {
              @Override
              protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // ComponentName是唯一标识符
                ComponentName resolvedComponentName =
                    (ComponentName)
                        XposedHelpers.callMethod(param.args[0], "getResolvedComponentName");

                XposedBridge.log(
                    TAG + ": User selected: " + resolvedComponentName.flattenToString());

                param.args[2] = freeBundle((Bundle) param.args[2]);

                XposedBridge.log(TAG + ": Successfully hooked ");
              }
            });
      } else {
        XposedHelpers.findAndHookMethod(
            "com.android.intentresolver.ChooserActivity",
            lpparam.classLoader,
            "safelyStartActivityInternal",
            TARGET_INFO,
            UserHandle.class,
            Bundle.class,
            new XC_MethodHook() {
              @Override
              protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // ComponentName是唯一标识符
                ComponentName resolvedComponentName =
                    (ComponentName)
                        XposedHelpers.callMethod(param.args[0], "getResolvedComponentName");

                XposedBridge.log(
                    TAG + ": User selected: " + resolvedComponentName.flattenToString());

                param.args[2] = freeBundle((Bundle) param.args[2]);

                XposedBridge.log(TAG + ": Successfully hooked ");
              }
            });
      }
    } catch (XposedHelpers.ClassNotFoundError e) {
      XposedBridge.log(TAG + ": Class not found ");
      XposedBridge.log(e);
    } catch (NoSuchMethodError e) {
      XposedBridge.log(TAG + ": Method not found ");
      XposedBridge.log(e);
    } catch (Throwable t) {
      XposedBridge.log(TAG + ": An unexpected error occurred during hooking.");
      XposedBridge.log(t);
    }
  }
}
