import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame {
    private JTextArea messageArea;
    private JTextField inputField;
    private BufferedReader in;
    private PrintWriter out;
    private String userName;

    public ChatClient() {
        setTitle("チャットクライアント");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        
        inputField = new JTextField();
        JButton sendButton = new JButton("送信");

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5555);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // ユーザー名の入力
            userName = JOptionPane.showInputDialog(this, "ユーザー名を入力してください:", "ユーザー名", JOptionPane.QUESTION_MESSAGE);
            if (userName != null && !userName.trim().isEmpty()) {
                out.println(userName);
            } else {
                System.exit(0);
            }

            // サーバーからのメッセージを受信
            new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        final String finalLine = line;
                        SwingUtilities.invokeLater(() -> messageArea.append(finalLine + "\n"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient());
    }
}
