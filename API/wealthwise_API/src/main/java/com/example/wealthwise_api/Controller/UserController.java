package com.example.wealthwise_api.Controller;


import com.example.wealthwise_api.DTO.ChangePasswordRequest;
import com.example.wealthwise_api.Services.ChangePasswordService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.SecurityMarker;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private ChangePasswordService changePasswordService;

    public UserController(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    @PostMapping(value="/changePassword", produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request){
        return changePasswordService.changePassword(request);
    }

}
