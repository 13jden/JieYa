package com.example.common.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时
    private static final String SECRET = "jieya-secret-key-32-bytes-long-long";  // 确保密钥长度至少为 32 字节

    // 生成token
    public static String generateToken(String username) throws Exception {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRE_TIME);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(now)
                .expirationTime(expiration)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(new MACSigner(SECRET.getBytes()));

        return signedJWT.serialize();
    }

    // 验证token
    public static boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(SECRET.getBytes());
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            return false;
        }
    }

    // 从token中获取用户名
    public static String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}