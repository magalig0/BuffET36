package com.mycompany.mavenproject1;

public class Usuario {
    private String nombre;
    private String contrase�a;
    private boolean esAdmin;

    public Usuario(String nombre, String contrase�a, boolean esAdmin) {
        this.nombre = nombre;
        this.contrase�a = contrase�a;
        this.esAdmin = esAdmin;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrase�a() { // Aseg�rate de tener este m�todo
        return contrase�a;
    }

    public boolean esAdmin() {
        return esAdmin;
    }
}