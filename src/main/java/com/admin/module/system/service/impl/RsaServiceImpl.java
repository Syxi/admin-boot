package com.admin.module.system.service.impl;

import com.admin.common.util.RsaUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

@Slf4j
@Service
public class RsaServiceImpl {

    // 存储密钥对的成员变量
    private KeyPair keyPair;



    /**
     * 初始化方法，在对象创建时自动生成密钥对
     * @throws Exception 如果生成密钥对过程中出现问题则抛出异常
     */
    @PostConstruct
    public void init() throws Exception {
        this.keyPair = RsaUtil.generateKeyPair();
    }

    /**
     * 获取当前生成的公钥
     * @return PublicKey 当前的公钥
     */
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    /**
     * 使用私钥解密传入的文本
     * @param encryptedText 需要解密的Base64编码的字符串
     * @return String 解密后的原文本
     * @throws Exception 如果解码或解密过程中出现问题则抛出异常
     */
    public String decrypt(String encryptedText)  {
        try {
            // 将Base64编码的字符串解码回字节数组
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            // 使用私钥解密字节数组得到原始数据
            byte[] decrypted = RsaUtil.decryptByPrivateKey(decoded, keyPair.getPrivate());
            // 将解密后的字节数组转换成字符串并返回
            return new String(decrypted);
        } catch (Exception e) {
            log.error("解密失败{}: ", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
