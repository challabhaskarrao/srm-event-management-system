package com.srm.events.admin.ui;

import com.srm.events.admin.api.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardFrame extends JFrame {

    private final ApiClient apiClient = ApiClient.getInstance();
    
    private JPanel mainContentPanel;
    private JTable currentTable;
    private DefaultTableModel tableModel;
    
    // For Mentor/HOD right pane
    private JPanel rightDetailPane;
    private JLabel eventNameLabel;
    private JLabel studentNameLabel;
    private JLabel regNoLabel;
    private JLabel eventTypeLabel;
    private JLabel datesLabel;
    private JLabel outstationLabel;
    private JLabel teamMembersLabel;
    private JLabel brochureLabel;
    private JTextArea remarksArea;
    private JButton approveButton;
    private JButton rejectButton;
    
    private Long selectedAppId = null;

    // For Admin filter/search
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JComboBox<String> statusFilterCombo;
    private JTextField searchField;

    public DashboardFrame() {
        setTitle("SRM Event Management System - Dashboard");
        setSize(1150, 720);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(13, 31, 56)); // SRM Navy
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("SRM Staff Console");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userInfoPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel(apiClient.getCurrentUserName() + " [" + apiClient.getCurrentUserRole() + "]");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            apiClient.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        userInfoPanel.add(userLabel);
        userInfoPanel.add(refreshBtn);
        userInfoPanel.add(logoutBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        mainContentPanel = new JPanel(new BorderLayout());
        add(mainContentPanel, BorderLayout.CENTER);
        
        setupWorkspace();
    }

    private void setupWorkspace() {
        String role = apiClient.getCurrentUserRole();
        mainContentPanel.removeAll();
        
        if ("ADMIN".equals(role)) {
            setupAdminWorkspace();
        } else {
            setupReviewerWorkspace(role);
        }
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void setupAdminWorkspace() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        filterPanel.add(new JLabel("Status Filter:"));
        String[] statuses = {"All", "SUBMITTED", "PENDING_HOD_APPROVAL", "APPROVED", "REJECTED_BY_MENTOR", "REJECTED_BY_HOD"};
        statusFilterCombo = new JComboBox<>(statuses);
        statusFilterCombo.addActionListener(e -> applyAdminFilters());
        filterPanel.add(statusFilterCombo);
        
        filterPanel.add(Box.createHorizontalStrut(20));
        
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyAdminFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyAdminFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyAdminFilters(); }
        });
        filterPanel.add(searchField);
        
        filterPanel.add(Box.createHorizontalStrut(20));
        
        JButton exportBtn = new JButton("Export to CSV");
        exportBtn.addActionListener(e -> exportToCsv());
        filterPanel.add(exportBtn);

        mainContentPanel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Student", "Reg No", "Event", "Type", "Dates", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        currentTable = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        currentTable.setRowSorter(rowSorter);
        
        mainContentPanel.add(new JScrollPane(currentTable), BorderLayout.CENTER);
    }

    private void applyAdminFilters() {
        if (rowSorter == null) return;
        
        String status = (String) statusFilterCombo.getSelectedItem();
        String search = searchField.getText().trim();
        
        RowFilter<DefaultTableModel, Integer> statusFilter = null;
        if (!"All".equals(status)) {
            statusFilter = RowFilter.regexFilter("^" + status + "$", 6);
        }
        
        RowFilter<DefaultTableModel, Integer> searchFilter = null;
        if (!search.isEmpty()) {
            searchFilter = RowFilter.regexFilter("(?i)" + search);
        }
        
        if (statusFilter != null && searchFilter != null) {
            rowSorter.setRowFilter(RowFilter.andFilter(List.of(statusFilter, searchFilter)));
        } else if (statusFilter != null) {
            rowSorter.setRowFilter(statusFilter);
        } else if (searchFilter != null) {
            rowSorter.setRowFilter(searchFilter);
        } else {
            rowSorter.setRowFilter(null);
        }
    }

    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }
            
            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                for (int i = 0; i < currentTable.getColumnCount(); i++) {
                    writer.print(currentTable.getColumnName(i));
                    if (i < currentTable.getColumnCount() - 1) writer.print(",");
                }
                writer.println();
                
                for (int i = 0; i < currentTable.getRowCount(); i++) {
                    for (int j = 0; j < currentTable.getColumnCount(); j++) {
                        Object val = currentTable.getValueAt(i, j);
                        String strVal = val != null ? val.toString().replace("\"", "\"\"") : "";
                        writer.print("\"" + strVal + "\"");
                        if (j < currentTable.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Export successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupReviewerWorkspace(String role) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        
        // Left: Table
        String[] columns = {"ID", "Student", "Event", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        currentTable = new JTable(tableModel);
        currentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = currentTable.getSelectedRow();
                if (row >= 0) {
                    showDetails(row);
                } else {
                    clearDetails();
                }
            }
        });
        
        splitPane.setLeftComponent(new JScrollPane(currentTable));
        
        // Right: Details
        rightDetailPane = new JPanel(new BorderLayout());
        rightDetailPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        eventNameLabel = new JLabel();
        studentNameLabel = new JLabel();
        regNoLabel = new JLabel();
        eventTypeLabel = new JLabel();
        datesLabel = new JLabel();
        outstationLabel = new JLabel();
        teamMembersLabel = new JLabel();
        brochureLabel = new JLabel();
        
        addDetailRow(infoPanel, "Event Name:", eventNameLabel);
        addDetailRow(infoPanel, "Student Name:", studentNameLabel);
        addDetailRow(infoPanel, "Reg No:", regNoLabel);
        addDetailRow(infoPanel, "Event Type:", eventTypeLabel);
        addDetailRow(infoPanel, "Dates:", datesLabel);
        addDetailRow(infoPanel, "Outstation:", outstationLabel);
        addDetailRow(infoPanel, "Team Members:", teamMembersLabel);
        addDetailRow(infoPanel, "Brochure Link:", brochureLabel);
        
        JPanel actionPanel = new JPanel(new BorderLayout(0, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        remarksArea = new JTextArea(4, 20);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        
        JPanel remarksWrapper = new JPanel(new BorderLayout());
        remarksWrapper.add(new JLabel("Remarks (" + (role.equals("MENTOR") ? "Mentor" : "HOD") + "):"), BorderLayout.NORTH);
        remarksWrapper.add(remarksScroll, BorderLayout.CENTER);
        actionPanel.add(remarksWrapper, BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        approveButton = new JButton(role.equals("MENTOR") ? "Recommend to HOD" : "Grant Final Approval");
        approveButton.setBackground(new Color(40, 167, 69));
        approveButton.setForeground(Color.WHITE);
        approveButton.addActionListener(e -> submitReview(role.equals("MENTOR") ? "RECOMMENDED" : "APPROVED"));
        
        rejectButton = new JButton("Reject");
        rejectButton.setBackground(new Color(220, 53, 69));
        rejectButton.setForeground(Color.WHITE);
        rejectButton.addActionListener(e -> submitReview("REJECTED"));
        
        buttonsPanel.add(rejectButton);
        buttonsPanel.add(approveButton);
        actionPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        rightDetailPane.add(infoPanel, BorderLayout.NORTH);
        rightDetailPane.add(actionPanel, BorderLayout.CENTER);
        
        splitPane.setRightComponent(rightDetailPane);
        
        mainContentPanel.add(splitPane, BorderLayout.CENTER);
        
        clearDetails(); // initially empty
    }

    private void addDetailRow(JPanel panel, String label, JLabel valueLabel) {
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        panel.add(l);
        panel.add(valueLabel);
    }
    
    private List<Map<String, Object>> currentData = null;

    private void loadData() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                String role = apiClient.getCurrentUserRole();
                if ("ADMIN".equals(role)) {
                    return apiClient.getAllApplications();
                } else {
                    return apiClient.getReviewQueue(role);
                }
            }

            @Override
            protected void done() {
                try {
                    currentData = get();
                    tableModel.setRowCount(0);
                    
                    String role = apiClient.getCurrentUserRole();
                    for (Map<String, Object> app : currentData) {
                        if ("ADMIN".equals(role)) {
                            tableModel.addRow(new Object[]{
                                app.get("applicationId"),
                                app.get("studentName"),
                                app.get("registrationNumber"),
                                app.get("eventName"),
                                app.get("eventType"),
                                app.get("startDate") + " to " + app.get("endDate"),
                                app.get("status")
                            });
                        } else {
                            tableModel.addRow(new Object[]{
                                app.get("applicationId"),
                                app.get("studentName"),
                                app.get("eventName"),
                                app.get("eventType")
                            });
                        }
                    }
                    
                    if (!"ADMIN".equals(role)) {
                        clearDetails();
                    } else {
                        applyAdminFilters();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardFrame.this, "Failed to load data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showDetails(int viewRow) {
        if (currentData == null) return;
        
        int modelRow = currentTable.convertRowIndexToModel(viewRow);
        Long id = ((Number) tableModel.getValueAt(modelRow, 0)).longValue();
        selectedAppId = id;
        
        Map<String, Object> app = currentData.stream()
                .filter(a -> ((Number)a.get("applicationId")).longValue() == id)
                .findFirst()
                .orElse(null);
                
        if (app != null) {
            eventNameLabel.setText(String.valueOf(app.get("eventName")));
            studentNameLabel.setText(String.valueOf(app.get("studentName")));
            regNoLabel.setText(String.valueOf(app.get("registrationNumber")));
            eventTypeLabel.setText(String.valueOf(app.get("eventType")));
            datesLabel.setText(app.get("startDate") + " to " + app.get("endDate"));
            outstationLabel.setText(Boolean.TRUE.equals(app.get("outsideState")) ? "Yes" : "No");
            
            Object team = app.get("teamMembers");
            if (team instanceof List) {
                teamMembersLabel.setText(String.join(", ", (List<String>) team));
            } else {
                teamMembersLabel.setText("N/A");
            }
            
            brochureLabel.setText(app.get("brochureLink") != null ? String.valueOf(app.get("brochureLink")) : "N/A");
            
            remarksArea.setText(""); // clear previous
            
            String role = apiClient.getCurrentUserRole();
            if ("HOD".equals(role)) {
                String mentorRemarks = String.valueOf(app.getOrDefault("mentorRemarks", "None"));
                remarksArea.setBorder(BorderFactory.createTitledBorder("Mentor Remarks: " + mentorRemarks));
            } else {
                remarksArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
            
            rightDetailPane.setVisible(true);
            approveButton.setEnabled(true);
            rejectButton.setEnabled(true);
            remarksArea.setEnabled(true);
        }
    }

    private void clearDetails() {
        selectedAppId = null;
        if (rightDetailPane != null) {
            eventNameLabel.setText("");
            studentNameLabel.setText("");
            regNoLabel.setText("");
            eventTypeLabel.setText("");
            datesLabel.setText("");
            outstationLabel.setText("");
            teamMembersLabel.setText("");
            brochureLabel.setText("");
            remarksArea.setText("");
            remarksArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
            remarksArea.setEnabled(false);
        }
    }

    private void submitReview(String status) {
        if (selectedAppId == null) return;
        
        String remarks = remarksArea.getText().trim();
        if ("REJECTED".equals(status) && remarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Remarks are required for rejection.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiClient.submitReview(selectedAppId, status, remarks);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(DashboardFrame.this, "Review submitted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadData(); // reload queue
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DashboardFrame.this, "Failed to submit review: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    approveButton.setEnabled(true);
                    rejectButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
