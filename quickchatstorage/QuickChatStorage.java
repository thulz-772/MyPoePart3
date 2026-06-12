package quickchatstorage;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QuickChatStorage {
    static ArrayList<Message> sentMessages = new ArrayList<>();
    static ArrayList<Message> disregardedMessages = new ArrayList<>();
    static ArrayList<Message> storedMessages = new ArrayList<>();
    static ArrayList<String> messageHashArray = new ArrayList<>();
    static ArrayList<String> messageIDArray = new ArrayList<>();
    static final String FILE_NAME = "messages.json";

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        // To Capture first name and last name for welcome message
        System.out.print("Enter your first name: ");
        String firstName = input.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = input.nextLine();

        // 1. Username validation
        String userName = "";
        boolean validUsername = false;
        while (!validUsername) {
            System.out.println("Enter Your Username :");
            userName = input.nextLine();
            validUsername = userName.contains("_") && userName.length() <= 5;
            if (validUsername) {
                System.out.println("Username Successfully captured");
            } else {
                System.out.println("Your Username must contain an underscore and be no longer than 5 characters in length. Try again.");
            }
        }

        // 2. Password validation
        String password = "";
        boolean validPassword = false;
        while (!validPassword) {
            System.out.print("Enter Your Password: ");
            password = input.nextLine();
            boolean hasUpper = false, hasDigit = false, hasSpecial = false;
            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                if (Character.isUpperCase(c)) hasUpper = true;
                else if (Character.isDigit(c)) hasDigit = true;
                else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
            }
            validPassword = (password.length() >= 8) && hasUpper && hasDigit && hasSpecial;
            if (validPassword) {
                System.out.println("Password successfully captured.");
            } else {
                System.out.println("Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
            }
        }

        // 3. Cell phone validation
        String cellNumber = "";
        boolean validCell = false;
        while (!validCell) {
            System.out.print("Enter your cell phone number with international code: ");
            cellNumber = input.nextLine();
            validCell = true;
            if (cellNumber.length() == 0 || cellNumber.charAt(0)!= '+') {
                validCell = false;
            } else {
                for (int i = 1; i < cellNumber.length(); i++) {
                    if (!Character.isDigit(cellNumber.charAt(i))) {
                        validCell = false;
                        break;
                    }
                }
                if (cellNumber.length() < 3 || cellNumber.length() > 14) {
                    validCell = false;
                }
            }
            if (validCell) {
                System.out.println("Cell phone number successfully added.");
            } else {
                System.out.println("Cell phone number incorrectly formatted or does not contain international code.");
            }
        }

        System.out.println("\nRegistration complete. Please log in.\n");

        // 4. Login Portal
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter username to login: ");
            String loginUser = input.nextLine();
            System.out.print("Enter password to login: ");
            String loginPass = input.nextLine();
            if (loginUser.equals(userName) && loginPass.equals(password)) {
                System.out.println("Welcome " + firstName + ", " + lastName + " it is great to see you again.");
                loggedIn = true;
            } else {
                System.out.println("Username or password incorrect, please try again.");
            }
        }

        // Load saved messages from JSON
        loadMessages();

        // QuickChat menu
        int choice;
        System.out.println("Welcome to QuickChat.");
        do {
            System.out.println("\n1. Send Messages");
            System.out.println("2. Show recently sent messages");
            System.out.println("3. Stored Messages");
            System.out.println("4. Quit");
            System.out.print("Choose an option: ");
            choice = input.nextInt();
            input.nextLine();
            switch(choice) {
                case 1 -> sendMessages(input);
                case 2 -> displayMessages();
                case 3 -> storedMessagesMenu(input);
                case 4 -> {
                    saveMessages();
                    System.out.println("Exiting QuickChat. Goodbye!");
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        } while (choice!= 4);
    }

    static void sendMessages(Scanner input) {
        System.out.print("How many messages do you want to send? ");
        int num = input.nextInt();
        input.nextLine();
        for (int i = 0; i < num; i++) {
            String recipient;
            // Validate recipient
            while (true) {
                System.out.print("Enter recipient with + and max 10 digits: ");
                recipient = input.nextLine();
                Message temp = new Message(recipient, "");
                String check = temp.checkRecipientCell();
                System.out.println(check);
                if (check.equals("Cell phone number successfully captured.")) {
                    break;
                }
                System.out.println("Try again.\n");
            }

            // Validate message length with retry
            String msg;
            Message m = null;
            while (true) {
                System.out.print("Enter message: ");
                msg = input.nextLine();
                m = new Message(recipient, msg);
                System.out.println(m.checkMessageLength());
                if (m.checkMessageLength().equals("Message ready to send.")) {
                    break;
                } else {
                    System.out.println("Try again.\n");
                }
            }

            m.createMessageHash();
            System.out.println("1. Send Message");
            System.out.println("2. Disregard Message");
            System.out.println("3. Store Message");
            int opt = input.nextInt();
            input.nextLine();
            String result = m.sentMessage(opt);
            System.out.println(result);
            System.out.println("\n--- Message Details ---");
            System.out.println("Message ID: " + m.messageID);
            System.out.println("Message Hash: " + m.messageHash);
            System.out.println("Recipient: " + m.recipient);
            System.out.println("Message: " + m.message);
        }
        System.out.println("Total messages sent: " + sentMessages.size());
    }

    static void displayMessages() {
        if (sentMessages.isEmpty()) {
            System.out.println("No messages sent yet.");
            return;
        }
        System.out.println("\n--- Recently Sent Messages ---");
        for (int i = 0; i < sentMessages.size(); i++) {
            Message m = sentMessages.get(i);
            System.out.println("\nMessage " + (i + 1) + ":");
            System.out.println(m.printMessages());
        }
    }

    static void storedMessagesMenu(Scanner input) {
        int choice;
        do {
            System.out.println("\n--- Stored Messages Menu ---");
            System.out.println("1. Display sender + recipient of all stored messages");
            System.out.println("2. Show longest stored message");
            System.out.println("3. Search by message ID");
            System.out.println("4. Search by recipient");
            System.out.println("5. Delete by message hash");
            System.out.println("6. Display full report of all stored messages");
            System.out.println("7. Back to main menu");
            System.out.print("Choose an option: ");
            choice = input.nextInt();
            input.nextLine();
            switch(choice) {
                case 1: displaySenderRecipient(); break;
                case 2: showLongestMessage(); break;
                case 3: searchByID(input); break;
                case 4: searchByRecipient(input); break;
                case 5: deleteByHash(input); break;
                case 6: displayFullReport(); break;
                case 7: return;
                default: System.out.println("Invalid option.");
            }
        } while (true);
    }

    static void displaySenderRecipient() {
        if (storedMessages.isEmpty()) {
            System.out.println("No stored messages.");
            return;
        }
        System.out.println("\n--- Stored Messages (Sender -> Recipient) ---");
        for (Message m : storedMessages) {
            System.out.println("Sender: YOU -> Recipient: " + m.recipient);
        }
    }

    static void showLongestMessage() {
        if (storedMessages.isEmpty()) {
            System.out.println("No stored messages.");
            return;
        }
        Message longest = storedMessages.get(0);
        for (Message m : storedMessages) {
            if (m.message.length() > longest.message.length()) {
                longest = m;
            }
        }
        System.out.println("\n--- Longest Stored Message ---");
        System.out.println(longest.printMessages());
    }

    static void searchByID(Scanner input) {
        System.out.print("Enter message ID to search: ");
        String id = input.nextLine();
        for (Message m : storedMessages) {
            if (m.messageID.equals(id)) {
                System.out.println("\nRecipient: " + m.recipient);
                System.out.println("Message: " + m.message);
                return;
            }
        }
        System.out.println("Message ID not found.");
    }

    static void searchByRecipient(Scanner input) {
        System.out.print("Enter recipient number to search: ");
        String recipient = input.nextLine();
        boolean found = false;
        for (Message m : storedMessages) {
            if (m.recipient.equals(recipient)) {
                System.out.println("\n" + m.printMessages());
                found = true;
            }
        }
        if (!found) System.out.println("No messages found for that recipient.");
    }

    static void deleteByHash(Scanner input) {
        System.out.print("Enter message hash to delete: ");
        String hash = input.nextLine();
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).messageHash.equals(hash)) {
                System.out.println("Message: \"" + storedMessages.get(i).message + "\" successfully deleted.");
                storedMessages.remove(i);
                messageHashArray.remove(hash);
                return;
            }
        }
        System.out.println("Message hash not found.");
    }

    static void displayFullReport() {
        if (storedMessages.isEmpty()) {
            System.out.println("No stored messages.");
            return;
        }
        System.out.println("\n--- Full Report of Stored Messages ---");
        for (Message m : storedMessages) {
            System.out.println("\nMessage Hash: " + m.messageHash);
            System.out.println("Recipient: " + m.recipient);
            System.out.println("Message: " + m.message);
        }
    }

    static void saveMessages() {
        try {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < storedMessages.size(); i++) {
                Message m = storedMessages.get(i);
                json.append("{");
                json.append("\"messageID\":\"").append(m.messageID).append("\",");
                json.append("\"recipient\":\"").append(m.recipient).append("\",");
                json.append("\"message\":\"").append(m.message.replace("\"", "\\\"")).append("\",");
                json.append("\"messageHash\":\"").append(m.messageHash).append("\"");
                json.append("}");
                if (i < storedMessages.size() - 1) json.append(",");
            }
            json.append("]");
            Files.write(Paths.get(FILE_NAME), json.toString().getBytes());
            System.out.println("Messages saved to " + FILE_NAME);
        } catch (Exception e) {
            System.out.println("Error saving messages: " + e.getMessage());
        }
    }

    static void loadMessages() {
        try {
            if (!Files.exists(Paths.get(FILE_NAME))) return;
            String content = new String(Files.readAllBytes(Paths.get(FILE_NAME)));
            content = content.trim();
            if (content.equals("[]")) return;
            content = content.substring(1, content.length() - 1);
            String[] objects = content.split("\\},\\{");
            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "");
                String[] parts = obj.split("\",\"");
                String id = parts[0].split(":\"")[1].replace("\"", "");
                String recipient = parts[1].split(":\"")[1].replace("\"", "");
                String message = parts[2].split(":\"")[1].replace("\"", "").replace("\\\"", "\"");
                String hash = parts[3].split(":\"")[1].replace("\"", "");
                Message m = new Message(recipient, message);
                m.messageID = id;
                m.messageHash = hash;
                storedMessages.add(m);
                messageHashArray.add(hash);
                messageIDArray.add(id);
            }
            System.out.println("Loaded " + storedMessages.size() + " messages from " + FILE_NAME);
        } catch (Exception e) {
            System.out.println("No saved messages found.");
        }
    }
}

class Message {
    String messageID;
    String recipient;
    String message;
    String messageHash;

    public Message(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
        this.messageID = generateMessageID();
    }

    String generateMessageID() {
        Random rand = new Random();
        long id = 10000000L + rand.nextLong(90000000L);
        return String.valueOf(id);
    }

    String checkMessageLength() {
        if (message.length() <= 250) {
            return "Message ready to send.";
        } else {
            return "Please enter a message of less than 250 characters.";
        }
    }

    String checkRecipientCell() {
        if (recipient.length() <= 12 && recipient.startsWith("+")) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    void createMessageHash() {
        String[] words = message.trim().split("\\s+");
        String firstWord = words.length > 0? words[0].toUpperCase() : "";
        String lastWord = words.length > 1? words[words.length - 1].toUpperCase() : firstWord;
        String idPart = messageID.substring(0, 2);
        int msgNum = QuickChatStorage.storedMessages.size() + QuickChatStorage.sentMessages.size();
        this.messageHash = idPart + ":" + msgNum + ":" + firstWord + lastWord;
    }

    String sentMessage(int option) {
        switch (option) {
            case 1 -> {
                QuickChatStorage.sentMessages.add(this);
                QuickChatStorage.messageHashArray.add(this.messageHash);
                QuickChatStorage.messageIDArray.add(this.messageID);
                return "Message successfully sent.";
            }
            case 2 -> {
                QuickChatStorage.disregardedMessages.add(this);
                return "Message disregarded.";
            }
            case 3 -> {
                QuickChatStorage.storedMessages.add(this);
                QuickChatStorage.messageHashArray.add(this.messageHash);
                QuickChatStorage.messageIDArray.add(this.messageID);
                return "Message successfully stored.";
            }
            default -> {
                return "Invalid option.";
            }
        }
    }

    public String printMessages() {
        return "ID: " + messageID + "\nHash: " + messageHash + "\nRecipient: " + recipient + "\nMessage: " + message;
    }
}