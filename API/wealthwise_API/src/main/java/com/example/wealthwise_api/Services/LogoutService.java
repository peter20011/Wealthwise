package com.example.wealthwise_api.Services;

import com.example.wealthwise_api.Entity.UserEntity;
import com.example.wealthwise_api.Util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutHandler {

    private final JWTUtil jwtUtil;
    Logger logger = LoggerFactory.getLogger(LogoutService.class);
    private final UserServiceDetails userServiceDetails;


    public LogoutService(JWTUtil jwtUtil, UserServiceDetails userServiceDetails) {
        this.jwtUtil = jwtUtil;
        this.userServiceDetails = userServiceDetails;
    }


    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        try{
            final String authHeader = request.getHeader("Authorization");

            if(authHeader == null ||!authHeader.startsWith("Bearer ")) {
                return;
            }

            String jwt= authHeader.substring(7);
            String email = jwtUtil.getSubject(jwt);//email in our case
            logger.warn("logout - email "+email);

            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserEntity userEntity = (UserEntity) userServiceDetails.loadUserByUsername(email);
                logger.warn("logout - userEntity "+userEntity);

                if(jwtUtil.isTokenValid(jwt, userEntity.getEmail())) {
                    SecurityContextHolder.clearContext();

                }}
        }catch (Exception e){
            logger.warn("logout - exception "+e.getMessage());
        }

    }
}