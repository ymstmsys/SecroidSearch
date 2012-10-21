/*
 * Copyright 2012 ymstmsys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ymstmsys.secroidsearch;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class PackageLogic {

    @SuppressLint("NewApi")
    public static List<App> listApps(Context context) {
        Map<Long, App> appMap = new HashMap<Long, App>();

        // get packageName
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            // ignore system app
            if (packageInfo.applicationInfo.sourceDir.startsWith("/system/")) {
                continue;
            }

            long lastUpdateTime;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
                lastUpdateTime = new File(packageInfo.applicationInfo.sourceDir).lastModified();
            } else {
                lastUpdateTime = packageInfo.lastUpdateTime;
            }

            String packageName = packageInfo.packageName;
            String appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();

            appMap.put(lastUpdateTime, new App(packageName, appName));
        }

        // sort by lastUpdateTime
        List<Long> lastUpdateTimes = new ArrayList<Long>(appMap.keySet());
        Collections.sort(lastUpdateTimes);
        Collections.reverse(lastUpdateTimes);

        // take app order by lastUpdatetime
        List<App> apps = new ArrayList<App>();
        for (Long lastUpdateTime : lastUpdateTimes) {
            apps.add(appMap.get(lastUpdateTime));
        }

        return apps;
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
