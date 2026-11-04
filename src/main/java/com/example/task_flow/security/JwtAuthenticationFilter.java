package com.example.task_flow.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class JwtAuthenticationFilter extends  OncePerRequestFilter{
	private final JwtService jwtService;
	private final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	@Override 
	protected void doFilterInternal(
		HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain
	) throws ServletException, IOException{

    		log.info("called filter auth");


    String authHeader = request.getHeader("Authorization");
    if(authHeader == null || !authHeader.startsWith("Bearer ")){
    	filterChain.doFilter(request, response);
    	return;
    }
    String accessToken = authHeader.substring(7);
    try {
    	    jwtService.isTokenValid(accessToken);
    		log.info("accessToken success validate");
    		String email = jwtService.extractEmail(accessToken);
    		Long userId = jwtService.extractUserId(accessToken);

    		if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
    			CustomUserDetails userDetails = new CustomUserDetails(userId, email);
    			 UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
    		authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    		SecurityContextHolder.getContext().setAuthentication(authToken);
    		}
    	
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
    		log.info("called catch filter io.jsonwebtoken.ExpiredJwtException");

            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Access token expired\", \"code\": 401}");
            return;
        } catch (Exception e) {
    		log.info("called catch filter Exception");

            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token\", \"code\": 401}");
            return;
        }
        filterChain.doFilter(request, response);
	}
}