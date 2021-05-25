package com.example.disastermngmnt;
import java.net.InetAddress;
import java.util.HashSet;

public class SocketHandler {
    private static HashSet<InetAddress> addressClient=new HashSet<>();
    public static  HashSet<InetAddress> getAddressClient() {
        return addressClient;
    }
    public static  void setAddressClient(InetAddress address) {
        SocketHandler.addressClient.add(address);
    }
    private static InetAddress host;
    public static InetAddress getHost() {
        return host;
    }
    public static void setHost(InetAddress host) {
        SocketHandler.host = host;
    }
}
