package client;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import java.net.Socket;

import static server.ClientHandler.broadcastMessage;

public class ChatUI {
    private JFrame frame;
    private JTextField textField;
    private JTextPane messageArea;
    private PrintWriter out;
    private String username;

    public ChatUI(String username) {
        this.username = username;

        frame = new JFrame("Chat Room - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        messageArea = new JTextPane();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(400, 40));

        JButton sendButton = new JButton("📩");
        sendButton.setPreferredSize(new Dimension(50, 40));
        sendButton.addActionListener(e -> sendMessage());

        JButton sendImageButton = new JButton("📷");
        sendImageButton.setPreferredSize(new Dimension(50, 40));
        sendImageButton.addActionListener(e -> sendImage());

        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(sendImageButton, BorderLayout.WEST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(username);
            new Thread(new IncomingReader(socket)).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            //appendMessage("[You]: " + message);
            textField.setText("");
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image to send");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                Socket imageSocket = new Socket("localhost", 12346);
                OutputStream os = imageSocket.getOutputStream();
                FileInputStream fis = new FileInputStream(selectedFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }

                fis.close();
                os.close();
                imageSocket.close();

                out.println("sent an image");
                //appendMessage(username + " sent an image: ");
                //appendImage(selectedFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = messageArea.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), message + "\n", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void appendImage(String imagePath) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = messageArea.getStyledDocument();
            Style style = messageArea.addStyle("StyleName", null);
            ImageIcon imageIcon = new ImageIcon(imagePath);
            StyleConstants.setIcon(style, imageIcon);
            try {
                doc.insertString(doc.getLength(), " ", style);
                doc.insertString(doc.getLength(), "\n", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private class IncomingReader implements Runnable {
        private BufferedReader in;

        public IncomingReader(Socket socket) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("[IMAGE]:")) {
                        String imagePath = message.substring(8).trim();
                        appendImage(imagePath);
                    } else {
                        appendMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
