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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class SecroidLogic {

    public static String getAppUrl(Context context, final App app) {
        // check network connect
        ConnectivityManager connectivityManager = ConnectivityManager.class.cast(context
                .getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            throw new NetworkException();
        }

        AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    return getAppUrl(app.getPackageName(), app.getAppName());
                } catch (RuntimeException e) {
                    return e;
                }
            }
        };

        try {
            Object result = asyncTask.execute().get();
            if (result instanceof RuntimeException) {
                throw RuntimeException.class.cast(result);
            }
            return String.class.cast(result);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getAppUrl(String packageName, String appName) {
        String url = internalGetAppUrl(packageName, appName);

        if (url != null) {
            return url;
        }

        String storeAppName = internalGetStoreAppName(packageName);

        if (storeAppName != null && !storeAppName.equals(appName)) {
            url = internalGetAppUrl(packageName, storeAppName);
        }

        return url;
    }

    private static String internalGetAppUrl(String packageName, String appName) {
        try {
            URL url = new URL("http://secroid.jp/cgi-bin/s.cgi?search=" + URLEncoder.encode(appName, "UTF-8"));

            Source source = new Source(url);

            int pos = 0;
            while (true) {
                Element itemElement = source.getNextElementByClass(pos, "item");

                if (itemElement == null) {
                    break;
                }

                Element aElement = itemElement.getFirstElement("a");
                String href = aElement.getAttributeValue("href");

                if (href.endsWith("/" + packageName + ".html")) {
                    return new URL(url, href).toString();
                }

                pos = itemElement.getEnd();
            }
        } catch (IOException e) {
            throw new NetworkException(e);
        }

        return null;
    }

    private static String internalGetStoreAppName(String packageName) {
        try {
            URL url = new URL("https://play.google.com/store/apps/details?hl=ja&id=" + packageName);

            Source source = new Source(url);

            Element element = source.getFirstElement("h1");

            if (element != null) {
                return element.getTextExtractor().toString();
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

}
