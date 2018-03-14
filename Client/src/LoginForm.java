/*
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class LoginForm {
    private final Dimension size1 = new Dimension(225, 350);
    private final Dimension size2 = new Dimension(225, 450);
    private final Dimension size3 = new Dimension(600, 500);
    private final Dimension size4 = new Dimension(225, 350);
    private int width, height;
    private JFrame frame;
    private JPanel panel;
    private JPasswordField password;
    private JTextField username;
    private JTextField server;
    private JTextField logfile;
    private JButton register;
    private JButton login;
    private JButton other;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel serverLabel;
    private JLabel logfileLabel;
    private JLabel errorLabel;
    private JLabel actionLabel;
    private JPanel chatPanel;
    private JPanel loginPanel;
    private JTextPane textPaneChat;
    private JButton buttonSendMessage;
    private JButton buttonSendNewUser;
    private JTextField textFieldChat;
    private JList listUsers;
    private JPanel registerPanel;
    private JButton btnRegister;
    private JTextField textFieldUsername;
    private JTextField txtFieldEmail;
    private JPasswordField passwordField;
    private JCheckBox confirm;
    private JButton backButton;
    private static boolean advanced = false, running = true;
    private static boolean GUI = true;
    public static String
            host = "chat.sralse.xyz",
            port = "45459",
            path = "./",
            log = "logfile.txt";
    private static String[] arguments = new String[]{"chat.sralse.xyz", "45459", "logfile.txt", null, null};
    private int lastmsgid = 0;
    private Socket serverSocket = null;
    private Thread listener = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    public static boolean DEBUG = false, OFFLINE = false;
    public static LoginForm instance;
    public boolean APPEND = true;
    private String userInput, token;
    private static String userName, pass, talkingWith;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < args.length; i++) {
            String s = args[i].toLowerCase();
            if (s.equals("-nogui")) GUI = false;
            else if (s.equals("::offline") && args.length > i + 1) {
                OFFLINE = true;
                DEBUG = true;
            } else if (s.equals("::debug") && args.length > i + 1) DEBUG = true;
            else if (s.equals("-h") && args.length > i + 1) arguments[0] = args[i + 1];
            else if (s.equals("-p") && args.length > i + 1) arguments[1] = args[i + 1];
            else if (s.equals("-f") && args.length > i + 1) arguments[2] = args[i + 1];
            else if (s.equals("-u") && args.length > i + 1) arguments[3] = args[i + 1];
            else if (s.equals("-pw") && args.length > i + 1) arguments[4] = args[i + 1];
        }

        //new Client();

        System.out.println("DEBUG: " + DEBUG + " OFFLINE-MODE: " + OFFLINE);
        new LoginForm();

    }

    public LoginForm() {
        instance = this;
        if (GUI) {
            // Initialise Icon
            ImageIcon frameIcon = new ImageIcon("rsc/icon.png");
            // Initialise Frame & it's settings.
            JFrame frame = new JFrame("MSN 2.0 - by Sralse");
            frame.setContentPane(loginPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIconImage(frameIcon.getImage());
            frame.setPreferredSize(size1);
            frame.setVisible(true);
            //frame.setResizable(false);
            frame.pack();
            this.frame = frame;
            setAdvanced(false);
            centerFrame(frame);
            // Initialise components
            errorLabel.setForeground(Color.RED);
            server.setText(host + ":" + port);
//            chatPanel.setVisible(false);
            // Actionlisteners
            register.addActionListener(e -> {
                loginPanel.setVisible(false);
                panel.setPreferredSize(size4);
                registerPanel.setVisible(true);
                registerPanel.setPreferredSize(size4);
                frame.setContentPane(panel);
                frame.setPreferredSize(size4);
                frame.pack();
                frame.setVisible(true);
                centerFrame(frame);
                registerPanel.requestFocus();
            });
            other.addActionListener(e -> {
                if (advanced) setAdvanced(false);
                else setAdvanced(true);
            });
            username.addActionListener(e -> password.requestFocus());
            password.addActionListener(e -> validate());
            login.addActionListener(e -> validate());
            textFieldChat.addActionListener(e -> {
                messageSend();
            });
            buttonSendMessage.addActionListener(e -> {
                messageSend();
            });
            listUsers.addListSelectionListener(evt -> {
                reformMessages();
            });
        }
        // Save arguments
        if (arguments.length > 0 && arguments[0] != null) host = arguments[0];
        if (arguments.length > 1 && arguments[1] != null) port = arguments[1];
        if (arguments.length > 2 && arguments[2] != null) log = arguments[2];
        if (arguments.length > 3 && arguments[3] != null) userName = arguments[3];
        if (arguments.length > 4 && arguments[4] != null) pass = arguments[4];
        if (GUI) {
            username.setText(userName);
            password.setText(pass);
            logfile.setText(arguments[2]);
        }
        start(); // Starts console or GUI depending on arguments.
        confirm.addItemListener(evt -> {
            System.out.println(confirm.isSelected());
            if (confirm.isSelected()) btnRegister.setEnabled(true);
            else btnRegister.setEnabled(false);
        });
        backButton.addActionListener(e -> {
            registerPanel.setVisible(false);
            panel.setPreferredSize(size1);
            loginPanel.setVisible(true);
            loginPanel.setPreferredSize(size1);
            frame.setContentPane(panel);
            frame.setPreferredSize(size1);
            frame.pack();
            frame.setVisible(true);
            centerFrame(frame);
            loginPanel.requestFocus();

        });
    }

    private void messageSend() {
        sendMessage(textFieldChat.getText());
        reformMessages();
        textFieldChat.setText(null);
    }

    private void start() {
        while (OFFLINE) ;
        try {
            // Set up a server connection
            serverSocket = new Socket(host, Integer.parseInt(port));
            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            // Set up a listener to receive messages.
            listener = new ServerListener(this, in);
            listener.start();
            // If we use console, set up an input.
            Scanner input = null;
            if (!GUI) input = new Scanner(System.in);
            // Main non GUI loop, listens for commands.
            String[] args;
            while (!GUI && listener.isAlive() && (userInput = input.nextLine()) != null) {
                args = userInput.split(" ");
                if (args[0].toLowerCase().equals("stop") ||
                        args[0].toLowerCase().equals("exit")) stop();
                else if (args[0].toLowerCase().equals("retry")) reconnect();
                else if (args[0].toLowerCase().equals("LoginForm") && args.length == 3) login(args[1], args[2]);
                else if (args[0].toLowerCase().equals("say") && getToken() != null && args.length > 2)
                    sendMessage(args);
                else if (args[0].toLowerCase().equals("get")) getMessages(args);
                else if (args[0].toLowerCase().equals("lmsg") ||
                        args[0].toLowerCase().equals("lastmessage") ||
                        args[0].toLowerCase().equals("last") ||
                        args[0].toLowerCase().equals("lastmsg")) getMessages(new String[]{"last"});
                else if (args[0].toLowerCase().equals("help") ||
                        args[0].toLowerCase().equals("-h") ||
                        args[0].toLowerCase().equals("h")) help();
                else {
                    System.out.println("Not logged in or command not recognised > " + userInput);
                    help();
                }
            }
            // We will wit until the user ends thread or the server breaks our connection.
            listener.join();
            System.err.println("Server is closed!");
            // All the dialogs or console errors.
            if (GUI) JOptionPane.showMessageDialog(frame,
                    "Server is closed!",
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnknownHostException e) {
            System.err.println("Host not found: " + host);
            if (GUI) JOptionPane.showMessageDialog(frame,
                    "Host not found!",
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
            if (GUI) JOptionPane.showMessageDialog(frame,
                    "Couldn't get I/O for the connection to " + host,
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    */
/***
     * This will center a (J)frame on the screen.
     * @param frame The frame which to center.
     *//*

    private void centerFrame(JFrame frame) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.width = dim.width;
        this.height = dim.height;
        frame.setLocation(width / 2 - frame.getSize().width / 2, height / 2 - frame.getSize().height / 2);
        frame.pack();
    }

    // Retrieve active user
    public static String getUser() {
        return userName;
    }

    */
/**
     * Compose all the latest messages
     *//*

    private void formMessages() {
        String document = "<html><body style=\"font-family: sans-serif;\">";
        //getMessages(new String[]{"renew"});
        MessageList.init();
        listUsers.setListData(MessageList.getCollection().toArray());
        MessageList list = MessageList.getLastUser();
        talkingWith = list.getUser();
        System.out.println("Last talked to user: " + talkingWith + " amount of messages: " + list.getList().size());
        for (Message msg : list.getList()) document += msg.composeMessage();
        document += "</body></html>";
        textPaneChat.setText(document);
    }

    public synchronized void reformMessages() {
        //MessageList.filter();
        MessageList list = MessageList.getCollection().get(listUsers.getSelectedIndex());
        talkingWith = list.getUser();
        String document = "<html><body style=\"font-family: sans-serif;\">";
        for (Message msg : list.getList()) document += msg.composeMessage();
        document += "</body></html>";
        textPaneChat.setText(document);
    }

    */
/**
     * This method will validate all fields.
     *//*

    private void validate() {
        if (OFFLINE) logMeIn();
        if (username.getText().length() < 4) {
            errorLabel.setText("Invalid credentials.");
            return;
        } else
            userName = username.getText();
        if (password.getPassword().length < 4) {
            errorLabel.setText("Invalid credentials.");
            return;
        }
        if (!server.getText().equals(host + ":" + port)) {
            host = getServer();
        }
        if (!(logfile.getText().length() > 0)) {
            log = "msglog.txt";
        } else
            log = logfile.getText();
        logMeIn();
        if (token == null) errorLabel.setText("Invalid credentials.");
    }

    */
/**
     * This method will log the user in.
     *//*

    private void logMeIn() {
        actionLabel.setText("Logging in...");
        if (!OFFLINE && listener != null && !listener.isAlive()) reconnect(host, port, log);
        try {
            if (!OFFLINE) {
                login(username.getText(), charArrayToString(password.getPassword()));
                // If we cant get the token within a second return (or if it's a false LoginForm)
                Thread.sleep(1000);
                if (token == null) return;
            }
            userName = username.getText();
            formMessages();
            actionLabel.setText("Logged in!");
            loginPanel.setVisible(false);
            panel.setPreferredSize(size3);
            chatPanel.setVisible(true);
            chatPanel.setPreferredSize(size3);
            frame.setContentPane(chatPanel);
            frame.setPreferredSize(size3);
            frame.pack();
            frame.setVisible(true);
            centerFrame(frame);
        } catch (NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String charArrayToString(char[] password) {
        String s = "";
        for (Character c : password) s += c.toString();
        return s;
    }

    private String getServer() {
        host = server.getText();
        if (host.toLowerCase().contains(":")) {
            port = host.substring(host.indexOf(":") + 1, (host.length()));
            host = host.substring(0, host.indexOf(":"));
            if (port == "") port = "45459";
        }
        System.out.println("Host: " + host + " Port: " + port + ".");
        return host;
    }

    private void setAdvanced(boolean state) {
        server.setVisible(state);
        serverLabel.setVisible(state);
        logfile.setVisible(state);
        logfileLabel.setVisible(state);
        advanced = state;
        if (state) {
            other.setText("Hide Advanced");
            frame.setSize(size2);
            panel.setSize(size2);
        } else {
            other.setText("Show Advanced");
            frame.setSize(size1);
            panel.setSize(size1);
        }
    }

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void openWebpage(String urlAsString) {
        try {
            openWebpage(new URL(urlAsString));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void help() {
        System.out.println("Possible commands are:"
                + "\n stop | exit               :   Stops current session."
                + "\n retry                     :   Tries to reconnect with the server"
                + "\n get <last-id> [last | new | c(ache) | renew]"
                + "\n                           :   Retrieves messages starting from 'last-id' (default = 0)"
                + "\n say <uname> <message>     :   Sends a message to 'uname'"
                + "\n lmsg | lastmessage        :   Retrieves last received message.");
    }

    public void getMessages(String[] args) {
        try {
            File file = new File(path + log);
            if (args[0].toLowerCase().equals("last")) {
                if (!file.exists()) {
                    System.err.println("File (" + path + log + ") does not exist. Create one by using 'get new'.");
                    return;
                }
                FileInputStream fs = new FileInputStream(path + log);
                BufferedReader br = new BufferedReader(new InputStreamReader(fs));
                int i = getLines(path + log) - 1;
                if (i > 4) System.out.println("There are " + i + " lines in the document.");
                else {
                    System.err.println("Empty log file (" + path + log + "). Unable to perform action(s).");
                    return;
                }
                for (int j = 0; j < i - 4; j++) br.readLine();
                for (i = 0; i < 4; i++) {
                    String s = br.readLine();
                    if (s == null) System.err.println("Null found in message log.");
                    if (i == 0) {
                        if (isInteger(s)) setLastMSGID(Integer.valueOf(s));
                        else {
                            System.err.println("Corrupt index found! Redownloading logfile!");
                            getMessages(new String[]{"renew"});
                            return;
                        }
                    }
                    System.out.println(s);
                }
                br.close();
                fs.close();
            } else if (args.length > 1 && args[1].toLowerCase().equals("new") && !file.exists())
                file.createNewFile();
            else if (args.length > 1 && (args[1].toLowerCase().equals("c") || args[1].toLowerCase().equals("cache"))) {
                if (file.exists()) {
                    FileInputStream fs = new FileInputStream(path);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fs));
                    String s;
                    while ((s = br.readLine()) != null) System.out.println(s);
                }
            } else if (args.length > 1 && args[1].toLowerCase().equals("renew")) {
                while (!file.delete()) ;
                getMessages(new String[]{"get"});
            } else if (getToken() == null) System.err.println("No token! Unable to perform action(s).");
            else if (args.length == 2)
                out.println("get " + getToken() + " " + args[1]);
            else
                out.println("get " + getToken());
        } catch (IOException e) {
            if (args.length > 1) System.err.println("Unable to perform action: " + args[1]);
            e.printStackTrace();
        }
    }

    public static int getLines(String path) throws IOException {
        FileInputStream fs = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fs));
        int i = 1;
        while (br.readLine() != null) i++;
        br.close();
        fs.close();
        return i;
    }

    public void sendMessage(String[] args) {
        System.out.println("Sending message to: " + args[1] + " message:");
        String msg = " " + userInput.substring(args[0].length() + args[1].length() + 2, userInput.length());
        out.println(args[0] + " " + getToken() + " " + args[1] + msg);
        System.out.println(msg);
    }

    public void sendMessage(String s) {
        if (!GUI) return;
        System.out.println("Sending message to " + talkingWith);
        out.println("say " + getToken() + " " + talkingWith + " " + s);
    }

    public void login(String user, String password) throws NoSuchAlgorithmException, InterruptedException {
        System.out.println("Logging in with user: " + user + " password: " + mda5(password));
        while (out == null) Thread.sleep(100);
        out.println("login " + user + " " + mda5(password));
    }

    public void stop() throws IOException, InterruptedException {
        running = false;
        serverSocket.close();
        listener.join();
        return;
    }

    public void reconnect(String host, String port, String text) {
        this.host = host;
        this.port = port;
        this.path = text;
        try {
            reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() throws IOException {
        serverSocket = new Socket(host, Integer.parseInt(port));
        out = new PrintWriter(serverSocket.getOutputStream(), true);
        (listener = new ServerListener(
                this, in = new BufferedReader(
                new InputStreamReader(
                        serverSocket.getInputStream())))).start();
        System.out.println("Connected to server.");
    }

    public static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), 10) < 0) return false;
        }
        return true;
    }

    private static String mda5(String pass) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] data = pass.getBytes();
        m.update(data, 0, data.length);
        BigInteger i = new BigInteger(1, m.digest());
        return String.format("%1$032X", i);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isDebugEnabled() {
        return DEBUG;
    }

    public int getLastMSGID() {
        return lastmsgid;
    }

    public String getPath() {
        return path;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLastMSGID(Integer lastMSGID) {
        this.lastmsgid = lastMSGID;
    }

    */
/**
     * @noinspection ALL
     *//*

    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    public String getLogPath() {
        return path + log;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    */
/**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     *//*

    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.setAlignmentX(0.0f);
        panel.setAlignmentY(0.0f);
        panel.setBackground(new Color(-16777216));
        panel.setMaximumSize(new Dimension(600, 400));
        panel.setMinimumSize(new Dimension(-1, -1));
        panel.setPreferredSize(new Dimension(-1, -1));
        panel.setVisible(true);
        loginPanel = new JPanel();
        loginPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(23, 3, new Insets(0, 0, 0, 0), 0, 0));
        loginPanel.setAlignmentX(0.0f);
        loginPanel.setAlignmentY(0.0f);
        loginPanel.setBackground(new Color(-14737633));
        loginPanel.setEnabled(true);
        loginPanel.setVisible(true);
        panel.add(loginPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        serverLabel = new JLabel();
        serverLabel.setEnabled(true);
        serverLabel.setForeground(new Color(-1));
        serverLabel.setText("Server");
        serverLabel.setToolTipText("If you have a custom e.g. '12345' port use it like: 'example.com:12345'");
        serverLabel.setVisible(true);
        loginPanel.add(serverLabel, new com.intellij.uiDesigner.core.GridConstraints(17, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        server = new JTextField();
        server.setHorizontalAlignment(0);
        server.setText("chat.sralse.xyz:45459");
        server.setVisible(true);
        loginPanel.add(server, new com.intellij.uiDesigner.core.GridConstraints(18, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(120, 20), null, 0, false));
        logfile = new JTextField();
        logfile.setHorizontalAlignment(0);
        logfile.setText("logfile.txt");
        loginPanel.add(logfile, new com.intellij.uiDesigner.core.GridConstraints(21, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(120, 20), null, 0, false));
        passwordLabel = new JLabel();
        passwordLabel.setForeground(new Color(-1));
        passwordLabel.setText("Password");
        loginPanel.add(passwordLabel, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usernameLabel = new JLabel();
        usernameLabel.setForeground(new Color(-1));
        usernameLabel.setText("Username:");
        loginPanel.add(usernameLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        password = new JPasswordField();
        password.setHorizontalAlignment(0);
        loginPanel.add(password, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        username = new JTextField();
        username.setHorizontalAlignment(0);
        loginPanel.add(username, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        login = new JButton();
        login.setBackground(new Color(-15132391));
        login.setText("Login");
        loginPanel.add(login, new com.intellij.uiDesigner.core.GridConstraints(11, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(120, 20), new Dimension(140, -1), 0, false));
        register = new JButton();
        register.setBackground(new Color(-15132391));
        register.setText("Register");
        loginPanel.add(register, new com.intellij.uiDesigner.core.GridConstraints(13, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(120, 20), new Dimension(140, -1), 0, false));
        other = new JButton();
        other.setBackground(new Color(-15132391));
        other.setText("Show Advanced");
        loginPanel.add(other, new com.intellij.uiDesigner.core.GridConstraints(15, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(120, 20), new Dimension(140, -1), 0, false));
        logfileLabel = new JLabel();
        logfileLabel.setForeground(new Color(-1));
        logfileLabel.setText("Logfile");
        loginPanel.add(logfileLabel, new com.intellij.uiDesigner.core.GridConstraints(20, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 6), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 6), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer4 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer4, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 6), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer5 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer5, new com.intellij.uiDesigner.core.GridConstraints(10, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer6 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer6, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(20, -1), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer7 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer7, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(20, -1), null, null, 0, false));
        actionLabel = new JLabel();
        actionLabel.setText("");
        loginPanel.add(actionLabel, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        errorLabel = new JLabel();
        errorLabel.setIconTextGap(4);
        errorLabel.setText("");
        errorLabel.setVisible(true);
        loginPanel.add(errorLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer8 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer8, new com.intellij.uiDesigner.core.GridConstraints(22, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 20), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer9 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer9, new com.intellij.uiDesigner.core.GridConstraints(16, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 20), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer10 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer10, new com.intellij.uiDesigner.core.GridConstraints(12, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 4), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer11 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer11, new com.intellij.uiDesigner.core.GridConstraints(14, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 4), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer12 = new com.intellij.uiDesigner.core.Spacer();
        loginPanel.add(spacer12, new com.intellij.uiDesigner.core.GridConstraints(19, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 3), null, new Dimension(-1, 3), 0, false));
        chatPanel = new JPanel();
        chatPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 3, new Insets(5, 5, 5, 5), -1, -1));
        chatPanel.setBackground(new Color(-16250872));
        chatPanel.setForeground(new Color(-11316397));
        chatPanel.setVisible(true);
        panel.add(chatPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(600, 400), null, new Dimension(600, 400), 0, false));
        textFieldChat = new JTextField();
        textFieldChat.setForeground(new Color(-11316397));
        textFieldChat.setText("");
        chatPanel.add(textFieldChat, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        buttonSendMessage = new JButton();
        buttonSendMessage.setText("send");
        chatPanel.add(buttonSendMessage, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setBackground(new Color(-16777216));
        scrollPane1.setForeground(new Color(-11316397));
        chatPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 3, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textPaneChat = new JTextPane();
        textPaneChat.setBackground(new Color(-16777216));
        textPaneChat.setContentType("text/html");
        textPaneChat.setEditable(false);
        textPaneChat.setForeground(new Color(-11316397));
        textPaneChat.putClientProperty("charset", "");
        scrollPane1.setViewportView(textPaneChat);
        buttonSendNewUser = new JButton();
        buttonSendNewUser.setText("Compose new message");
        chatPanel.add(buttonSendNewUser, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setForeground(new Color(-15463393));
        chatPanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-15461345)), "Conversations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(-4473925)));
        listUsers = new JList();
        listUsers.setBackground(new Color(-15132391));
        listUsers.setForeground(new Color(-11316397));
        listUsers.setSelectionMode(0);
        scrollPane2.setViewportView(listUsers);
        registerPanel = new JPanel();
        registerPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(13, 3, new Insets(5, 5, 5, 5), -1, -1));
        registerPanel.setBackground(new Color(-15132391));
        panel.add(registerPanel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(200, 350), null, new Dimension(200, 350), 0, false));
        btnRegister = new JButton();
        btnRegister.setBackground(new Color(-15132391));
        btnRegister.setEnabled(false);
        btnRegister.setText("Register");
        registerPanel.add(btnRegister, new com.intellij.uiDesigner.core.GridConstraints(11, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer13 = new com.intellij.uiDesigner.core.Spacer();
        registerPanel.add(spacer13, new com.intellij.uiDesigner.core.GridConstraints(12, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer14 = new com.intellij.uiDesigner.core.Spacer();
        registerPanel.add(spacer14, new com.intellij.uiDesigner.core.GridConstraints(11, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer15 = new com.intellij.uiDesigner.core.Spacer();
        registerPanel.add(spacer15, new com.intellij.uiDesigner.core.GridConstraints(11, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        textFieldUsername = new JTextField();
        textFieldUsername.setForeground(new Color(-986896));
        textFieldUsername.setMargin(new Insets(0, 0, 0, 0));
        textFieldUsername.setText("");
        registerPanel.add(textFieldUsername, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtFieldEmail = new JTextField();
        txtFieldEmail.setForeground(new Color(-986896));
        txtFieldEmail.setMargin(new Insets(0, 0, 0, 0));
        registerPanel.add(txtFieldEmail, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordField = new JPasswordField();
        passwordField.setForeground(new Color(-986896));
        passwordField.setMargin(new Insets(0, 0, 0, 0));
        registerPanel.add(passwordField, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        confirm = new JCheckBox();
        confirm.setBackground(new Color(-15132391));
        confirm.setForeground(new Color(-986896));
        confirm.setSelected(false);
        confirm.setText("Yes, I have read and accept.");
        registerPanel.add(confirm, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer16 = new com.intellij.uiDesigner.core.Spacer();
        registerPanel.add(spacer16, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setForeground(new Color(-986896));
        label1.setText("Email Adress");
        registerPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setForeground(new Color(-986896));
        label2.setText("Username");
        registerPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setForeground(new Color(-986896));
        label3.setText("Password");
        registerPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setForeground(new Color(-986896));
        label4.setText("Do you accept our agreement?");
        registerPanel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer17 = new com.intellij.uiDesigner.core.Spacer();
        registerPanel.add(spacer17, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        backButton = new JButton();
        backButton.setBackground(new Color(-14737633));
        backButton.setText("Back to login");
        registerPanel.add(backButton, new com.intellij.uiDesigner.core.GridConstraints(10, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    */
/**
     * @noinspection ALL
     *//*

    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
*/
