package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Server {
    private int port;
    private static final int CONNECTIONS_LIMIT = 64;
    private ExecutorService service = Executors.newFixedThreadPool(CONNECTIONS_LIMIT);
    private Semaphore semaphore = new Semaphore(CONNECTIONS_LIMIT);
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");


    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (var serverSocket = new ServerSocket(port)) {
            while (true) {
                semaphore.acquire();
                handleClient(serverSocket.accept());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        service.execute(() -> {
            try (final var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 final var out = new BufferedOutputStream(clientSocket.getOutputStream())) {
                while (true) {
                    final var requestLine = in.readLine();
                    final var parts = parsingRequest(requestLine);

                    if (parts.length != 3) {
                        out.write("Bad request :( ".getBytes());
                        out.flush();
                        break;
                    }

                    final var path = parts[1];

                    if (checkingNotFoundError(path, out)) {
                        break;
                    }

                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);

                    if (checkingForClassic(path, mimeType, filePath, out)) {
                        break;
                    }

                    final var length = Files.size(filePath);

                    if (sendResponse(mimeType, length, filePath, out)) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        });
    }

    private String[] parsingRequest(String requestLine) {
        return requestLine.split(" ");
    }

    private boolean checkingNotFoundError(String path, BufferedOutputStream out) {
        if (!validPaths.contains(path)) {
            try {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean checkingForClassic(String path, String mimeType, Path filePath, BufferedOutputStream out) {
        try {
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean sendResponse(String mimeType, long length, Path filePath, BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
