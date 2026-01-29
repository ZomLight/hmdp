package com.hmdp.utils;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
@ConfigurationProperties(prefix = "jwt.token")
public class JwtHelper {
    private  long tokenExpiration;
    private  String tokenSignKey;

    /**
     * 生成token字符串
     */
    public String createToken(String phone) {
        System.out.println("tokenExpiration = " + tokenExpiration);
        System.out.println("tokenSignKey = " + tokenSignKey);
        String token = Jwts.builder()
                .setSubject("YYGH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration*1000*60)) //单位分钟
                .claim("phone", phone)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }


    /**
     * 从token字符串获取phone
     */
    public  String getPhone(String token) {
        if(StrUtil.isEmpty(token)){
            return null;
        }
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        String phone = (String)claims.get("phone");
        return phone;
    }

    /**
     * 判断token是否有效
     * @param token
     * @return boolean
     */
    public  boolean isExpiration(String token){
        try {
            boolean isExpire = Jwts.parser()
                    .setSigningKey(tokenSignKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
            return isExpire;
        } catch (Exception e) {
            return true;
        }
    }
}
