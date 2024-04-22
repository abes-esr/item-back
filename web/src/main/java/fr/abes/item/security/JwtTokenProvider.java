package fr.abes.item.security;


import fr.abes.item.core.constant.Constant;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public String generateToken(User u) {

        Date now = Calendar.getInstance().getTime();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(u.getUserKey())// USER_KEY de la base
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("userNum", u.getUserNum())
                .claim("iln", u.getIln())
                .claim("library", u.getLibrary())
                .claim("rcr", u.getLibRcr())
                .claim("loginAllowed", u.getLoginAllowed())
                .claim("role", u.getRole())
                .claim("shortName", u.getShortName())
                .claim("userGroup", u.getUserGroup())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error(Constant.JWT_SIGNATURE_INVALID, ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error(Constant.JWT_TOKEN_INVALID, ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error(Constant.JWT_TOKEN_EXPIRED, ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error(Constant.JWT_TOKEN_UNSUPPORTED, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error(Constant.JWT_CLAIMS_STRING_EMPTY, ex.getMessage());
        }
        return false;
    }
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public User getUtilisateurFromJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        User u = new User();
        u.setUserNum(claims.get("userNum").toString());
        u.setIln(claims.get("iln").toString());
        u.setLibrary(claims.get("library").toString());
        u.setLibRcr(claims.get("rcr").toString());
        u.setLoginAllowed(claims.get("loginAllowed").toString());
        u.setRole(claims.get("role").toString());
        u.setShortName(claims.get("shortName").toString());
        u.setUserGroup(claims.get("userGroup").toString());
        return u;
    }
}
