package com.sachin.leavemanagement.repository;

import com.sachin.leavemanagement.entity.LeaveRequest;
import com.sachin.leavemanagement.entity.LeaveStatus;
import com.sachin.leavemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee(User employee);

    List<LeaveRequest> findByEmployeeManagerAndStatus(User manager, LeaveStatus status);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    @org.springframework.data.jpa.repository.Query(
        "SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager = :manager " +
        "AND lr.status = 'APPROVED' AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingApprovedLeaveForTeam(User manager, LocalDate startDate, LocalDate endDate);
}
