package com.parking.api_gateway.security.service;
import com.parking.api_gateway.security.entity.UserSecurityEntity;
import com.parking.api_gateway.security.entity.UserSecurityEntity.Role;
import com.parking.api_gateway.security.repository.UserSecurityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
/**
 * Unit tests for initializeDefaultUsers() in UserSecurityService (Issue #80).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserSecurityService - default users initialization (Issue #80)")
class UserSecurityServiceDefaultUsersTest {
    @Mock
    private UserSecurityRepository userRepository;
    @Mock
    private SecurityAuditService auditService;
    private UserSecurityService buildService() {
        return new UserSecurityService(userRepository, auditService);
    }
    // Test 1: both users created when DB is empty
    @Test
    @DisplayName("Creates both admin and operator when neither exists in DB")
    void createsBothUsersWhenDbIsEmpty() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.existsByUsername("operator")).thenReturn(false);
        when(userRepository.save(any(UserSecurityEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        buildService().initializeDefaultUsers();
        ArgumentCaptor<UserSecurityEntity> captor = ArgumentCaptor.forClass(UserSecurityEntity.class);
        verify(userRepository, times(2)).save(captor.capture());
        List<UserSecurityEntity> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);
        UserSecurityEntity admin = saved.stream()
                .filter(u -> "admin".equals(u.getUsername()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("admin not saved"));
        UserSecurityEntity operator = saved.stream()
                .filter(u -> "operator".equals(u.getUsername()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("operator not saved"));
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getEnabled()).isTrue();
        assertThat(admin.getForcePasswordChange()).isTrue();
        assertThat(admin.getPassword()).startsWith("$2a$");
        assertThat(operator.getRole()).isEqualTo(Role.OPERATOR);
        assertThat(operator.getEnabled()).isTrue();
        assertThat(operator.getForcePasswordChange()).isTrue();
        assertThat(operator.getPassword()).startsWith("$2a$");
    }
    // Test 2: no duplicates on second call
    @Test
    @DisplayName("Does NOT create duplicates when both users already exist")
    void doesNotCreateDuplicatesWhenUsersExist() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.existsByUsername("operator")).thenReturn(true);
        buildService().initializeDefaultUsers();
        verify(userRepository, never()).save(any(UserSecurityEntity.class));
    }
    // Test 3: only operator created when admin already exists
    @Test
    @DisplayName("Creates only operator when admin already exists")
    void createsOnlyOperatorWhenAdminExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        when(userRepository.existsByUsername("operator")).thenReturn(false);
        when(userRepository.save(any(UserSecurityEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        buildService().initializeDefaultUsers();
        ArgumentCaptor<UserSecurityEntity> captor = ArgumentCaptor.forClass(UserSecurityEntity.class);
        verify(userRepository, times(1)).save(captor.capture());
        UserSecurityEntity saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("operator");
        assertThat(saved.getRole()).isEqualTo(Role.OPERATOR);
    }
}