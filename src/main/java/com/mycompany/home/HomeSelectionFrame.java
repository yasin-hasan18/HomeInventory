package com.mycompany.home;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

public class HomeSelectionFrame extends JFrame {

    private final String currentUsername;
    private final String currentFullName;
    private JPanel cardsPanel;

    private static final Color[] CARD_COLORS = {
            new Color(66, 133, 244), new Color(52, 168, 83),
            new Color(155, 89, 182), new Color(230, 126, 34), new Color(22, 160, 133)
    };

    public HomeSelectionFrame(String username, String fullName) {
        this.currentUsername = username;
        this.currentFullName = (fullName == null || fullName.isBlank()) ? username : fullName;

        setTitle("Simple Home Inventory - Select Home");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        setContentPane(mainPanel);

        JLabel title = new JLabel("Which home would you like to manage?", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(46, 90, 55));
        mainPanel.add(title, BorderLayout.NORTH);

        cardsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        cardsPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        ensureAtLeastOneHome();
        refreshCards();
    }

    private void refreshCards() {
        cardsPanel.removeAll();
        List<String> homes = loadHomes();
        for (int i = 0; i < homes.size(); i++) {
            cardsPanel.add(buildHomeCard(homes.get(i), CARD_COLORS[i % CARD_COLORS.length]));
        }
        cardsPanel.add(buildAddHomeCard());
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel buildHomeCard(String homeName, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton deleteBtn = new JButton("\u2715");
        deleteBtn.setMargin(new Insets(2, 6, 2, 6));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBackground(Color.WHITE);
        deleteBtn.addActionListener(e -> handleDeleteHome(homeName));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(deleteBtn, BorderLayout.EAST);

        JLabel icon = new JLabel("\uD83C\uDFE0", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 50));

        JLabel nameLbl = new JLabel(homeName, SwingConstants.CENTER);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        nameLbl.setForeground(Color.WHITE);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(icon);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(nameLbl);
        centerPanel.add(Box.createVerticalGlue());

        card.add(topRow, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);

        centerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openDashboard(homeName);
            }
        });

        return card;
    }

    private JPanel buildAddHomeCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(230, 230, 230));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel plus = new JLabel("+ Add New Home", SwingConstants.CENTER);
        plus.setFont(new Font("SansSerif", Font.BOLD, 16));
        plus.setForeground(new Color(90, 90, 90));

        card.add(plus, BorderLayout.CENTER);
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleAddHome();
            }
        });
        return card;
    }

    private List<String> loadHomes() {
        List<String> homes = new ArrayList<>();
        List<String[]> homesData = FileHelper.readLines(FileHelper.HOMES_FILE);
        for (String[] h : homesData) {
            if (h.length >= 3 && h[1].equals(currentUsername)) {
                homes.add(h[2]);
            }
        }
        return homes;
    }

    private void ensureAtLeastOneHome() {
        if (!loadHomes().isEmpty()) return;
        String[] defaultHome = {
                String.valueOf(FileHelper.getNextId(FileHelper.HOMES_FILE)),
                currentUsername, "My Home"
        };
        FileHelper.appendLine(FileHelper.HOMES_FILE, defaultHome);
    }

    private void handleAddHome() {
        String homeName = JOptionPane.showInputDialog(this, "Enter new home name:", "Add Home", JOptionPane.PLAIN_MESSAGE);
        if (homeName == null) return;
        homeName = homeName.trim();
        if (homeName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Home name cannot be empty.", "Invalid Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> homesData = FileHelper.readLines(FileHelper.HOMES_FILE);
        for (String[] h : homesData) {
            if (h.length >= 3 && h[1].equals(currentUsername) && h[2].equalsIgnoreCase(homeName)) {
                JOptionPane.showMessageDialog(this, "This home already exists.", "Duplicate Home", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String[] newHome = {
                String.valueOf(FileHelper.getNextId(FileHelper.HOMES_FILE)),
                currentUsername, homeName
        };
        FileHelper.appendLine(FileHelper.HOMES_FILE, newHome);
        refreshCards();
    }

    private void handleDeleteHome(String homeName) {
        if (loadHomes().size() <= 1) {
            JOptionPane.showMessageDialog(this, "You must keep at least one home.", "Can't Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> inventoryData = FileHelper.readLines(FileHelper.INVENTORY_FILE);
        int itemCount = 0;
        for (String[] item : inventoryData) {
            if (item.length >= 9 && item[1].equals(currentUsername) && item[2].equals(homeName)) {
                itemCount++;
            }
        }

        if (itemCount > 0) {
            JOptionPane.showMessageDialog(this,
                    "Can't delete \"" + homeName + "\" - it still has " + itemCount + " item(s) in it.\nDelete those items first.",
                    "Home In Use", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete home \"" + homeName + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        List<String[]> homesData = FileHelper.readLines(FileHelper.HOMES_FILE);
        List<String[]> updatedHomes = new ArrayList<>();
        for (String[] h : homesData) {
            if (h.length >= 3 && h[1].equals(currentUsername) && h[2].equals(homeName)) continue;
            updatedHomes.add(h);
        }
        FileHelper.writeLines(FileHelper.HOMES_FILE, updatedHomes);
        refreshCards();
    }

    private void openDashboard(String homeName) {
        new MainDashboard(currentUsername, currentFullName, homeName).setVisible(true);
        dispose();
    }
}
