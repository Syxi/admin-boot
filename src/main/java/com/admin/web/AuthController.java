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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Base64;

/**
 * 登录认证控制器
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

    /**
     * 用户登录
     *
     * @param loginParams 登录参数（用户名、密码、验证码等）
     * @return 认证令牌（AccessToken和RefreshToken）
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public ResultVO<AuthTokenVO> login(@Valid @RequestBody LoginParams loginParams) {
        log.debug("用户登录请求: username={}", loginParams.getUsername());
        AuthTokenVO authTokenVO = authService.login(loginParams);
        return ResultVO.success(authTokenVO);
    }

    /**
     * 用户注销
     *
     * @return 注销结果
     */
    @Operation(summary = "注销")
    @DeleteMapping("/logout")
    public ResultVO<Boolean> logout() {
        log.debug("用户注销请求");
        authService.logout();
        return ResultVO.success();
    }

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    @Operation(summary = "刷新token")
    @PostMapping("/refreshToken")
    public ResultVO<AuthTokenVO> refreshToken(@Valid @RequestBody RefreshToken refreshToken) {
        log.debug("刷新token请求");
        AuthTokenVO authTokenVO = authService.refreshToken(refreshToken.getRefreshToken());
        return ResultVO.success(authTokenVO);
    }

    /**
     * 获取验证码
     *
     * @return 验证码图片的Base64编码和验证码关键字
     */
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public ResultVO<CaptchaVO> getCaptcha() {
        CaptchaVO captchaVO = authService.getCaptcha();
        return ResultVO.success(captchaVO);
    }

    /**
     * 获取验证码开关状态
     *
     * @return 验证码是否启用
     */
    @Operation(summary = "登录验证码开关")
    @GetMapping("/captchaEnabled")
    public ResultVO<Boolean> isCaptchaEnabled() {
        String captchaEnabled = redisTemplate.opsForValue().get(SwitchConfigEnum.captcha_enabled.getKey());
        boolean isCaptchaEnabled = Boolean.parseBoolean(captchaEnabled);
        return ResultVO.success(isCaptchaEnabled);
    }

    /**
     * 获取RSA公钥，用于前端加密敏感信息
     *
     * @return Base64编码的公钥
     */
    @Operation(summary = "加密公钥")
    @GetMapping("/aes/secretKey")
    public ResultVO<String> getAesSecretKey() {
        PublicKey publicKey = rsaService.getPublicKey();
        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return ResultVO.success(encodedKey);
    }
}
