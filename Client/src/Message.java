public class Message {
    private int messageID;
    private String receiver,message,sender;
    private String colorSenderFG = "#de1919",
            colorBG = "#000000",
            colorSenderText = "#ffffff",
            colorReceiverFG = "#44c8c8",
            colorReceiverBG = "#161616",
            colorReceiverText = "#dbd9d9";

    public Message(String messageID, String sender, String receiver, String message) {
        this.messageID = Integer.parseInt(messageID);
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public Message(int messageID, String sender, String receiver, String message) {
        this.messageID = messageID;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public int getMessageID() {
        return messageID;
    }

    public String composeMessage() {
        if(login.getUser().equals(sender))
            return "<b bgcolor=" + colorBG + " color=" + colorSenderFG + "> " + sender + " :</b>" +
                    "<font bgcolor=" + colorBG + " color=" + colorReceiverText + "> " + message + " </font><br>";
        else
            return "<b bgcolor=" + colorBG + " color=" + colorReceiverFG + "> " + sender + " :</b>" +
                    "<font  bgcolor=" + colorBG + " color=" + colorSenderText + "> " + message + " </font><br>";

    }
}
