package com.sachin.leavemanagement.service;

import com.sachin.leavemanagement.dto.LeaveRequestDto;
import com.sachin.leavemanagement.entity.LeaveRequest;
import com.sachin.leavemanagement.entity.LeaveStatus;
import com.sachin.leavemanagement.entity.User;
import com.sachin.leavemanagement.repository.LeaveRequestRepository;
import com.sachin.leavemanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    private static final double MAX_CONCURRENT_TEAM_LEAVE_RATIO = 0.3;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    public LeaveRequest applyForLeave(Long employeeId, LeaveRequestDto dto) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        int days = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        if (days <= 0) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        if (days > employee.getLeaveBalance()) {
            throw new IllegalStateException("Insufficient leave balance: requested " + days
                    + " days, available " + employee.getLeaveBalance());
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setNumberOfDays(days);
        request.setReason(dto.getReason());
        request.setStatus(employee.getManager() != null
                ? LeaveStatus.PENDING_MANAGER_APPROVAL
                : LeaveStatus.PENDING_HR_APPROVAL);

        return leaveRequestRepository.save(request);
    }

    public LeaveRequest managerDecision(Long requestId, boolean approve, String comment) {
        LeaveRequest request = getPendingManagerRequest(requestId);
        User manager = request.getEmployee().getManager();

        if (!approve) {
            request.setStatus(LeaveStatus.REJECTED);
            request.setManagerComment(comment);
            return leaveRequestRepository.save(request);
        }

        long teamSize = userRepository.findAll().stream()
                .filter(u -> manager.equals(u.getManager()))
                .count();
        List<LeaveRequest> overlapping = leaveRequestRepository
                .findOverlappingApprovedLeaveForTeam(manager, request.getStartDate(), request.getEndDate());

        if (teamSize > 0 && (double) overlapping.size() / teamSize >= MAX_CONCURRENT_TEAM_LEAVE_RATIO) {
            throw new IllegalStateException(
                    "Cannot approve: team availability would drop below the staffing threshold for this period");
        }

        request.setStatus(LeaveStatus.PENDING_HR_APPROVAL);
        request.setManagerComment(comment);
        return leaveRequestRepository.save(request);
    }

    public LeaveRequest hrDecision(Long requestId, boolean approve, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (request.getStatus() != LeaveStatus.PENDING_HR_APPROVAL) {
            throw new IllegalStateException("Request is not pending HR approval");
        }

        if (approve) {
            User employee = request.getEmployee();
            employee.setLeaveBalance(employee.getLeaveBalance() - request.getNumberOfDays());
            userRepository.save(employee);
            request.setStatus(LeaveStatus.APPROVED);
        } else {
            request.setStatus(LeaveStatus.REJECTED);
        }
        request.setHrComment(comment);
        return leaveRequestRepository.save(request);
    }

    public List<LeaveRequest> getRequestsForEmployee(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return leaveRequestRepository.findByEmployee(employee);
    }

    public List<LeaveRequest> getPendingApprovalsForManager(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found"));
        return leaveRequestRepository.findByEmployeeManagerAndStatus(manager, LeaveStatus.PENDING_MANAGER_APPROVAL);
    }

    public List<LeaveRequest> getPendingApprovalsForHr() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING_HR_APPROVAL);
    }

    private LeaveRequest getPendingManagerRequest(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (request.getStatus() != LeaveStatus.PENDING_MANAGER_APPROVAL) {
            throw new IllegalStateException("Request is not pending manager approval");
        }
        return request;
    }
}
