import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

//@SuppressWarnings("unused")
public class Client {
    public static Client client = null;
    private static JFrame frame = null;
    private static Console console = null;
    private JPanel panel1;
    private JPanel panelLogin;
    private JPanel paneLogin;
    private JPanel paneChat;
    private JPanel paneRegister;
    private JButton btnSend;
    private JButton btnRegister;
    private JButton btnLogin;
    private JButton btnSearch;
    private JPasswordField pwFieldRegister;
    private JPasswordField pwFieldUserPassword;
    private JTextField txtFieldRegisterUser;
    private JTextField txtFieldRegisterEmail;
    public JTextField txtFieldChat;
    public JTextField txtFieldUser;
    public JTextField txtFieldServer;
    public JTextField txtFieldLogFile;
    public JTextField txtFieldSearchUser;
    private JLabel lblSttxt;
    private JLabel lblLogFile;
    private JLabel lblServer;
    private JLabel lblPassword;
    private JLabel lblUser;
    private JLabel lblPort;
    private JLabel lblStatus;
    private JLabel lblMailUser;
    private JLabel lblPAssword;
    private JLabel lblUserMail;
    public JList listUsers;
    public JSpinner spinner1;
    private JCheckBox checkBox1;
    private JProgressBar progressBar1;
    public JTextPane txtPaneMessages;
    private JTabbedPane tabbedPane1;
    public JScrollPane scrollChat;
    private JScrollPane scrollUsers;
    // Dimensions
    private Dimension
            currentSize = new Dimension(300,330),
            sizeLogin = new Dimension(300, 330),
            sizeRegister = new Dimension(300, 360),
            sizeChat = new Dimension(750, 400);
    private static int
            loadingMax,
            loadingStatus = 0,
            loadingAddition = 1;

    public static void main(String[] args) throws IOException {
        // Set the UI to look like Windows
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        // Init our Frame
        frame = new JFrame("Client");
        frame.setContentPane(new Client().panel1);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        URL iconURL = Client.class.getResource("sim.png");
        frame.setIconImage(new ImageIcon(iconURL).getImage());
        frame.pack();
        frame.setVisible(true);
        // Ensure we have a console object
        console = new Console(args);
    }

    private Client() {
        // Declare our client and frame
        client = this;
        // Center frame
        centerFrame();
        // Action listeners
        setupActionListeners();
        // Autoscroll
        DefaultCaret caret = (DefaultCaret)txtPaneMessages.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void setupActionListeners() {
        // Login Form
        txtFieldUser.addActionListener(e -> pwFieldUserPassword.requestFocus());
        pwFieldUserPassword.addActionListener(e -> login());
        btnLogin.addActionListener(e -> login());
        // Register Form
        txtFieldRegisterUser.addActionListener(e -> txtFieldRegisterEmail.requestFocus());
        txtFieldRegisterEmail.addActionListener(e -> pwFieldRegister.requestFocus());
        pwFieldRegister.addCaretListener(e -> checkCheckBox());
        checkBox1.addActionListener(e -> checkRegisterButton());
        btnRegister.addActionListener(e -> register());
        // Chat Form
        txtFieldChat.addActionListener(e -> sendChat());
        btnSend.addActionListener(e -> sendChat());
        listUsers.addListSelectionListener(e -> changeChat());
        txtFieldSearchUser.addCaretListener(e -> searchUser());
    }

    private void searchUser() {
        
    }

    private void changeChat() {
        MessageList ml = (MessageList) listUsers.getSelectedValue();
        txtPaneMessages.setText("");
        txtPaneMessages.setText(ml.formMessages());
    }

    private void sendChat() {
        //MessageList.addLocal(txtFieldChat.getText());
        Communication.sendMessage(MessageList.getCurrentUser(), txtFieldChat.getText());
        txtFieldChat.setText("");
    }

    private void checkRegisterButton() {
        if(checkBox1.isSelected()) btnRegister.setEnabled(true);
        else btnRegister.setEnabled(false);
    }

    private void checkCheckBox() {
        if(pwFieldRegister.getPassword().length >= 4) checkBox1.setEnabled(true);
        else checkBox1.setEnabled(false);
    }

    private void register() {
        // Register with a new user.
        console.register(
                txtFieldRegisterUser.getText(),
                txtFieldRegisterEmail.getText(),
                pwFieldRegister.getPassword(),
                txtFieldServer.getText(),
                (Integer) spinner1.getValue());
        console.user = txtFieldRegisterUser.getText();
        console.userpass = Security.mda5(String.valueOf(txtFieldRegisterUser.getText()));
        console.host = txtFieldServer.getText();
        console.logfile = txtFieldLogFile.getText();
        console.port = ""+spinner1.getValue();
        console.login();
    }

    private void login() {
        // Set our console variables
        console.user = txtFieldUser.getText();
        console.userpass = Security.mda5(String.valueOf(pwFieldUserPassword.getPassword()));
        console.host = txtFieldServer.getText();
        console.logfile = txtFieldLogFile.getText();
        console.port = ""+spinner1.getValue();
        console.login();
    }

    public void enableChat() {
        tabbedPane1.setEnabledAt(2,true);
        tabbedPane1.setSelectedIndex(2);
        tabbedPane1.setEnabledAt(0, false);
        tabbedPane1.setEnabledAt(1, false);
        txtFieldChat.requestFocus();
        resize(sizeChat);
    }

    public void setStatus(String s, boolean error) {
        lblStatus.setText(s);
        if (error) {
            lblStatus.setForeground(Color.red);
            console.error(s, false);
        }
        else {
            lblStatus.setForeground(Color.black);
            console.log(s);
        }
        repack();
    }

    public void setLoading(int i) {
        loadingMax = i;
        loadingStatus = 0;
        progressBar1.setMaximum(loadingMax);
    }

    public void setLoadingAdd(int i) {
        loadingAddition = i;
    }

    public void addLoading() {
        loadingStatus = progressBar1.getValue() + loadingAddition;
        progressBar1.setValue(loadingStatus);
    }

    public void addLoading(int i) {
        loadingStatus = progressBar1.getValue() + i;
        progressBar1.setValue(loadingStatus);
    }

    /** Given dimension this will resize the current frame and center it */
    private void resize(Dimension d) {
        if (d == null) { Console.warn("Cannot set size not null."); return; }
        currentSize = d;
        frame.setPreferredSize(currentSize);
        panel1.setPreferredSize(currentSize);
        panelLogin.setPreferredSize(currentSize);
        tabbedPane1.setPreferredSize(currentSize);
        centerFrame();
    }

    /** This function can be used to center our frame */
    private void centerFrame() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(
                (dim.width / 2) - (frame.getSize().width * 2),
                (dim.height / 2) - (frame.getSize().height / 2));
        repack();
    }

    private void repack() {
        frame.pack();
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspect ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new CardLayout(0, 0));
        panel1.setPreferredSize(new Dimension(150, 200));
        panelLogin = new JPanel();
        panelLogin.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelLogin.setPreferredSize(new Dimension(120, 220));
        panel1.add(panelLogin, "Card1");
        tabbedPane1 = new JTabbedPane();
        panelLogin.add(tabbedPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        paneLogin = new JPanel();
        paneLogin.setLayout(new GridBagLayout());
        paneLogin.setPreferredSize(new Dimension(120, 220));
        tabbedPane1.addTab("Login", paneLogin);
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        paneLogin.add(spacer1, gbc);
        pwFieldUserPassword = new JPasswordField();
        pwFieldUserPassword.setPreferredSize(new Dimension(90, 25));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        paneLogin.add(pwFieldUserPassword, gbc);
        txtFieldUser = new JTextField();
        txtFieldUser.setPreferredSize(new Dimension(90, 24));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        paneLogin.add(txtFieldUser, gbc);
        lblUser = new JLabel();
        lblUser.setText("User/Email:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        paneLogin.add(lblUser, gbc);
        lblPassword = new JLabel();
        lblPassword.setText("Password");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        paneLogin.add(lblPassword, gbc);
        txtFieldServer = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        paneLogin.add(txtFieldServer, gbc);
        lblServer = new JLabel();
        lblServer.setText("Server");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        paneLogin.add(lblServer, gbc);
        lblLogFile = new JLabel();
        lblLogFile.setText("Log File:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        paneLogin.add(lblLogFile, gbc);
        txtFieldLogFile = new JTextField();
        txtFieldLogFile.setPreferredSize(new Dimension(90, 24));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        paneLogin.add(txtFieldLogFile, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.VERTICAL;
        paneLogin.add(spacer2, gbc);
        spinner1 = new JSpinner(new SpinnerNumberModel(45459, 1, 65535, 1));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paneLogin.add(spinner1, gbc);
        btnLogin = new JButton();
        btnLogin.setText("login");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paneLogin.add(btnLogin, gbc);
        lblPort = new JLabel();
        lblPort.setText("Port:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        paneLogin.add(lblPort, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        paneLogin.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        paneLogin.add(spacer4, gbc);
        paneRegister = new JPanel();
        paneRegister.setLayout(new GridBagLayout());
        tabbedPane1.addTab("Register", paneRegister);
        btnRegister = new JButton();
        btnRegister.setEnabled(false);
        btnRegister.setText("Register");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paneRegister.add(btnRegister, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.VERTICAL;
        paneRegister.add(spacer5, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        pwFieldRegister = new JPasswordField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paneRegister.add(pwFieldRegister, gbc);
        txtFieldRegisterEmail = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paneRegister.add(txtFieldRegisterEmail, gbc);
        txtFieldRegisterUser = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paneRegister.add(txtFieldRegisterUser, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        lblSttxt = new JLabel();
        lblSttxt.setText("Status:");
        panelLogin.add(lblSttxt, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
