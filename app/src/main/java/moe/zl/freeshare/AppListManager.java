package moe.zl.freeshare;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AppListManager {

    // SharedPreferences 文件名和键名
    private static final String PREFS_NAME = "app_list_prefs";
    private static final String KEY_MODE = "key_mode";
    private static final String KEY_APP_SET = "key_app_set";

    // 单例实例
    private static volatile AppListManager instance;

    private final SharedPreferences sharedPreferences;

    // 定义模式的枚举
    public enum Mode {
        WHITELIST, // 白名单模式
        BLACKLIST  // 黑名单模式
    }

    // 私有构造函数
    private AppListManager(Context context) {
        // 使用 Application Context 防止内存泄漏
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 获取单例实例
     *
     * @param context Context
     * @return AppListManager 实例
     */
    public static AppListManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AppListManager.class) {
                if (instance == null) {
                    instance = new AppListManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 设置当前模式（白名单或黑名单）
     *
     * @param mode 模式
     */
    public void setMode(Mode mode) {
        sharedPreferences.edit().putString(KEY_MODE, mode.name()).apply();
    }

    /**
     * 获取当前模式
     *
     * @return 当前模式，默认为黑名单
     */
    public Mode getMode() {
        String modeName = sharedPreferences.getString(KEY_MODE, Mode.BLACKLIST.name());
        return Mode.valueOf(modeName);
    }

    /**
     * 向列表中添加一个应用
     *
     * @param packageName 应用的包名
     */
    public void addApp(String packageName) {
        Set<String> appSet = getAppList();
        appSet.add(packageName);
        sharedPreferences.edit().putStringSet(KEY_APP_SET, appSet).apply();
    }

    /**
     * 从列表中移除一个应用
     *
     * @param packageName 应用的包名
     */
    public void removeApp(String packageName) {
        Set<String> appSet = getAppList();
        appSet.remove(packageName);
        sharedPreferences.edit().putStringSet(KEY_APP_SET, appSet).apply();
    }

    /**
     * 获取完整的应用列表
     *
     * @return 包含所有包名的 Set
     */
    public Set<String> getAppList() {
        // 返回一个可修改的副本，避免直接修改原始集合
        return new HashSet<>(sharedPreferences.getStringSet(KEY_APP_SET, Collections.emptySet()));
    }

    /**
     * 核心方法：检查指定包名的应用是否符合当前规则
     *
     * @param packageName 要检查的应用包名
     * @return true 如果符合条件，否则 false
     */
    public boolean isAppCompliant(String packageName) {
        Mode currentMode = getMode();
        Set<String> appSet = getAppList();
        boolean isInList = appSet.contains(packageName);

        if (currentMode == Mode.WHITELIST) {
            // 白名单模式：在列表中的应用才符合条件
            return isInList;
        } else { // currentMode == Mode.BLACKLIST
            // 黑名单模式：不在列表中的应用才符合条件
            return !isInList;
        }
    }

    /**
     * 清空列表
     */
    public void clearList() {
        sharedPreferences.edit().remove(KEY_APP_SET).apply();
    }
}