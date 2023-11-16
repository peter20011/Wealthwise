import com.example.wealthwise_api.DAO.UserDAO;
import com.example.wealthwise_api.DTO.AuthenticationFailedResponse;
import com.example.wealthwise_api.DTO.RegistrationResponse;
import com.example.wealthwise_api.DTO.UserRegistrationRequest;
import com.example.wealthwise_api.Entity.UserEntity;
import com.example.wealthwise_api.Services.EmailValidatorService;
import com.example.wealthwise_api.Services.PasswordValidatorService;
import com.example.wealthwise_api.Services.RegistrationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class RegistrationServiceTest {
    @InjectMocks
    private RegistrationService registrationService;
    @Mock
    private UserDAO userDAO;
    @Mock
    private EmailValidatorService emailValidatorService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordValidatorService passwordValidatorService;


    @Test
    public void testSuccessfulRegistration() {
        // Arrange
        UserRegistrationRequest request = createValidUserRegistrationRequest();

        // Mock the behavior of emailValidatorService
        when(emailValidatorService.test(anyString())).thenReturn(true);

        when(userDAO.existsUserWithEmail(request.email())).thenReturn(false);

        // Act
        ResponseEntity<Object> response = registrationService.register(request);
        System.out.println(response.getBody());
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof RegistrationResponse);
        assertEquals("User registered successfully", (((RegistrationResponse) response.getBody()).message()));
    }

    @Test
    public void testRegistrationWithEmptyFields() {
        // Arrange
        UserRegistrationRequest request = createInvalidUserRegistrationRequest();


        // Act
        ResponseEntity<Object> response = registrationService.register(request);


        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthenticationFailedResponse);
        assertEquals("Please fill all fields!", ((AuthenticationFailedResponse) response.getBody()).message());
    }


    private UserRegistrationRequest createValidUserRegistrationRequest() {
        return new UserRegistrationRequest("John", "Wick", "12-12-2001", "john.doe@example.com", "Password1", "Password1");
    }

    private UserRegistrationRequest createInvalidUserRegistrationRequest() {
        return new UserRegistrationRequest("", "", "", "", "", "");
    }

}

