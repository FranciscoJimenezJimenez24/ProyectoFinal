package jimenezfranciscoProyectoFinal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class prueba {

	public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(2026);
            System.out.println("Server listening on port 12345...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);

                File file = new File("fichero.txt");
                byte[] fileBytes = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(fileBytes, 0, fileBytes.length);

                // Encrypt fileBytes
                byte[] encryptedBytes = encrypt(fileBytes);

                OutputStream os = socket.getOutputStream();
                os.write(encryptedBytes, 0, encryptedBytes.length);
                os.flush();

                socket.close();
                System.out.println("Encrypted file sent to client.");

                bis.close();
                fis.close();
            }
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    private static byte[] encrypt(byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec keySpec = new SecretKeySpec("0123456789abcdef".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(input);
    }

}
