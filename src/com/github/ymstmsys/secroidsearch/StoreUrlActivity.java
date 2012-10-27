package com.github.ymstmsys.secroidsearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class StoreUrlActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent != null) {
            String packageName = null;

            // action view
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                packageName = uri.getQueryParameter("id");
            }
            // action send
            else if (Intent.ACTION_SEND.equals(intent.getAction())) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                Pattern pattern = Pattern.compile("https://play\\.google\\.com/store/apps/details\\?[\\w=\\.]*");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    Uri uri = Uri.parse(matcher.group());
                    packageName = uri.getQueryParameter("id");
                }
            }

            if (packageName != null) {
                viewSecroid(packageName);
            }
        }

        finish();
    }

    protected void viewSecroid(String packageName) {
        // get secroid app url
        String appUrl = SecroidLogic.getAppUrl(packageName);

        // send intent
        Uri uri = Uri.parse(appUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

}
