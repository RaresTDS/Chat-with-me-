package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final int IMAGE_PORT = 12346;
    private static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Server is running on port " + PORT);
        new Thread(Server::startImageServer).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startImageServer() {
        try (ServerSocket imageServer = new ServerSocket(IMAGE_PORT)) {
            while (true) {
                new ImageHandler(imageServer.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
                writer.flush();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                clientName = in.readLine();
                if (clientName != null && !clientName.trim().isEmpty()) {
                    System.out.println("[SERVER]: " + clientName + " has joined the chat.");
                    broadcastMessage("[SERVER]: " + clientName + " has joined the chat.");
                }

                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException e) {
                System.out.println("[SERVER]: " + clientName + " has disconnected.");
            } finally {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                broadcastMessage("[SERVER]: " + clientName + " has left the chat.");
            }
        }
    }

    private static class ImageHandler extends Thread {
        private final Socket socket;

        public ImageHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream is = socket.getInputStream();
                File imageFile = new File("received_" + System.currentTimeMillis() + ".png");
                FileOutputStream fos = new FileOutputStream(imageFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                is.close();
                System.out.println("[SERVER]: Received image -> " + imageFile.getName());
                broadcastMessage("[IMAGE]: " + imageFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
