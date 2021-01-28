package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private static final List<String> allowedMethods = List.of("GET", "POST");
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
//        String correctPath = requestPath;
//        if (requestPathContainsQuery(requestPath)) {
//            correctPath = getPathWithoutQuery(requestPath);
//        }
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

    private boolean requestPathContainsQuery(String path) {
        return path.matches(".+\\?.+");
    }

    private String getPathWithoutQuery(String path) {
        Pattern pattern = Pattern.compile(".+\\?");
        Matcher matcher = pattern.matcher(path);
        String pathWithoutQuery = null;
        while (matcher.find()) {
            pathWithoutQuery = matcher.group().replaceAll("\\?", "");
        }
        return pathWithoutQuery;
    }

    private Map<String, List<String>> getQuery(String path) throws URISyntaxException {
        List<NameValuePair> parse = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);
        Map<String, List<String>> map = new HashMap<>();
        for (NameValuePair nameValuePair : parse) {
            // URLDecoder используется для того, чтобы в случае чего, раскодировать
            // закодированные символы, например, кириллицу
            if (map.get(nameValuePair.getName()) != null) {
                map.get(URLDecoder.decode(nameValuePair.getName(), StandardCharsets.UTF_8))
                        .add(URLDecoder.decode(nameValuePair.getValue(), StandardCharsets.UTF_8));
            } else {
                List<String> list = new ArrayList<>();
                list.add(URLDecoder.decode(nameValuePair.getValue(), StandardCharsets.UTF_8));
                map.put(nameValuePair.getName(), list);
            }
        }
        return map;
    }

    private Map<String, List<String>> processingBodyParams(String requestBody) {
        String[] splited = requestBody.split("&");
        Map<String, List<String>> map = new HashMap<>();
        for (String s : splited) {
            String[] split = s.split("=");
            if (map.get(split[0]) != null) {
                map.get(URLDecoder.decode(split[0], StandardCharsets.UTF_8)).add(URLDecoder.decode(split[1], StandardCharsets.UTF_8));
            } else {
                List<String> list = new ArrayList<>();
                list.add(URLDecoder.decode(split[1], StandardCharsets.UTF_8));
                map.put(URLDecoder.decode(split[0], StandardCharsets.UTF_8), list);
            }
        }
        return map;
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private boolean checkRequestLineParts(String[] parts) {
        return parts.length == 3;
    }

    private boolean checkProtocolType(String protocolType) {
        return protocolType.equals("HTTP/1.1") || protocolType.equals("HTTP/2.0");
    }

    private Map<String, String> getRequestHeaders(byte[] headersBytes) {
        String[] headersBytesString = new String(headersBytes).split("\r\n");
        Map<String, String> map = new HashMap<>();
        for (String s : headersBytesString) {
            String[] split = s.split(":");
            map.put(split[0], split[1]);
        }
        return map;
    }


    private void handleClient(Socket clientSocket) {
        service.execute(() -> {
            try (final var in = new BufferedInputStream((clientSocket.getInputStream()));
                 final var out = new BufferedOutputStream(clientSocket.getOutputStream())) {
                while (true) {
                    // лимит на request line и заголовки
                    final int limit = 4096;
                    in.mark(limit);
                    byte[] buffer = new byte[limit];
                    int read = in.read(buffer);
                    final var requestLineDelimiter = new byte[]{'\r', '\n'};
                    // возвращаем индекс последнего байта до \r\n
                    final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

                    if (requestLineEnd == -1) {
                        badRequest(out);
                        break;
                    }

                    // читаем request line
                    final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
                    if (!checkRequestLineParts(requestLine)) {
                        badRequest(out);
                        break;
                    }

                    System.out.println(Arrays.toString(requestLine));


                    final var method = requestLine[0];
                    if (!allowedMethods.contains(method)) {
                        badRequest(out);
                        break;
                    }

                    RequestBuilder builder = new RequestBuilder();
                    builder.setRequestMethod(method);

                    System.out.println(method);

                    var path = requestLine[1];

                    if (!path.startsWith("/")) {
                        badRequest(out);
                        break;
                    }

                    // Если путь не поддерживается сервером, то пометить его
                    // как "невалидный" и из мапы выбрать соответствующий handler
                    if (!validPaths.contains(path)) {
                        builder.setRequestPath("InvalidPaths");
                        sendResponse(builder.build(), out);
                        break;
                    }

//                    builder.setRequestPath(path);
                    System.out.println(path);

                    // Если путь содержит query параметры,
                    // то сохранить параметры и вернуть путь без них
                    if (!requestPathContainsQuery(path)) {
                        builder.setRequestPath(path);
                    } else {
                        builder.setQuery(getQuery(path));
                        path = getPathWithoutQuery(path);
                        builder.setRequestPath(path);
                    }

                    final var protocolType = requestLine[2];
                    if (!checkProtocolType(protocolType)) {
                        badRequest(out);
                    }

                    builder.setProtocolType(protocolType);
                    System.out.println(protocolType);

                    final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                    final var headersStart = requestLineEnd + requestLineDelimiter.length;
                    final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                    if (headersEnd == -1) {
                        badRequest(out);
                        continue;
                    }

                    // отматываем на начало буфера
                    in.reset();
                    // пропускаем requestLine
                    in.skip(headersStart);

                    final var headersBytes = in.readNBytes(headersEnd - headersStart);
                    Map<String, String> requestHeaders = getRequestHeaders(headersBytes);
                    builder.setRequestHeaders(requestHeaders);

                    System.out.println(requestHeaders);

                    // для GET тела нет
                    if (!method.equals("GET")) {
                        in.skip(headersDelimiter.length);
                        final var content = requestHeaders.get("Content-Length");
                        final int contentLength = Integer.parseInt(content.replaceAll(" ", "").trim());
                        final var bodyBytes = in.readNBytes(contentLength);
                        final var requestBody = new String(bodyBytes);

                        builder.setRequestBody(requestBody);
//                        builder.setBodyParams(processingBodyParams(requestBody));

                        System.out.println(requestBody);
                    }

                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);
                    final var length = Files.size(filePath);

                    builder.setFilePath(filePath)
                            .setMimeType(mimeType)
                            .setFileSize(length);
                    sendResponse(builder.build(), out);
                    break;


//                    int x;
//                    List<Byte> bytes = new ArrayList<>();
//                    while ((x = in.read()) != -1) {
//                        bytes.add((byte) x);
//                        if (in.available() == 0) {
//                            break;
//                        }
//                    }
//                    byte[] bytesArray = new byte[bytes.size()];
//                    int counter = 0;
//                    for (Byte readByte : bytes) {
//                        bytesArray[counter++] = readByte;
//                    }
//                    String request = new String(bytesArray);
////                    System.out.println(request);
//
//                    if (!processingRequest(request)) {
//                        out.write("Bad request :( ".getBytes());
//                        break;
//                    }
//
//                    final var parts = parsingRequestLine(getRequestLine(request));
//
//                    RequestBuilder builder = new RequestBuilder()
//                            .setRequestMethod(parts[0]);
//
//                    var path = parts[1];
////                    System.out.println(path);
//
//                    if (!requestPathContainsQuery(path)) {
//                        builder.setRequestPath(path);
//                    } else {
//                        builder.setQuery(getQuery(path));
//                        path = getPathWithoutQuery(path);
//                        builder.setRequestPath(path);
//                    }
//
//                    builder.setProtocolType(parts[2]);
//
//                    if (!validPaths.contains(path)) {
//                        builder.setRequestPath("InvalidPaths");
//                        sendResponse(builder.build(), out);
//                        break;
//                    }
//
//                    builder.setRequestHeaders(getRequestHeaders(request));
//
//                    if (getRequestBody(request) != null) {
//                        String requestBody = getRequestBody(request);
//                        builder.setBodyParams(processingBodyParams(requestBody));
//                        builder.setRequestBody(requestBody);
//                    }
//
//                    final var filePath = Path.of(".", "public", path);
//                    final var mimeType = Files.probeContentType(filePath);
//                    final var length = Files.size(filePath);
//
//                    builder.setFilePath(filePath)
//                            .setMimeType(mimeType)
//                            .setFileSize(length);
//                    sendResponse(builder.build(), out);
//                    break;
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        });
    }

//    private String[] parsingRequestLine(String requestLine) {
//        return requestLine.split(" ");
//    }

//    private boolean processingRequest(String request) {
//        // Если у POST запроса отсутствует тело
//        if (request.matches("POST.+")) {
//            return false;
//        }
//        if (request.matches("POST.+\\r\\n\\r\\n.+")) {
//            return processingPOSTRequest(request);
//        } else {
//            // Проверка GET запроса
//            return checkRequestLineParts(parsingRequestLine(getRequestLine((request))));
//        }
//    }

//    private Map<String, String> getRequestHeaders(String request) {
//        Pattern pattern = Pattern.compile("(.+):(.+)");
//        Matcher matcher = pattern.matcher(request);
//        Map<String, String> headers = new HashMap<>();
//        while (matcher.find()) {
//            headers.put(matcher.group(1), matcher.group(2));
//        }
//        return headers;
//    }


//    private boolean processingPOSTRequest(String request) {
//        return checkRequestLineParts(parsingRequestLine(getRequestLine(request)));
//    }

//    private String getRequestLine(String request) {
//        Pattern pattern = Pattern.compile("(GET|POST).+");
//        Matcher matcher = pattern.matcher(request);
//        String requestLine = null;
//        while (matcher.find()) {
//            requestLine = matcher.group();
//        }
//        return requestLine;
//    }

//    private String getRequestBody(String request) {
//        // Получение тела POST запроса
//        Pattern pattern = Pattern.compile("(.+)(\\r\\n\\r\\n)(.+)");
//        Matcher matcher = pattern.matcher(request);
//        String requestBody = null;
//        while (matcher.find()) {
//            requestBody = matcher.group(3);
//        }
//        return requestBody;
//    }

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
}
