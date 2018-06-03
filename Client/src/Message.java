
public class Message {
    private int messageID;
    private String receiver,message,sender;
    private String
            colorSenderFG = "#de1919",
            colorBG = "#e8e8e8",
            colorSenderText = "#ffffff",
            colorReceiverFG = "#44c8c8",
            colorReceiverBG = "#161616",
            colorReceiverText = "#dbd9d9";

    public Message(String messageID, String sender, String receiver, String message) {
        this.messageID = Integer.parseInt(messageID);
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        MessageList.setLastMSGID(messageID);
    }

    public int getMessageID() {
        return messageID;
    }

    public String composeMessage() {
        if(Console.user.equals(sender))
            return "<b id="+messageID+" color=" + colorSenderFG + "> " + sender + " :</b>" +
                    "<font> " + message + " </font><br>";
        else
            return "<b id="+messageID+" bgcolor=" + colorBG + " color=" + colorReceiverFG + "> " + sender + " :</b>" +
                    "<font  bgcolor=" + colorBG + "> " + message + " </font><br>";
    }
}
