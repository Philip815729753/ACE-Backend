package com.ace.service;

import com.ace.mapper.AdministratorMapper;
import com.ace.pojo.Administrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdministratorServiceTest {

    @Mock
    private AdministratorMapper administratorMapper;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AdminService administratorService;

    private Administrator administrator;

    @BeforeEach
    void setUp() {
        administrator = new Administrator();
        administrator.setUsername("testUser");
        administrator.setPassword("testPass");
    }

    /**
     * Test the admin register method
     */
    @Test
    void testAddAdministrator() {
        administratorService.register(administrator);
        verify(administratorMapper, times(1)).insertAdministrator(administrator);
    }

    /**
     *
     * This test verifies that the `findByUsername` method on the `administratorMapper` mock
     * is called exactly once with the string "testUser" as the argument when the `findByUsername`
     * method of `administratorMapper` is invoked. It also checks that the returned `Administrator`
     * object has the expected username.
     */
    @Test
    void testGetAdministratorByUsername() {
        when(administratorMapper.findByUsername("testUser")).thenReturn(administrator);
        Administrator found = administratorMapper.findByUsername("testUser");
        verify(administratorMapper, times(1)).findByUsername("testUser");
        assertEquals("testUser", found.getUsername());
    }


    /**
     * Test the verify method
     *
     * This test verifies that the `verify` method correctly authenticates the administrator
     * and returns a JWT token if authentication is successful, or "fail" if it is not.
     */
    @Test
    void testVerify() {
        Authentication authentication = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken("testUser")).thenReturn("mockToken");

        String result = administratorService.verify(administrator);
        assertEquals("mockToken", result);

        when(authentication.isAuthenticated()).thenReturn(false);
        result = administratorService.verify(administrator);
        assertEquals("fail", result);
    }
}
