package com.example.disastermngmnt;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientSide {
}

class ReadThreadForClient implements Runnable
{
        private DatagramSocket socket;
        private String chatName;
        MainActivity4 activity4;
        private static final int MAX_LEN = 1000;
        int portServerSend=2243;
        ReadThreadForClient(String chatName, MainActivity4 activity4){
            this.chatName=chatName;
            this.activity4=activity4;
        }

        @Override
        public void run()
        {
                byte[] buffer;
                DatagramPacket datagram;
                String message;
            while(true)
            {
                try
                {
                    buffer = new byte[ReadThreadForClient.MAX_LEN];
                    datagram = new DatagramPacket(buffer,buffer.length);
                    socket = new DatagramSocket(portServerSend);
                    socket.receive(datagram);
                        Log.i("ReadFromServer", "SUCCESS");
                    message = new String(buffer,0,datagram.getLength(),"UTF-8");
                    if(!message.startsWith(chatName))
                    {
                        activity4.writeToList(message);
                    }
                }
                catch(IOException e)
                {
                    Log.i("ReadFromServer", e.toString());

                }
                socket.disconnect();
                socket.close();
            }
        }
}

class WriteThreadForClient implements Runnable
{
        private DatagramSocket socket;
        private InetAddress address;
        private String chatName;
        private String message;
        int portClientSend=2234;
        WriteThreadForClient(String chatName,String message) throws SocketException {
            this.socket =new DatagramSocket();
            this.address =SocketHandler.getHost();
            this.chatName=chatName;
            this.message=message;
        }

        @Override
        public void run()
        {
            // Since we are deploying
            message = chatName + ": " + message;
            byte[] buffer = message.getBytes();
            DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,address,portClientSend);
            try {
                socket.send(datagram);
                Log.i("WroteToServer", "SUCCESS");
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.disconnect();
            socket.close();
        }
}