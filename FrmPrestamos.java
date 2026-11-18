package com.mycompany.biblioteca;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Consulta de préstamos y registro de devoluciones. */
public class FrmPrestamos extends JFrame {
    private final JTextField txtBuscar = new JTextField(16);
    private final JCheckBox chkPendientes = new JCheckBox("Solo pendientes");
    private final DefaultTableModel modeloPrest = new DefaultTableModel(
            new String[]{"N°", "Fecha", "Devolver hasta", "Socio", "Bibliotecario", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel modeloDet = new DefaultTableModel(
            new String[]{"ID detalle", "Libro", "Devuelto", "Fecha devolución"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaPrest = new JTable(modeloPrest);
    private final JTable tablaDet = new JTable(modeloDet);
    private accesobd bd;

    public FrmPrestamos() {
        setTitle("Préstamos y devoluciones");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JButton bBuscar = new JButton("Buscar"), bTodos = new JButton("Ver todos");
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Socio (nombre o cédula):"));
        filtro.add(txtBuscar); filtro.add(chkPendientes); filtro.add(bBuscar); filtro.add(bTodos);
        add(filtro, BorderLayout.NORTH);

        JScrollPane spPrest = new JScrollPane(tablaPrest);
        spPrest.setBorder(BorderFactory.createTitledBorder("Préstamos"));
        JScrollPane spDet = new JScrollPane(tablaDet);
        spDet.setBorder(BorderFactory.createTitledBorder("Libros del préstamo seleccionado"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spPrest, spDet);
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        JButton bDevolver = new JButton("Registrar devolución del libro seleccionado");
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.add(bDevolver);
        add(sur, BorderLayout.SOUTH);

        try {
            bd = Config.nuevaConexion();
            cargarPrestamos();
        } catch (Exception e) {
            error(e.getMessage());
        }

        tablaPrest.getSelectionModel().addListSelectionListener(ev -> {
            int f = tablaPrest.getSelectedRow();
            if (!ev.getValueIsAdjusting() && f >= 0)
                cargarDetalle((Integer) modeloPrest.getValueAt(f, 0));
        });
        bBuscar.addActionListener(e -> cargarPrestamos());
        txtBuscar.addActionListener(e -> cargarPrestamos());
        bTodos.addActionListener(e -> { txtBuscar.setText(""); chkPendientes.setSelected(false); cargarPrestamos(); });
        bDevolver.addActionListener(e -> devolver());
    }

    private void cargarPrestamos() {
        modeloPrest.setRowCount(0);
        modeloDet.setRowCount(0);
        String sql = "SELECT p.id_prestamo, p.fecha_prestamo, p.fecha_prevista, "
                + "CONCAT(s.apellidos,' ',s.nombres), CONCAT(b.apellidos,' ',b.nombres), "
                + "CASE WHEN p.estado = 'Activo' AND p.fecha_prevista < CURDATE() THEN 'Atrasado' ELSE p.estado END "
                + "FROM prestamo p JOIN socio s ON s.id_socio = p.id_socio "
                + "JOIN bibliotecario b ON b.id_bibliotecario = p.id_bibliotecario "
                + "WHERE (s.cedula LIKE ? OR s.nombres LIKE ? OR s.apellidos LIKE ?) "
                + (chkPendientes.isSelected() ? "AND p.estado = 'Activo' " : "")
                + "ORDER BY p.id_prestamo DESC";
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            String like = "%" + txtBuscar.getText().trim() + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    modeloPrest.addRow(new Object[]{rs.getInt(1), rs.getDate(2), rs.getDate(3),
                            rs.getString(4), rs.getString(5), rs.getString(6)});
            }
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private void cargarDetalle(int idPrestamo) {
        modeloDet.setRowCount(0);
        String sql = "SELECT d.id_detalle, l.titulo, d.devuelto, d.fecha_devolucion "
                + "FROM detalle_prestamo d JOIN libro l ON l.id_libro = d.id_libro WHERE d.id_prestamo = ?";
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    modeloDet.addRow(new Object[]{rs.getInt(1), rs.getString(2),
                            rs.getInt(3) == 1 ? "Sí" : "No", rs.getDate(4)});
            }
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private void devolver() {
        int fd = tablaDet.getSelectedRow();
        if (fd < 0) { error("Seleccione un libro de la tabla de abajo"); return; }
        int idDetalle = (Integer) modeloDet.getValueAt(fd, 0);
        int idPrestamo = (Integer) modeloPrest.getValueAt(tablaPrest.getSelectedRow(), 0);

        Connection c = bd.getConexion();
        try {
            c.setAutoCommit(false);
            int idLibro;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id_libro, devuelto FROM detalle_prestamo WHERE id_detalle = ? FOR UPDATE")) {
                ps.setInt(1, idDetalle);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("El registro ya no existe");
                    if (rs.getInt(2) == 1) throw new SQLException("Ese libro ya fue devuelto");
                    idLibro = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE detalle_prestamo SET devuelto = 1, fecha_devolucion = CURDATE() WHERE id_detalle = ?")) {
                ps.setInt(1, idDetalle);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE libro SET disponibles = disponibles + 1 WHERE id_libro = ?")) {
                ps.setInt(1, idLibro);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM detalle_prestamo WHERE id_prestamo = ? AND devuelto = 0")) {
                ps.setInt(1, idPrestamo);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        try (PreparedStatement up = c.prepareStatement(
                                "UPDATE prestamo SET estado = 'Devuelto' WHERE id_prestamo = ?")) {
                            up.setInt(1, idPrestamo);
                            up.executeUpdate();
                        }
                    }
                }
            }
            c.commit();
            JOptionPane.showMessageDialog(this, "Devolución registrada");
            cargarPrestamos();
            for (int i = 0; i < modeloPrest.getRowCount(); i++)
                if ((Integer) modeloPrest.getValueAt(i, 0) == idPrestamo) {
                    tablaPrest.setRowSelectionInterval(i, i);
                    break;
                }
        } catch (SQLException e) {
            try { c.rollback(); } catch (SQLException ignorada) { }
            error(e.getMessage());
        } finally {
            try { c.setAutoCommit(true); } catch (SQLException ignorada) { }
        }
    }

    private void error(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
