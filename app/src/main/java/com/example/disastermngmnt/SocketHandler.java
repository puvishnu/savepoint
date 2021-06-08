package com.example.disastermngmnt;
import java.net.InetAddress;
import java.util.HashSet;

public class SocketHandler {
    private static HashSet<InetAddress> addressClient=new HashSet<>();
    private static HashSet<String> nameClient=new HashSet<>();
    private static InetAddress host;
    public static  HashSet<InetAddress> getAddressClient() {
        return addressClient;
    }
    public static  void setAddressClient(InetAddress address) {
        SocketHandler.addressClient.add(address);
    }
    public static  HashSet<String> getNameClient() {
        return nameClient;
    }
    public static  void setNameClient(String name) {
        SocketHandler.nameClient.add(name);
    }
    public static InetAddress getHost() {
        return host;
    }
    public static void setHost(InetAddress host) {
        SocketHandler.host = host;
    }
}
