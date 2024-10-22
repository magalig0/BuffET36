package com.mycompany.mavenproject1;

import java.sql.*;
import java.util.ArrayList;

public class ConexionDB {
    private Connection conexion;
    private String url = "jdbc:mysql://localhost:3306/buffet"; // Cambia por tu base de datos
    private String usuario = "root"; // Cambia por tu usuario
    private String contrasena = ""; // Cambia por tu contraseña

    public ConexionDB() {
        try {
            conexion = DriverManager.getConnection(url, usuario, contrasena);
            System.out.println("Conexión establecida con éxito.");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConexion() {
        return conexion;
    }

    public Usuario verificarUsuario(String nombre, String contraseña) {
        String query = "SELECT es_admin FROM usuarios WHERE nombre = ? AND contraseña = ?";
        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setString(1, nombre);
            statement.setString(2, contraseña);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                boolean esAdmin = resultSet.getBoolean("es_admin");
                return new Usuario(nombre, contraseña, esAdmin);
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar el usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Retorna null si no se encuentra el usuario o hay error
    }

    public boolean registrarUsuario(Usuario usuario) {
        String query = "INSERT INTO usuarios (nombre, contraseña, es_admin) VALUES (?, ?, ?)";
        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getContraseña());
            statement.setBoolean(3, usuario.esAdmin());
            statement.executeUpdate();
            return true; // Registro exitoso
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false; // Registro fallido
        }
    }

    public void insertarReserva(Reserva reserva) {
        String query = "INSERT INTO reservas (producto_id, cantidad, horario) VALUES (?, ?, ?)";
        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setInt(1, reserva.getProducto().getId());
            statement.setInt(2, reserva.getCantidad());
            statement.setTimestamp(3, Timestamp.valueOf(reserva.getHorario()));
            statement.executeUpdate();
            System.out.println("Reserva insertada con éxito.");
        } catch (SQLException e) {
            System.err.println("Error al insertar reserva: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean insertarProducto(Producto producto) {
        String sql = "INSERT INTO productos (nombre, precio, cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, producto.getNombre());
            stmt.setDouble(2, producto.getPrecio());
            stmt.setInt(3, producto.getCantidadDisponible());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void borrarProducto(int id) {
        String query = "DELETE FROM productos WHERE id = ?";
        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al borrar producto: " + e.getMessage());
        }
    }

    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    public void actualizarProducto(Producto producto) {
        String query = "UPDATE productos SET nombre = ?, precio = ?, cantidad = ? WHERE id = ?";
        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setString(1, producto.getNombre());
            statement.setDouble(2, producto.getPrecio());
            statement.setInt(3, producto.getCantidadDisponible());
            statement.setInt(4, producto.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
        }
    }

    public ArrayList<Producto> cargarProductosDesdeDB() {
        ArrayList<Producto> productos = new ArrayList<>();
        String query = "SELECT id, nombre, precio, cantidad FROM productos";

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int cantidad = rs.getInt("cantidad");
                productos.add(new Producto(id, nombre, precio, cantidad));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }
}
