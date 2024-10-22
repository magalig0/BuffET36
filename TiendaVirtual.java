package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TiendaVirtual extends JFrame {
    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    private ArrayList<Producto> listaProductos = new ArrayList<>();
    private ArrayList<Reserva> reservas = new ArrayList<>();
    private JComboBox<String> comboHorarios;
    private JTextArea carrito;
    private Usuario usuarioActual;

    public TiendaVirtual() {
        iniciarSesion();
        if (usuarioActual == null) {
            System.exit(0);
        }

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
        btnEliminarDelCarrito.addActionListener(e -> eliminarProductoDelCarrito());
        add(btnEliminarDelCarrito);

        JButton btnAgregarCarrito = new JButton("Agregar al Carrito");
        btnAgregarCarrito.setBounds(20, 300, 200, 30);
        btnAgregarCarrito.addActionListener(e -> agregarProductoAlCarrito());
        add(btnAgregarCarrito);

        JButton btnConfirmarReserva = new JButton("Confirmar Reserva");
        btnConfirmarReserva.setBounds(350, 270, 200, 30);
        btnConfirmarReserva.addActionListener(e -> confirmarReserva());
        add(btnConfirmarReserva);

        cargarProductos();
        mostrarOpcionesAdmin();

        setVisible(true);
    }

    private void registrarUsuario() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre de usuario:");
        String contraseña = JOptionPane.showInputDialog(this, "Ingrese la contraseña:");

        if (nombre != null && contraseña != null) {
            ConexionDB conexionDB = new ConexionDB();
            if (conexionDB.registrarUsuario(new Usuario(nombre, contraseña, false))) {
                JOptionPane.showMessageDialog(this, "Usuario registrado con éxito.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el usuario.");
            }
            conexionDB.cerrarConexion();
        }
    }

    private void iniciarSesion() {
        while (true) {
            String[] opciones = {"Iniciar Sesión", "Registrarse"};
            int seleccion = JOptionPane.showOptionDialog(this, "¿Desea iniciar sesión o registrarse?", "Inicio",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

            if (seleccion == 0) { // Opción de iniciar sesión
                String nombre = JOptionPane.showInputDialog(this, "Ingrese su nombre de usuario:");
                String contraseña = JOptionPane.showInputDialog(this, "Ingrese su contraseña:");

                ConexionDB conexionDB = new ConexionDB();
                usuarioActual = conexionDB.verificarUsuario(nombre, contraseña);
                conexionDB.cerrarConexion();

                if (usuarioActual != null) {
                    break; // Salir del bucle si el inicio de sesión es exitoso
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.");
                }
            } else if (seleccion == 1) { // Opción de registrarse
                registrarUsuario();
            } else {
                System.exit(0); // Salir si se cierra el diálogo
            }
        }
    }

    private void mostrarOpcionesAdmin() {
        if (usuarioActual != null && usuarioActual.esAdmin()) {
            JButton btnModificarProducto = new JButton("Modificar Producto");
            btnModificarProducto.setBounds(20, 440, 200, 30);
            btnModificarProducto.addActionListener(e -> modificarProducto());
            add(btnModificarProducto);

            JButton btnAgregarProducto = new JButton("Agregar Producto");
            btnAgregarProducto.setBounds(20, 480, 200, 30);
            btnAgregarProducto.addActionListener(e -> agregarProducto());
            add(btnAgregarProducto);

            JButton btnBorrarProducto = new JButton("Borrar Producto");
            btnBorrarProducto.setBounds(20, 520, 200, 30);
            btnBorrarProducto.addActionListener(e -> borrarProducto());
            add(btnBorrarProducto);
        }
    }

    private void modificarProducto() {
        String[] opciones = listaProductos.stream().map(Producto::getNombre).toArray(String[]::new);
        String productoSeleccionado = (String) JOptionPane.showInputDialog(this, "Seleccione un producto:", "Modificar Producto", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (productoSeleccionado != null) {
            Producto producto = listaProductos.stream().filter(p -> p.getNombre().equals(productoSeleccionado)).findFirst().orElse(null);

            if (producto != null) {
                String nuevoNombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", producto.getNombre());
                String nuevoPrecioStr = JOptionPane.showInputDialog(this, "Nuevo precio:", producto.getPrecio());
                String nuevaCantidadStr = JOptionPane.showInputDialog(this, "Nueva cantidad:", producto.getCantidadDisponible());

                try {
                    double nuevoPrecio = Double.parseDouble(nuevoPrecioStr);
                    int nuevaCantidad = Integer.parseInt(nuevaCantidadStr);

                    // Actualizar producto
                    producto.setNombre(nuevoNombre);
                    producto.setPrecio(nuevoPrecio);
                    producto.setCantidadDisponible(nuevaCantidad);

                    // Actualizar en la base de datos
                    ConexionDB conexionDB = new ConexionDB();
                    conexionDB.actualizarProducto(producto);
                    conexionDB.cerrarConexion();

                    JOptionPane.showMessageDialog(this, "Producto modificado exitosamente.");
                    cargarProductos(); // Actualizar la lista de productos en la interfaz
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Error en los valores ingresados.");
                }
            }
        }
    }

    private void borrarProducto() {
        String[] opciones = listaProductos.stream().map(Producto::getNombre).toArray(String[]::new);
        String productoSeleccionado = (String) JOptionPane.showInputDialog(this, "Seleccione un producto a eliminar:", "Borrar Producto", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (productoSeleccionado != null) {
            Producto productoAEliminar = listaProductos.stream().filter(p -> p.getNombre().equals(productoSeleccionado)).findFirst().orElse(null);

            if (productoAEliminar != null) {
                // Eliminar de la base de datos
                ConexionDB conexionDB = new ConexionDB();
                conexionDB.borrarProducto(productoAEliminar.getId());
                conexionDB.cerrarConexion();

                JOptionPane.showMessageDialog(this, "Producto eliminado exitosamente.");
                cargarProductos(); // Actualizar la lista de productos en la interfaz
            }
        }
    }

    private void agregarProducto() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del producto:");
        String precioStr = JOptionPane.showInputDialog(this, "Ingrese el precio:");
        String cantidadStr = JOptionPane.showInputDialog(this, "Ingrese la cantidad:");

        try {
            double precio = Double.parseDouble(precioStr);
            int cantidad = Integer.parseInt(cantidadStr);
            Producto nuevoProducto = new Producto(listaProductos.size() + 1, nombre, precio, cantidad);

            // Insertar en la base de datos
            ConexionDB conexionDB = new ConexionDB();
            conexionDB.insertarProducto(nuevoProducto);
            conexionDB.cerrarConexion();

            JOptionPane.showMessageDialog(this, "Producto agregado exitosamente.");
            cargarProductos(); // Actualizar la lista de productos en la interfaz
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error en los valores ingresados.");
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
            String cantidadStr = JOptionPane.showInputDialog(this, "Ingrese la cantidad:");
            if (cantidadStr == null || cantidadStr.isEmpty()) return; // Si el usuario cancela

            try {
                int cantidad = Integer.parseInt(cantidadStr);
                if (cantidad > productoSeleccionado.getCantidadDisponible()) {
                    JOptionPane.showMessageDialog(this, "No hay suficiente stock disponible.");
                    return;
                }

                String horarioStr = (String) comboHorarios.getSelectedItem();
                LocalDateTime horario = LocalDateTime.parse(horarioStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                carrito.append("Producto: " + productoSeleccionado.getNombre() + " - Cantidad: " + cantidad + " - Horario: " + horario + "\n");
                reservas.add(new Reserva(productoSeleccionado, cantidad, horario));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido.");
            }
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
        for (Reserva reserva : reservas) {
            reserva.getProducto().reducirCantidad(reserva.getCantidad());
            conexionDB.insertarReserva(reserva);
        }

        JOptionPane.showMessageDialog(this, "Reserva confirmada para " + reservas.size() + " productos.");
        conexionDB.cerrarConexion();
        limpiarCarritoYReservas();
    }

    private void limpiarCarritoYReservas() {
        carrito.setText("");
        reservas.clear();
    }

    private void cargarProductos() {
        modeloProductos.setRowCount(0); // Limpiar la tabla antes de cargar
        listaProductos.clear(); // Limpiar la lista antes de cargar

        // Conexión a la base de datos
        ConexionDB conexionDB = new ConexionDB();
        listaProductos = conexionDB.cargarProductosDesdeDB(); // Método que debes implementar en ConexionDB
        conexionDB.cerrarConexion();

        // Agregar productos a la tabla
        for (Producto producto : listaProductos) {
            modeloProductos.addRow(new Object[]{producto.getNombre(), producto.getPrecio(), producto.getCantidadDisponible()});
        }
    }

    public static void main(String[] args) {
        new TiendaVirtual();
    }
}