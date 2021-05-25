package com.example.disastermngmnt;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import androidx.core.app.ActivityCompat;

public class WiFiBroadcast extends BroadcastReceiver {
    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    MainActivity3 activity;

    public WiFiBroadcast(WifiP2pManager.Channel channel, WifiP2pManager manager, MainActivity3 activity) {
        this.channel = channel;
        this.manager = manager;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                    return;
                }
                manager.requestPeers(channel, activity.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if(manager!=null)
                {
                    NetworkInfo networkInfo=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.isConnected())
                        manager.requestConnectionInfo(channel,activity.connectionInfoListener);
                }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }
    }
}
