package Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    Connection con;
    public Connection getConnection(){
        try {
            String myBD= "jdbc:mysql://localhost:3307/sistemaventa?serverTimezone=UTC";
            con = DriverManager.getConnection(myBD, "root", "Pepa123oso@");
            return con;
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
            return null;
        }
    
}
