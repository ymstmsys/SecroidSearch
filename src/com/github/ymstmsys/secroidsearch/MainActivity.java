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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
        adapter.notifyDataSetChanged();
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
        String appUrl = null;
        String errorMessage = null;
        try {
            appUrl = SecroidLogic.getAppUrl(packageName);
            if (appUrl == null) {
                errorMessage = getString(R.string.secroid_error_message);
            }
        } catch (NetworkException e) {
            errorMessage = getString(R.string.network_error_message);
        } catch (RuntimeException e) {
            Log.e("SecroidSearch", e.getMessage(), e);
            throw e;
        }

        if (errorMessage != null) {
            Bundle bundle = new Bundle();
            bundle.putString("errorMessage", errorMessage);
            showDialog(1, bundle);
            return;
        }

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

}
