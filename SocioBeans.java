package com.mycompany.biblioteca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Bean de la tabla socio con sus operaciones CRUD. */
public class SocioBeans {
    private int idSocio;
    private String cedula, nombres, apellidos, telefono, email;
    private final accesobd bd;

    public SocioBeans() throws Exception {
        bd = Config.nuevaConexion();
    }

    public int getIdSocio() { return idSocio; }
    public void setIdSocio(int idSocio) { this.idSocio = idSocio; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void insertarSocio() throws SQLException {
        String sql = "INSERT INTO socio (cedula, nombres, apellidos, telefono, email) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, cedula);
            ps.setString(2, nombres);
            ps.setString(3, apellidos);
            ps.setString(4, telefono);
            ps.setString(5, email);
            ps.executeUpdate();
        }
    }

    public void actualizarSocio() throws SQLException {
        String sql = "UPDATE socio SET cedula=?, nombres=?, apellidos=?, telefono=?, email=? WHERE id_socio=?";
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, cedula);
            ps.setString(2, nombres);
            ps.setString(3, apellidos);
            ps.setString(4, telefono);
            ps.setString(5, email);
            ps.setInt(6, idSocio);
            ps.executeUpdate();
        }
    }

    public void eliminarSocio() throws SQLException {
        try (PreparedStatement ps = bd.getConexion().prepareStatement("DELETE FROM socio WHERE id_socio=?")) {
            ps.setInt(1, idSocio);
            ps.executeUpdate();
        }
    }

    /** Busca por cédula y carga los datos en el bean. */
    public boolean consultarSocio(String cedulaBuscada) throws SQLException {
        try (PreparedStatement ps = bd.getConexion().prepareStatement("SELECT * FROM socio WHERE cedula=?")) {
            ps.setString(1, cedulaBuscada);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                cargar(rs, this);
                return true;
            }
        }
    }

    public List<SocioBeans> listarSocios() throws Exception {
        List<SocioBeans> lista = new ArrayList<>();
        try (Statement st = bd.getConexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM socio ORDER BY apellidos, nombres")) {
            while (rs.next()) {
                SocioBeans s = new SocioBeans();
                cargar(rs, s);
                lista.add(s);
            }
        }
        return lista;
    }

    private static void cargar(ResultSet rs, SocioBeans s) throws SQLException {
        s.idSocio = rs.getInt("id_socio");
        s.cedula = rs.getString("cedula");
        s.nombres = rs.getString("nombres");
        s.apellidos = rs.getString("apellidos");
        s.telefono = rs.getString("telefono");
        s.email = rs.getString("email");
    }
}
