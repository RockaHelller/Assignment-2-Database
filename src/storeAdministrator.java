import java.sql.*;
import java.util.Scanner;

public class storeAdministrator {

    private static final String URL = "jdbc:postgresql://localhost:5432/store_database";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "Qwedcxzas123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // CONNECTION PART
        try (Connection connection = DriverManager.getConnection(URL,USERNAME,PASSWORD)) {

            System.out.println("Which operation do you want? \n create\n show\n update\n delete\n tables-metadata\n one-table-metadata");
            String operation = scanner.nextLine();

            // Selecting a CRUD operation
            switch (operation.toLowerCase()) {

                case "create":
                    insertRecord(scanner, connection);
                    break;

                case "show":
                    retrieve_books(connection);
                    break;



               

            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            scanner.close();
        }
    }




    private static void insertRecord(Scanner scanner, Connection connection) throws SQLException {

        System.out.println("What do you want to create? Book, Author, Customer, Order");
        String table = scanner.nextLine();

        switch (table.toLowerCase()) {

            case "book":
                create_book(scanner, connection);
                break;

            case "author":
                create_author(scanner, connection);
                break;

            case "customer":
                create_customer(scanner, connection);
                break;

            case "order":
                create_order(scanner, connection);
                break;

            default:
                System.out.println("Invalid table.");
        }
    }

    private static void create_order(Scanner scanner, Connection connection) throws SQLException {

        System.out.print("What is the order ID? ");
        int order_id = Integer.parseInt(scanner.nextLine());

        System.out.print("What is the customer ID? ");
        int customer_id = Integer.parseInt(scanner.nextLine());

        System.out.print("What is the book ID? ");
        int book_id = Integer.parseInt(scanner.nextLine());

        System.out.print("What is the order quantity? ");
        int order_amount = Integer.parseInt(scanner.nextLine());

        String checkVolumeSql = "SELECT current_stock FROM Books WHERE id_of_book = ?";

        String insertOrderSql = "INSERT INTO Orders (id_of_order, id_of_customer, id_of_book, amount_of_order) VALUES (?, ?, ?, ?)";

        String updateBookVolumeSql = "UPDATE Books SET current_stock = current_stock - ? WHERE id_of_book = ?";

        try {
            connection.setAutoCommit(false); // Start transaction

            // Check book volume
            try (PreparedStatement checkVolumeStmt = connection.prepareStatement(checkVolumeSql)) {

                checkVolumeStmt.setInt(1, book_id);
                ResultSet resultSet = checkVolumeStmt.executeQuery();

                if (!resultSet.next() || resultSet.getInt("current_stock") < order_amount) {
                    throw new SQLException("The order cannot be processed due to lack of stock for book " + book_id);
                }
            }

            // Insert order
            try (PreparedStatement insertOrderStmt = connection.prepareStatement(insertOrderSql)) {
                insertOrderStmt.setInt(1, order_id);
                insertOrderStmt.setInt(2, customer_id);
                insertOrderStmt.setInt(3, book_id);
                insertOrderStmt.setInt(4, order_amount);
                insertOrderStmt.executeUpdate();
            }

            // Update book volume
            try (PreparedStatement updateBookVolumeStmt = connection.prepareStatement(updateBookVolumeSql)) {
                updateBookVolumeStmt.setInt(1, order_amount);
                updateBookVolumeStmt.setInt(2, book_id);
                updateBookVolumeStmt.executeUpdate();
            }

            connection.commit(); // Commit transaction
            System.out.println("Order inserted and book stock updated.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            connection.rollback(); // Rollback transaction
        } finally {
            connection.setAutoCommit(true); // Reset auto-commit to true
        }
    }

    private static void create_book(Scanner scanner, Connection connection) throws SQLException {

        System.out.print("What is the book ID? ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("What is author's ID? ");
        int authorId = Integer.parseInt(scanner.nextLine());

        System.out.print("What is the book title? ");
        String title = scanner.nextLine();

        System.out.print("What genre is the book?");
        String genre = scanner.nextLine();

        System.out.print("How many books are added? ");
        int volume = Integer.parseInt(scanner.nextLine());

        String sql = "INSERT INTO Books (id_of_book, id_of_author, title, genre, current_stock) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, authorId);
            stmt.setString(3, title);
            stmt.setString(4, genre);
            stmt.setInt(5, volume);
            int rowsInserted = stmt.executeUpdate();
            System.out.println(rowsInserted + " are inserted successfully.");
        }
    }

    private static void create_author(Scanner scanner, Connection connection) throws SQLException {

        System.out.print("What is the author's ID? ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("What is author's name? ");
        String name = scanner.nextLine();

        String sql_query = "INSERT INTO Authors (id_of_author, author_name) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql_query)) {

            stmt.setInt(1, id);
            stmt.setString(2, name);
            int rowsInserted = stmt.executeUpdate();

            System.out.println(rowsInserted + " are inserted successfully.");
        }
    }

    private static void create_customer(Scanner scanner, Connection connection) throws SQLException {

        System.out.print("What is the customer ID? ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("What is the customer name? ");
        String name = scanner.nextLine();

        String sql_query = "INSERT INTO Customers (id_of_customer, customer_name) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql_query)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            int rowsInserted = stmt.executeUpdate();
            System.out.println(rowsInserted + " are inserted successfully.");
        }
    }

    private static void retrieve_books(Connection connection) throws SQLException {
        String sql_query = "SELECT Books.title, Books.current_stock, Books.genre, Authors.author_name, COALESCE(Orders.amount_of_order, 0) as amount_of_order " +
                "FROM Books " +
                "LEFT JOIN Authors ON Books.id_of_author = Authors.id_of_author " +
                "LEFT JOIN Orders ON Books.id_of_book = Orders.id_of_book";

        System.out.println("Retrieving all books with authors and orders:");

        try (PreparedStatement stmt = connection.prepareStatement(sql_query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {

                String title = resultSet.getString("title");
                int current_stock = resultSet.getInt("current_stock");
                String genre = resultSet.getString("genre");
                String author_name = resultSet.getString("author_name");
                int order_amount = resultSet.getInt("amount_of_order");

                System.out.println("Title: " + title + ", Genre: " + genre + ", Current stock: " + current_stock + ", Author: " + author_name + ", Order amount: " + order_amount);
            }
        }
    }



   

}