package com.mysunriser.backend.security;

import com.mysunriser.backend.Dao.UserDao;
import com.mysunriser.backend.entity.UserAccount;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final UserDao userDao;

    public JwtAuthFilter(JwtService jwtService, UserDao userDao) {
        this.jwtService = jwtService;
        this.userDao = userDao;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null
                && authHeader.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            if (!token.isEmpty() && jwtService.isTokenValid(token)) {
                try {
                    String username = jwtService.extractUsername(token);
                    int tokenVersion = jwtService.extractTokenVersion(token);
                    UserAccount user = userDao.findByUsername(username);
                    if (!isUserTokenCurrent(user, tokenVersion)) {
                        filterChain.doFilter(request, response);
                        return;
                    }

                    String role = user.getRole();
                    String normalizedRole = role == null ? "USER" : role.toUpperCase(Locale.ROOT);

                    List<GrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole));

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (RuntimeException e) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isUserTokenCurrent(UserAccount user, int tokenVersion) {
        if (user == null || !"active".equalsIgnoreCase(user.getStatus())) {
            return false;
        }

        Integer currentVersion = user.getTokenVersion();
        return currentVersion != null && currentVersion == tokenVersion;
    }
}
