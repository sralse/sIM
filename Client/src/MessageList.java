import java.io.*;
import java.util.ArrayList;

public class MessageList {

    private ArrayList<Message> messageCollection = new ArrayList<>();
    private String user;
    private int lastID;
    private static ArrayList<MessageList> usersCollection = new ArrayList<>();
    private static ArrayList<String> users = new ArrayList<>();
    private static boolean init = false;
    public static int lastLineCount = 0, lastOveralID;

    public MessageList(String user) {
        this.user = user;
    }

    public static String[] getUsers() {
        return (String[]) users.toArray();
    }

    public static ArrayList<MessageList> getCollection() {
        return usersCollection;
    }

    private void add(Message message) {
        messageCollection.add(message);
        if (message.getMessageID() > lastID) setLastID(message.getMessageID());
    }

    public static void loadUsers() {
        try {
            if(LoginForm.DEBUG) System.out.println("Loading Messages...");
            Thread.sleep(1000);
            FileInputStream fs = new FileInputStream(LoginForm.path+ LoginForm.log);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            int i = -1;
            String id = null, sender = null, receiver = null, s, progUser = LoginForm.getUser();
            while ((s = br.readLine()) != null) {
                i++; lastLineCount++;
                if (i==0) id = s;
                if (i==1) sender = s;
                if (i==2) receiver = s;
                if (i==3) {
                    add(progUser,id,sender,receiver,s);
                    i = -1;
                }
            }
            br.close();
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Message> getList() {
        return messageCollection;
    }

    public static MessageList getLastUser() {
        int i = 0;
        MessageList rls = null;
        for (MessageList list : usersCollection) {
            if (list.lastID > i) {
                i = list.lastID;
                rls = list;
            }
        }
        return rls;
    }

    public static int getLastID() {
        int i = 0;
        for (MessageList msglist:usersCollection) if (msglist.lastID > i) i = msglist.lastID;
        return i;
    }

    private void setLastID(int lastID) {
        this.lastID = lastID;
    }

    public static void init() {
        if (init) return;
        System.out.println("Loading conversations...");
        while(LoginForm.instance == null) try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LoginForm.instance.getMessages(new String[]{"renew"});
        while(BClientListener.incomingMessage) ;
        loadUsers();
        System.out.println("Loaded MessagesList, amount of conversations: " + usersCollection.size());

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                while (true) {
                    while(BClientListener.incomingMessage) Thread.sleep(350);
                    LoginForm.instance.getMessages(new String[]{"get",""+getLastID()});
                    MessageList.renew(LoginForm.getUser());
                    Thread.sleep(350);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.err.println("Receiver Thread has stopped.");
            }
        }).start();

        init = true;
    }

    public String getUser() {
        return user;
    }

    public static void filter() {
        for (MessageList msglist:usersCollection) {
            for (Message msg : msglist.messageCollection) {
                if (msglist.messageCollection.size() > 100) {
                    msglist.messageCollection.remove(msg);
                }
            }
        }
    }

    public synchronized static void renew(String user) {
        try {
            FileInputStream fs = new FileInputStream(LoginForm.path+ LoginForm.log);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            while (!br.ready());
            int i = -1, j = 0;
            String id = null, sender = null, receiver = null, s;
            while ((s = br.readLine()) != null) {
                j++;
                if (j>lastLineCount) {
                    i++;
                    if (i==0) id = s;
                    if (i==1) sender = s;
                    if (i==2) receiver = s;
                    if (i==3) {
                        add(user,id,sender,receiver,s);
                        i = -1;
                    }
                }
            }
            br.close();
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void add(String progUser, String id, String sender, String receiver, String msg) {
        boolean exist = false;
        for (MessageList list:usersCollection) {
            if(progUser.equals(receiver) && list.getUser().equals(sender)) {
                list.add(new Message(id,sender,receiver,msg));
                exist = true;
            } else if (progUser.equals(sender) && list.getUser().equals(receiver)) {
                list.add(new Message(id,sender,receiver,msg));
                exist = true;
            }
        }
        if (!exist) {
            System.out.println("Making new MessageList for ("+sender+")");
            MessageList msglist = new MessageList(sender);
            msglist.add(new Message(id,sender,receiver,msg));
            usersCollection.add(msglist);
        }
    }
}
