package com.mycompany.home;

import java.awt.*;
import java.util.List;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private JPasswordField passwordField;
    private final JCheckBox rememberMeCheck;
    private final JPanel mainPanel;
    private final WaveHeaderPanel waveHeader;
    private final JLabel title;
    private final JLabel subtitle;
    private final JLabel userLabel;
    private final JLabel passLabel;
    private final RoundedInputField userInput;
    private final RoundedInputField passInput;
    private final RoundedButton loginBtn;
    private final JButton goToRegisterBtn;
    
    private final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LoginFrame.class);
    private static final String PREF_USERNAME = "rememberedUsername";
    private static final String PREF_REMEMBER = "rememberMeEnabled";

    public LoginFrame() {
        setTitle("Home Inventory - Login");
        
        setSize(800, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        title = new JLabel("Welcome", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 46));
        title.setForeground(new Color(46, 90, 55));
        mainPanel.add(title);

        subtitle = new JLabel("Login to your account", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitle.setForeground(Color.GRAY);
        mainPanel.add(subtitle);
        
        userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        userLabel.setForeground(new Color(70, 100, 75));
        mainPanel.add(userLabel);

        userInput = new RoundedInputField("\uD83D\uDC64", new Color(219, 232, 217), false);
        mainPanel.add(userInput);
        usernameField = userInput.getTextField();
        
        passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        passLabel.setForeground(new Color(70, 100, 75));
        mainPanel.add(passLabel);

        passInput = new RoundedInputField("\uD83D\uDD12", new Color(219, 232, 217), true);

        JButton toggleEye = new JButton("\uD83D\uDC41");
        toggleEye.setBorderPainted(false);
        toggleEye.setContentAreaFilled(false);
        toggleEye.setFocusPainted(false);
        passInput.setEastComponent(toggleEye);

        mainPanel.add(passInput);
        passwordField = passInput.getPasswordField();
        passwordField.setEchoChar('\u2022');

        toggleEye.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                passwordField.setEchoChar('\u2022');
            } else {
                passwordField.setEchoChar((char) 0);
            }
        });

        rememberMeCheck = new JCheckBox("Remember Me");
        rememberMeCheck.setForeground(new Color(80, 80, 80));
        rememberMeCheck.setFont(new Font("SansSerif", Font.PLAIN, 16));
        rememberMeCheck.setOpaque(false);
        mainPanel.add(rememberMeCheck);

        loginBtn = new RoundedButton("Login", new Color(63, 122, 69), Color.WHITE);
        mainPanel.add(loginBtn);

        goToRegisterBtn = new JButton("Don't have an account? Register");
        goToRegisterBtn.setBorderPainted(false);
        goToRegisterBtn.setContentAreaFilled(false);
        goToRegisterBtn.setForeground(new Color(33, 82, 148));
        goToRegisterBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        mainPanel.add(goToRegisterBtn);

        loginBtn.addActionListener(e -> handleLogin());
        goToRegisterBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        passwordField.addActionListener(e -> handleLogin());

        mainPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents();
            }
        });

        layoutComponents();
        loadRememberedUsername();
    }

    private void layoutComponents() {
    int w = mainPanel.getWidth();
    int h = mainPanel.getHeight();
    if (w == 0 || h == 0) return;

    int fieldWidth = (int) (w * 0.87);
    int fieldX = (w - fieldWidth) / 2;

    waveHeader.setBounds(0, 0, w, (int) (h * 0.38));
    title.setBounds(fieldX, (int) (h * 0.40), fieldWidth, (int) (h * 0.06));
    subtitle.setBounds(fieldX, (int) (h * 0.465), fieldWidth, (int) (h * 0.04));

    userLabel.setBounds(fieldX + 5, (int) (h * 0.515), fieldWidth, (int) (h * 0.03));
    userInput.setBounds(fieldX, (int) (h * 0.545), fieldWidth, (int) (h * 0.07));

    passLabel.setBounds(fieldX + 5, (int) (h * 0.625), fieldWidth, (int) (h * 0.03));
    passInput.setBounds(fieldX, (int) (h * 0.655), fieldWidth, (int) (h * 0.07));

    rememberMeCheck.setBounds(fieldX, (int) (h * 0.735), (int) (fieldWidth * 0.55), (int) (h * 0.035));
    loginBtn.setBounds(fieldX, (int) (h * 0.79), fieldWidth, (int) (h * 0.07));
    goToRegisterBtn.setBounds(fieldX, (int) (h * 0.885), fieldWidth, (int) (h * 0.04));
}

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> users = FileHelper.readLines(FileHelper.USERS_FILE);
        boolean found = false;
        String fullName = "";

        for (String[] u : users) {
            if (u.length >= 5 && u[2].equals(username) && u[4].equals(password)) {
                found = true;
                fullName = u[1];
                break;
            }
        }

        if (found) {
            saveRememberedUsername(username);
            new HomeSelectionFrame(username, fullName).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadRememberedUsername() {
        boolean remembered = prefs.getBoolean(PREF_REMEMBER, false);
        if (remembered) {
            usernameField.setText(prefs.get(PREF_USERNAME, ""));
            rememberMeCheck.setSelected(true);
        }
    }

    private void saveRememberedUsername(String username) {
        if (rememberMeCheck.isSelected()) {
            prefs.putBoolean(PREF_REMEMBER, true);
            prefs.put(PREF_USERNAME, username);
        } else {
            prefs.putBoolean(PREF_REMEMBER, false);
            prefs.remove(PREF_USERNAME);
        }
    }

    public static void main(String[] args) {
        FileHelper.initializeFiles();

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}