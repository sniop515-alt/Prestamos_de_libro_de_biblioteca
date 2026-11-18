package com.mycompany.biblioteca;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Formulario CRUD de socios. */
public class FrmSocios extends JFrame {
    private final JTextField txtId = new JTextField(), txtCedula = new JTextField(),
            txtNombres = new JTextField(), txtApellidos = new JTextField(),
            txtTelefono = new JTextField(), txtEmail = new JTextField();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Cédula", "Nombres", "Apellidos", "Teléfono", "Email"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private SocioBeans socio;

    public FrmSocios() {
        setTitle("Socios");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(750, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        txtId.setEditable(false);
        JPanel form = new JPanel(new GridLayout(6, 2, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Datos del socio"));
        form.add(new JLabel("ID:"));        form.add(txtId);
        form.add(new JLabel("Cédula:"));    form.add(txtCedula);
        form.add(new JLabel("Nombres:"));   form.add(txtNombres);
        form.add(new JLabel("Apellidos:")); form.add(txtApellidos);
        form.add(new JLabel("Teléfono:"));  form.add(txtTelefono);
        form.add(new JLabel("Email:"));     form.add(txtEmail);

        JButton bNuevo = new JButton("Nuevo"), bGuardar = new JButton("Guardar"),
                bActualizar = new JButton("Actualizar"), bEliminar = new JButton("Eliminar");
        JPanel botones = new JPanel();
        botones.add(bNuevo); botones.add(bGuardar); botones.add(bActualizar); botones.add(bEliminar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        try {
            socio = new SocioBeans();
            cargarTabla();
        } catch (Exception e) {
            error(e);
        }

        tabla.getSelectionModel().addListSelectionListener(ev -> {
            int f = tabla.getSelectedRow();
            if (!ev.getValueIsAdjusting() && f >= 0) {
                txtId.setText(texto(f, 0));
                txtCedula.setText(texto(f, 1));
                txtNombres.setText(texto(f, 2));
                txtApellidos.setText(texto(f, 3));
                txtTelefono.setText(texto(f, 4));
                txtEmail.setText(texto(f, 5));
            }
        });

        bNuevo.addActionListener(e -> limpiar());
        bGuardar.addActionListener(e -> ejecutar(() -> { leer(); socio.insertarSocio(); }, "Socio guardado"));
        bActualizar.addActionListener(e -> {
            if (txtId.getText().isEmpty()) { aviso("Seleccione un socio de la tabla"); return; }
            ejecutar(() -> { leer(); socio.setIdSocio(Integer.parseInt(txtId.getText())); socio.actualizarSocio(); },
                    "Socio actualizado");
        });
        bEliminar.addActionListener(e -> {
            if (txtId.getText().isEmpty()) { aviso("Seleccione un socio de la tabla"); return; }
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar este socio?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                ejecutar(() -> { socio.setIdSocio(Integer.parseInt(txtId.getText())); socio.eliminarSocio(); },
                        "Socio eliminado");
            }
        });
    }

    private interface Accion { void run() throws Exception; }

    private void ejecutar(Accion a, String ok) {
        try {
            a.run();
            cargarTabla();
            limpiar();
            aviso(ok);
        } catch (Exception e) {
            error(e);
        }
    }

    private String texto(int fila, int col) {
        Object v = modelo.getValueAt(fila, col);
        return v == null ? "" : v.toString();
    }

    private void leer() {
        if (txtCedula.getText().trim().isEmpty() || txtNombres.getText().trim().isEmpty()
                || txtApellidos.getText().trim().isEmpty())
            throw new IllegalArgumentException("Cédula, nombres y apellidos son obligatorios");
        socio.setCedula(txtCedula.getText().trim());
        socio.setNombres(txtNombres.getText().trim());
        socio.setApellidos(txtApellidos.getText().trim());
        socio.setTelefono(txtTelefono.getText().trim());
        socio.setEmail(txtEmail.getText().trim());
    }

    private void cargarTabla() throws Exception {
        modelo.setRowCount(0);
        List<SocioBeans> lista = socio.listarSocios();
        for (SocioBeans s : lista)
            modelo.addRow(new Object[]{s.getIdSocio(), s.getCedula(), s.getNombres(),
                    s.getApellidos(), s.getTelefono(), s.getEmail()});
    }

    private void limpiar() {
        txtId.setText(""); txtCedula.setText(""); txtNombres.setText("");
        txtApellidos.setText(""); txtTelefono.setText(""); txtEmail.setText("");
        tabla.clearSelection();
    }

    private void aviso(String m) { JOptionPane.showMessageDialog(this, m); }
    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
