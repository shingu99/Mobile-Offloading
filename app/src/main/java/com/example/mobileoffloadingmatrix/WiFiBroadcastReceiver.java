package com.example.mobileoffloadingmatrix;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class WiFiBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Activity mainActivity;
    public WifiP2pManager.PeerListListener listner;
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    TextView connectionStatus;

    public WiFiBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, Activity mainActivity, WifiP2pManager.PeerListListener listner, WifiP2pManager.ConnectionInfoListener connectionInfoListener, TextView connectionStatus) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mainActivity = mainActivity;
        this.listner = listner;
        this.connectionInfoListener = connectionInfoListener;
        this.connectionStatus = connectionStatus;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFi is ON", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "WiFi is OFF", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mManager.requestPeers(mChannel,listner );
                    return;
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if(mManager==null)
            {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected())
            {
                mManager.requestConnectionInfo(mChannel, connectionInfoListener);
            }
            else
            {
                connectionStatus.setText("Device Disconnected");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}



