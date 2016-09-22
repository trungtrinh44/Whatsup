package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by trung on 16/09/2016.
 */
public class Client {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private ConcurrentHashMap<String, File> receiverFileMap;
    private String name;

    public Client(Socket socket, String name) throws IOException {
        this.socket = socket;
        this.name = name;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        receiverFileMap = new ConcurrentHashMap<>();
    }


    public ConcurrentHashMap<String, File> getReceiverFileMap() {
        return receiverFileMap;
    }

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public synchronized void write(List<String> messages, int code) {
        try {
            outputStream.writeInt(code);
            if (messages == null) return;
            for (String message : messages) outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void write(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
