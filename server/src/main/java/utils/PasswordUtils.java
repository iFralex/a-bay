package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Genera un hash sicuro della password
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12)); // 12 è il cost factor
    }

    // Verifica una password in chiaro rispetto a un hash salvato
    public static boolean verifyPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }
}
