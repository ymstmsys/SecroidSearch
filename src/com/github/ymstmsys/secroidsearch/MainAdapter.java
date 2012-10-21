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

public class MainAdapter extends BaseAdapter {

    Context context;

    LayoutInflater inflater;

    List<App> apps = new ArrayList<App>();

    ConcurrentMap<String, WeakReference<Drawable>> iconMap = new ConcurrentHashMap<String, WeakReference<Drawable>>();

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
            System.out.println(app.getPackageName());
            iconDrawable = PackageLogic.getAppIcon(context, app.getPackageName());
            iconMap.put(app.getPackageName(), new WeakReference<Drawable>(iconDrawable));
        }
        ImageView iconImage = (ImageView) convertView.findViewById(R.id.icon_image);
        iconImage.setImageDrawable(iconDrawable);

        return convertView;
    }

}
