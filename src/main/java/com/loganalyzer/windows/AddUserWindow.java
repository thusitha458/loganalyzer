package com.loganalyzer.windows;

import com.loganalyzer.mysql.MySqlConnector;
import com.loganalyzer.sftp.Server;
import com.loganalyzer.users.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddUserWindow {
    public static void display(MySqlConnector mySqlConnector) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("User Management");
        window.setMinWidth(250);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Add User");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label username = new Label("Username:");
        grid.add(username, 0, 1);

        TextField usernameTextField = new TextField();
        grid.add(usernameTextField, 1, 1);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        Label confirmPw = new Label("Confirm:");
        grid.add(confirmPw, 0, 3);

        PasswordField confirmPwBox = new PasswordField();
        grid.add(confirmPwBox, 1, 3);

        Label role = new Label("Role:");
        grid.add(role, 0, 4);

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.setPromptText("Role");
        roleComboBox.getItems().addAll("ADMIN", "NORMAL");
        grid.add(roleComboBox, 1, 4);

        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().addAll(addButton, cancelButton);
        grid.add(hbBtn, 1, 6);

        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 8);

        cancelButton.setOnAction(e -> window.close());
        addButton.setOnAction(e -> {
            String usernameVal = usernameTextField.getText();
            String passwordVal = pwBox.getText();
            String confirmPasswordVal = confirmPwBox.getText();
            String roleVal = roleComboBox.getValue();

            if (usernameVal.trim().isEmpty() || passwordVal.trim().isEmpty() || confirmPasswordVal.trim().isEmpty() || roleVal == null) {
                actionTarget.setText("Fields are empty");
                return;
            }

            if (!passwordVal.equals(confirmPasswordVal)) {
                actionTarget.setText("Passwords do not match");
                return;
            }

            String strRegEx = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}$";
            if (!passwordVal.matches(strRegEx)) {
                actionTarget.setText("Password should have min 8 chars with at least 1 number, 1 uppercase, 1 lowercase and 1 special character");
                return;
            }

            User user = new User(usernameVal, passwordVal, roleVal);
            mySqlConnector.insertUser(user);

            window.close();
        });

        Scene scene = new Scene(grid, 400, 400);
        window.setScene(scene);
        window.showAndWait();
    }
}
