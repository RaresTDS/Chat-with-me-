package server;

import java.util.*;

public class MessageHandler {
    private static List<String> messageHistory = new ArrayList<>();

    public static void addMessage(String message) {
        synchronized (messageHistory) {
            messageHistory.add(message);
        }
    }

    public static List<String> getMessageHistory() {
        synchronized (messageHistory) {
            return new ArrayList<>(messageHistory);
        }
    }
}
