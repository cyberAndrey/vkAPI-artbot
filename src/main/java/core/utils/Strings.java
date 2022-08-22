package core.utils;

public class Strings {
    public static final String URL = "jdbc:postgresql://localhost:5432/dbvkapi";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "root";
    public static final String INSERT_INTO_TAGS = "INSERT INTO tags(photo_id, tag_text) VALUES (?,?)";
    public static final String INSERT_INTO_AUTHORS = "INSERT INTO authors(author_id, name, anonymous, nick) VALUES (?,?,?,?)";
    public static final String INSERT_INTO_PHOTOS = "insert into photos(photo_id, author) values(?,?)";
    public static final String GROUP_URL = "photo-206249029_";
    public static final String GET_PHOTOS_OF_AUTHOR = "select * from photos where author = ";
    public static final String GET_USER_BY_ID = "select * from authors where author_id = ";
    public static final String GET_TAGS_BY_PHOTO = "select * from tags where photo_id=";
    public static final String DELETE_FROM_TAGS = "delete from tags where photo_id=";
    public static final String DELETE_FROM_PHOTOS = "delete from photos where photo_id=";
    public static final String DELETE_FROM_USERS = "delete from authors where author_id=";
    public static final String GET_AUTHOR_ID = "select author_id from authors where nick =";
    public static final String GET_ALL_PHOTOS = "select * from photos";
    public static final String GET_NICK_BY_ID = "select nick from authors where author_id=";
    public static final String CONFIG_FILE = "config.properties";
    public static final String TOKEN = "token", GROUP_ID = "groupId", USER_ID = "userId", ACCESS = "access";
    public static final String DB_ERROR = "Соединения с бд нет";


}
