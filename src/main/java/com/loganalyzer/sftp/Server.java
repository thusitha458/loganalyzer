package com.loganalyzer.sftp;

public class Server {
    private String name;
    private String username = null;
    private String password = null;
    private String host = null;
    private int port = 22;
    private String fileName = null;

    public Server(String name, String username, String password, String host, int port, String fileName) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
