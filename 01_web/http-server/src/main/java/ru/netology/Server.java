package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private final int port;
    private static final int CONNECTIONS_LIMIT = 64;
    private final ExecutorService service = Executors.newFixedThreadPool(CONNECTIONS_LIMIT);
    private final Semaphore semaphore = new Semaphore(CONNECTIONS_LIMIT);
    private List<String> allPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private List<String> validPaths = new CopyOnWriteArrayList<>(allPaths);
    private final ConcurrentMap<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
        installDefaultHandlers();
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

    public void addHandler(String requestMethod, String requestPath, Handler handler) {
        if (handlers.get(requestMethod) != null) {
            handlers.get(requestMethod).put(requestPath, handler);
        } else {
            ConcurrentMap<String, Handler> map = new ConcurrentHashMap<>();
            map.put(requestPath, handler);
            handlers.put(requestMethod, map);
        }
        validPaths.add(requestPath);
    }

    private void sendResponse(Request request, BufferedOutputStream out) {
        handlers.get(request.getRequestMethod())
                .get(request.getRequestPath())
                .handle(request, out);
    }

    private String[] parsingRequestLine(String requestLine) {
        return requestLine.split(" ");
    }

    private boolean processingRequest(String request) {
        // Если у POST запроса отсутствует тело
        if (request.matches("POST.+")) {
            return false;
        }
        if (request.matches("POST.+\\r\\n\\r\\n.+")) {
            return processingPOSTRequest(request);
        } else {
            // Если GET запрос без тела (допустим, что сервер не обрабатывает GET запросы с телом)
            return checkRequestLineParts(parsingRequestLine(request));
        }
    }

    private boolean checkRequestLineParts(String[] parts) {
        return parts.length == 3;
    }

    private boolean processingPOSTRequest(String request) {
        return checkRequestLineParts(parsingRequestLine(getRequestLine(request)));
    }

    private String getRequestLine(String request) {
        Pattern pattern = Pattern.compile("(.+)(\\r\\n\\r\\n)(.+)");
        Matcher matcher = pattern.matcher(request);
        String requestLine = null;
        while (matcher.find()) {
            requestLine = matcher.group(1);
        }
        return requestLine;
    }

    private String getRequestBody(String request) {
        // Получение тела POST запроса
        Pattern pattern = Pattern.compile("(.+)(\\r\\n\\r\\n)(.+)");
        Matcher matcher = pattern.matcher(request);
        String requestBody = null;
        while (matcher.find()) {
            requestBody = matcher.group(3);
        }

        return requestBody;
    }

    private void handleClient(Socket clientSocket) {
        service.execute(() -> {
            try (final var in = new BufferedInputStream((clientSocket.getInputStream()));
                 final var out = new BufferedOutputStream(clientSocket.getOutputStream())) {
                while (true) {
                    int x;
                    List<Byte> bytes = new ArrayList<>();
                    while ((x = in.read()) != -1) {
                        bytes.add((byte) x);
                        if (in.available() == 1) {
                            break;
                        }
                    }
                    byte[] bytesArray = new byte[bytes.size()];
                    int counter = 0;
                    for (Byte readByte : bytes) {
                        bytesArray[counter++] = readByte;
                    }
                    String request = new String(bytesArray);

                    if (!processingRequest(request)) {
                        out.write("Bad request :( ".getBytes());
                        break;
                    }

                    final var parts = parsingRequestLine(request);
                    final var path = parts[1];

                    RequestBuilder builder = new RequestBuilder()
                            .setRequestMethod(parts[0])
                            .setRequestPath(parts[1])
                            .setProtocolType(parts[2]);

                    if (!validPaths.contains(parts[1])) {
                        builder.setRequestPath("InvalidPaths");
                        sendResponse(builder.build(), out);
                        break;
                    }

                    if (getRequestBody(request) != null) {
                        builder.setRequestBody(getRequestBody(request));
                    }

                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);
                    final var length = Files.size(filePath);

                    builder.setFilePath(filePath)
                            .setMimeType(mimeType)
                            .setFileSize(length);
                    sendResponse(builder.build(), out);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        });
    }

//    Установка handler`ов по-умолчанию:
    private void installDefaultHandlers() {
        // Для GET-запроса
        ConcurrentMap<String, Handler> defaultHandlers = new ConcurrentHashMap<>();

        for (int i = 0; i < validPaths.size(); i++) {
            defaultHandlers.put(validPaths.get(i), ((request, out) -> {
                try {
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + request.getMimeType() + "\r\n" +
                                    "Content-Length: " + request.getFileSize() + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(request.getFilePath(), out);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        defaultHandlers.put("/classic.html", ((request, out) -> {
            try {
                final var template = Files.readString(request.getFilePath());
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + request.getMimeType() + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        defaultHandlers.put("InvalidPaths", ((request, out) -> {
            try {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        handlers.put("GET", defaultHandlers);
    }

    public ConcurrentMap<String, Map<String, Handler>> getHandlers() {
        return handlers;
    }

}
