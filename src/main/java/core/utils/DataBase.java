package core.utils;

import java.sql.*;
import java.util.Properties;

public class DataBase {

    public static Connection conn;

    public static Connection createConnection() {

        try {
            Properties props = new Properties();
            props.setProperty("user", Strings.USERNAME);
            props.setProperty("password", Strings.PASSWORD);
            conn = DriverManager.getConnection(Strings.URL, props);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;

    }

    public static void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void insertIntoAuthors(String[] mes) {
        try {
            PreparedStatement ps = conn.prepareStatement(Strings.INSERT_INTO_AUTHORS);
            ps.setString(1, mes[0]);
            ps.setString(2, mes[1]);
            ps.setBoolean(3,Boolean.parseBoolean(mes[2]));
            ps.setString(4, mes[3]);

            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertIntoTags(String photo_id, String tag_text) {
        try {
            PreparedStatement ps = conn.prepareStatement(Strings.INSERT_INTO_TAGS);
            ps.setString(1, photo_id);
            ps.setString(2, tag_text);

            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ResultSet getFromDB(String query) {
        Statement stmt;
        try {
            if (conn != null) {
                stmt = conn.createStatement();
                return stmt.executeQuery(query);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(Strings.DB_ERROR);
        return null;
    }

    public static ResultSet getPhotosOfAuthor(String authorName) {
        Statement stmt;
        try {
            if (conn != null) {
                stmt = conn.createStatement();
                return stmt.executeQuery(Strings.GET_PHOTOS_OF_AUTHOR + "'" + authorName + "'");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(Strings.DB_ERROR);
        return null;
    }

    public static ResultSet getNick(String author_id){
        Statement stmt;
        try {
            if (conn != null) {
                stmt = conn.createStatement();
                return stmt.executeQuery(Strings.GET_NICK_BY_ID + "'" + author_id + "'");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(Strings.DB_ERROR);
        return null;
    }

    public static ResultSet getAllPhotos() {
        Statement stmt;
        try {
            if (conn != null) {
                stmt = conn.createStatement();
                return stmt.executeQuery(Strings.GET_ALL_PHOTOS);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(Strings.DB_ERROR);
        return null;
    }

    public static void insertToPhotos(String photo_id, String user_id){
        try {
            PreparedStatement ps = conn.prepareStatement(Strings.INSERT_INTO_PHOTOS);
            ps.setString(1, photo_id);
            ps.setString(2, user_id);

            ps.execute();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public static boolean checkUser(int user) {
        Statement stmt;
        try {
            if (conn != null) {
                stmt = conn.createStatement();
                ResultSet response =  stmt.executeQuery(Strings.GET_USER_BY_ID + "'" + user + "'");
                return response.next();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(Strings.DB_ERROR);
        return false;
    }

    public static String checkUser(String userName) {
        Statement stmt;
        try {
            if (conn != null) {
                stmt = conn.createStatement();
                ResultSet response =  stmt.executeQuery(Strings.GET_AUTHOR_ID + "'" + userName + "'");
                response.next();
                return response.getString("author_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(Strings.DB_ERROR);
        return null;
    }

    public static boolean deleteFromTags(int photoId) {
        if (conn != null)
            try {
            PreparedStatement preparedStmt = conn.prepareStatement(Strings.DELETE_FROM_TAGS + "'" + photoId + "'");
            preparedStmt.execute();
            return true;
            } catch (Exception e) { }
        return false;
    }

    public static boolean deleteFromPhotos(Integer photoId) {
        if (conn != null)
            try {
                PreparedStatement preparedStmt = conn.prepareStatement(Strings.DELETE_FROM_PHOTOS + "'" + photoId + "'");
                preparedStmt.execute();
                return true;
            } catch (Exception e) { }
        return false;
    }

    public static boolean deleteFromUsers(Integer userId) {
        if (conn != null)
            try {
                PreparedStatement preparedStmt = conn.prepareStatement(Strings.DELETE_FROM_USERS + "'" + userId + "'");
                preparedStmt.execute();
                return true;
            } catch (Exception e) { }
        return false;
    }
}
