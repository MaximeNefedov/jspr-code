package ru.netology;

import java.nio.file.Path;
import java.util.Map;

public class Request {
    private String requestMethod;
    private Map<String, String> requestHeaders;
    private String requestPath;
    private String protocolType;
    private String requestBody;
    private String mimeType;
    private Path filePath;
    private long fileSize;

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Path getFilePath() {
        return filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        String headers = sb.toString();
        String requestBodyForView = "body is empty";
        if (requestBody != null && !requestBody.isEmpty()) {
            requestBodyForView = requestBody;
        }
        return String.format("Request:\n%s %s %s\n%s\r\nbody: %s\r\n\r\nMimeType: %s\nFile path: %s\nFile size: %d",
                requestMethod, requestPath, protocolType, headers, requestBodyForView, mimeType, filePath, fileSize);
    }
}
