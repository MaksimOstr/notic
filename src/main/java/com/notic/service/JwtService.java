package com.notic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${JWT_EXPIRE_TIME}")
    private long jwtExpireTime;

    @Value("${JWT_ISSUER}")
    private String jwtIssuer;

    @Value("${JWT_HEADER_ALG}")
    private String jwtHeaderAlg;

    private final JwtEncoder jwtEncoder;

    public String getJwsToken(Collection<String> authorities, Long subjectId) {
        return generateToken(subjectId.toString(), authorities);
    }

    private String generateToken(String subject, Collection<String> authorities) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtExpireTime);
        JwsHeader jwsHeader = JwsHeader.with(() -> jwtHeaderAlg).build();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(subject)
                .issuer(jwtIssuer)
                .claim("roles", authorities)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }
}
