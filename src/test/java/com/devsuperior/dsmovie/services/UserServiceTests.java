package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserUtil userUtil;

    private String existingUsername;
    private String nonExistingUsername;
    private UserEntity user;
    private List<UserDetailsProjection> userDetails;

    @BeforeEach
    void setup() {
        existingUsername = "maria@gmail.com";
        nonExistingUsername = "fake@gmail.com";
        user = UserFactory.createUserEntity();
        userDetails = UserDetailsFactory.createCustomClientUser(existingUsername);

        Mockito.when(userRepository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userDetails);
        Mockito.when(userRepository.searchUserAndRolesByUsername(nonExistingUsername)).thenReturn(List.of());
        Mockito.when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());
    }

    @Test
    public void authenticatedShouldReturnUserEntityWhenUserExists() {
        Mockito.when(userUtil.getLoggedUsername()).thenReturn(existingUsername);
        UserEntity result = userService.authenticated();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingUsername, result.getUsername());
        Assertions.assertEquals(user.getPassword(), result.getPassword());
        Assertions.assertEquals(user.getRoles().size(), result.getRoles().size());
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Mockito.when(userUtil.getLoggedUsername()).thenReturn(nonExistingUsername);

        Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.authenticated());
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        UserDetails result = userService.loadUserByUsername(existingUsername);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingUsername, result.getUsername());
        Assertions.assertEquals(user.getPassword(), result.getPassword());
        Assertions.assertEquals(user.getRoles().size(), result.getAuthorities().size());
    }

    @Test
    public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(nonExistingUsername));
    }
}
