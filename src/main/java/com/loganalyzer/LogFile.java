package com.loganalyzer;

import com.loganalyzer.file.FileUtils;
import com.loganalyzer.sftp.Server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.loganalyzer.file.FileUtils.dateFormat;

public class LogFile {
    private Server server = null;
    private List<String> logFileLines = null;
    private DateInfo logFileDateInfo = null;

    public void reset() {
        server = null;
        logFileLines = null;
        logFileDateInfo = null;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public DateInfo getLogFileDateInfo() {
        return logFileDateInfo;
    }

    public void setLogFileDateInfo(DateInfo logFileDateInfo) {
        this.logFileDateInfo = logFileDateInfo;
    }

    public List<String> getLogFileLines() {
        return logFileLines;
    }

    public void setLogFileLines(List<String> logFileLines) {
        this.logFileLines = logFileLines;
    }

    public List<String> getFilteredLines(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<String> filteredLines = new ArrayList<>();
        if (this.logFileLines != null) {
            filteredLines = logFileLines.stream().filter(line -> {
                LocalDateTime localDateTime = FileUtils.getLocalDateTimeFromLine(line);
                if (localDateTime != null) {
                    boolean afterStart = localDateTime.compareTo(startDateTime) >= 0;
                    boolean beforeFinish = localDateTime.compareTo(endDateTime) <= 0;
                    return afterStart && beforeFinish;
                }
                return false;
            }).collect(Collectors.toList());
        }
        return filteredLines;
    }
}
