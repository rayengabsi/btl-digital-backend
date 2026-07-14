package com.btl.digital;

import com.btl.digital.dto.request.CreateBeneficiaryRequest;
import com.btl.digital.dto.request.UpdateBeneficiaryRequest;
import com.btl.digital.dto.response.BeneficiaryResponse;
import com.btl.digital.entity.Beneficiary;
import com.btl.digital.entity.User;
import com.btl.digital.exception.ResourceNotFoundException;
import com.btl.digital.repository.BeneficiaryRepository;
import com.btl.digital.repository.UserRepository;
import com.btl.digital.service.impl.BeneficiaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTests {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BeneficiaryServiceImpl beneficiaryService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    private void mockAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(owner));
    }

    @Test
    void testGetMyBeneficiaries() {
        mockAuthentication();
        Beneficiary beneficiary = Beneficiary.builder()
                .id(1L)
                .nickname("My Friend")
                .beneficiaryName("Friend Name")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryIban("TN00123456789")
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        when(beneficiaryRepository.findByOwnerOrderByCreatedAtDesc(owner)).thenReturn(List.of(beneficiary));

        List<BeneficiaryResponse> result = beneficiaryService.getMyBeneficiaries();

        assertEquals(1, result.size());
        assertEquals("My Friend", result.get(0).getNickname());
        assertEquals("Friend Name", result.get(0).getBeneficiaryName());
        verify(beneficiaryRepository, times(1)).findByOwnerOrderByCreatedAtDesc(owner);
    }

    @Test
    void testGetBeneficiary_Success() {
        mockAuthentication();
        Beneficiary beneficiary = Beneficiary.builder()
                .id(1L)
                .nickname("My Friend")
                .beneficiaryName("Friend Name")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryIban("TN00123456789")
                .owner(owner)
                .build();

        when(beneficiaryRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(beneficiary));

        BeneficiaryResponse result = beneficiaryService.getBeneficiary(1L);

        assertNotNull(result);
        assertEquals("My Friend", result.getNickname());
        verify(beneficiaryRepository, times(1)).findByIdAndOwner(1L, owner);
    }

    @Test
    void testGetBeneficiary_NotFound() {
        mockAuthentication();
        when(beneficiaryRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> beneficiaryService.getBeneficiary(1L));
        verify(beneficiaryRepository, times(1)).findByIdAndOwner(1L, owner);
    }

    @Test
    void testCreateBeneficiary_Success() {
        mockAuthentication();
        CreateBeneficiaryRequest request = CreateBeneficiaryRequest.builder()
                .nickname("My Friend")
                .beneficiaryName("Friend Name")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryIban("TN00123456789")
                .build();

        when(beneficiaryRepository.existsByOwnerAndBeneficiaryAccountNumber(owner, "123456789")).thenReturn(false);
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(invocation -> {
            Beneficiary b = invocation.getArgument(0);
            b.setId(1L);
            b.setCreatedAt(LocalDateTime.now());
            return b;
        });

        BeneficiaryResponse result = beneficiaryService.createBeneficiary(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("My Friend", result.getNickname());
        verify(beneficiaryRepository, times(1)).existsByOwnerAndBeneficiaryAccountNumber(owner, "123456789");
        verify(beneficiaryRepository, times(1)).save(any(Beneficiary.class));
    }

    @Test
    void testCreateBeneficiary_DuplicateAccountNumber() {
        mockAuthentication();
        CreateBeneficiaryRequest request = CreateBeneficiaryRequest.builder()
                .nickname("My Friend")
                .beneficiaryName("Friend Name")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryIban("TN00123456789")
                .build();

        when(beneficiaryRepository.existsByOwnerAndBeneficiaryAccountNumber(owner, "123456789")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> beneficiaryService.createBeneficiary(request));
        verify(beneficiaryRepository, times(1)).existsByOwnerAndBeneficiaryAccountNumber(owner, "123456789");
        verify(beneficiaryRepository, never()).save(any(Beneficiary.class));
    }

    @Test
    void testUpdateBeneficiary_Success() {
        mockAuthentication();
        UpdateBeneficiaryRequest request = UpdateBeneficiaryRequest.builder()
                .nickname("New Nickname")
                .build();

        Beneficiary beneficiary = Beneficiary.builder()
                .id(1L)
                .nickname("My Friend")
                .beneficiaryName("Friend Name")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryIban("TN00123456789")
                .owner(owner)
                .build();

        when(beneficiaryRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(beneficiary));
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(beneficiary);

        BeneficiaryResponse result = beneficiaryService.updateBeneficiary(1L, request);

        assertNotNull(result);
        assertEquals("New Nickname", result.getNickname());
        verify(beneficiaryRepository, times(1)).findByIdAndOwner(1L, owner);
        verify(beneficiaryRepository, times(1)).save(beneficiary);
    }

    @Test
    void testDeleteBeneficiary_Success() {
        mockAuthentication();
        Beneficiary beneficiary = Beneficiary.builder()
                .id(1L)
                .nickname("My Friend")
                .beneficiaryName("Friend Name")
                .beneficiaryAccountNumber("123456789")
                .beneficiaryIban("TN00123456789")
                .owner(owner)
                .build();

        when(beneficiaryRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(beneficiary));

        assertDoesNotThrow(() -> beneficiaryService.deleteBeneficiary(1L));

        verify(beneficiaryRepository, times(1)).findByIdAndOwner(1L, owner);
        verify(beneficiaryRepository, times(1)).delete(beneficiary);
    }
}
