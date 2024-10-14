package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.sql.*;


public class TiendaVirtual extends JFrame {
    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    private ArrayList<Producto> listaProductos = new ArrayList<>();
    private ArrayList<Reserva> reservas = new ArrayList<>();
    private JComboBox<String> comboHorarios;
    private JTextArea carrito;

    public TiendaVirtual() {
        setTitle("Tienda Virtual");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel labelCatalogo = new JLabel("Catálogo de Productos");
        labelCatalogo.setBounds(20, 20, 200, 20);
        add(labelCatalogo);

        String[] columnas = {"Producto", "Precio", "Disponibilidad"};
        modeloProductos = new DefaultTableModel(columnas, 0);
        tablaProductos = new JTable(modeloProductos);
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.setBounds(20, 50, 300, 200);
        add(scrollProductos);

        JLabel labelCarrito = new JLabel("Carrito de Compras");
        labelCarrito.setBounds(350, 20, 200, 20);
        add(labelCarrito);

        carrito = new JTextArea();
        carrito.setBounds(350, 50, 300, 200);
        add(carrito);

        JLabel labelHorario = new JLabel("Seleccionar Horario:");
        labelHorario.setBounds(20, 270, 150, 20);
        add(labelHorario);

        comboHorarios = new JComboBox<>(generarHorarios());
        comboHorarios.setBounds(150, 270, 100, 20);
        add(comboHorarios);

        JButton btnEliminarDelCarrito = new JButton("Eliminar del Carrito");
        btnEliminarDelCarrito.setBounds(20, 340, 200, 30);
        add(btnEliminarDelCarrito);
        
        btnEliminarDelCarrito.addActionListener(e -> eliminarProductoDelCarrito());

        JButton btnAgregarCarrito = new JButton("Agregar al Carrito");
        btnAgregarCarrito.setBounds(20, 300, 200, 30);
        add(btnAgregarCarrito);
        btnAgregarCarrito.addActionListener(e -> agregarProductoAlCarrito());

        JButton btnConfirmarReserva = new JButton("Confirmar Reserva");
        btnConfirmarReserva.setBounds(350, 270, 200, 30);
        add(btnConfirmarReserva);
        btnConfirmarReserva.addActionListener(e -> confirmarReserva());

        cargarProductos();
        
        setVisible(true);
    }

    private void cargarProductos() {
        listaProductos.add(new Producto(1, "Focaccia", 5.00, 10));
        listaProductos.add(new Producto(2, "Milanguche de sanguinesa", 5.00, 10));
        listaProductos.add(new Producto(3, "Pizza", 6.00, 8));
        listaProductos.add(new Producto(4, "Empanada de algo", 2.50, 15));
        listaProductos.add(new Producto(5, "Pebete", 2.50, 15));
        listaProductos.add(new Producto(6, "Chegusan", 2.50, 15));
        
        for (Producto producto : listaProductos) {
            modeloProductos.addRow(new Object[]{producto.getNombre(), producto.getPrecio(), producto.getCantidadDisponible()});
        }
    }

    private String[] generarHorarios() {
        String[] horarios = new String[5];
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        for (int i = 0; i < 5; i++) {
            horarios[i] = now.plusMinutes(i * 50).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return horarios;
    }

private void agregarProductoAlCarrito() {
    int filaSeleccionada = tablaProductos.getSelectedRow();
    if (filaSeleccionada >= 0) {
        Producto productoSeleccionado = listaProductos.get(filaSeleccionada);
        
        // Obtener la cantidad del usuario
        String cantidadStr = JOptionPane.showInputDialog(this, "Ingrese la cantidad:");
        int cantidad = Integer.parseInt(cantidadStr);
        
        // Verificar disponibilidad
        if (cantidad > productoSeleccionado.getCantidadDisponible()) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock disponible.");
            return;
        }

        // Obtener el horario seleccionado y convertirlo a LocalDateTime
        String horarioStr = (String) comboHorarios.getSelectedItem();
        LocalDateTime horario = LocalDateTime.parse(horarioStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        carrito.append("Producto: " + productoSeleccionado.getNombre() + " - Cantidad: " + cantidad + " - Horario: " + horario + "\n");
        reservas.add(new Reserva(productoSeleccionado, cantidad, horario)); // Usar el horario como LocalDateTime
    }
}

    private void eliminarProductoDelCarrito() {
        String[] lineas = carrito.getText().split("\n");
        if (lineas.length == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        String ultimaReserva = lineas[lineas.length - 1];
        carrito.setText(carrito.getText().replace(ultimaReserva + "\n", ""));
        
        if (!reservas.isEmpty()) {
            reservas.remove(reservas.size() - 1);
        }

        JOptionPane.showMessageDialog(this, "Producto eliminado del carrito.");
    }

    private void confirmarReserva() {
    if (reservas.isEmpty()) {
        JOptionPane.showMessageDialog(this, "El carrito está vacío.");
        return;
    }

    ConexionDB conexionDB = new ConexionDB();
    if (conexionDB.getConexion() == null) {
        JOptionPane.showMessageDialog(this, "No se pudo establecer la conexión a la base de datos.");
        return;
    }

    for (Reserva reserva : reservas) {
        if (productoExiste(reserva.getProducto().getId(), conexionDB)) {
            // Actualizar la disponibilidad del producto
            reserva.getProducto().reducirCantidad(reserva.getCantidad());
            conexionDB.insertarReserva(reserva);
        } else {
            JOptionPane.showMessageDialog(this, "El producto con ID " + reserva.getProducto().getId() + " no existe.");
            return;
        }
    }

    JOptionPane.showMessageDialog(this, "Reserva confirmada para " + reservas.size() + " productos.");
    conexionDB.cerrarConexion(); // Cerrar la conexión
    limpiarCarritoYReservas();
}

    private boolean productoExiste(int productoId, ConexionDB conexionDB) {
     String query = "SELECT COUNT(*) FROM productos WHERE id = ?";
        try (PreparedStatement statement = conexionDB.getConexion().prepareStatement(query)) {
         statement.setInt(1, productoId);
          ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1) > 0; // Retorna true si existe al menos un registro
        }
    } catch (SQLException e) {
        System.err.println("Error al verificar el producto: " + e.getMessage());
        e.printStackTrace();
    }
    return false; // Por defecto, retorna false
}

    private void limpiarCarritoYReservas() {
        carrito.setText("");
        reservas.clear();
    }

    // Método main para ejecutar la aplicación
    public static void main(String[] args) {
        new TiendaVirtual();
    }
}
