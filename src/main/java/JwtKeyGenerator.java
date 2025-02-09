import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;

import java.security.Key;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // ✅ Use the latest API for generating a secure HS256 key
        Key key = Jwts.SIG.HS256.key().build();

        // ✅ Encode Key in Base64
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("🔥 Secure JWT Secret Key: " + base64Key);
    }
}
