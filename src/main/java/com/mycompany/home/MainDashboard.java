package com.mycompany.home;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

public class MainDashboard extends JFrame {

    private final String currentUsername;
    private final String currentFullName;
    private final String currentHomeName;
    private JTextField itemNameField;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> locationCombo;
    private JTextField quantityField;
    private JTextField priceField;
    private JTextField extraInfoField;
    private JLabel extraInfoLabel;

    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    
    private JComboBox<String> roomFilterCombo;

    private JLabel totalItemsValueLbl;
    private JLabel totalValueValueLbl;
    private JLabel lowStockValueLbl;

    private Integer selectedId = null;

    private static final String[] CATEGORIES = {
            "Grocery", "Electronics", "Medicine", "Kitchen", "Clothing", "Furniture", "Others"
    };
    
    private static final String[] DEFAULT_ROOMS = {
        "Living Room", "Bedroom", "Kitchen", "Dining Room",
        "Bathroom", "Guest Room", "Study Room", "Store Room"
    };
    
    private static final int LOW_STOCK_THRESHOLD = 2;
    private static final double DEPRECIATION_RATE = 0.15;

   public MainDashboard(String username, String fullName, String homeName) {
        this.currentUsername = username;
        this.currentFullName = (fullName == null || fullName.isBlank()) ? username : fullName;
        this.currentHomeName = homeName;

        setTitle("Simple Home Inventory - Dashboard");
        java.net.URL iconURL = getClass().getResource("/homeinventory/pikachu.jpg");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            this.setIconImage(icon.getImage());
        }

        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        setLayout(new BorderLayout(10, 10));
        ensureDefaultRooms();
        add(buildHeaderPanel(), BorderLayout.NORTH);
        
        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        refreshTable();
        refreshKPIs();
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(218,218, 224));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel appTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        appTitlePanel.setOpaque(false);

        JLabel appIcon = new JLabel("\uD83C\uDFE0");
        appIcon.setFont(new Font("SansSerif", Font.PLAIN, 26));

        JLabel appTitle = new JLabel("Simple Home Inventory  —  " + currentHomeName);
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        appTitle.setForeground(new Color(60, 60, 60));

        appTitlePanel.add(appIcon);
        appTitlePanel.add(appTitle);

        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profilePanel.setOpaque(false);

        String initial = currentFullName.isEmpty() ? "?" : currentFullName.substring(0, 1).toUpperCase();
        JLabel avatarLbl = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(102, 126, 234));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLbl.setForeground(Color.WHITE);
        avatarLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        avatarLbl.setPreferredSize(new Dimension(36, 36));

        JLabel welcomeLbl = new JLabel(currentFullName);
        welcomeLbl.setFont(new Font("SansSerif", Font.BOLD, 15));

        JButton homeBtn = new JButton("🏠 Homes");
        homeBtn.setBackground(new Color(63, 122, 69));
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setFocusPainted(false);
        homeBtn.setOpaque(true);
        homeBtn.setContentAreaFilled(true);
        homeBtn.setBorderPainted(false);
        homeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        homeBtn.setToolTipText("Back to Home Selection");

        homeBtn.addActionListener(e -> goToHomeSelection());
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(178, 34, 34));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.addActionListener(e -> handleLogout());

        profilePanel.add(avatarLbl);
        profilePanel.add(welcomeLbl);
        profilePanel.add(homeBtn);
        profilePanel.add(logoutBtn);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.add(appTitlePanel, BorderLayout.WEST);
        topRow.add(profilePanel, BorderLayout.EAST);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        kpiPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        totalItemsValueLbl = new JLabel("0", SwingConstants.CENTER);
        totalValueValueLbl = new JLabel("0.00 Tk", SwingConstants.CENTER);
        lowStockValueLbl = new JLabel("0", SwingConstants.CENTER);

        kpiPanel.add(buildKpiCard("Total Items", totalItemsValueLbl, new Color(66, 133, 244), "\uD83D\uDCE6"));
        kpiPanel.add(buildKpiCard("Total Value", totalValueValueLbl, new Color(52, 168, 83), "\uD83D\uDDC4"));
        kpiPanel.add(buildKpiCard("Low Stock Alerts", lowStockValueLbl, new Color(219, 68, 55), "\uD83D\uDD14"));

        header.add(topRow, BorderLayout.NORTH);
        header.add(kpiPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildKpiCard(String title, JLabel valueLbl, Color color, String iconText) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel iconLbl = new JLabel(iconText);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 26));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(iconLbl, BorderLayout.EAST);

        valueLbl.setForeground(Color.WHITE);
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 22));

        card.add(topRow, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setPreferredSize(new Dimension(260, 0));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15),
                BorderFactory.createTitledBorder("Item Details")));

        itemNameField = new JTextField();
        categoryCombo = new JComboBox<>(CATEGORIES);
        locationCombo = new JComboBox<>(loadRooms().toArray(new String[0]));
        quantityField = new JTextField();
        
        priceField = new JTextField();
        extraInfoField = new JTextField();
        extraInfoLabel = new JLabel("Serial No. (optional)");

        categoryCombo.addActionListener(e -> updateExtraInfoLabel());

        form.add(labeled("Item Name", itemNameField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Category", categoryCombo));
        form.add(Box.createVerticalStrut(8));

        JButton addRoomBtn = new JButton("+");
        addRoomBtn.setMargin(new Insets(2, 6, 2, 6));
        addRoomBtn.setFocusPainted(false);
        addRoomBtn.addActionListener(e -> handleAddRoom());

        JButton deleteRoomBtn = new JButton("-");
        deleteRoomBtn.setMargin(new Insets(2, 6, 2, 6));
        deleteRoomBtn.setFocusPainted(false);
        deleteRoomBtn.addActionListener(e -> handleDeleteRoom());

        JPanel roomBtnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        roomBtnPanel.setOpaque(false);
        roomBtnPanel.add(addRoomBtn);
        roomBtnPanel.add(deleteRoomBtn);

        JPanel locationRow = new JPanel(new BorderLayout(4, 0));
        locationRow.setOpaque(false);
        locationRow.add(locationCombo, BorderLayout.CENTER);
        locationRow.add(roomBtnPanel, BorderLayout.EAST);

        form.add(labeledDynamic(new JLabel("Location"), locationRow));
        form.add(Box.createVerticalStrut(8));
        
        form.add(labeled("Quantity", quantityField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Unit Price", priceField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeledDynamic(extraInfoLabel, extraInfoField));
        form.add(Box.createVerticalStrut(20));

        JButton addBtn = coloredButton("Add Item", new Color(52, 168, 83));
        JButton updateBtn = coloredButton("Update Item", new Color(66, 133, 244));
        JButton deleteBtn = coloredButton("Delete Item", new Color(219, 68, 55));
        JButton detailsBtn = coloredButton("View Details", new Color(155, 89, 182));
        JButton clearBtn = coloredButton("Clear Form", new Color(120, 120, 120));

        addBtn.addActionListener(e -> handleAdd());
        updateBtn.addActionListener(e -> handleUpdate());
        deleteBtn.addActionListener(e -> handleDelete());
        detailsBtn.addActionListener(e -> handleViewDetails());
        clearBtn.addActionListener(e -> clearForm());

        form.add(addBtn);
        form.add(Box.createVerticalStrut(6));
        form.add(updateBtn);
        form.add(Box.createVerticalStrut(6));
        form.add(deleteBtn);
        form.add(Box.createVerticalStrut(6));
        form.add(detailsBtn);
        form.add(Box.createVerticalStrut(6));
        form.add(clearBtn);
        form.add(Box.createVerticalGlue());

        return form;
    }

    private JPanel labeled(String labelText, JComponent field) {
        return labeledDynamic(new JLabel(labelText), field);
    }

    private JPanel labeledDynamic(JLabel label, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.add(label);
        p.add(field);
        return p;
    }

    private JButton coloredButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return btn;
    }

    private void updateExtraInfoLabel() {
        String category = (String) categoryCombo.getSelectedItem();
        if ("Electronics".equals(category)) {
            extraInfoLabel.setText("Serial No.");
        } else if ("Furniture".equals(category)) {
            extraInfoLabel.setText("Dimensions");
        } else {
            extraInfoLabel.setText("Note (optional)");
        }
    }
    
    private List<String> loadRooms() {
        List<String> rooms = new ArrayList<>();
        List<String[]> data = FileHelper.readLines(FileHelper.ROOMS_FILE);
        for (String[] row : data) {
            if (row.length >= 4 && row[1].equals(currentUsername) && row[2].equals(currentHomeName)) {
                rooms.add(row[3]);
            }
        }
        return rooms;
    }

    private void ensureDefaultRooms() {
        if (!loadRooms().isEmpty()) return;
        for (String room : DEFAULT_ROOMS) {
            String[] row = {
                String.valueOf(FileHelper.getNextId(FileHelper.ROOMS_FILE)),
                currentUsername, currentHomeName, room
            };
            FileHelper.appendLine(FileHelper.ROOMS_FILE, row);
        }
    }

    private void refreshLocationCombo() {
        locationCombo.setModel(new DefaultComboBoxModel<>(loadRooms().toArray(new String[0])));
        refreshRoomFilterCombo();
    }

    private void refreshRoomFilterCombo() {
        if (roomFilterCombo == null) return;
        List<String> rooms = new ArrayList<>();
        rooms.add("All Rooms");
        rooms.addAll(loadRooms());
        roomFilterCombo.setModel(new DefaultComboBoxModel<>(rooms.toArray(new String[0])));
    }

    private void handleAddRoom() {
        String roomName = JOptionPane.showInputDialog(this, "Enter new room name:", "Add Room", JOptionPane.PLAIN_MESSAGE);
        if (roomName == null) return;
        roomName = roomName.trim();
        if (roomName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Room name cannot be empty.", "Invalid Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> rooms = FileHelper.readLines(FileHelper.ROOMS_FILE);
        for(String[] r : rooms) {
            if(r.length >= 4 && r[1].equals(currentUsername) && r[2].equals(currentHomeName) && r[3].equalsIgnoreCase(roomName)) {
                JOptionPane.showMessageDialog(this, "This room already exists.", "Duplicate Room", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String[] newRoom = {
            String.valueOf(FileHelper.getNextId(FileHelper.ROOMS_FILE)),
            currentUsername, currentHomeName, roomName
        };
        FileHelper.appendLine(FileHelper.ROOMS_FILE, newRoom);
        
        refreshLocationCombo();
        locationCombo.setSelectedItem(roomName);
    }

    private void handleDeleteRoom() {
        String selectedRoom = (String) locationCombo.getSelectedItem();
        if (selectedRoom == null) return;

        List<String[]> inv = FileHelper.readLines(FileHelper.INVENTORY_FILE);
        int count = 0;
        for (String[] item : inv) {
            if (item.length >= 9 && item[1].equals(currentUsername) && item[2].equals(currentHomeName) && item[5].equals(selectedRoom)) {
                count++;
            }
        }

        if (count > 0) {
            JOptionPane.showMessageDialog(this,
                    "Can't delete \"" + selectedRoom + "\" - it still has " + count + " item(s) in it.\n" +
                    "Move or delete those items first.",
                    "Room In Use", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete room \"" + selectedRoom + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        List<String[]> rooms = FileHelper.readLines(FileHelper.ROOMS_FILE);
        List<String[]> updated = new ArrayList<>();
        for (String[] r : rooms) {
            if (r.length >= 4 && r[1].equals(currentUsername) && r[2].equals(currentHomeName) && r[3].equals(selectedRoom)) continue;
            updated.add(r);
        }
        FileHelper.writeLines(FileHelper.ROOMS_FILE, updated);
        refreshLocationCombo();
    }

    private JPanel buildTablePanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 15));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search by item name or category...");
        
        List<String> filterRooms = new ArrayList<>();
        filterRooms.add("All Rooms");
        filterRooms.addAll(loadRooms());
        roomFilterCombo = new JComboBox<>(filterRooms.toArray(new String[0]));
        roomFilterCombo.addActionListener(e -> applyFilter());

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        JPanel rightFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightFilterPanel.add(new JLabel("Filter Room:"));
        rightFilterPanel.add(roomFilterCombo);
        
        searchPanel.add(rightFilterPanel, BorderLayout.EAST);

        String[] columns = {"ID", "Item Name", "Category", "Location", "Quantity", "Unit Price", "Total Value"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelectedRow();
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        wrapper.add(searchPanel, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrapper;
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        String selectedRoom = (String) roomFilterCombo.getSelectedItem();
        
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
        }
        if (selectedRoom != null && !"All Rooms".equals(selectedRoom)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(selectedRoom) + "$", 3));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void handleAdd() {
        FormData data = readForm();
        if (data == null) return;

        String[] item = {
            String.valueOf(FileHelper.getNextId(FileHelper.INVENTORY_FILE)),
            currentUsername, currentHomeName, data.itemName, data.category, data.location,
            String.valueOf(data.quantity), String.valueOf(data.unitPrice), data.extraInfo
        };
        FileHelper.appendLine(FileHelper.INVENTORY_FILE, item);

        clearForm();
        refreshTable();
        refreshKPIs();
    }

    private void handleUpdate() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a row in the table first.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FormData data = readForm();
        if (data == null) return;

        List<String[]> inv = FileHelper.readLines(FileHelper.INVENTORY_FILE);
        for (int i = 0; i < inv.size(); i++) {
            String[] row = inv.get(i);
            if (row.length >= 9 && row[0].equals(String.valueOf(selectedId)) && row[1].equals(currentUsername) && row[2].equals(currentHomeName)) {
                row[3] = data.itemName;
                row[4] = data.category;
                row[5] = data.location;
                row[6] = String.valueOf(data.quantity);
                row[7] = String.valueOf(data.unitPrice);
                row[8] = data.extraInfo;
                inv.set(i, row);
                break;
            }
        }
        FileHelper.writeLines(FileHelper.INVENTORY_FILE, inv);

        clearForm();
        refreshTable();
        refreshKPIs();
    }

    private void handleDelete() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a row in the table first.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        List<String[]> inv = FileHelper.readLines(FileHelper.INVENTORY_FILE);
        List<String[]> updated = new ArrayList<>();
        for (String[] row : inv) {
            if (row.length >= 9 && row[0].equals(String.valueOf(selectedId)) && row[1].equals(currentUsername) && row[2].equals(currentHomeName)) continue;
            updated.add(row);
        }
        FileHelper.writeLines(FileHelper.INVENTORY_FILE, updated);
        
        clearForm();
        refreshTable();
        refreshKPIs();
    }

    private void handleViewDetails() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a row in the table first.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FormData data = readForm();
        if (data == null) return;

        InventoryItem item = createItem(selectedId, data);
        String message = item.generateDescription()
                + String.format("%n%nEstimated Current Value (after %.0f%% depreciation): %.2f Tk",
                DEPRECIATION_RATE * 100, item.getCurrentValue(DEPRECIATION_RATE));

        JOptionPane.showMessageDialog(this, message, "Item Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private InventoryItem createItem(int id, FormData data) {
        switch (data.category) {
            case "Electronics": return new ElectronicItem(id, data.itemName, data.category, data.location, data.quantity, data.unitPrice, data.extraInfo);
            case "Furniture": return new FurnitureItem(id, data.itemName, data.category, data.location, data.quantity, data.unitPrice, data.extraInfo);
            default: return new GeneralItem(id, data.itemName, data.category, data.location, data.quantity, data.unitPrice, data.extraInfo);
        }
    }

    private void goToHomeSelection() {
        SwingUtilities.invokeLater(() -> {
            HomeSelectionFrame homeFrame = new HomeSelectionFrame(currentUsername, currentFullName);
            homeFrame.setVisible(true);
            dispose();
        });
    }

    private void handleLogout() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    private static class FormData {
        String itemName, category, location, extraInfo;
        int quantity;
        double unitPrice;
    }

    private FormData readForm() {
        String itemName = itemNameField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String location = (String) locationCombo.getSelectedItem();
        String extraInfo = extraInfoField.getText().trim();
        String qtyText = quantityField.getText().trim();
        String priceText = priceField.getText().trim();

        if (itemName.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name, Quantity and Unit Price are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int quantity; double unitPrice;
        try {
            quantity = Integer.parseInt(qtyText);
            if (quantity < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a whole number (0 or more).", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            unitPrice = Double.parseDouble(priceText);
            if (unitPrice < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Unit Price must be a number (0 or more).", "Invalid Price", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        FormData data = new FormData();
        data.itemName = itemName; data.category = category; data.location = location; data.extraInfo = extraInfo;
        data.quantity = quantity; data.unitPrice = unitPrice;
        return data;
    }

    private void clearForm() {
        selectedId = null;
        itemNameField.setText(""); quantityField.setText(""); priceField.setText(""); extraInfoField.setText("");
        categoryCombo.setSelectedIndex(0); locationCombo.setSelectedIndex(0);
        table.clearSelection();
        updateExtraInfoLabel();
    }

    private void fillFormFromSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);

        selectedId = (Integer) tableModel.getValueAt(modelRow, 0);
        itemNameField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
        categoryCombo.setSelectedItem(tableModel.getValueAt(modelRow, 2));
        locationCombo.setSelectedItem(tableModel.getValueAt(modelRow, 3));
        quantityField.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
        priceField.setText(String.valueOf(tableModel.getValueAt(modelRow, 5)));
        extraInfoField.setText(fetchExtraInfo(selectedId));
        updateExtraInfoLabel();
    }

    private String fetchExtraInfo(int id) {
        List<String[]> inv = FileHelper.readLines(FileHelper.INVENTORY_FILE);
        for (String[] row : inv) {
            if (row.length >= 9 && row[0].equals(String.valueOf(id))) {
                return row[8];
            }
        }
        return "";
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<String[]> inv = FileHelper.readLines(FileHelper.INVENTORY_FILE);
        
        // Reverse iterate to show newest first, like "ORDER BY id DESC"
        for (int i = inv.size() - 1; i >= 0; i--) {
            String[] row = inv.get(i);
            if (row.length >= 9 && row[1].equals(currentUsername) && row[2].equals(currentHomeName)) {
                int id = Integer.parseInt(row[0]);
                String itemName = row[3];
                String category = row[4];
                String location = row[5];
                int quantity = Integer.parseInt(row[6]);
                double unitPrice = Double.parseDouble(row[7]);
                double totalValue = quantity * unitPrice;

                tableModel.addRow(new Object[]{
                        id, itemName, category, location, quantity,
                        String.format("%.2f", unitPrice), String.format("%.2f", totalValue)
                });
            }
        }
    }

    private void refreshKPIs() {
        List<String[]> inv = FileHelper.readLines(FileHelper.INVENTORY_FILE);
        int totalItems = 0;
        double totalValue = 0;
        int lowStockCount = 0;

        for (String[] row : inv) {
            if (row.length >= 9 && row[1].equals(currentUsername) && row[2].equals(currentHomeName)) {
                int qty = Integer.parseInt(row[6]);
                double price = Double.parseDouble(row[7]);
                totalItems += qty;
                totalValue += qty * price;
                if (qty < LOW_STOCK_THRESHOLD) lowStockCount++;
            }
        }

        totalItemsValueLbl.setText(String.valueOf(totalItems));
        totalValueValueLbl.setText(String.format("%.2f Tk", totalValue));
        lowStockValueLbl.setText(String.valueOf(lowStockCount));
    }
}
