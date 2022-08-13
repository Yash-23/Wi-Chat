package com.stg_solutions.wichat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.stg_solutions.wichat.BroadcastReceivers.WifiDirectBroadcastReceiver;
import com.stg_solutions.wichat.InitThreads.ClientInit;
import com.stg_solutions.wichat.InitThreads.ServerInit;


public class ChatPage extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final String DEFAULT_CHAT_NAME = "";
    private EditText EnterName;
    private Button enterChat;
    //adding the image view later
    private ImageView disconnect;
    public static String chatName;

    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    public static ServerInit server;


    //get set functions
    public EditText getSetChatName() {
        return EnterName;
    }

    public Button getGoToChat() {
        return enterChat;
    }

    public ImageView getDisconnect() {
        return disconnect;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        init();

        enterChat = findViewById(R.id.EnterChatButton);
        Chat();

        EnterName = findViewById(R.id.Name);
        EnterName.setText(loadChatName(this));

        disconnect = findViewById(R.id.disconnect);
        disconnect();
    }


    public void init() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = WifiDirectBroadcastReceiver.createInstance();
        mReceiver.setmManager(mManager);
        mReceiver.setmChannel(mChannel);
        mReceiver.setmActivity(this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Discovery process succeeded");
            }

            @Override
            public void onFailure(int i) {
                Log.v(TAG, "Discovery process failed");
            }
        });
   }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    public void Chat() {
        enterChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!EnterName.getText().toString().equals("")){
                    saveChatName(ChatPage.this,EnterName.getText().toString());
                    chatName = loadChatName(ChatPage.this);

                    //start init threads
                    if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_OWNER){
                        Toast.makeText(ChatPage.this, "I'm the group owner  " + mReceiver.getOwnerAddr().getHostAddress(), Toast.LENGTH_SHORT).show();
                        server = new ServerInit();

                    }

                    else if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_CLIENT){
                        Toast.makeText(ChatPage.this, "I'm the client", Toast.LENGTH_SHORT).show();
                        ClientInit client = new ClientInit(mReceiver.getOwnerAddr());
                        client.start();
                    }

                    //Open the ChatActivity

                    Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                    startActivity(intent);

                }
                else{
                    Toast.makeText(ChatPage.this, "Enter a chat name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveChatName(Context context,String chatName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("chatName",chatName);
        edit.apply();
    }

    public static String loadChatName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("chatName",DEFAULT_CHAT_NAME);
    }

    public void disconnect(){
        disconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mManager.removeGroup(mChannel, null);
                finish();
            }
        });
    }
    
}