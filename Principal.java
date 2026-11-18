package com.mycompany.biblioteca;

import java.awt.GridLayout;
import javax.swing.*;

/** Menú principal del sistema de biblioteca. */
public class Principal extends JFrame {

    public Principal() {
        setTitle("Sistema de biblioteca");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(320, 230);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));
        JButton bSocios = new JButton("Socios");
        JButton bPrestamo = new JButton("Nuevo préstamo");
        JButton bVer = new JButton("Préstamos y devoluciones");
        JButton bSalir = new JButton("Salir");
        add(bSocios); add(bPrestamo); add(bVer); add(bSalir);
        bSocios.addActionListener(e -> new FrmSocios().setVisible(true));
        bPrestamo.addActionListener(e -> new FrmPrestamo().setVisible(true));
        bVer.addActionListener(e -> new FrmPrestamos().setVisible(true));
        bSalir.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Principal().setVisible(true));
    }
}
