package ru.netology;

import java.nio.file.Path;

public class Request {
    private String requestMethod;
    private String requestPath;
    private String protocolType;
    private String requestHeader;
    private String requestBody;
    private String mimeType;
    private Path filePath;
    private long fileSize;

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
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

    public String getRequestHeader() {
        return requestHeader;
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
        return "Request{" +
                "requestMethod='" + requestMethod + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", protocolType='" + protocolType + '\'' +
                ", requestHeader='" + requestHeader + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", filePath=" + filePath +
                ", fileSize=" + fileSize +
                '}';
    }
}
