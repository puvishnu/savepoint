package com.example.disastermngmnt;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.net.SocketException;
import java.util.ArrayList;

public class MainActivity4 extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    Button send_btn;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        send_btn = findViewById(R.id.send);
        arrayList = new ArrayList<>();
        msgData=findViewById(R.id.msg);
        send=findViewById(R.id.sendButton);
        l = findViewById(R.id.hello);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.chat, R.id.textViewChat, arrayList);
        l.setAdapter(arrayAdapter);
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        ChatName=sharedpreferences.getString("Name", "NOT");
        type = sharedpreferences.getInt("Type", 0);
        activity4=this;
        t_msg=findViewById(R.id.textViewChat);
        if(type==0) {
            msgData.setText("Hii");
            acc = new AudioCallForClients();
            acc.startListen();
            Thread t_ClientRead = new Thread(new ReadThreadForClient(ChatName,activity4));
            t_ClientRead.start();
        }
        else {
            acs = new AudioCallForServer();
            acs.startListen();
            Thread t_ServerRead = new Thread(new ReadThreadForServer(ChatName,activity4));
            t_ServerRead.start();
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
                                   WriteThreadForServer(ChatName,data));
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
    }
    public void letsTalk(View v) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(MainActivity4.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(MainActivity4.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
                if(send_btn.getText().toString().equals("TALK")){
                    // stream audio
                    send_btn.setText("OVER");
                    if(type==0) {
                        acc.startTalk();
                    }
                    else {
                        acs.startTalk();
                    }

                }else if(send_btn.getText().toString().equals("OVER")){
                    send_btn.setText("TALK");
                    if(type==0) {
                        acc.startListen();
                    }
                    else {
                        acs.startListen();
                    }
                }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(type==0)
            acc.endCall();
        else
            acs.endCall();
    }
}
