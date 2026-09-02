package com.srm.events.admin;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class AdminApplication {

    private AdminApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminApplication::showWindow);
    }

    private static void showWindow() {
        UIManager.put("Label.font", new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JFrame frame = new JFrame("SRM Event Management — Admin Console");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(520, 280));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(64, 56, 64, 56));

        JLabel title = new JLabel("Admin Console", SwingConstants.CENTER);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(new Color(13, 31, 56));

        JLabel phase = new JLabel("Development Foundation — C0", SwingConstants.CENTER);
        phase.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        phase.setForeground(new Color(83, 100, 123));

        content.add(Box.createVerticalGlue());
        content.add(title);
        content.add(Box.createVerticalStrut(14));
        content.add(phase);
        content.add(Box.createVerticalGlue());

        frame.add(content, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
