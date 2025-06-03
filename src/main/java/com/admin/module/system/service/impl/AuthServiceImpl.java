package com.admin.module.system.service.impl;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.CodeGenerator;
import com.admin.common.enums.CaptchaTypeEnum;
import com.admin.common.properties.CaptchaProperties;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.service.TokenService;
import com.admin.module.system.service.AuthService;
import com.admin.module.system.vo.AuthTokenVO;
import com.admin.module.system.vo.CaptchaVO;
import com.admin.module.system.vo.LoginParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *@author sy
 *@date 2023/9/14 9:07
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {


    private final RedisTemplate<String, Object> redisTemplate;

    private final AuthenticationManager authenticationManager;

    private final CaptchaProperties captchaProperties;

    private final CodeGenerator codeGenerator;

    private final  Font captchaFont;

    private final TokenService tokenService;

    private final RsaServiceImpl rsaService;


    @Override
    public AuthTokenVO login(LoginParams loginParams) {
        // 解密
        String decryptUsername = rsaService.decrypt(loginParams.getUsername());
        String decryptPassword = rsaService.decrypt(loginParams.getPassword());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(decryptUsername, decryptPassword);
        // 创建用于密码认证的令牌
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginParams.getUsername(), loginParams.getPassword());
        // 执行认证 （认证中）
        Authentication authentication = authenticationManager.authenticate(token);
        // 认证成功后生成 jwt 令牌
        AuthTokenVO authTokenVO = tokenService.generateToken(authentication);
        // token 存到security上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authTokenVO;
    }

    /**
     * 注销
     */
    @Override
    public void logout() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(token) && token.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
            token = token.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());
            // 将jwt令牌加入黑名单
            tokenService.blacklistToken(token);
        }
        // 清除security上下文
        SecurityContextHolder.clearContext();
    }

    /**
     * 获取验证码
     *
     * @return
     */
    @Override
    public CaptchaVO getCaptcha() {

        String captchaType = captchaProperties.getType();
        int width = captchaProperties.getWidth();
        int height = captchaProperties.getHeight();
        int interfereCount = captchaProperties.getInterfereCount();
        int codeLength = captchaProperties.getCode().getLength();

        AbstractCaptcha captcha;
        if (CaptchaTypeEnum.CIRCLE.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createCircleCaptcha(width, height, codeLength, interfereCount);
        } else if (CaptchaTypeEnum.GIG.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createGifCaptcha(width, height, codeLength);
        } else if (CaptchaTypeEnum.LINE.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createLineCaptcha(width, height, height, interfereCount);
        } else if (CaptchaTypeEnum.SHEAR.name().equalsIgnoreCase(captchaType)) {
            captcha = CaptchaUtil.createShearCaptcha(width, height, height, interfereCount);
        } else {
            throw new IllegalArgumentException("Invalid captcha type: " + captchaType);
        }

        captcha.setGenerator(codeGenerator);
        captcha.setTextAlpha(captchaProperties.getTextAlpha());
        captcha.setFont(captchaFont);

        String captchaCode = captcha.getCode();
        String imageBase64Data = captcha.getImageBase64Data();

        // 验证码文本缓存至redis，用于登录校验
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(SecurityConstants.CAPTCHA_CODE_PREFIX + captchaKey, captchaCode,
                captchaProperties.getExpireSeconds(), TimeUnit.SECONDS);

        return CaptchaVO.builder()
                .captchaKey(captchaKey)
                .captchaBase64(imageBase64Data)
                .build();
    }

    /***
     * 刷新token
     * @param refreshToken
     * @return
     */
    @Override
    public AuthTokenVO refreshToken(String refreshToken) {
        return tokenService.refreshToken(refreshToken);
    }




}
