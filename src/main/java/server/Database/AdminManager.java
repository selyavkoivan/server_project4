package server.Database;

import com.google.gson.Gson;
import server.Consts.Answer;
import server.Consts.MainAdminData;
import server.Database.DatabaseConnector.DataBase;
import server.Models.Admin;
import server.Models.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminManager {
    private static Statement stmt;
    private static AdminManager databaseManager;

    public static AdminManager getDatabaseManager() {
        if (databaseManager == null) databaseManager = new AdminManager();
        return databaseManager;
    }

    private AdminManager() {
        stmt = DataBase.getDatabase().getStmt();
    }

    public Admin getAdminData(String message) {
        Admin admin = new Gson().fromJson(message, Admin.class);
        String query = "SELECT * FROM test.admin INNER JOIN test.user on test.user.userId = test.admin.userId WHERE test.admin.adminId = " + admin.getAdminId();
        try {
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return new Admin(rs.getInt(1), rs.getString("position"), new User(rs.getInt(5),
                    rs.getString("login"), rs.getString("password"), rs.getString("name")));

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Admin getAdminData(User user) {
        String query = "SELECT * FROM test.admin WHERE userID = " + user.getUserId();
        try {
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return new Admin(rs.getInt("adminId"), rs.getString("position"), user);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



    public String editAdmin(String adminStr) {

        Admin admin = new Gson().fromJson(adminStr, Admin.class);
        String query = "UPDATE test.admin\n" +
                "inner join test.user on test.user.userId = test.admin.userId\n" +
                "set test.admin.position = '" + admin.getPosition() + "', test.user.login = '" + admin.getLogin() + "', test.user.name = '" + admin.getLogin() + "'\n" +
                "where test.admin.adminId = " + admin.getAdminId();

        try {
            stmt.executeUpdate(query);
            return Answer.SUCCESS.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return Answer.ERROR.toString();
        }
    }

    public void SetNewAdmin(String data) throws SQLException {
        Admin admin = new Gson().fromJson(data, Admin.class);
        String query = "INSERT INTO test.admin (position, userId) \n" +
                "VALUES ('" + admin.getPosition() + "', (SELECT userId FROM test.user where login = '" + admin.getLogin() + "'))";
        stmt.executeUpdate(query);
    }

    public void SetNewAdmin(Admin admin) throws SQLException {
        String query = "INSERT INTO test.admin (position, userId) \n" +
                "VALUES ('" + admin.getPosition() + "', (SELECT userId FROM test.user where login = '" + admin.getLogin() + "'))";
        stmt.executeUpdate(query);
    }

    public void SetNewAdmin() throws SQLException {
        String query = "INSERT INTO test.admin (position, userId) \n" +
                "VALUES ('" + MainAdminData.POSITION + "', (SELECT userId FROM test.user where login = '" + MainAdminData.LOGIN + "'))";

        stmt.executeUpdate(query);

    }

}
