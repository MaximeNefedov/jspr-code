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
            // classic case
            out.println("GET /classic.html /HTTP/1.1");

            // 404
//            out.println("GET /clas!sic.html /HTTP/1.1");

            // Bad requests
//            out.println("POST /classic.html /HTTP/1.1");
//            out.println("GET /classic.html /HTTP/1.1");


            // Из стандатрого списка файлов
//            out.println("GET /index.html /HTTP/1.1");

            // Созданный файл messages.txt и и добавленный для него handler
//            out.println("GET /messages.txt /HTTP/1.1");

            // POST - запрос. Просто добавит переданный данные в ответ
//            out.println("POST /messages.txt /HTTP/1.1" +
//                    "\r\n\r\n" +
//                    "Hello, Server! Please add this string into file");

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
