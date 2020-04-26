package com.loganalyzer.sftp;

import com.jcraft.jsch.*;

public class SftpConnector {
//    private String username = "test";
//    private String password = "test";
//    private String sftpHost = "localhost";
//    private int sftpPort = 22;

    private ChannelSftp setUpSftpSession(Server server) throws JSchException {
        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(server.getUsername(), server.getHost(), server.getPort());
        jschSession.setPassword(server.getPassword());
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        jschSession.connect();

        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    public void uploadFile(String localFilePath, String remoteFilePath, Server server) throws JSchException, SftpException {
        ChannelSftp channelSftp = setUpSftpSession(server);
        channelSftp.connect();
        channelSftp.put(localFilePath, remoteFilePath);
        channelSftp.exit();
    }

    public void downloadFile(String localFilePath, String remoteFilePath, Server server) throws JSchException, SftpException {
        ChannelSftp channelSftp = setUpSftpSession(server);
        channelSftp.connect();

        channelSftp.get(remoteFilePath, localFilePath);

        channelSftp.exit();
    }
}
