import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException {
        String username = "root";
        String password = "root";
        String connectionUrl = "jdbc:mysql://localhost:3307/traindb?verifyServerCertificate=false&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        Class.forName("com.mysql.cj.jdbc.Driver");

        try(Connection connection = DriverManager.getConnection(connectionUrl, username, password);
            Statement statement = connection.createStatement()) {
            createTable(statement);
            createBlob(connection);
            downloadBlob(statement);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createTable(Statement statement) throws SQLException {
        statement.execute("drop table if exists books");
        statement.executeUpdate("create table if not exists books (id mediumint not null primary key auto_increment, name varchar(30), img blob)");
    }

    private static void createBlob(Connection connection) throws IOException, SQLException {
        BufferedImage image = ImageIO.read(new File(new File("smile.jpg").getAbsolutePath()));
        Blob createBlob = connection.createBlob();

        try (OutputStream outputStream = createBlob.setBinaryStream(1)) {
            ImageIO.write(image, "jpg", outputStream);
        }

        PreparedStatement preparedStatement = connection.prepareStatement("insert into books (name, img) values (?, ?)");
        preparedStatement.setString(1, "monstrik");
        preparedStatement.setBlob(2, createBlob);
        preparedStatement.execute();
    }

    private static void downloadBlob(Statement statement) throws SQLException, IOException {
        ResultSet resultSet = statement.executeQuery("select * from books");

        while (resultSet.next()) {
            Blob downloadBlob = resultSet.getBlob("img");
            BufferedImage image = ImageIO.read(downloadBlob.getBinaryStream());
            File outFile = new File("saved.png");
            ImageIO.write(image, "png", outFile);
        }
    }
}
