package com.loganalyzer.windows;

import com.loganalyzer.mysql.MySqlConnector;
import com.loganalyzer.sftp.Server;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddServerWindow {
    public static void display(MySqlConnector mySqlConnector) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Server Management");
        window.setMinWidth(250);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Add Server");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label serverName = new Label("Server Name:");
        grid.add(serverName, 0, 1);

        TextField serverTextField = new TextField();
        grid.add(serverTextField, 1, 1);

        Label username = new Label("Username:");
        grid.add(username, 0, 2);

        TextField usernameTextField = new TextField();
        grid.add(usernameTextField, 1, 2);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 3);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 3);

        Label host = new Label("Host:");
        grid.add(host, 0, 4);

        TextField hostTextField = new TextField();
        grid.add(hostTextField, 1, 4);

        Label port = new Label("Port:");
        grid.add(port, 0, 5);

        TextField portTextField = new TextField();
        grid.add(portTextField, 1, 5);

        Label fileName = new Label("Log file:");
        grid.add(fileName, 0, 6);

        TextField fileNameTextField = new TextField();
        grid.add(fileNameTextField, 1, 6);

        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().addAll(addButton, cancelButton);
        grid.add(hbBtn, 1, 8);

        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 10);

        cancelButton.setOnAction(e -> window.close());
        addButton.setOnAction(e -> {
            String serverNameVal = serverTextField.getText();
            String usernameVal = usernameTextField.getText();
            String passwordVal = pwBox.getText();
            String hostVal = hostTextField.getText();
            String portVal = portTextField.getText();
            String fileNameVal = fileNameTextField.getText();

            if (serverNameVal.trim().isEmpty() || usernameVal.trim().isEmpty() || passwordVal.trim().isEmpty() || hostVal.trim().isEmpty()
                || portVal.trim().isEmpty() || fileNameVal.trim().isEmpty()) {
                actionTarget.setText("Fields are empty");
                return;
            }

            int portValInt;

            try {
                portValInt = Integer.parseInt(portVal);
            } catch (Exception exception) {
                actionTarget.setText("Port should be an integer");
                return;
            }

            Server server = new Server(serverNameVal, usernameVal, passwordVal, hostVal, portValInt, fileNameVal);
            mySqlConnector.insertServer(server);
            window.close();
        });

        Scene scene = new Scene(grid, 400, 400);
        window.setScene(scene);
        window.showAndWait();
    }
}
