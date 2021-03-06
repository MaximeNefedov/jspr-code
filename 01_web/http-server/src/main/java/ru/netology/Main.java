package ru.netology;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
  public static void main(String[] args) {
    Server server = new Server(9999);

    server.addHandler("GET", "/messages.html", (request, out) -> {
      try {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Queries: " + request.getQueryParams() + "\r\n" +
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
    });

    server.addHandler("POST", "/messages.html", (request, out) -> {
      try {
        final var template = Files.readString(request.getFilePath());
        final var content = template.replace(
                "{message}",
                request.getRequestBody()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + request.getMimeType() + "\r\n" +
                        "Content-Length: " + request.getFileSize() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        "Сообщение, переданное в POST-запросе: " + request.getRequestBody() +
                        "\r\n"
        ).getBytes());
        Files.copy(request.getFilePath(), out);
        out.write(content);
        out.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    server.start();
  }
}



