package com.example.netstorage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class AuthHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

        public AuthHandler(Server server, Socket socket) {
//    public AuthHandler(Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            socket.setSoTimeout(120000);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            server.executorService.execute(() -> {
                try {
                    authentication();
                    sendMetadata();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Ожидание команд клиента");
//            closeConnection();
        }

    }

    private void sendMetadata() throws IOException {

        while (true) {
            String str2 = null;
            try {
                str2 = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str2.startsWith("/file")) {
                String[] parts2 = str2.split("\\s");
                String filename = parts2[1];
                System.out.println("Filename: " + filename);
                sendMsg("Загрузка " + filename);
                break;
            } else {
                sendMsg("*Неверный тип файла");
            }
        }
    }

    private void authentication() throws IOException {
        while (true) {
            String str = null;
            try {
                str = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                String login = parts[1];
                String password = parts[2];
                System.out.println("Login: " + login);
                System.out.println("Password: " + password);
                sendMsg("/authok " + login);
                break;
            } else {
                sendMsg("*Неверные логин/пароль");
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}