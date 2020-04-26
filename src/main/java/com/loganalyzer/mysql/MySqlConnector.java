package com.loganalyzer.mysql;

import com.loganalyzer.Main;
import com.loganalyzer.encryption.AES;
import com.loganalyzer.file.FileUtils;
import com.loganalyzer.sftp.Server;
import com.loganalyzer.users.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MySqlConnector {
    private Connection connection = null;

    public MySqlConnector() throws SQLException {
        Map<String, String> properties = FileUtils.loadPropertiesFile();
        connection = DriverManager.getConnection("jdbc:mysql://"
                + properties.get("mysql.host")
                + "/"
                + properties.get("mysql.database")
                + "?user="
                + properties.get("mysql.user")
                + "&password="
                + properties.get("mysql.password")
        );
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
        ) {
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String role = resultSet.getString("role");
                users.add(new User(username, password, role));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return users;
    }

    public void insertUser(User user) {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users (username, password, role) "
                        + "values (?, ?, ?)")
        ) {
            String salt = BCrypt.gensalt();

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, BCrypt.hashpw(user.getPassword(), salt));
            preparedStatement.setString(3, user.getRole());

            preparedStatement.execute();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public User findUser(String username) {
        User user = null;
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        ) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String password = resultSet.getString("password");
                    String role = resultSet.getString("role");
                    user = new User(username, password, role);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return user;
    }

    public void insertServer(Server server) {
        try (
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO servers (name, username, password, host, port, fileName) "
                        + "values (?, ?, ?, ?, ?, ?)")
        ) {
            preparedStatement.setString(1, server.getName());
            preparedStatement.setString(2, server.getUsername());
            preparedStatement.setString(3, AES.encrypt(server.getPassword(), Main.SECRET_KEY));
            preparedStatement.setString(4, server.getHost());
            preparedStatement.setInt(5, server.getPort());
            preparedStatement.setString(6, server.getFileName());

            preparedStatement.execute();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public List<Server> getServers() {
        List<Server> servers = new ArrayList<>();
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM servers");
        ) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String host = resultSet.getString("host");
                int port = resultSet.getInt("port");
                String fileName = resultSet.getString("fileName");
                servers.add(new Server(name, username, AES.decrypt(password, Main.SECRET_KEY), host, port, fileName));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return servers;
    }
}
