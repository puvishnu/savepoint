package com.example.disastermngmnt;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;

public class ServerSide {
}
class ReadThreadForServer implements Runnable
{
        private DatagramSocket socket;
        private static final int MAX_LEN = 1000;
        int portClientSend=2234;
        String chatName;
        MainActivity4 activity4;
        ReadThreadForServer(String chatName,MainActivity4 activity4){
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
                    buffer = new byte[ReadThreadForServer.MAX_LEN];
                    datagram = new DatagramPacket(buffer,buffer.length);
                    socket = new DatagramSocket(portClientSend);
                    socket.receive(datagram);
                    Log.i("ReadFromClient", "SUCCESS");
                    message = new String(buffer,0,datagram.getLength(),"UTF-8");
                    if(message.contains("Hii")) {
                        SocketHandler.setAddressClient(datagram.getAddress());
                        int index=message.indexOf(":");
                        SocketHandler.setNameClient(message.substring(0,index));
                    }
                    activity4.writeToList(message);
                    socket.disconnect();
                    socket.close();
                    }
                catch(IOException e)
                {
                    Log.i("ReadFromClient", e.toString());
                }
            }
        }
}
class WriteThreadForServer implements Runnable
{
        private DatagramSocket socket;
        private String chatName;
        int portServerSend=2243;
        private String message;
        WriteThreadForServer(String chatName,String message) throws SocketException {
            this.socket =new DatagramSocket();
            this.message=message;
            this.chatName=chatName;
        }

        @Override
        public void run()
        {
            // Since we are deploying
            message = chatName + ": " + message;
            byte[] buffer = message.getBytes();
            HashSet<InetAddress> addressClients=SocketHandler.getAddressClient();
            for(InetAddress address:addressClients) {
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, address, portServerSend);
                try {
                    socket.send(datagram);
                    Log.i("WroteToClient", "SUCCESS");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            socket.disconnect();
            socket.close();
        }
}