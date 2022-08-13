package com.stg_solutions.wichat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;

    ImageView wifiIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //to open wifi settings
        wifiIcon = (ImageView) findViewById(R.id.wifi_icon);
        goToSettings();

        Button WiFiCheck = (Button) findViewById(R.id.ConnectionCheck);
        WiFiCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                Intent start = new Intent(MainActivity.this, ChatPage.class);
                startActivity(start);
                finish();
            }
        });
    }

    private void goToSettings(){
        wifiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //Open Wifi settings
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
            }
        });
    }
    public ImageView getGoToSettings() { return wifiIcon; }

}