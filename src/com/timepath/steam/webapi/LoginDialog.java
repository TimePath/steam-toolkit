package com.timepath.steam.webapi;

/**
 *
 * @author timepath
 */
public abstract class LoginDialog extends javax.swing.JDialog {

    /**
     * Creates new form LoginDialog
     */
    public LoginDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * @return the captchaInput
     */
    public javax.swing.JTextField getCaptchaInput() {
        return captchaInput;
    }

    /**
     * @return the captchaLabel
     */
    public javax.swing.JLabel getCaptchaLabel() {
        return captchaLabel;
    }

    /**
     * @return the loginButton
     */
    public javax.swing.JButton getLoginButton() {
        return loginButton;
    }

    /**
     * @return the messageLabel
     */
    public javax.swing.JLabel getMessageLabel() {
        return messageLabel;
    }

    /**
     * @return the passInput
     */
    public javax.swing.JPasswordField getPassInput() {
        return passInput;
    }

    /**
     * @return the steamguardInput
     */
    public javax.swing.JTextField getSteamguardInput() {
        return steamguardInput;
    }

    /**
     * @return the userInput
     */
    public javax.swing.JTextField getUserInput() {
        return userInput;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        userInput = new javax.swing.JTextField();
        passInput = new javax.swing.JPasswordField();
        loginButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        steamguardInput = new javax.swing.JTextField();
        captchaPanel = new javax.swing.JPanel();
        captchaLabel = new javax.swing.JLabel();
        captchaInput = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.FlowLayout());

        userInput.setText("user");
        userInput.setMinimumSize(new java.awt.Dimension(120, 28));
        jPanel1.add(userInput);

        passInput.setText("pass");
        passInput.setMinimumSize(new java.awt.Dimension(120, 28));
        passInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                login(evt);
            }
        });
        jPanel1.add(passInput);

        loginButton.setText("login");
        jPanel1.add(loginButton);

        messageLabel.setText("message");
        jPanel1.add(messageLabel);

        getContentPane().add(jPanel1);

        steamguardInput.setMinimumSize(new java.awt.Dimension(120, 28));
        getContentPane().add(steamguardInput);

        captchaLabel.setText("captcha");
        captchaPanel.add(captchaLabel);

        captchaInput.setMinimumSize(new java.awt.Dimension(120, 28));
        captchaPanel.add(captchaInput);

        getContentPane().add(captchaPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void login(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_login
        login();
    }//GEN-LAST:event_login

    public abstract void login();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and
         * feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for(javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch(ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch(InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch(IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        } catch(javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginDialog.class.getName()).log(
                    java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                LoginDialog dialog = new LoginDialog(new javax.swing.JFrame(), true) {

                    @Override
                    public void login() {
                        
                    }
                    
                };
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField captchaInput;
    private javax.swing.JLabel captchaLabel;
    private javax.swing.JPanel captchaPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPasswordField passInput;
    private javax.swing.JTextField steamguardInput;
    private javax.swing.JTextField userInput;
    // End of variables declaration//GEN-END:variables
}
