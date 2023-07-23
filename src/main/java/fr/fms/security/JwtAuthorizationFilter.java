package fr.fms.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;

public class JwtAuthorizationFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //permet les accès de domaines différent du back
        /* if (response.getHeader("Access-Control-Allow-Origin").isEmpty())*/
        response.addHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, " + "Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
        response.addHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin, Access-Control-Allow-Credentials, Authorization"); response.addHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,DELETE,PUT");
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        System.out.println(token);

        //response.setStatus(HttpServletResponse.SC_OK);

       /*if (request.getMethod().equals("OPTIONS")) {  //si la requete contient une OPTION renvoyer OK -- côté front : Authorization
            response.setStatus(HttpServletResponse.SC_OK);  //Status code (200) indicating the request succeeded normally
            System.out.println(response.getStatus());
        } else*/
            if (token != null && token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            try {
                String jwtToken = token.substring(7);
                System.out.println(jwtToken);
                JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(SecurityConstants.SECRET)).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(jwtToken);

                String username = decodedJWT.getSubject();
                String roles[] = decodedJWT.getClaim("roles").asArray(String.class);
                Collection<GrantedAuthority> authorities = new ArrayList<>();
                for (String role : roles) authorities.add(new SimpleGrantedAuthority(role));
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                System.out.println(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } catch (Exception e) {
                response.setHeader(SecurityConstants.ERROR_MSG, e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        filterChain.doFilter(request, response);
    }

}


