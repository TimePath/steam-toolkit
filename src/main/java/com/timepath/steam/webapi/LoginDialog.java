package com.timepath.steam.webapi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author TimePath
 */
public abstract class LoginDialog extends JDialog {

    private JTextField captchaInput;
    private JLabel captchaLabel;
    private JButton loginButton;
    private JLabel messageLabel;
    private JPasswordField passInput;
    private JTextField steamguardInput;
    private JTextField userInput;

    /**
     * Creates new form LoginDialog
     *
     * @param parent
     * @param modal
     */
    protected LoginDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {
        /*
         * Create and display the dialog
         */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginDialog dialog = new LoginDialog(new JFrame(), true) {
                    @Override
                    public void login() {
                    }
                };
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setVisible(true);
            }
        });
    }

    private void initComponents() {
        JPanel jPanel6 = new JPanel();
        JPanel jPanel7 = new JPanel();
        JPanel jPanel4 = new JPanel();
        userInput = new JTextField();
        passInput = new JPasswordField();
        JPanel jPanel1 = new JPanel();
        JLabel jLabel1 = new JLabel();
        steamguardInput = new JTextField();
        JPanel captchaPanel = new JPanel();
        captchaLabel = new JLabel();
        captchaInput = new JTextField();
        messageLabel = new JLabel();
        loginButton = new JButton();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jPanel6.setLayout(new BorderLayout());
        jPanel7.setLayout(new BorderLayout());
        jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.LINE_AXIS));
        userInput.setText("user");
        userInput.setMinimumSize(new Dimension(120, 28));
        jPanel4.add(userInput);
        passInput.setText("pass");
        passInput.setMinimumSize(new Dimension(120, 28));
        passInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                login();
            }
        });
        jPanel4.add(passInput);
        jPanel7.add(jPanel4, BorderLayout.NORTH);
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.LINE_AXIS));
        jLabel1.setText("steamguard");
        jPanel1.add(jLabel1);
        steamguardInput.setMinimumSize(new Dimension(120, 28));
        jPanel1.add(steamguardInput);
        jPanel7.add(jPanel1, BorderLayout.CENTER);
        captchaPanel.setLayout(new BoxLayout(captchaPanel, BoxLayout.LINE_AXIS));
        captchaLabel.setText("captcha");
        captchaPanel.add(captchaLabel);
        captchaInput.setMinimumSize(new Dimension(120, 28));
        captchaPanel.add(captchaInput);
        jPanel7.add(captchaPanel, BorderLayout.SOUTH);
        jPanel6.add(jPanel7, BorderLayout.PAGE_START);
        messageLabel.setText("message");
        jPanel6.add(messageLabel, BorderLayout.CENTER);
        loginButton.setText("login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                login();
            }
        });
        jPanel6.add(loginButton, BorderLayout.SOUTH);
        getContentPane().add(jPanel6, BorderLayout.CENTER);
        pack();
    }

    public abstract void login();

    /**
     * @return the captchaInput
     */
    public JTextField getCaptchaInput() {
        return captchaInput;
    }

    /**
     * @return the captchaLabel
     */
    public JLabel getCaptchaLabel() {
        return captchaLabel;
    }

    /**
     * @return the loginButton
     */
    public JButton getLoginButton() {
        return loginButton;
    }

    /**
     * @return the messageLabel
     */
    public JLabel getMessageLabel() {
        return messageLabel;
    }

    /**
     * @return the passInput
     */
    public JPasswordField getPassInput() {
        return passInput;
    }

    /**
     * @return the steamguardInput
     */
    public JTextField getSteamguardInput() {
        return steamguardInput;
    }

    /**
     * @return the userInput
     */
    public JTextField getUserInput() {
        return userInput;
    }
    // End of variables declaration//GEN-END:variables
}
