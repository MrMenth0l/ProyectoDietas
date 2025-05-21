package org.example.Model;

public class Usuario {
    private String correo;
    private String password;
    private String id;
    private String nombre;
    private int edad;
    private String genero;
    private double altura;
    private double peso;


    public Usuario(String correo, String password) {
        this.correo = correo;
        this.password = password;

    }

    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGenero() {
        return genero;
    }

    public String getId() {
        return id;
    }

    public int getEdad() {
        return edad;
    }

    public double getAltura() {
        return altura;
    }

    public double getPeso() {
        return peso;
    }
}
