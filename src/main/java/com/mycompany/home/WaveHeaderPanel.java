package com.mycompany.home;

import java.awt.*;
import javax.swing.*;
import java.net.URL;

public class WaveHeaderPanel extends JPanel {
    private Image bgImage;

    public WaveHeaderPanel(String imagePath) {
        setOpaque(false);
        URL url = getClass().getResource(imagePath);
        if (url != null) {
            bgImage = new ImageIcon(url).getImage();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            
            g.setColor(new Color(219, 232, 217)); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}