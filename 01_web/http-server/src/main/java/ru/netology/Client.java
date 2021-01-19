package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final int PORT = 9999;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try (var socket = new Socket("localhost", PORT);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("GET /classic.html /HTTP/1.1");
            StringBuilder sb = new StringBuilder("Server response:\n");
            String x;
            while ((x = in.readLine()) != null) {
                sb.append(x).append("\n");
            }
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
