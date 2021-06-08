package com.example.disastermngmnt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.net.SocketException;
import java.util.ArrayList;

public class MainActivity4 extends AppCompatActivity implements LocationListener {
    private SharedPreferences sharedpreferences;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    ImageButton send_btn;
    ImageButton send;
    ListView l;
    EditText msgData;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;
    private static boolean isRecording = false;
    AudioCallForClients acc;
    AudioCallForServer acs;
    String ChatName;
    int type;
    MainActivity4 activity4;
    TextView t_msg;
    static String locateMe;
    static String locateRescue=null;
    static int menuId;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        send_btn = findViewById(R.id.send);
        arrayList = new ArrayList<>();
        msgData = findViewById(R.id.msg);
        send = findViewById(R.id.sendButton);
        l = findViewById(R.id.hello);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.chat, R.id.textViewChat, arrayList);
        l.setAdapter(arrayAdapter);
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        ChatName = sharedpreferences.getString("Name", "NOT");
        type = sharedpreferences.getInt("Type", 0);
        activity4 = this;
        t_msg = findViewById(R.id.textViewChat);
        Toast.makeText(getApplicationContext(), "Hold on MIC Icon to speak", Toast.LENGTH_LONG).show();
        if(type==0) {
            Thread t_clientSendHii = null;
            try {
                t_clientSendHii = new Thread(new
                        WriteThreadForClient(ChatName,"Hii"));
            } catch (SocketException e) {
                e.printStackTrace();
            }
            // Spawn a thread for reading messages
            t_clientSendHii.start();
            acc = new AudioCallForClients();
            acc.startListen();
            Thread t_ClientRead = new Thread(new ReadThreadForClient(ChatName,activity4));
            t_ClientRead.start();
            menuId=R.menu.mymenuvictim;
        }
        else {
            acs = new AudioCallForServer();
            acs.startListen();
            Thread t_ServerRead = new Thread(new ReadThreadForServer(ChatName,activity4));
            t_ServerRead.start();
            menuId=R.menu.mymenu;
        }
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data=msgData.getText().toString();
                if((data!=null) && !(data.equals(" ")))
                {
                   if(type==0){
                       Thread t_clientSend = null;
                       try {
                           t_clientSend = new Thread(new
                                   WriteThreadForClient(ChatName,data));
                       } catch (SocketException e) {
                           e.printStackTrace();
                       }
                       // Spawn a thread for reading messages
                       t_clientSend.start();
                    }
                    else {
                       Thread t_serverSend = null;
                       try {
                           t_serverSend = new Thread(new
                                   WriteThreadForServer(ChatName,data.toUpperCase()));
                       } catch (SocketException e) {
                           e.printStackTrace();
                       }
                       // Spawn a thread for reading messages
                       t_serverSend.start();
                    }
                    arrayList.add("You: "+data);
                    arrayAdapter.notifyDataSetChanged();
                    msgData.setText("");
                }
            }
        });
        send_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(MainActivity4.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(MainActivity4.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    Toast.makeText(getApplicationContext(),"You are listening....",Toast.LENGTH_SHORT).show();
                    if(type==0) {
                        acc.startListen();
                    }
                    else {
                        acs.startListen();
                    }
                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    Toast.makeText(getApplicationContext(),"You are speaking....",Toast.LENGTH_SHORT).show();
                    if(type==0) {
                        acc.startTalk();
                    }
                    else {
                        acs.startTalk();
                    }
                    return true;
                }
                return false;
            }
        });
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        builder = new AlertDialog.Builder(this);
    }
    public void onLocationChanged(Location location) {
        locateMe=location.getLatitude() + "," + location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menuId, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                // do something
                getMyLocation();
                return true;
            case R.id.item2:
                // do something
                setAlertBox();
                return true;
            case R.id.item3:
                // do something
                if(locateRescue!=null)
                goToRescuersLocation();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    public void setAlertBox(){
        String allNames="";
        for(String str:SocketHandler.getNameClient()){
            allNames+=str+"\n";
        }
        builder.setMessage(allNames)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Victims List");
        alert.show();
    }
    private void goToRescuersLocation() {
        String uri = "http://maps.google.com/maps?f=d&hl=en&daddr="+locateRescue;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    public void getMyLocation(){
        Thread t_serverSendLocation = null;
        try {
            t_serverSendLocation = new Thread(new
                    WriteThreadForServer("LTNLG",locateMe));
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // Spawn a thread for reading messages
        t_serverSendLocation.start();
    }
    public void writeToList(String msg){
        arrayList.add(msg);
        Log.i("WroteToList", msg);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }
    public void getRescueLocation(String locale){
        locateRescue=locale;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(type==0)
            acc.endCall();
        else
            acs.endCall();
    }
}
