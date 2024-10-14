package com.mycompany.mavenproject1;

import java.sql.*;

public class ConexionDB {
    private Connection conexion;

    public ConexionDB() {
        try {
            String url = "jdbc:mysql://localhost:3306/buffet"; // Aseg�rate que esta base de datos exista
            String user = "root"; // Aseg�rate que el usuario tenga permisos
            String password = ""; // Si tienes contrase�a, col�cala aqu�
            conexion = DriverManager.getConnection(url, user, password);
            System.out.println("Conexi�n establecida con �xito.");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConexion() {
        return conexion;
    }

    public void insertarProducto(Producto producto) {
        String query = "INSERT INTO productos (nombre, descripcion, precio, cantidad) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setString(1, producto.getNombre());
            statement.setString(2, producto.getDescripcion());
            statement.setDouble(3, producto.getPrecio());
            statement.setInt(4, producto.getCantidadDisponible());
            statement.executeUpdate();
            System.out.println("Producto insertado con �xito.");
        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertarReserva(Reserva reserva) {
        String query = "INSERT INTO reservas (producto_id, cantidad, horario) VALUES (?, ?, ?)";
        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setInt(1, reserva.getProducto().getId());
            statement.setInt(2, reserva.getCantidad());
            statement.setTimestamp(3, Timestamp.valueOf(reserva.getHorario())); // Aseg�rate de que 'horario' sea un LocalDateTime
            statement.executeUpdate();
            System.out.println("Reserva insertada con �xito.");
        } catch (SQLException e) {
            System.err.println("Error al insertar reserva: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                System.out.println("Conexi�n cerrada.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexi�n: " + e.getMessage());
            }
        }
    }
}
