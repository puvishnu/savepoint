package com.example.disastermngmnt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity3 extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    private String username;
    private int type;
    Button b1, b2;
    TextView t1;
    ListView l1;
    IntentFilter intentFilter;
    BroadcastReceiver receiver;
    WifiManager wifi;
    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNam;
    WifiP2pDevice[] device;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        setTitle("Home");
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        username = sharedpreferences.getString("Name", "NOT");
        type = sharedpreferences.getInt("Type", 0);
        Toast.makeText(this, "Welcome " + username+"  "+String.valueOf(type), Toast.LENGTH_SHORT).show();
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter = new IntentFilter();
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiBroadcast(channel, manager, this);
        if (type == 1)
            setDeviceName("RESCUER");
        else if(type==0)
            setDeviceName(username.toLowerCase());
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        b1 = findViewById(R.id.button3);
        b2 = findViewById(R.id.button4);
        l1 = findViewById(R.id.list);
        t1=findViewById(R.id.textView2);
        b1.setText("WiFi On\\Off");
        t1.setText("Not Connected");
        t1.setTextColor(Color.RED);
        if (!wifi.isWifiEnabled()) {
            b2.setVisibility(View.INVISIBLE);
            l1.setVisibility(View.INVISIBLE);
        } else {
            b2.setVisibility(View.VISIBLE);
            l1.setVisibility(View.VISIBLE);
        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                if (!wifi.isWifiEnabled()) {
                    b2.setVisibility(View.INVISIBLE);
                    l1.setVisibility(View.INVISIBLE);
                } else {
                    b2.setVisibility(View.VISIBLE);
                }

            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(MainActivity3.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MainActivity3.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Discovering devices", Toast.LENGTH_SHORT).show();
                        Log.w("S", "Discovering devices");
                        t1.setText("Searching...");
                        t1.setTextColor(Color.RED);
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Discovering failed", Toast.LENGTH_SHORT).show();
                        Log.w("F", "Discovering failed");
                    }
                });
                l1.setVisibility(View.VISIBLE);
            }
        });
        l1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice deviceClick = device[i];
                WifiP2pConfig config = new WifiP2pConfig();
                if(type==1)
                    config.groupOwnerIntent = config.GROUP_OWNER_INTENT_MAX;  //Value between 0-15
                else
                    config.groupOwnerIntent = config.GROUP_OWNER_INTENT_MIN;  //Value between 0-15
                config.deviceAddress = deviceClick.deviceAddress;
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.connect(channel, config ,new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Failed to Connect "+deviceClick.deviceName, Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });
    }
    WifiP2pManager.ConnectionInfoListener connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress grpOwn=wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                t1.setText("GroupOwner");
                t1.setTextColor(Color.GREEN);
            }
            else if(wifiP2pInfo.groupFormed ){
                Toast.makeText(getApplicationContext(), "Connected to a Group", Toast.LENGTH_SHORT).show();
                t1.setText("Connected");
                t1.setTextColor(Color.GREEN);
            }
        }
    };
    WifiP2pManager.PeerListListener peerListListener=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if(!wifiP2pDeviceList.equals(peers))
            {
                if(type==1) {
                    peers.clear();
                    peers.addAll(wifiP2pDeviceList.getDeviceList());
                    deviceNam = new String[wifiP2pDeviceList.getDeviceList().size()];
                    device = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];
                    int index = 0;
                    for (WifiP2pDevice d : wifiP2pDeviceList.getDeviceList()) {
                        deviceNam[index] = d.deviceName;
                        device[index] = d;
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNam);
                    l1.setAdapter(arrayAdapter);
                    if (peers.size() == 0)
                        Toast.makeText(getApplicationContext(), "No devices found", Toast.LENGTH_SHORT).show();
                }
                else if(type==0){
                    int index=0,i=0;
                    peers.clear();
                    peers.addAll(wifiP2pDeviceList.getDeviceList());
                    for (WifiP2pDevice d : wifiP2pDeviceList.getDeviceList()) {
                        if (d.deviceName.equalsIgnoreCase("RESCUER")) {
                            index++;
                        }
                    }
                    if (index == 0)
                        Toast.makeText(getApplicationContext(), "No Rescue Groups found", Toast.LENGTH_SHORT).show();
                    else{
                    deviceNam = new String[index];
                    device = new WifiP2pDevice[index];
                    for (WifiP2pDevice d : wifiP2pDeviceList.getDeviceList()) {
                        if(d.deviceName.equalsIgnoreCase("RESCUER")) {
                            deviceNam[i] = d.deviceName;
                            device[i] = d;
                            i++;
                        }
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNam);
                    l1.setAdapter(arrayAdapter);
                    }
                }
            }
        }
    } ;
    @Override
    protected void onResume() {
        super.onResume();
        if (!wifi.isWifiEnabled()) {
            b1.setText("WiFi On\\Off");
            t1.setText("Not Connected");
            b2.setVisibility(View.INVISIBLE);
            l1.setVisibility(View.INVISIBLE);
        } else {
            b2.setVisibility(View.VISIBLE);
        }
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onBackPressed() {
        this.finishAffinity();
    }
    public void setDeviceName(String devName) {
        try {
            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = manager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object arglist[] = new Object[3];
            arglist[0] = channel;
            arglist[1] = devName;
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.w("S", "Success");
                }

                @Override
                public void onFailure(int reason) {
                    Log.w("F", "FAIL");
                }
            };

            setDeviceName.invoke(manager, arglist);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    public class ClientClass extends Thread{
        String hostAdd;
        private InputStream inputStream;
        private OutputStream outputStream;
        public ClientClass(InetAddress hostAddress) {
            hostAdd=hostAddress.getHostAddress();
            socket=new Socket();
        }

        @Override
        public void run() {
            super.run();
            try {
                socket.connect(new InetSocketAddress(hostAdd,8889),500);
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
