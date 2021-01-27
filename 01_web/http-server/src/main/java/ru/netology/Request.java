package ru.netology;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Request {
    private String requestMethod;
    private Map<String, String> requestHeaders;
    private Map<String, List<String>> queries;
    private String requestPath;
    private String protocolType;
    private String requestBody;
    private String mimeType;
    private Path filePath;
    private long fileSize;

    // Если это POST запрос:
    private Map<String, List<String>> bodyParams;

    public List<String> getQueryParam(String name) {
        return queries.get(name);
    }

    public Map<String, List<String>> getQueryParams() {
        return queries;
    }

    public void setBodyParams(Map<String, List<String>> bodyParams) {
        this.bodyParams = bodyParams;
    }

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

    public void setQueries(Map<String, List<String>> queries) {
        this.queries = queries;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public Map<String, List<String>> getBodyParams() {
        return bodyParams;
    }

    public List<String> getBodyParam(String param) {
        return bodyParams.get(param);
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

    public Map<String, List<String>> getQueries() {
        return queries;
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
