import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security {
    public static String mda5(String pass) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        byte[] data = pass.getBytes();
        m.update(data,0,data.length);
        BigInteger i = new BigInteger(1,m.digest());
        return String.format("%1$032X", i);
    }

    public static String sha256(String pass) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-128");
        //String(digest.digest(msg.getBytes(StandardCharsets.UTF_8), pass);
        return pass;
    }
}
