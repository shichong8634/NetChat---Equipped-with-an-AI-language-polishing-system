package client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ChatClient {
    private static Socket socket;
    public static boolean connection_state = false;
    public static String username;

    private static JFrame frame;
    static JTextArea chatArea;
    private static JTextField messageField;
    private static ObjectOutputStream oos;

    public static void main(String[] args){

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        username = scanner.nextLine();

        createGUI();

        while (!connection_state) {
            connect();

            try {
                Thread.sleep(3000);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void connect(){
        try {
            socket = new Socket("127.0.0.1", 9999);
            connection_state = true;
            oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            JSONObject login = new JSONObject();
            login.put("type", "login");
            login.put("username", ChatClient.username);

            oos.writeObject(login);
            oos.flush();

            new Thread(new Client_listen(socket,ois)).start();
            new Thread(new Client_heart(socket,oos)).start();
        }catch (Exception e){
            e.printStackTrace();
            connection_state = false;
        }
    }

    public static void reconnect(){
        while (!connection_state){
            System.out.println("Trying to reconnect.....");
            connect();
            try {
                Thread.sleep(3000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void improveMessage(JButton improveButton) {

        String message = messageField.getText();

        if (message.isEmpty()) {
            return;
        }

        String apiKey = System.getenv("DEEPSEEK_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "DeepSeek API key is not configured.");
            return;
        }

        new Thread(() -> {

            try {

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content",
                        "Improve this chat message. Keep the original meaning and make it clear and natural. Return only the improved message:\n"
                                + message);

                org.json.simple.JSONArray messages =
                        new org.json.simple.JSONArray();

                messages.add(userMessage);

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "deepseek-flash");
                requestBody.put("messages", messages);
                requestBody.put("stream", false);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                "https://api.deepseek.com/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(
                                requestBody.toJSONString()))
                        .build();

                HttpClient client = HttpClient.newHttpClient();

                HttpResponse<String> response =
                        client.send(request,
                                HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {

                    JSONObject result =
                            (JSONObject) new org.json.simple.parser.JSONParser()
                                    .parse(response.body());

                    org.json.simple.JSONArray choices =
                            (org.json.simple.JSONArray) result.get("choices");

                    JSONObject choice =
                            (JSONObject) choices.get(0);

                    JSONObject messageObject =
                            (JSONObject) choice.get("message");

                    String improvedMessage =
                            (String) messageObject.get("content");

                    SwingUtilities.invokeLater(() ->
                            messageField.setText(improvedMessage)
                    );

                } else {

                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame,
                                    "DeepSeek API error: "
                                            + response.statusCode())
                    );
                }

            } catch (Exception e) {

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(frame,
                                "AI Improve failed: " + e.getMessage())
                );
            }

        }).start();
    }

    private static void createGUI() {
        frame = new JFrame("NetChat AI - " + username);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel backgroundPanel = new JPanel() {

            private Image backgroundImage =
                    new ImageIcon("image/background.png").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.drawImage(
                        backgroundImage,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        this
                );
            }
        };

        backgroundPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setOpaque(false);
        chatArea.setForeground(Color.WHITE);

        messageField = new JTextField();
        messageField.setOpaque(false);
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);

        JButton sendButton = new JButton("Send");
        JButton improveButton = new JButton("AI Improve");

        sendButton.addActionListener(e -> {

            String message = messageField.getText();

            if (!message.isEmpty()) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("type", "chat");
                    object.put("username", ChatClient.username);
                    object.put("msg", message);

                    oos.writeObject(object);
                    oos.flush();

                    messageField.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        improveButton.addActionListener(e -> {
            improveButton.setEnabled(false);
            improveMessage(improveButton);
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(improveButton);
        buttonPanel.add(sendButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        bottomPanel.setOpaque(false);

        backgroundPanel.add(scrollPane, BorderLayout.CENTER);
        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(backgroundPanel);

        frame.setVisible(true);
    }
}

class Client_listen implements Runnable{
    private Socket socket;
    private ObjectInputStream ois;

    Client_listen(Socket socket,ObjectInputStream ois){
        this.socket = socket;
        this.ois = ois;
    }

    @Override
    public void run() {
        try {
            while (true){
                Object message = ois.readObject();

                SwingUtilities.invokeLater(() -> {

                    if (message instanceof JSONObject) {
                        JSONObject object = (JSONObject) message;

                        if ("chat".equals(object.get("type"))) {
                            ChatClient.chatArea.append(
                                    object.get("username") + ": "
                                            + object.get("msg") + "\n"
                            );
                        } else {
                            ChatClient.chatArea.append(message.toString() + "\n");
                        }

                    } else {
                        ChatClient.chatArea.append(message.toString() + "\n");
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class Client_heart implements Runnable{
    private Socket socket;
    private ObjectOutputStream oos;

    Client_heart(Socket socket, ObjectOutputStream oos){
        this.socket = socket;
        this.oos = oos;
    }

    @Override
    public void run() {
        try {
            System.out.println("Heartbeat thread started...");
            while (true){
                Thread.sleep(5000);
                JSONObject object = new JSONObject();
                object.put("type","heart");
                object.put("msg","heartbeat");
                oos.writeObject(object);
                oos.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                socket.close();
                ChatClient.connection_state = false;
                ChatClient.reconnect();
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
    }
}
