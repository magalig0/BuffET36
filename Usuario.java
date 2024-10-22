package com.mycompany.mavenproject1;

public class Usuario {
    private String nombre;
    private String contraseña;
    private boolean esAdmin;

    public Usuario(String nombre, String contraseña, boolean esAdmin) {
        this.nombre = nombre;
        this.contraseña = contraseña;
        this.esAdmin = esAdmin;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContraseña() { // Asegúrate de tener este método
        return contraseña;
    }

    public boolean esAdmin() {
        return esAdmin;
    }
}