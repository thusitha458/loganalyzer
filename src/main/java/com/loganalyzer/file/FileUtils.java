package com.loganalyzer.file;

import com.loganalyzer.DateInfo;
import com.loganalyzer.encryption.AES;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class FileUtils {
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDate getLocalDateFromLine(String line) {
        if (line != null && line.length() >= 19) {
            return LocalDate.parse(line.substring(0, 19), dateFormat);
        }
        return null;
    }

    public static LocalDateTime getLocalDateTimeFromLine(String line) {
        if (line != null && line.length() >= 19) {
            return LocalDateTime.parse(line.substring(0, 19), dateFormat);
        }
        return null;
    }

    public static DateInfo getDateInfoOfLogFile(String filePath, String secretKey) {
        final DateInfo dateInfo = new DateInfo(null, null);
        try (
                FileReader fr = new FileReader(filePath);
                BufferedReader br = new BufferedReader(fr);
        ) {
            String line;

            while ((line = br.readLine()) != null) {
                String decrypted = AES.decrypt(line, secretKey);
                LocalDate localDate = getLocalDateFromLine(decrypted);
                if (localDate != null) {
                    if (dateInfo.getMinDate() == null) {
                        dateInfo.setMinDate(localDate);
                    }
                    dateInfo.setMaxDate(localDate);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed loading file, maybe it does not exist as of now");
        }
        return dateInfo;
    }

    public static List<String> getLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(lines::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static List<String> getLinesFromLogFile(String filePath, String secretKey) {
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(line -> {
                String decrypted = AES.decrypt(line, secretKey);
                lines.add(decrypted);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void writeToLogFile(String filePath, List<String> lines, String secretKey) {
        DateInfo dateInfo = getDateInfoOfLogFile(filePath, secretKey);
        try (FileWriter fw = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            lines.forEach(line -> {
                LocalDate localDate = getLocalDateFromLine(line);
                if (localDate != null && (dateInfo.getMaxDate() == null || localDate.compareTo(dateInfo.getMaxDate()) > 0)) {
                    out.println(AES.encrypt(line, secretKey));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DateInfo getDateInfoFromLines(List<String> lines) {
        DateInfo dateInfo = new DateInfo(null, null);
        lines.forEach(line -> {
            LocalDate localDate = getLocalDateFromLine(line);
            if (localDate != null) {
                if (dateInfo.getMinDate() == null) {
                    dateInfo.setMinDate(localDate);
                }
                dateInfo.setMaxDate(localDate);
            }
        });
        return dateInfo;
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    public static boolean exists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static Map<String, String> loadPropertiesFile() {
        Map<String, String> properties = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get("loganalyzer.properties"))) {
            stream.forEach(line -> {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    properties.put(parts[0].trim(), parts[1].trim());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }
}
