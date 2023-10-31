package com.example.wealthwise_api.Services;


import com.example.wealthwise_api.DAO.UserDAO;
import com.example.wealthwise_api.DTO.TokenRequest;
import com.example.wealthwise_api.DTO.UserDataResponse;
import com.example.wealthwise_api.Entity.UserEntity;
import com.example.wealthwise_api.Util.JWTUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDAO userDAO;
    private final JWTUtil jwtUtil;

    public UserService(@Qualifier("jpa") UserDAO userDAO, JWTUtil jwtUtil) {
        this.userDAO = userDAO;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<?> getDataUser(TokenRequest tokenResponse){
        try {
            String email = jwtUtil.getSubject(tokenResponse.token());

            UserEntity userEntity = userDAO.findUserByEmail(email);

            if(userEntity == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            UserDataResponse userDataResponse = new UserDataResponse(userEntity.getName(), userEntity.getSurname(), userEntity.getEmail(), userEntity.getBirthDay());

            return new ResponseEntity<>(userDataResponse, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
