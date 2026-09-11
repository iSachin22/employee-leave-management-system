package com.sachin.leavemanagement.controller;

import com.sachin.leavemanagement.dto.DecisionDto;
import com.sachin.leavemanagement.dto.LeaveRequestDto;
import com.sachin.leavemanagement.entity.LeaveRequest;
import com.sachin.leavemanagement.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
    public LeaveRequest applyForLeave(@PathVariable Long employeeId, @Valid @RequestBody LeaveRequestDto dto) {
        return leaveService.applyForLeave(employeeId, dto);
    }

    @PostMapping("/{requestId}/manager-decision")
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequest managerDecision(@PathVariable Long requestId, @RequestBody DecisionDto decision) {
        return leaveService.managerDecision(requestId, decision.isApproved(), decision.getComment());
    }

    @PostMapping("/{requestId}/hr-decision")
    @PreAuthorize("hasRole('HR')")
    public LeaveRequest hrDecision(@PathVariable Long requestId, @RequestBody DecisionDto decision) {
        return leaveService.hrDecision(requestId, decision.isApproved(), decision.getComment());
    }

    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getForEmployee(@PathVariable Long employeeId) {
        return leaveService.getRequestsForEmployee(employeeId);
    }

    @GetMapping("/manager/{managerId}/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public List<LeaveRequest> getPendingForManager(@PathVariable Long managerId) {
        return leaveService.getPendingApprovalsForManager(managerId);
    }

    @GetMapping("/hr/pending")
    @PreAuthorize("hasRole('HR')")
    public List<LeaveRequest> getPendingForHr() {
        return leaveService.getPendingApprovalsForHr();
    }
}
