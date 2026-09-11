package com.sachin.leavemanagement.service;

import com.sachin.leavemanagement.dto.LeaveRequestDto;
import com.sachin.leavemanagement.entity.LeaveRequest;
import com.sachin.leavemanagement.entity.LeaveStatus;
import com.sachin.leavemanagement.entity.Role;
import com.sachin.leavemanagement.entity.User;
import com.sachin.leavemanagement.repository.LeaveRequestRepository;
import com.sachin.leavemanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private UserRepository userRepository;

    private LeaveService leaveService;

    private User employee;
    private User manager;

    @BeforeEach
    void setUp() {
        leaveService = new LeaveService(leaveRequestRepository, userRepository);

        manager = new User(2L, "manager@company.com", "pass", "Manager One", Role.MANAGER, 24, null);
        employee = new User(1L, "emp@company.com", "pass", "Employee One", Role.EMPLOYEE, 10, manager);
    }

    @Test
    void applyForLeave_succeeds_whenBalanceIsSufficient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(7));
        dto.setReason("Family function");

        LeaveRequest result = leaveService.applyForLeave(1L, dto);

        assertEquals(3, result.getNumberOfDays());
        assertEquals(LeaveStatus.PENDING_MANAGER_APPROVAL, result.getStatus());
    }

    @Test
    void applyForLeave_throws_whenBalanceIsInsufficient() {
        employee.setLeaveBalance(2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(5));

        assertThrows(IllegalStateException.class, () -> leaveService.applyForLeave(1L, dto));
    }

    @Test
    void hrDecision_deductsLeaveBalance_whenApproved() {
        LeaveRequest request = new LeaveRequest();
        request.setId(10L);
        request.setEmployee(employee);
        request.setNumberOfDays(3);
        request.setStatus(LeaveStatus.PENDING_HR_APPROVAL);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveService.hrDecision(10L, true, "Approved by HR");

        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        assertEquals(7, employee.getLeaveBalance());
        verify(userRepository).save(employee);
    }

    @Test
    void hrDecision_rejectsWithoutTouchingBalance_whenNotApproved() {
        LeaveRequest request = new LeaveRequest();
        request.setId(11L);
        request.setEmployee(employee);
        request.setNumberOfDays(3);
        request.setStatus(LeaveStatus.PENDING_HR_APPROVAL);

        when(leaveRequestRepository.findById(11L)).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveService.hrDecision(11L, false, "Not enough notice");

        assertEquals(LeaveStatus.REJECTED, result.getStatus());
        assertEquals(10, employee.getLeaveBalance());
        verify(userRepository, never()).save(employee);
    }

    @Test
    void managerDecision_throws_whenRequestNotPendingManagerApproval() {
        LeaveRequest request = new LeaveRequest();
        request.setId(20L);
        request.setEmployee(employee);
        request.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(20L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> leaveService.managerDecision(20L, true, "ok"));
    }

    @Test
    void managerDecision_rejectsCleanly_withoutConflictCheck() {
        LeaveRequest request = new LeaveRequest();
        request.setId(21L);
        request.setEmployee(employee);
        request.setStatus(LeaveStatus.PENDING_MANAGER_APPROVAL);

        when(leaveRequestRepository.findById(21L)).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveService.managerDecision(21L, false, "Understaffed already");

        assertEquals(LeaveStatus.REJECTED, result.getStatus());
    }
}
