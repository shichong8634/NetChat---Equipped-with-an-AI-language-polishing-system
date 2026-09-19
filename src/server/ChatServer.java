package server;

import org.json.simple.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class ChatServer {
    private static List<ObjectOutputStream> clients = new ArrayList<>();
    private static List<String> usernames = new ArrayList<>();

    public static void main(String[] args) {

        try {
            System.out.println("' Socket' server is starting...");
            ServerSocket serverSocket = new ServerSocket(9999);
            while (true) {
                Socket socket = serverSocket.accept();
                ObjectOutputStream oos =
                        new ObjectOutputStream(socket.getOutputStream());
                clients.add(oos);
                System.out.println("A client connected successfully");
                new Thread(new Server_listen(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void broadcast(Object message) {

        Iterator<ObjectOutputStream> iterator = clients.iterator();

        while (iterator.hasNext()) {
            ObjectOutputStream oos = iterator.next();

            try {
                oos.writeObject(message);
                oos.flush();

            } catch (Exception e) {
                iterator.remove();
            }
        }
    }

    static class Server_listen implements Runnable {
        private Socket socket;

        Server_listen(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    Object message = ois.readObject();


                    if (message instanceof JSONObject) {
                        JSONObject object = (JSONObject) message;

                        if ("login".equals(object.get("type"))) {
                            String username = (String) object.get("username");
                            usernames.add(username);

                            System.out.println(username + " joined the chat.");

                            ChatServer.broadcast("Online users: " + usernames);
                        }

                        if ("chat".equals(object.get("type"))) {
                            ChatServer.broadcast(message);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}