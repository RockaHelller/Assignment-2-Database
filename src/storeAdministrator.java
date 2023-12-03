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

            System.out.println("Which operation do you want? \n create\n retrieve\n update\n delete\n tables-metadata\n one-table-metadata");
            String operation = scanner.nextLine();

            // Selecting a CRUD operation
            switch (operation.toLowerCase()) {

               

            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            scanner.close();
        }
    }

   

}