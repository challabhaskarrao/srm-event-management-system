package com.srm.events.admin.ui;

import com.srm.events.admin.api.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton signInButton;

    public LoginFrame() {
        setTitle("SRM Event Management System - Staff & Administrative Console");
        setSize(460, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel headerLabel = new JLabel("SRM Faculty & Admin Console");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createVerticalStrut(40));

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        formPanel.setMaximumSize(new Dimension(350, 150));

        formPanel.add(new JLabel("Staff Email"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        signInButton = new JButton("Sign In");
        signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInButton.setPreferredSize(new Dimension(150, 40));
        signInButton.setMaximumSize(new Dimension(150, 40));
        signInButton.addActionListener(this::handleLogin);

        mainPanel.add(signInButton);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        signInButton.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ApiClient.getInstance().login(email, password);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // This will re-throw any exceptions caught in doInBackground
                    DashboardFrame dashboard = new DashboardFrame();
                    dashboard.setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
                    signInButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
