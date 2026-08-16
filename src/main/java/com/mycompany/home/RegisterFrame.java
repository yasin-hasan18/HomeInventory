package com.mycompany.home;

import java.awt.*;
import java.util.List;
import javax.swing.*;

public class RegisterFrame extends JFrame {

    private final JTextField fullNameField;
    private final JTextField usernameField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;

    private final JPanel mainPanel;
    private final WaveHeaderPanel waveHeader;
    private final JLabel title;
    private final JLabel subtitle;
    private final RoundedButton registerBtn;
    private final JButton backToLoginBtn;

    private final JLabel[] fieldLabels = new JLabel[5];
    private final RoundedInputField[] fieldInputs = new RoundedInputField[5];

    private static final String[] LABEL_TEXT = {
            "Full Name", "Username", "Email", "Password", "Confirm Password"
    };
    private static final String[] ICONS = {
            "\uD83D\uDC64", "\uD83D\uDC64", "\u2709", "\uD83D\uDD12", "\uD83D\uDD12"
    };
    private static final boolean[] IS_PASSWORD = {
            false, false, false, true, true
    };

    public RegisterFrame() {
        setTitle("Home Inventory - Create Account");
        setSize(800, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        
        java.net.URL iconURL = getClass().getResource("/com/mycompany/home/pikachu.jpg");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            this.setIconImage(icon.getImage());
        }

        mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        waveHeader = new WaveHeaderPanel("/com/mycompany/home/login_bg.png");
        mainPanel.add(waveHeader);

        title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 40));
        title.setForeground(new Color(46, 90, 55));
        mainPanel.add(title);

        subtitle = new JLabel("Sign up to get started", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);
        mainPanel.add(subtitle);

        for (int i = 0; i < LABEL_TEXT.length; i++) {
            JLabel lbl = new JLabel(LABEL_TEXT[i]);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            lbl.setForeground(new Color(70, 100, 75));
            mainPanel.add(lbl);
            fieldLabels[i] = lbl;

            RoundedInputField input = new RoundedInputField(ICONS[i], new Color(219, 232, 217), IS_PASSWORD[i]);

            if (IS_PASSWORD[i]) {
                JButton toggleEye = new JButton("\uD83D\uDC41");
                toggleEye.setBorderPainted(false);
                toggleEye.setContentAreaFilled(false);
                toggleEye.setFocusPainted(false);
                input.setEastComponent(toggleEye);
                JPasswordField pf = input.getPasswordField();
                pf.setEchoChar('\u2022');
                toggleEye.addActionListener(e -> {
                    if (pf.getEchoChar() == 0) {
                        pf.setEchoChar('\u2022');
                    } else {
                        pf.setEchoChar((char) 0);
                    }
                });
            }

            mainPanel.add(input);
            fieldInputs[i] = input;
        }

        fullNameField = fieldInputs[0].getTextField();
        usernameField = fieldInputs[1].getTextField();
        emailField = fieldInputs[2].getTextField();
        passwordField = fieldInputs[3].getPasswordField();
        confirmPasswordField = fieldInputs[4].getPasswordField();

        registerBtn = new RoundedButton("Register", new Color(63, 122, 69), Color.WHITE);
        mainPanel.add(registerBtn);

        backToLoginBtn = new JButton("Already have an account? Login");
        backToLoginBtn.setBorderPainted(false);
        backToLoginBtn.setContentAreaFilled(false);
        backToLoginBtn.setForeground(new Color(33, 82, 148));
        backToLoginBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        mainPanel.add(backToLoginBtn);

        registerBtn.addActionListener(e -> handleRegister());
        backToLoginBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        mainPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents();
            }
        });

        layoutComponents();
    }

    private void layoutComponents() {
        int w = mainPanel.getWidth();
        int h = mainPanel.getHeight();
        if (w == 0 || h == 0) return;

        int fieldWidth = (int) (w * 0.87);
        int fieldX = (w - fieldWidth) / 2;

        waveHeader.setBounds(0, 0, w, (int) (h * 0.26));
        title.setBounds(fieldX, (int) (h * 0.285), fieldWidth, (int) (h * 0.055));
        subtitle.setBounds(fieldX, (int) (h * 0.36), fieldWidth, (int) (h * 0.03));

        double rowStart = 0.385;
        double rowHeight = 0.085;

        for (int i = 0; i < fieldLabels.length; i++) {
            double y = rowStart + (i * rowHeight);
            fieldLabels[i].setBounds(fieldX + 5, (int) (h * y), fieldWidth, (int) (h * 0.022));
            fieldInputs[i].setBounds(fieldX, (int) (h * (y + 0.025)), fieldWidth, (int) (h * 0.055));
        }

        double afterFields = rowStart + (fieldLabels.length * rowHeight) + 0.02;
        registerBtn.setBounds(fieldX, (int) (h * afterFields), fieldWidth, (int) (h * 0.065));
        backToLoginBtn.setBounds(fieldX, (int) (h * (afterFields + 0.09)), fieldWidth, (int) (h * 0.035));
    }

    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> users = FileHelper.readLines(FileHelper.USERS_FILE);
        for (String[] u : users) {
            if (u.length >= 3 && u[2].equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "This username is already taken.", "Username Not Available", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String[] newUser = {
                String.valueOf(FileHelper.getNextId(FileHelper.USERS_FILE)),
                fullName, username, email, password
        };
        FileHelper.appendLine(FileHelper.USERS_FILE, newUser);

        JOptionPane.showMessageDialog(this, "Account created successfully! Please log in.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        new LoginFrame().setVisible(true);
        dispose();
    }
}
