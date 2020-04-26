package com.loganalyzer;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jfoenix.controls.JFXTimePicker;
import com.loganalyzer.encryption.AES;
import com.loganalyzer.file.FileUtils;
import com.loganalyzer.mysql.MySqlConnector;
import com.loganalyzer.sftp.Server;
import com.loganalyzer.sftp.SftpConnector;
import com.loganalyzer.users.User;
import com.loganalyzer.windows.AddServerWindow;
import com.loganalyzer.windows.AddUserWindow;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class Main extends Application {
    public final static String SECRET_KEY = "ssshhhhhhhhhhh!!!!";

    private MySqlConnector mySqlConnector = null;
    private SftpConnector sftpConnector = null;
    private List<Server> servers = null;
    private final String DOWNLOAD_FILE = "downloads/temp.log";

    public Main() {
        try {
            mySqlConnector = new MySqlConnector();
            sftpConnector = new SftpConnector();

            servers = mySqlConnector.getServers();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            primaryStage.setTitle("Log Analyzer");
            primaryStage.setScene(getLoginScene(primaryStage));
            primaryStage.show();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Scene getLoginScene(Stage stage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        Button btn = new Button("Sign in");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 6);

        btn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwBox.getText();

            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                User user = Main.this.mySqlConnector.findUser(username);
                if (user != null && user.getPassword() != null && BCrypt.checkpw(password, user.getPassword())) {
                    stage.setScene(getDisplayScene(stage, user));
                } else {
                    actionTarget.setFill(Color.FIREBRICK);
                    actionTarget.setText("Invalid credentials");
                }
            } else {
                actionTarget.setFill(Color.FIREBRICK);
                actionTarget.setText("Please enter a username and a password");
            }
        });

        return new Scene(grid, 400, 400);
    }

    private Scene getDisplayScene(Stage stage, User user) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 10;");

        ComboBox<String> serverComboBox = new ComboBox<>();
        serverComboBox.setPromptText("Select server");
        servers.forEach(server -> serverComboBox.getItems().add(server.getName()));

        Button loadButton = new Button("Load");

        Button downloadButton = new Button("Download");
        Button downloadAllButton = new Button("Download All");
        final HBox downloadButtonsWrapper = new HBox(10, downloadButton, downloadAllButton);
        downloadButtonsWrapper.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(downloadButtonsWrapper, Priority.ALWAYS);

        final HBox serverBox = new HBox(20, serverComboBox, loadButton);
        if (user != null && "ADMIN".equals(user.getRole())) {
            serverBox.getChildren().add(downloadButtonsWrapper);
        }
        serverBox.setAlignment(Pos.CENTER_LEFT);
        serverBox.setStyle("-fx-padding: 10;" +
                "-fx-background-color: rgba(255, 255, 255, 0.5);");

        final DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");

        JFXTimePicker startTimePicker = new JFXTimePicker();
        startTimePicker.setDefaultColor(Color.valueOf("#808080"));
        startTimePicker.setPromptText("Start Time");

        final DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");

        JFXTimePicker endTimePicker = new JFXTimePicker();
        endTimePicker.setDefaultColor(Color.valueOf("#808080"));
        endTimePicker.setPromptText("End Time");

        final HBox filterOptionsBox = new HBox(10, startDatePicker, startTimePicker, endDatePicker, endTimePicker);

        final Button filterButton = new Button("Filter");
        final HBox filterButtonBox = new HBox(10, filterButton);
        filterButtonBox.setAlignment(Pos.CENTER_RIGHT);

        final VBox filterBox = new VBox(30, filterOptionsBox, filterButtonBox);
        filterBox.setAlignment(Pos.BASELINE_CENTER);
        filterBox.setStyle("-fx-padding: 10;" +
                "-fx-background-color: rgba(255, 255, 255, 0.5);");

        Accordion accordion = new Accordion();

        TitledPane serverPane = new TitledPane("Servers" , serverBox);
        TitledPane filterPane = new TitledPane("Filters", filterBox);
        accordion.getPanes().add(serverPane);
        accordion.getPanes().add(filterPane);
        accordion.setExpandedPane(serverPane);

        final TextArea textArea = new TextArea();
        textArea.setEditable(false);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        layout.getChildren().addAll(accordion, textArea);

        Menu fileMenu = new Menu("File");

        MenuItem addServer = new MenuItem("Add Server...");
        addServer.setOnAction(e -> {
            AddServerWindow.display(mySqlConnector);
            servers = mySqlConnector.getServers();
            serverComboBox.getItems().clear();
            servers.forEach(server -> serverComboBox.getItems().add(server.getName()));
        });

        MenuItem addUser = new MenuItem("Add User...");
        addUser.setOnAction(e -> {
            AddUserWindow.display(mySqlConnector);
        });

        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> {
            stage.setScene(getLoginScene(stage));
        });

        if (user != null && "ADMIN".equals(user.getRole())) {
            fileMenu.getItems().add(addServer);
            fileMenu.getItems().add(new SeparatorMenuItem());
            fileMenu.getItems().add(addUser);
            fileMenu.getItems().add(new SeparatorMenuItem());
        }
        fileMenu.getItems().add(logout);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);


        BorderPane wrappingLayout = new BorderPane();
        wrappingLayout.setTop(menuBar);
        wrappingLayout.setCenter(layout);

        final LogFile logFile = new LogFile();

        filterButton.setOnAction(actionEvent -> {
            if (logFile.getLogFileLines() != null) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();

                LocalTime startTime = startTimePicker.getValue();
                LocalTime endTime = endTimePicker.getValue();

                if (startDate == null || endDate == null) {
                    Alert alert = new Alert(
                            Alert.AlertType.ERROR,
                            "Start and end dates are required",
                            ButtonType.OK
                    );
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }

                LocalDateTime startDateTime = startTime != null ? LocalDateTime.of(startDate, startTime) : LocalDateTime.of(startDate, LocalTime.of(0, 0));
                LocalDateTime endDateTime = endTime != null ? LocalDateTime.of(endDate, endTime) : LocalDateTime.of(endDate, LocalTime.of(23, 59, 59));

                if (startDateTime.compareTo(endDateTime) > 0) {
                    Alert alert = new Alert(
                            Alert.AlertType.ERROR,
                            "Start date time should be before the end date time",
                            ButtonType.OK
                    );
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }

                textArea.clear();
                List<String> filteredLines = logFile.getFilteredLines(startDateTime, endDateTime);
                filteredLines.forEach(line -> textArea.appendText(line + "\n"));
            }
        });

        loadButton.setOnAction(actionEvent -> {
            String selectedServerName = serverComboBox.getValue();
            if (selectedServerName == null) {
                Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        "Please select a server",
                        ButtonType.OK
                );
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            Server selectedServer = servers.stream().filter(server -> server.getName().equals(selectedServerName)).findFirst().orElse(null);
            if (selectedServer == null) {
                Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        "Please select a server",
                        ButtonType.OK
                );
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            FileUtils.deleteFile(DOWNLOAD_FILE);
            textArea.clear();
            logFile.reset();
            logFile.setServer(selectedServer);

            String logFilePath = "logs/" + selectedServer.getName().trim() + ".log";
            if (!FileUtils.exists(logFilePath)) {
                Alert alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Logs do not exist. Please download the logs first",
                        ButtonType.OK
                );
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            logFile.setLogFileLines(FileUtils.getLinesFromLogFile(logFilePath, SECRET_KEY));
            logFile.setLogFileDateInfo(FileUtils.getDateInfoFromLines(logFile.getLogFileLines()));

            logFile.getLogFileLines().forEach(line -> {
                textArea.appendText(line + "\n");
            });

            if (logFile.getLogFileDateInfo().getMaxDate() != null && logFile.getLogFileDateInfo().getMinDate() != null) {
                startDatePicker.setDayCellFactory(d ->
                        new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                                setDisable(item.isAfter(logFile.getLogFileDateInfo().getMaxDate()) || item.isBefore(logFile.getLogFileDateInfo().getMinDate()));
                            }
                        });
                startDatePicker.setValue(logFile.getLogFileDateInfo().getMinDate());

                endDatePicker.setDayCellFactory(d ->
                        new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);
                                setDisable(item.isAfter(logFile.getLogFileDateInfo().getMaxDate()) || item.isBefore(logFile.getLogFileDateInfo().getMinDate()));
                            }
                        });
                endDatePicker.setValue(logFile.getLogFileDateInfo().getMaxDate());
            }
            accordion.setExpandedPane(filterPane);
        });

        downloadAllButton.setOnAction(actionEvent -> {
            try {
                FileUtils.deleteFile(DOWNLOAD_FILE);
                textArea.clear();
                logFile.reset();
                servers.forEach(server -> {
                    try {
                        FileUtils.deleteFile(DOWNLOAD_FILE);
                        sftpConnector.downloadFile(DOWNLOAD_FILE, server.getFileName().trim(), server);

                        String logFilePath = "logs/" + server.getName().trim() + ".log";

                        List<String> newFileLines = FileUtils.getLines(DOWNLOAD_FILE);
                        FileUtils.writeToLogFile(logFilePath, newFileLines, SECRET_KEY);
                    } catch (JSchException | SftpException e) {
                        System.out.println("Failed loading log files for server: " + server.getName());
                    }
                });
            } finally {
                FileUtils.deleteFile(DOWNLOAD_FILE);
                Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        "Success!!!",
                        ButtonType.OK
                );
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        downloadButton.setOnAction(actionEvent -> {
            try {
                String selectedServerName = serverComboBox.getValue();
                if (selectedServerName == null) {
                    Alert alert = new Alert(
                            Alert.AlertType.INFORMATION,
                            "Please select a server",
                            ButtonType.OK
                    );
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
                Server selectedServer = servers.stream().filter(server -> server.getName().equals(selectedServerName)).findFirst().orElse(null);
                if (selectedServer == null) {
                    Alert alert = new Alert(
                            Alert.AlertType.INFORMATION,
                            "Please select a server",
                            ButtonType.OK
                    );
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    return;
                }
                FileUtils.deleteFile(DOWNLOAD_FILE);
                textArea.clear();
                logFile.reset();
                logFile.setServer(selectedServer);
                sftpConnector.downloadFile(DOWNLOAD_FILE, selectedServer.getFileName().trim(), selectedServer);

                String logFilePath = "logs/" + selectedServer.getName().trim() + ".log";

                List<String> newFileLines = FileUtils.getLines(DOWNLOAD_FILE);
                FileUtils.writeToLogFile(logFilePath, newFileLines, SECRET_KEY);
                logFile.setLogFileLines(FileUtils.getLinesFromLogFile(logFilePath, SECRET_KEY));
                logFile.setLogFileDateInfo(FileUtils.getDateInfoFromLines(logFile.getLogFileLines()));

                logFile.getLogFileLines().forEach(line -> {
                    textArea.appendText(line + "\n");
                });

                if (logFile.getLogFileDateInfo().getMaxDate() != null && logFile.getLogFileDateInfo().getMinDate() != null) {
                    startDatePicker.setDayCellFactory(d ->
                            new DateCell() {
                                @Override
                                public void updateItem(LocalDate item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setDisable(item.isAfter(logFile.getLogFileDateInfo().getMaxDate()) || item.isBefore(logFile.getLogFileDateInfo().getMinDate()));
                                }
                            });
                    startDatePicker.setValue(logFile.getLogFileDateInfo().getMinDate());

                    endDatePicker.setDayCellFactory(d ->
                            new DateCell() {
                                @Override
                                public void updateItem(LocalDate item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setDisable(item.isAfter(logFile.getLogFileDateInfo().getMaxDate()) || item.isBefore(logFile.getLogFileDateInfo().getMinDate()));
                                }
                            });
                    endDatePicker.setValue(logFile.getLogFileDateInfo().getMaxDate());
                }
                accordion.setExpandedPane(filterPane);
            } catch (JSchException | SftpException e) {
                Alert alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Something went wrong",
                        ButtonType.OK
                );
                alert.setHeaderText(null);
                alert.showAndWait();
                e.printStackTrace();
            } finally {
                FileUtils.deleteFile(DOWNLOAD_FILE);
            }
        });

        return new Scene(wrappingLayout, 800, 800);
    }
}
