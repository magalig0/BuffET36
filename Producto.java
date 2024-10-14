package com.mycompany.mavenproject1;

public class Producto {
    private int id; // ID único
    private String nombre;
    private double precio;
    private int cantidadDisponible;

    public Producto(int id, String nombre, double precio, int cantidadDisponible) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidadDisponible = cantidadDisponible;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void reducirCantidad(int cantidad) {
        this.cantidadDisponible -= cantidad;
    }
}