package com.stg_solutions.wichat.BroadcastReceivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.stg_solutions.wichat.ChatPage;
import com.stg_solutions.wichat.MainActivity;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiDirectBR";
    private Activity mActivity;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private InetAddress ownerAddr;
    private int isGroupeOwner;
    private static WifiDirectBroadcastReceiver instance;

    private List<String> peersName = new ArrayList<String>();
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    public static final int IS_OWNER = 1;
    public static final int IS_CLIENT = 2;

    //constructor
    private WifiDirectBroadcastReceiver(){ super(); }

    public static WifiDirectBroadcastReceiver createInstance(){
        if(instance == null){
            instance = new WifiDirectBroadcastReceiver();
        }
        return instance;
    }

    public List<String> getPeersName() { return peersName; }
    public List<WifiP2pDevice> getPeers() { return peers; }
    public int isGroupeOwner() { return isGroupeOwner; }
    public InetAddress getOwnerAddr() { return ownerAddr; }
    public void setmManager(WifiP2pManager mManager) { this.mManager = mManager; }
    public void setmChannel(WifiP2pManager.Channel mChannel) { this.mChannel = mChannel; }
    public void setmActivity(Activity mActivity) { this.mActivity = mActivity; }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //check weather wifi p2p is enabled or disabled
        if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
            Log.v(TAG,"WIFI_P2P_STATE_CHANGED_ACTION");

            //check WiFi P2P is supported
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Toast.makeText(mActivity, "Wifi P2P is supported by this device", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(mActivity, "Wifi P2P is not supported by this device", Toast.LENGTH_SHORT).show();
            }
        }
        //available peer list changed
        else if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)){
            Log.v(TAG,"WIFI_P2P_PEERS_CHANGED_ACTION");
        }
        //device wifi state has changed
        else if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){
            Log.v(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        }
        //State of connectivity has changed (new connection/disconnection)
        else if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
            Log.v(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
            if(mManager==null ){
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()){
                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        ownerAddr= info.groupOwnerAddress;

                        //create server thread and accept incoming connections
                        if(info.groupFormed && info.isGroupOwner){
                            isGroupeOwner = IS_OWNER;
                            activateGoToChat("server");
                        }
                        //create client thread and connect to group owner
                        else if(info.groupFormed){
                            isGroupeOwner = IS_CLIENT;
                            activateGoToChat("client");
                        }
                    }
                });
            }
        }
    }

    public void activateGoToChat(String role){
        if(mActivity.getClass() == ChatPage.class){
            ((ChatPage)mActivity).getGoToChat().setText("Start the chat "+role);
            ((ChatPage)mActivity).getGoToChat().setVisibility(View.VISIBLE);
            ((ChatPage)mActivity).getSetChatName().setVisibility(View.VISIBLE);
            ((ChatPage)mActivity).getDisconnect().setVisibility(View.VISIBLE);
            ((MainActivity)mActivity).getGoToSettings().setVisibility(View.VISIBLE);

            //// TODO: get owneraddress and pass it to main
        }
    }
}
