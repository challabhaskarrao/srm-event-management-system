package com.srm.events.admin;

import com.srm.events.admin.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class AdminApplication {

    private AdminApplication() {
    }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore
            }
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
