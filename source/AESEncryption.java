import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AESEncryption {

	private String algorithm = "AES/CBC/PKCS5Padding";
	//private static final String pass = "password";
	private static String pass;
	private static String alias;
	private static String salt;
	private static final int iterCount = 65536;
	private static final int keySize = 128;
	private static KeyStore ks;
	private static String sourcePath;

	public String encrypt(String Data) throws Exception {
		SecretKeySpec key = generateKey();
		storeSecretKey(key);
		Cipher c = Cipher.getInstance(algorithm);
        c.init(Cipher.ENCRYPT_MODE, key);
		storeInitVector(c.getIV());
        byte[] encryptedValue = c.doFinal(Data.getBytes());
		return DatatypeConverter.printBase64Binary(encryptedValue);
    }

    public String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = getSecretKey();
        Cipher c = Cipher.getInstance(algorithm);
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(getInitVector()));
		deleteFile("iv.txt");
		deleteFile("keystore.jceks");
		byte[] decodedValue = DatatypeConverter.parseBase64Binary(encryptedData);
        byte[] decryptedValue = c.doFinal(decodedValue);
		return new String(decryptedValue);
    }

	public static String generateSalt() {
		SecureRandom rnd = new SecureRandom();
		byte[] bytes = new byte[20];
		rnd.nextBytes(bytes);
		return new String(bytes);
	}

	public static SecretKeySpec generateKey() {
		salt = generateSalt();
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			PBEKeySpec spec = new PBEKeySpec(pass.toCharArray(), salt.getBytes("UTF-8"), iterCount, keySize);
			SecretKey secretKey = factory.generateSecret(spec);
			SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
			return secret;
		} catch(Exception e){
			System.out.println("Error encountered.");
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public static SecretKeySpec getSecretKey() {
		FileInputStream inputStream = null;
		char[] password = pass.toCharArray();
		try {
			ks = KeyStore.getInstance("JCEKS");
			inputStream = new FileInputStream(sourcePath + "//keystore.jceks");
			ks.load(inputStream, password);
			SecretKey secretKey = (SecretKey)ks.getKey(alias, password);
			SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
			return secret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void storeSecretKey(SecretKeySpec key) {
		char[] password = pass.toCharArray();
		try {
			ks = KeyStore.getInstance("JCEKS");
			ks.load(null, password);
			KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);
			KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(key);
			ks.setEntry(alias, entry, param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(sourcePath + "//keystore.jceks");
			ks.store(outputStream, password);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void storeInitVector(byte[] initVector){
		try {
			File f = new File(sourcePath + "//iv.txt");
			PrintWriter pw = new PrintWriter(f);
			String iv = DatatypeConverter.printBase64Binary(initVector);
			pw.println(iv);
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] getInitVector() {
		try {
			File f = new File(sourcePath + "//iv.txt");
			Scanner fileScanner = new Scanner(f);
			String iv = fileScanner.nextLine();
			byte[] initVector = DatatypeConverter.parseBase64Binary(iv);
			fileScanner.close();
			return initVector;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void deleteFile(String fileName) {
		try {
			File f = new File(sourcePath + "//" + fileName);
			Path path = Paths.get(f.getAbsolutePath());
			Files.delete(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setSourcePath(String path) {
		sourcePath = path;
	}
	
	public void setPassword(String p){
		pass = p;
	}
	
	public void setAlias(String a){
		alias = a;
	}
}