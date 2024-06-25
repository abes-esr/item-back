package fr.abes.item.security;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.exception.WsAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class CustomAuthenticationManager implements AuthenticationManager {

    private final AuthenticationEventPublisher authenticationEventPublisher;

    private final UtilisateurService utilisateurService;

    @Value("${wsAuthSudoc.url}")
    String urlWsAuthSudoc;

    public CustomAuthenticationManager(AuthenticationEventPublisher authenticationEventPublisher, UtilisateurService utilisateurService) {
        this.authenticationEventPublisher = authenticationEventPublisher;
        this.utilisateurService = utilisateurService;
    }


    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        log.debug(Constant.ENTER_AUTHENTICATE);

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        User u = this.callWsAuth(name, password);

        if (u != null) {

            u.setMail(this.getEmail(Integer.parseInt(u.getUserNum())));
            List<GrantedAuthority> authorities;
            if (u.getRole() != null && (u.getRole().equals("USER") || u.getRole().equals("ADMIN"))) {
                authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(u.getRole()));
            } else {
                authorities = Collections.emptyList();
            }
            u.setAuthorities(authorities);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(u, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            authenticationEventPublisher.publishAuthenticationSuccess(auth);
            return auth;
        }
        else {
            authenticationEventPublisher.publishAuthenticationFailure(new BadCredentialsException(Constant.WRONG_LOGIN_AND_OR_PASS), authentication);
            throw new BadCredentialsException(Constant.WRONG_LOGIN_AND_OR_PASS);
        }
    }


    private User callWsAuth(String userKey, String password) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String requestJson = "{\n" +
                    "\t\"userKey\": \"" + userKey + "\",\n" +
                    "\t\"password\": \"" + password + "\"\n" +
                    "}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            return restTemplate.postForObject(this.urlWsAuthSudoc, entity, User.class);
        }
        catch (Exception e) {
            log.error(Constant.ERROR_SUDOC_WS_AUTHENTICATION + e);
            throw new WsAuthException(e.getMessage());
        }
    }
    public String getEmail(Integer userNum) {
        try {
            return utilisateurService.findEmailById(userNum);
        }
        catch (EmptyResultDataAccessException e)
        {
            return ""; // genere l'erreur "the given id must not be null"
        }
    }
}
