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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author ymstmsys
 */
public class MainAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater inflater;

    private List<App> apps = new ArrayList<App>();

    private ConcurrentMap<String, WeakReference<Drawable>> iconMap = new ConcurrentHashMap<String, WeakReference<Drawable>>();

    private static final Comparator<App> lastUpdateTimeComparator = new Comparator<App>() {
        @Override
        public int compare(App app1, App app2) {
            long diff = app2.getLastUpdateTime() - app1.getLastUpdateTime();
            return diff == 0 ? 0 : (diff > 0 ? 1 : -1);
        }
    };

    private static final Comparator<App> appNameComparator = new Comparator<App>() {
        @Override
        public int compare(App app1, App app2) {
            return app1.getAppName().compareTo(app2.getAppName());
        }
    };

    public MainAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setApps(List<App> apps) {
        this.apps = apps;
    }

    public int getCount() {
        return apps.size();
    }

    public Object getItem(int position) {
        return apps.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_main_item, null);
        }

        App app = apps.get(position);

        TextView packageText = TextView.class.cast(convertView.findViewById(R.id.package_text));
        packageText.setText(app.getPackageName());

        TextView appText = TextView.class.cast(convertView.findViewById(R.id.app_text));
        appText.setText(app.getAppName());

        // TODO: add icon unvisible option
        Drawable iconDrawable;
        WeakReference<Drawable> iconReference = iconMap.get(app.getPackageName());
        if (iconReference != null && iconReference.get() != null) {
            iconDrawable = iconReference.get();
        } else {
            iconDrawable = PackageLogic.getAppIcon(context, app.getPackageName());
            iconMap.put(app.getPackageName(), new WeakReference<Drawable>(iconDrawable));
        }
        ImageView iconImage = (ImageView) convertView.findViewById(R.id.icon_image);
        iconImage.setImageDrawable(iconDrawable);

        return convertView;
    }

    public void sort(SortType sortType) {
        if (sortType == SortType.LAST_UPDATE_TIME) {
            sortByLastUpdateTime();
        } else if (sortType == SortType.APP_NAME) {
            sortByAppName();
        }
    }

    private void sortByLastUpdateTime() {
        Collections.sort(apps, lastUpdateTimeComparator);
        notifyDataSetChanged();
    }

    private void sortByAppName() {
        Collections.sort(apps, appNameComparator);
        notifyDataSetChanged();
    }

}
