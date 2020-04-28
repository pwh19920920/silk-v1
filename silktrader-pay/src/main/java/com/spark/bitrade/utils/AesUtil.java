package com.spark.bitrade.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * <p>Aes加解密工具类</p>
 * @author octopus
 * @date 2018-9-28
 */
public class AesUtil {

	private  int keySize;
	private  int iterationCount;
	private  Cipher cipher;

	public static  final  String secretKey = "8D8A0D62AE00DEEFF60341811A8A3CB5";

	public AesUtil(){
		this(128,1000);
	}

	public AesUtil(int keySize, int iterationCount) {
		this.keySize = keySize;
		this.iterationCount = iterationCount;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw fail(e);
		}
	}

	/**
	 * 加密
	 * @param salt
	 *            盐
	 * @param iv
	 *            向量
	 * @param passphrase
	 *            秘钥
	 * @param plaintext
	 *            加密内容
	 * @return
	 */
	public String encrypt(String salt, String iv, String passphrase, String plaintext) {
		try {
			if(StringUtils.isEmpty(salt)){
				System.err.println("salt is null");
				return null;
			}
			if(StringUtils.isEmpty(iv)){
				System.err.println("iv is null");
				return null;
			}
			if(StringUtils.isEmpty(passphrase)){
				System.err.println("passphrase is null");
				return null;
			}
			if(StringUtils.isEmpty(plaintext)){
				System.err.println("plaintext is null");
				return null;
			}
			SecretKey key = generateKey(salt, passphrase);
			byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, iv, plaintext.getBytes("UTF-8"));
			return base64(encrypted);
		} catch (UnsupportedEncodingException e) {
			throw fail(e);
		}
	}

	/**
	 * 解密
	 * @param salt
	 *            盐
	 * @param iv
	 *            向量
	 * @param passphrase
	 *            秘钥
	 * @param ciphertext
	 *            解密内容
	 * @return
	 */
	public String decrypt(String salt, String iv, String passphrase, String ciphertext) {
		try {

			if(StringUtils.isEmpty(salt)){
				System.err.println("salt is null");
				return null;
			}
			if(StringUtils.isEmpty(iv)){
				System.err.println("iv is null");
				return null;
			}
			if(StringUtils.isEmpty(passphrase)){
				System.err.println("passphrase is null");
				return null;
			}
			if(StringUtils.isEmpty(ciphertext)){
				System.err.println("ciphertext is null");
				return null;
			}
			SecretKey key = generateKey(salt, passphrase);
			byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, base64(ciphertext));
			return new String(decrypted, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw fail(e);
		}
	}

	private byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
		try {
			cipher.init(encryptMode, key, new IvParameterSpec(hex(iv)));
			return cipher.doFinal(bytes);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw fail(e);
		}
	}

	private SecretKey generateKey(String salt, String passphrase) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize);
			SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
			return key;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw fail(e);
		}
	}

	public static String random(int length) {
		byte[] salt = new byte[length];
		new SecureRandom().nextBytes(salt);
		return hex(salt);
	}

	public static String base64(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}

	public static byte[] base64(String str) {
		return Base64.decodeBase64(str);
	}

	public static String hex(byte[] bytes) {
		return Hex.encodeHexString(bytes);
	}

	public static byte[] hex(String str) {
		try {
			return Hex.decodeHex(str.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalStateException(e);
		}
	}

	private IllegalStateException fail(Exception e) {
		return new IllegalStateException(e);
	}

	public static void main(String[] args){
		System.out.println(random(16).substring(0,16));

		// 随机生成密钥
		byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();

		System.out.println(new String(key));


	}


}