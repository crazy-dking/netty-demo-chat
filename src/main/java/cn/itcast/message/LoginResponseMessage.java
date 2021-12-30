package cn.itcast.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class LoginResponseMessage extends AbstractResponseMessage {
    public LoginResponseMessage(boolean b, String s) {
        super(b,s);
    }

    @Override
    public int getMessageType() {
        return LoginResponseMessage;
    }

}
