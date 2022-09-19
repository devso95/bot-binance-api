package deso.future_bot.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TokenProvider {
    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private static final String MERCHANT_KEY = "merchant";

    private static final String PERMISSION_KEY = "permission";

    private static final String ADMIN_PERMISSION_KEY = "admin_permission";

    private static final String DRIVER_ID_KEY = "driver_id";

    private static final String SESSION = "session";

    private static final String TRANSPORTATION_COMPANY_KEY = "transportation";

    private final Key key;

    private final JwtParser jwtParser;

    private final long tokenValidityInMilliseconds;

    private final long tokenValidityInMillisecondsForRememberMe;

    @Value("${jwt.secret.key}")
    private String secret;

    public TokenProvider() {
        byte[] keyBytes;
        log.debug("Using a Base64-encoded JWT secret key " + secret);
        if (secret == null) {
            secret = "Njk0ZDQwMmRlNGZjZWNlZGIyN2ZmMzM3ZTk4ZTQ1MTBmMzdhMDk2ZDIyZWIwNDE2ZDM3YjMwNWVkMjQ0OGVlZTMwYjUwMDMwMWZhMWQyYTYxN2YyN2Y4OGQ0MjBhZDNiOQ==";
        }
        keyBytes = Decoders.BASE64.decode(secret);
        key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        this.tokenValidityInMilliseconds = 1000 * 86400L;
        this.tokenValidityInMillisecondsForRememberMe = 1000 * 30 * 86400L;
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES_KEY, authorities);

        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(authentication.getName())
                .setId(String.valueOf(customUserDetails.getId()))
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .setIssuedAt(new Date(now))
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        Long userId = getUserIdFromToken(claims);

        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .filter(auth -> !auth.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        CustomUserDetails principal = new CustomUserDetails(userId, claims.getSubject(), StringUtils.EMPTY, authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        try {
            jwtParser.parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }

    public Long getUserIdFromToken(Claims claims) {
        String claim = getClaimFromToken(claims, Claims::getId);
        return Long.parseLong(claim);
    }

    public Long getSession(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        return getData(claims, SESSION);
    }

    private Long getData(Claims claims, String permissionKey) {
        try {
            Object value = claims.get(permissionKey);
            if (value != null) {
                String claim = String.valueOf(value);
                return Long.parseLong(claim);
            } else {
                return null;
            }
        } catch (Exception ignore) {
            return null;
        }
    }

    private Boolean getDataBoolean(Claims claims, String permissionKey) {
        try {
            Object value = claims.get(permissionKey);
            if (value != null) {
                String claim = String.valueOf(value);
                return Boolean.parseBoolean(claim);
            } else {
                return null;
            }
        } catch (Exception ignore) {
            return null;
        }
    }

    public static <T> T getClaimFromToken(Claims claims, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(claims);
    }

}
