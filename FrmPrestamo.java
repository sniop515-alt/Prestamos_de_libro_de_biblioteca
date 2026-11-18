package com.mycompany.biblioteca;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Proceso principal: registrar un préstamo.
 * Elige socio y bibliotecario, agrega libros (máximo 3), define los días de préstamo,
 * guarda préstamo + detalle y descuenta los ejemplares disponibles (en una transacción).
 */
public class FrmPrestamo extends JFrame {
    private static final int MAX_LIBROS = 3;

    private static class Item {
        final int id; final String texto;
        Item(int id, String texto) { this.id = id; this.texto = texto; }
        @Override public String toString() { return texto; }
    }

    private final JComboBox<Item> cbSocio = new JComboBox<>();
    private final JComboBox<Item> cbBibliotecario = new JComboBox<>();
    private final JComboBox<Item> cbLibro = new JComboBox<>();
    private final JSpinner spDias = new JSpinner(new SpinnerNumberModel(7, 1, 30, 1));
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID libro", "Título"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private accesobd bd;

    public FrmPrestamo() {
        setTitle("Nuevo préstamo");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel datos = new JPanel(new GridLayout(3, 2, 6, 6));
        datos.setBorder(BorderFactory.createTitledBorder("Datos del préstamo"));
        datos.add(new JLabel("Socio:"));          datos.add(cbSocio);
        datos.add(new JLabel("Bibliotecario:"));  datos.add(cbBibliotecario);
        datos.add(new JLabel("Días de préstamo:")); datos.add(spDias);

        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linea.setBorder(BorderFactory.createTitledBorder("Agregar libro (máximo " + MAX_LIBROS + ")"));
        JButton bAgregar = new JButton("Agregar"), bQuitar = new JButton("Quitar seleccionado");
        linea.add(new JLabel("Libro:")); linea.add(cbLibro);
        linea.add(bAgregar); linea.add(bQuitar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(datos, BorderLayout.NORTH);
        norte.add(linea, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton bGuardar = new JButton("Guardar préstamo");
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.add(bGuardar);
        add(sur, BorderLayout.SOUTH);

        try {
            bd = Config.nuevaConexion();
            cargarCombos();
        } catch (Exception e) {
            error(e.getMessage());
        }

        bAgregar.addActionListener(e -> agregar());
        bQuitar.addActionListener(e -> {
            int f = tabla.getSelectedRow();
            if (f >= 0) modelo.removeRow(f);
        });
        bGuardar.addActionListener(e -> guardar());
    }

    private void cargarCombos() throws SQLException {
        try (Statement st = bd.getConexion().createStatement()) {
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_socio, CONCAT(cedula,' - ',apellidos,' ',nombres) FROM socio ORDER BY apellidos")) {
                while (rs.next()) cbSocio.addItem(new Item(rs.getInt(1), rs.getString(2)));
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_bibliotecario, CONCAT(apellidos,' ',nombres) FROM bibliotecario ORDER BY apellidos")) {
                while (rs.next()) cbBibliotecario.addItem(new Item(rs.getInt(1), rs.getString(2)));
            }
        }
        cargarLibros();
    }

    private void cargarLibros() throws SQLException {
        cbLibro.removeAllItems();
        String sql = "SELECT l.id_libro, CONCAT(l.titulo,' - ',a.nombre,' (disp. ',l.disponibles,')') "
                + "FROM libro l JOIN autor a ON a.id_autor = l.id_autor WHERE l.disponibles > 0 ORDER BY l.titulo";
        try (Statement st = bd.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) cbLibro.addItem(new Item(rs.getInt(1), rs.getString(2)));
        }
    }

    private void agregar() {
        Item libro = (Item) cbLibro.getSelectedItem();
        if (libro == null) { error("No hay libros disponibles"); return; }
        if (modelo.getRowCount() >= MAX_LIBROS) { error("Máximo " + MAX_LIBROS + " libros por préstamo"); return; }
        for (int i = 0; i < modelo.getRowCount(); i++)
            if ((Integer) modelo.getValueAt(i, 0) == libro.id) { error("Ese libro ya está en la lista"); return; }
        String titulo = libro.texto.substring(0, libro.texto.indexOf(" - "));
        modelo.addRow(new Object[]{libro.id, titulo});
    }

    private void guardar() {
        if (modelo.getRowCount() == 0) { error("Agregue al menos un libro"); return; }
        Item socio = (Item) cbSocio.getSelectedItem();
        Item biblio = (Item) cbBibliotecario.getSelectedItem();
        if (socio == null || biblio == null) { error("Seleccione socio y bibliotecario"); return; }
        int dias = (Integer) spDias.getValue();

        Connection c = bd.getConexion();
        try {
            c.setAutoCommit(false);
            int idPrestamo;
            try (PreparedStatement pp = c.prepareStatement(
                    "INSERT INTO prestamo (fecha_prestamo, fecha_prevista, estado, id_socio, id_bibliotecario) "
                    + "VALUES (CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 'Activo', ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pp.setInt(1, dias);
                pp.setInt(2, socio.id);
                pp.setInt(3, biblio.id);
                pp.executeUpdate();
                try (ResultSet k = pp.getGeneratedKeys()) { k.next(); idPrestamo = k.getInt(1); }
            }
            try (PreparedStatement pd = c.prepareStatement(
                    "INSERT INTO detalle_prestamo (id_prestamo, id_libro) VALUES (?,?)");
                 PreparedStatement pl = c.prepareStatement(
                    "UPDATE libro SET disponibles = disponibles - 1 WHERE id_libro = ? AND disponibles > 0")) {
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    int idLibro = (Integer) modelo.getValueAt(i, 0);
                    pl.setInt(1, idLibro);
                    if (pl.executeUpdate() == 0)
                        throw new SQLException("Sin ejemplares disponibles de: " + modelo.getValueAt(i, 1));
                    pd.setInt(1, idPrestamo);
                    pd.setInt(2, idLibro);
                    pd.executeUpdate();
                }
            }
            c.commit();
            JOptionPane.showMessageDialog(this, "Préstamo N° " + idPrestamo + " registrado");
            modelo.setRowCount(0);
            cargarLibros();
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
