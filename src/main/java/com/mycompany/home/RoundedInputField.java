package com.mycompany.home;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RoundedInputField extends JPanel {
    private JTextField textField;
    private JPasswordField passwordField;
    private final Color bgColor;

    public RoundedInputField(String iconText, Color bgColor, boolean isPassword) {
        this.bgColor = bgColor;
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel iconLbl = new JLabel(iconText, SwingConstants.CENTER);
        iconLbl.setPreferredSize(new Dimension(40, 40));
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        add(iconLbl, BorderLayout.WEST);

        if (isPassword) {
            passwordField = new JPasswordField();
            passwordField.setBorder(new EmptyBorder(5, 5, 5, 10));
            passwordField.setOpaque(false);
            passwordField.setBackground(new Color(0, 0, 0, 0));
            add(passwordField, BorderLayout.CENTER);
        } else {
            textField = new JTextField();
            textField.setBorder(new EmptyBorder(5, 5, 5, 10));
            textField.setOpaque(false);
            textField.setBackground(new Color(0, 0, 0, 0));
            add(textField, BorderLayout.CENTER);
        }
    }

    public void setEastComponent(Component c) {
        add(c, BorderLayout.EAST);
    }

    public JTextField getTextField() {
        return textField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.dispose();
        super.paintComponent(g);
    }
}