package model;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

/**
 * For the security of the accounts.
 */
public class Security {
    /**
     * Salt and Hash the password to be stored in the database.
     * 
     * @param password
     * @return salted and hashed password
     */
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash) + 63408097 + Base64.getEncoder().encodeToString(salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * For checking if the inputted password is the same with the stored password.
     * 
     * @param inputPassword
     * @param storedHash
     * @return True if they are the same, false otherwise.
     */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        try {
            String[] hashSalt = storedHash.split("63408097");
            KeySpec spec = new PBEKeySpec(inputPassword.toCharArray(), Base64.getDecoder().decode(hashSalt[1]), 65536,
                    256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();
            String newHash = Base64.getEncoder().encodeToString(hash);
            return newHash.equals(hashSalt[0]);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Generates a random password with length of 8 to 24. (Courtesy of my incorrect
     * CCC101 assignment)
     * 
     * @return
     */
    public static String randomPasswordGenerator() {
        Random rand = new Random();
        StringBuilder password = new StringBuilder();
        int length = rand.nextInt(8, 25); // provide length from 8 to 24
        int random = rand.nextInt(1, 5);

        // valid characters
        char[] lowerChar = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
        char[] upperChar = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
        char[] symbols = { '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=', '{', '}', '[', ']',
                '<', '>', '?' };
        char[] numbers = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };

        // ensure that the first 4 characters are from different types
        int[] randomArr = new int[4];
        int anRandom = rand.nextInt(1, 5);
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                randomArr[i] = anRandom;

                anRandom = rand.nextInt(1, 5);
            } else {
                int prev = i - 1;
                while (prev >= 0) {
                    if (randomArr[prev] == anRandom) {
                        anRandom = rand.nextInt(1, 5);
                    }
                    prev--;
                }
                randomArr[i] = anRandom;

                anRandom = rand.nextInt(1, 5);
            }
        }
        int password_char;
        for (int j = 0; j < length; j++) {
            if (j < 4) {
                if (randomArr[j] == 1) {
                    password_char = rand.nextInt(0, lowerChar.length);
                    password.append(lowerChar[password_char]);
                } else if (randomArr[j] == 2) {
                    password_char = rand.nextInt(0, upperChar.length);
                    password.append(upperChar[password_char]);
                } else if (randomArr[j] == 3) {
                    password_char = rand.nextInt(0, symbols.length);
                    password.append(symbols[password_char]);
                } else if (randomArr[j] == 4) {
                    password_char = rand.nextInt(0, numbers.length);
                    password.append(numbers[password_char]);
                }
            } else {
                if (random == 1) {
                    password_char = rand.nextInt(0, lowerChar.length);
                    password.append(lowerChar[password_char]);
                } else if (random == 2) {
                    password_char = rand.nextInt(0, upperChar.length);
                    password.append(upperChar[password_char]);
                } else if (random == 3) {
                    password_char = rand.nextInt(0, symbols.length);
                    password.append(symbols[password_char]);
                } else if (random == 4) {
                    password_char = rand.nextInt(0, numbers.length);
                    password.append(numbers[password_char]);
                }
            }

            random = rand.nextInt(1, 5);
        }
        return password.toString();
    }
}