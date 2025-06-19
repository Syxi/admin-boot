package com.admin.web;


import com.admin.common.enums.SwitchConfigEnum;
import com.admin.common.result.ResultVO;
import com.admin.module.system.service.AuthService;
import com.admin.module.system.service.impl.RsaServiceImpl;
import com.admin.module.system.vo.AuthTokenVO;
import com.admin.module.system.vo.CaptchaVO;
import com.admin.module.system.vo.LoginParams;
import com.admin.module.system.vo.RefreshToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * @author suYan
 * @date 2023/4/9 16:10
 */
@Tag(name = "登录认证")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final RedisTemplate<String, String> redisTemplate;

    private final RsaServiceImpl rsaService;


    @Operation(summary = "登录")
    @PostMapping("/login")
    public ResultVO<AuthTokenVO> login(@RequestBody LoginParams loginParams) {
        AuthTokenVO authTokenVO = authService.login(loginParams);
       return ResultVO.success(authTokenVO);
    }


    @Operation(summary = "注销")
    @DeleteMapping("/logout")
    public ResultVO<Boolean> logout() {
        authService.logout();
        return ResultVO.success();
    }


    @Operation(summary = "刷新token")
    @PostMapping("/refreshToken")
    public ResultVO<?> refreshToken(@RequestBody RefreshToken refreshToken) {
        AuthTokenVO authTokenVO = authService.refreshToken(refreshToken.getRefreshToken());
        return ResultVO.success(authTokenVO);


    }

    /**
     *
     * @return
     */
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public ResultVO<CaptchaVO> getCaptcha() {
        CaptchaVO captchaVO = authService.getCaptcha();
        return ResultVO.success(captchaVO);
    }

    @Operation(summary = "登录验证码开关")
    @GetMapping("/captchaEnabled")
    public ResultVO<Boolean> isCaptchaEnabled() {
        String captchaEnabled = redisTemplate.opsForValue().get(SwitchConfigEnum.captcha_enabled.getKey());
        boolean isCaptchaEnabled = Boolean.parseBoolean(captchaEnabled);
        return ResultVO.success(isCaptchaEnabled);
    }

    @Operation(summary = "加密公钥")
    @GetMapping("/aes/secretKey")
    public ResultVO<String> getAesSecretKet() {
        // 获取公钥
        PublicKey publicKey = rsaService.getPublicKey();
        // 将公钥编码为 Base64格式的字符串
        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return ResultVO.success(encodedKey);
    }

    // jwt 签署密钥随机生成
    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256); // For HS256, the key size should be at least 256 bits
        SecretKey secretKey = keyGen.generateKey();

        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("Generated Base64 Encoded Key: " + encodedKey);
    }
}
