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

import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * @author ymstmsys
 */
public class MainActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainAdapter adapter = new MainAdapter(this);
        setListAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshListAdapter();
    }

    protected void refreshListAdapter() {
        List<App> apps = PackageLogic.listApps(this);
        MainAdapter adapter = MainAdapter.class.cast(getListAdapter());
        adapter.setApps(apps);

        SortType sortType = getSortTypePreference();
        adapter.sort(sortType);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        MainAdapter adapter = MainAdapter.class.cast(getListAdapter());
        final String packageName = App.class.cast(adapter.getItem(position)).getPackageName();

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // View Secroid
                if (which == 0) {
                    viewSecroid(packageName);
                }
                // View Store
                else if (which == 1) {
                    viewStore(packageName);
                }
                // View Info
                else if (which == 2) {
                    viewInfo(packageName);
                }
                // Uninstall
                else if (which == 3) {
                    uninstall(packageName);
                }
            }
        };

        Dialog dialog = new AlertDialog.Builder(this).setItems(R.array.actions, listener).create();
        dialog.show();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        if (id == 1) {
            String errorMessage = bundle.getString("errorMessage");

            return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.error)
                    .setMessage(errorMessage).setNegativeButton(R.string.ok, null).create();
        }
        return null;
    }

    protected void viewSecroid(String packageName) {
        // get secroid app url
        Locale locale = getResources().getConfiguration().locale;
        String appUrl = SecroidLogic.getAppUrl(packageName, locale);

        // send intent
        Uri uri = Uri.parse(appUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    protected void viewStore(String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Bundle bundle = new Bundle();
            bundle.putString("errorMessage", getString(R.string.store_error_message));
            showDialog(1, bundle);
        }
    }

    protected void viewInfo(String packageName) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("pkg", packageName);
            startActivity(intent);
        } else {
            Uri uri = Uri.fromParts("package", packageName, null);
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", uri);
            startActivity(intent);
        }
    }

    protected void uninstall(String packageName) {
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            refreshListAdapter();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SortType sortType = getSortTypePreference();

        if (sortType == SortType.LAST_UPDATE_TIME) {
            menu.findItem(R.id.menu_sort_lastUpdateTime).setVisible(false);
            menu.findItem(R.id.menu_sort_appName).setVisible(true);
        } else if (sortType == SortType.APP_NAME) {
            menu.findItem(R.id.menu_sort_lastUpdateTime).setVisible(true);
            menu.findItem(R.id.menu_sort_appName).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // sort by lastUpdateTime
        if (item.getItemId() == R.id.menu_sort_lastUpdateTime) {
            MainAdapter adapter = MainAdapter.class.cast(getListAdapter());
            adapter.sort(SortType.LAST_UPDATE_TIME);
            putSortTypePreference(SortType.LAST_UPDATE_TIME);
        }
        // sort by appName
        else if (item.getItemId() == R.id.menu_sort_appName) {
            MainAdapter adapter = MainAdapter.class.cast(getListAdapter());
            adapter.sort(SortType.APP_NAME);
            putSortTypePreference(SortType.APP_NAME);
        }
        // show description
        else if (item.getItemId() == R.id.menu_description) {
            Uri uri = Uri.parse("market://details?id=com.github.ymstmsys.secroidsearch");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Bundle bundle = new Bundle();
                bundle.putString("errorMessage", getString(R.string.store_error_message));
                showDialog(1, bundle);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected SortType getSortTypePreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortType = preferences.getString("sortType", null);

        if (sortType == null) {
            return SortType.LAST_UPDATE_TIME;
        }

        try {
            return SortType.valueOf(sortType);
        } catch (IllegalArgumentException e) {
            return SortType.LAST_UPDATE_TIME;
        }
    }

    protected void putSortTypePreference(SortType sortType) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = preferences.edit();
        editor.putString("sortType", sortType.name());
        editor.commit();
    }

}
