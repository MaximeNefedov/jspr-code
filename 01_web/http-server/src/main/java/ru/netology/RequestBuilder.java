package ru.netology;

import java.nio.file.Path;
import java.util.Map;

public class RequestBuilder {
    private final Request request = new Request();

    public RequestBuilder setRequestMethod(String requestMethod) {
        request.setRequestMethod(requestMethod);
        return this;
    }

    public RequestBuilder setRequestPath(String requestPath) {
        request.setRequestPath(requestPath);
        return this;
    }

    public RequestBuilder setProtocolType(String protocolType) {
        request.setProtocolType(protocolType);
        return this;
    }

    public RequestBuilder setRequestHeaders(Map<String, String> requestHeaders) {
        request.setRequestHeaders(requestHeaders);
        return this;
    }

    public RequestBuilder setRequestBody(String requestBody) {
        request.setRequestBody(requestBody);
        return this;
    }

    public RequestBuilder setMimeType(String mimeType) {
        request.setMimeType(mimeType);
        return this;
    }

    public RequestBuilder setFilePath(Path filePath) {
        request.setFilePath(filePath);
        return this;
    }

    public RequestBuilder setFileSize(long fileSize) {
        request.setFileSize(fileSize);
        return this;
    }

    public Request build() {
        return request;
    }
}
