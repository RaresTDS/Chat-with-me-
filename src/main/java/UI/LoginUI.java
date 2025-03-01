package UI;

import client.ChatUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LoginUI {
    private static final String USERS_FILE = "users.txt";
    private Map<String, String> userDatabase = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().createAndShowGUI());
    }

    public LoginUI() {
        loadUsers();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(360, 500);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        ImageIcon originalIcon = new ImageIcon("src/main/java/UI/speech-bubble.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(resizedIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(imageLabel);
        panel.add(Box.createVerticalStrut(15));

        JPanel usernamePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        usernamePanel.setBackground(Color.WHITE);
        JLabel usernameLabel = new JLabel("Username", SwingConstants.CENTER);
        JTextField usernameField = new JTextField(20);

        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(280, 35));

        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        JPanel passwordPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        passwordPanel.setBackground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Password", SwingConstants.CENTER);
        JPasswordField passwordField = new JPasswordField(20);

        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(280, 35));

        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));

        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.setBackground(new Color(24, 119, 242));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(280, 45));

        registerButton.setBackground(new Color(34, 153, 84));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(280, 45));

        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                if (userDatabase.containsKey(username)) {
                    if (userDatabase.get(username).equals(password)) {
                        JOptionPane.showMessageDialog(frame, "Welcome " + username + "!");
                        frame.dispose();
                        ChatUI chatUI = new ChatUI(username);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "User does not exist. Please register.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                if (userDatabase.containsKey(username)) {
                    JOptionPane.showMessageDialog(frame, "Username already exists. Choose another.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    registerUser(username, password);
                    JOptionPane.showMessageDialog(frame, "Account created! You can now log in.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(usernamePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passwordPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerButton);
        panel.add(Box.createVerticalStrut(20));

        frame.add(panel);
        frame.setVisible(true);
    }

    private void loadUsers() {
        File file = new File("users.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    userDatabase.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerUser(String username, String password) {
        File file = new File("users.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(username + "," + password);
            bw.newLine();
            bw.flush();
            userDatabase.put(username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
