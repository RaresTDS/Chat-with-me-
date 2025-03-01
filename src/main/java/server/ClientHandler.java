package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

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
            try {
                while ((message = in.readLine()) != null) {
                    System.out.println(clientName + ": " + message);
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException e) {
                System.out.println("[SERVER]: " + clientName + " has disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                broadcastMessage("[SERVER]: " + clientName + " has left the chat.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clientWriters) {
                clientWriters.remove(out);
            }
            broadcastMessage("[SERVER]: " + clientName + " has left the chat.");
        }
    }

    public static void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
}
