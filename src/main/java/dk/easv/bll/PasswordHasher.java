package dk.easv.bll;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    // Generate random salt
    public static String generateSalt() throws Exception {
        SecureRandom random = new SecureRandom(); //generate secure random number
        byte[] salt = new byte[SALT_LENGTH]; // creates an empty array of 16 bytes
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt); // convert into readable string to store in the database
    }

    // Hash password with salt
    public static String hashPassword(String password, String salt) throws Exception {
        byte[] saltBytes = Base64.getDecoder().decode(salt); // before we stored salt as text,now we convert it back into bytes

        //this is the way for hashing - password, salt, iterations, size
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                ITERATIONS,
                KEY_LENGTH
        );

        // choosing algorithm
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded(); // generate hash

        return Base64.getEncoder().encodeToString(hash); // convert to string
    }

    // Verify password
    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) throws Exception {
        String newHash = hashPassword(inputPassword, storedSalt); // entered password
        return newHash.equals(storedHash); // compares with stored password
    }
}