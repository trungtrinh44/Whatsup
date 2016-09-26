package server;

import common.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

/**
 * Created by trung on 16/09/2016.
 */

public class ServerListener extends Thread {

    private ServerClient client;
    private ConcurrentHashMap<String, ServerClient> clientAddressMap;
    //private String clientAddress;

    public ServerListener(ServerClient socket, ConcurrentHashMap<String, ServerClient> addressmap) {
        client = socket;
        clientAddressMap = addressmap;
    }

    @Override
    public void run() {
        try {
            InputStream input = client.getInputStream();
            //client.setName(Protocol.readString(input));
            //Get client ip address
            //clientAddress = client.getSocket().getRemoteSocketAddress().toString().substring(1);
            clientAddressMap.put(client.getAddress(), client);
            System.out.println("Connected with client at " + client.getAddress() + " " + client.getName());
            boolean isConnected = true;
            while (isConnected) {
                switch (Protocol.readInt(input)) {
                    case Protocol.ONLINE_LIST_CODE:
                        sendList();
                        break;
                    case Protocol.SEND_MSG_CODE:
                        sendMessage(Protocol.readString(input), Protocol.readString(input));
                        break;
                    case Protocol.FILE_REQ_CODE:
                        sendFileRequest(Protocol.readString(input), Protocol.readString(input));
                        break;
                    case Protocol.END_CONNECT_CODE:
                        isConnected = false;
                        client.write(asList(Protocol.intToBytes(Protocol.END_CONNECT_CODE)));
                        clientAddressMap.remove(client.getAddress());
                        break;
                    case Protocol.ACCEPT_FILE:
                        fileReqAccept(Protocol.readString(input), Protocol.readString(input),
                                Protocol.readString(input), Protocol.readString(input), Protocol.readInt(input));
                        break;
                    case Protocol.DENY_FILE:
                        fileReqDenied(Protocol.readString(input));
                        break;
                }
            }
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientAddressMap.remove(client.getAddress());
            System.out.println("Client at " + client.getAddress() + " disconnect from server.");
        }
    }

    private void sendList() {
        /*List<String> stringList = new ArrayList<>(clientAddressMap.size() + 1);
        stringList.add(String.valueOf(clientAddressMap.size()));
        stringList.addAll(clientAddressMap.entrySet().stream().map(entry -> entry.getKey() + " " + entry.getValue().getName()).collect(Collectors.toList()));
        client.write(stringList, Protocol.ONLINE_LIST_CODE);*/
        try {
            List<byte[]> packet = new ArrayList<>(clientAddressMap.size() * 2 + 2);
            packet.add(Protocol.intToBytes(Protocol.ONLINE_LIST_CODE));
            packet.add(Protocol.intToBytes(clientAddressMap.size()));
            for (Map.Entry<String, ServerClient> e : clientAddressMap.entrySet()) {
                packet.add(Protocol.stringToBytes(e.getKey() + " " + e.getValue().getName()));
            }
            client.write(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fileReqDenied(String fromAddress) {
        /*clientAddressMap.get(fromAddress).write(asList(client.getName(),
                client.getSocket().getRemoteSocketAddress().toString()), Protocol.DENY_FILE);*/
        try {
            //byte[] name = client.getName().getBytes(Protocol.ENCODE);
            //byte[] address = clientAddress.getBytes(Protocol.ENCODE);
            //clientAddressMap.get(fromAddress).write(asList(Protocol.intToBytes(Protocol.DENY_FILE),
            //      Protocol.intToBytes(name.length), name, Protocol.intToBytes(address.length), address));
            clientAddressMap.get(fromAddress).write(asList(Protocol.intToBytes(Protocol.DENY_FILE),
                    Protocol.stringToBytes(client.getName()), Protocol.stringToBytes(client.getAddress())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean sendFileRequest(String receiver, String filename) {
        ServerClient recei = clientAddressMap.get(receiver);
        if (recei == null) return false;
        try {
            /*byte[] name = client.getName().getBytes(Protocol.ENCODE);
            byte[] address = clientAddress.getBytes(Protocol.ENCODE);
            byte[] file = filename.getBytes(Protocol.ENCODE);*/
            recei.write(asList(Protocol.intToBytes(Protocol.FILE_REQ_CODE)
                    , Protocol.stringToBytes(client.getName()), Protocol.stringToBytes(client.getAddress()),
                    Protocol.stringToBytes(filename)));
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        /*clientAddressMap.get(receiver).write(asList(client.getName(),
                client.getSocket().getRemoteSocketAddress().toString(), filename), Protocol.FILE_REQ_CODE);*/
    }

    private void fileReqAccept(String receiver, String name, String address, String server, int port) {
        /*clientAddressMap.get(receiver).write(asList(name, address, server, port), Protocol.ACCEPT_FILE);*/
        try {
            /*byte[] nam = name.getBytes(Protocol.ENCODE);
            byte[] add = address.getBytes(Protocol.ENCODE);
            byte[] ser = server.getBytes(Protocol.ENCODE);*/

            clientAddressMap.get(receiver).write(asList(Protocol.intToBytes(Protocol.ACCEPT_FILE),
                    Protocol.stringToBytes(name), Protocol.stringToBytes(address),
                    Protocol.stringToBytes(server), Protocol.intToBytes(port)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean sendMessage(String toAddress, String message) {
        try {


            ServerClient receiver = clientAddressMap.get(toAddress);
        if (receiver != null) {
            //byte[] name = client.getName().getBytes(Protocol.ENCODE);
            //byte[] address = clientAddress.getBytes(Protocol.ENCODE);
            //byte[] mess = message.getBytes(Protocol.ENCODE);
            receiver.write(asList(Protocol.intToBytes(Protocol.SEND_MSG_CODE),
                    Protocol.stringToBytes(client.getName()), Protocol.stringToBytes(client.getAddress()),
                    Protocol.stringToBytes(message)));
            /*receiver.write(asList(client.getName(),
                    client.getSocket().getRemoteSocketAddress().toString(), message), Protocol.SEND_MSG_CODE);*/
            return true;
        } else return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
