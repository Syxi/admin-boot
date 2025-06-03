package com.admin.common.security.service;

import com.admin.module.system.vo.AuthTokenVO;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

/**
 * 令牌接口
 */
public interface TokenService {


     /**
      * 生成认证 Token
      *
      * @param authentication 用户认证信息
      * @return 认证 Token 响应
      */
     AuthTokenVO generateToken(Authentication authentication);

     /**
      * 解析 Token 获取认证信息
      *
      * @param token JWT Token
      * @return 用户认证信息
      */
     Authentication parseToken(String token);


     /**
      * 获取 token 的Claims, claims中包含了用户的基本信息
      * @param token
      * @return
      */
     Claims getTokenClaims(String token);


     /**
      * 校验 Token 是否有效
      *
      * @param token JWT Token
      * @return 是否有效
      */
     boolean validateToken(String token);


     /**
      *  刷新 Token
      *
      * @param refreshToken 刷新令牌
      * @return 认证 Token 响应
      */
     AuthTokenVO refreshToken(String refreshToken);

     /**
      * 将 Token 加入黑名单
      *
      * @param token JWT Token
      */
     default void blacklistToken(String token) {

     }
}
