package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static void main(String[] args) {

        String password = "password123";
        String password2 = "password123";
        String password3 = "pippo";
        String hashedPassword = hashPassword(password);
        String hashedPassword2 = hashPassword(password2);
        String hashedPassword3 = hashPassword(password3);

        System.out.println("Password in chiaro: " + password);
        System.out.println("Hash della password: " + hashedPassword);
        System.out.println("Password2 in chiaro: " + password2);
        System.out.println("Hash della password2: " + hashedPassword2);
        System.out.println("Password3 in chiaro: " + password3);
        System.out.println("Hash della password3: " + hashedPassword3);

        boolean isMatch = verifyPassword(password, hashedPassword);
        System.out.println("La password corrisponde all'hash? " + isMatch);
        isMatch = verifyPassword(password2, hashedPassword2);
        System.out.println("La password2 corrisponde all'hash? " + isMatch);
    }

    // Genera un hash sicuro della password
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12)); // 12 è il cost factor
    }

    // Verifica una password in chiaro rispetto a un hash salvato
    public static boolean verifyPassword(String password, String storedHash) {
        return BCrypt.checkpw(password, storedHash);
    }
}
