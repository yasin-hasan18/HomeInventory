package com.mycompany.home;

import java.awt.*;
import javax.swing.*;

public class RoundedButton extends JButton {
    private final Color bgColor;

    public RoundedButton(String text, Color bgColor, Color fgColor) {
        super(text);
        this.bgColor = bgColor;
        setForeground(fgColor);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFont(new Font("SansSerif", Font.BOLD, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.dispose();
        super.paintComponent(g);
    }
}