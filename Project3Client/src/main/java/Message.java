import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    String msg;
    String sender;
    String receiver;
    Types type;

    enum Types {
        SEND_ID,
        ACCEPT_ID,
        REJECT_ID,
        SERVER_MSG,
        QUEUE,
        BLACK,
        RED,
        SEND_MSG,
        MOVE,
        WIN,
        LOSE
    }

    Message(String msg, String sender, String receiver, Types type) {
        this.msg = msg;
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
    }
}
