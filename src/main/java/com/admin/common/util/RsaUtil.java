package com.admin.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 公钥和私钥生成工具类
 */
@Component
@Slf4j
public class RsaUtil {

    // 定义使用的算法为RSA
    private static final String ALGORITHM = "RSA";
    // 定义密钥长度为2048位，提供更高的安全性
    private static final int KEY_SIZE = 2048;


    /**
     * 生成RSA密钥对（公钥和私钥）
     * @return KeyPair 包含公钥和私钥的对象
     * @throws Exception 如果生成过程中出现错误则抛出异常
     */
    public static KeyPair generateKeyPair() throws Exception {
        // 获取指定算法的密钥对生成器实例
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        // 初始化密钥对生成器，指定密钥长度
        keyPairGenerator.initialize(KEY_SIZE);
        // 生成并返回密钥对
        return keyPairGenerator.genKeyPair();
    }

    /**
     * 使用公钥加密数据
     * @param data 要加密的数据
     * @param publicKey 加密用的公钥
     * @return byte[] 加密后的数据
     * @throws Exception 如果加密过程中出现错误则抛出异常
     */
    public static byte[] encryptByPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        // 获取指定算法的密码器实例
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        // 初始化密码器为加密模式，并指定使用哪个公钥
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 执行加密操作并返回结果
        return cipher.doFinal(data);
    }

    /**
     * 使用私钥解密数据
     * @param encryptedData 要解密的数据
     * @param privateKey 解密用的私钥
     * @return byte[] 解密后的原始数据
     * @throws Exception 如果解密过程中出现错误则抛出异常
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        // 获取指定算法的密码器实例
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        // 初始化密码器为解密模式，并指定使用哪个私钥
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        // 执行解密操作并返回结果
        return cipher.doFinal(encryptedData);
    }


}
