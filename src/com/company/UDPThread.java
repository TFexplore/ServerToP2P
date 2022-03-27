package com.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class UDPThread extends Thread {

    ConcurrentHashMap<String, Client> clients;

    DatagramSocket socket;
    DatagramPacket packet;
    boolean flag = true;
    Gson gson;
    public static UDPThread Instance;

    static {
        try {
            Instance = new UDPThread();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public UDPThread() throws SocketException, UnknownHostException {
        clients = new ConcurrentHashMap<>();
        socket = new DatagramSocket(8088);
        gson = new Gson();
    }

    public static UDPThread getInstance() {
        return Instance;
    }

    @Override
    public void run() {
        super.run();
        System.out.println("***************服务器Udp监听已开启***************");
        try {
            while (flag) {
                byte[] recvBuf = new byte[1024];
                packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                String res = new String(recvBuf, 0, recvBuf.length);
                String str = new String(res).trim();
                //System.out.println("udp-recv:" + packet.getAddress() + ":" + packet.getPort());
               // System.out.println(str);
                Msg msg = gson.fromJson(str, Msg.class);
                MsgAnalyze(msg);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MsgAnalyze(Msg msg) throws IOException {
        String num;
        String msgbuf;
        switch (msg.getCode()) {
            case 0:

                break;
            case 1://心跳
                String[] strings = msg.getData().split("#");
                if (!clients.containsKey(strings[0])){
                    System.out.println("用户："+msg.getData());
                    System.out.println("udp-recv:" + packet.getAddress() + ":" + packet.getPort());
                }
                clients.put(strings[0], new Client(strings[0], strings[1], packet.getAddress().getHostAddress(), packet.getPort()));
                break;
            case 2://获取用户列表
                System.out.println("----获取用户列表："+msg.getData());
                num = msg.getData();
                msgbuf = gson.toJson(new Msg(msg.getCode(), gson.toJson(clients)));
                MsgSend(clients.get(num), msgbuf);
                break;
            case 3://建立连接
                System.out.println("----建立连接："+msg.getData());
                String[] str = msg.getData().split("#");
                msgbuf = gson.toJson(new Msg(msg.getCode(), gson.toJson(clients.get(str[1]))));
                MsgSend(clients.get(str[0]), msgbuf);
                break;
            case 4://断开连接

            default:

                break;
        }

    }

    private void MsgSend(Client client, String msg) throws IOException {
        System.out.println("udp-send:" + msg);
        InetAddress address = InetAddress.getByName(client.getIp());
        byte[] bufBytes = msg.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(bufBytes, bufBytes.length, address, client.getPort());
        DatagramSocket socket = new DatagramSocket();
        socket.send(datagramPacket);
    }
}
