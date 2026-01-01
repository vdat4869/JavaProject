package com.uth.confms.assignment.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.conference.entity.Conference;
import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.repository.PCMemberRepository;
import com.uth.confms.pc.service.COIService;
import com.uth.confms.assignment.dto.AssignmentCreateDTO;
import com.uth.confms.assignment.dto.AssignmentResponseDTO;
import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý assignment (phân công reviewer cho submission)
 * 
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 * <ul>
 *   <li>Tạo assignment (chỉ chair)</li>
 *   <li>Reviewer accept/decline assignments</li>
 *   <li>Xóa assignments</li>
 *   <li>Kiểm tra COI trước khi assign</li>
 *   <li>Quản lý assignment status</li>
 * </ul>
 * 
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final PCMemberRepository pcMemberRepository;
    private final ConferenceRepository conferenceRepository;
    private final COIService coiService;
    
    @Transactional
    public AssignmentResponseDTO createAssignment(AssignmentCreateDTO dto, Long chairId) {
        Submission submission = submissionRepository.findById(dto.getSubmissionId())
                .orElseThrow(() -> new NotFoundException("Submission with id " + dto.getSubmissionId() + " not found"));
        
        Conference conference = conferenceRepository.findById(submission.getConferenceId())
                .orElseThrow(() -> new NotFoundException("Conference not found"));
        
        // Check authorization - only chair can assign
        if (!conference.getChairId().equals(chairId)) {
            throw new UnauthorizedException("Only conference chair can assign reviewers");
        }
        
        // Check if reviewer is a PC member
        PCMember pcMember = pcMemberRepository.findByConferenceIdAndUserId(
                submission.getConferenceId(), dto.getReviewerId())
                .orElseThrow(() -> new BusinessException("Reviewer must be a PC member of this conference"));
        
        if (pcMember.getStatus() != PCMember.PCMemberStatus.ACCEPTED) {
            throw new BusinessException("Reviewer must have accepted the PC invitation");
        }
        
        // Check for COI
        if (coiService.hasCOI(dto.getReviewerId(), dto.getSubmissionId())) {
            throw new BusinessException("Cannot assign reviewer with conflict of interest");
        }
        
        // Check if assignment already exists
        if (assignmentRepository.existsBySubmissionIdAndReviewerId(dto.getSubmissionId(), dto.getReviewerId())) {
            throw new BusinessException("Assignment already exists for this reviewer and submission");
        }
        
        // Create assignment
        Assignment assignment = Assignment.builder()
                .submissionId(dto.getSubmissionId())
                .reviewerId(dto.getReviewerId())
                .status(Assignment.AssignmentStatus.ASSIGNED)
                .isPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false)
                .build();
        
        assignment = assignmentRepository.save(assignment);
        
        return mapToDTO(assignment);
    }
    
    @Transactional
    public AssignmentResponseDTO acceptAssignment(Long assignmentId, Long reviewerId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        
        // Check authorization
        if (!assignment.getReviewerId().equals(reviewerId)) {
            throw new UnauthorizedException("You can only accept your own assignments");
        }
        
        if (assignment.getStatus() != Assignment.AssignmentStatus.ASSIGNED) {
            throw new BusinessException("Assignment is not in ASSIGNED status");
        }
        
        assignment.setStatus(Assignment.AssignmentStatus.ACCEPTED);
        assignment = assignmentRepository.save(assignment);
        
        return mapToDTO(assignment);
    }
    
    @Transactional
    public AssignmentResponseDTO declineAssignment(Long assignmentId, Long reviewerId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        
        // Check authorization
        if (!assignment.getReviewerId().equals(reviewerId)) {
            throw new UnauthorizedException("You can only decline your own assignments");
        }
        
        if (assignment.getStatus() != Assignment.AssignmentStatus.ASSIGNED) {
            throw new BusinessException("Assignment is not in ASSIGNED status");
        }
        
        assignment.setStatus(Assignment.AssignmentStatus.DECLINED);
        assignment = assignmentRepository.save(assignment);
        
        return mapToDTO(assignment);
    }
    
    @Transactional
    public void deleteAssignment(Long assignmentId, Long chairId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        
        Submission submission = submissionRepository.findById(assignment.getSubmissionId())
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        
        Conference conference = conferenceRepository.findById(submission.getConferenceId())
                .orElseThrow(() -> new NotFoundException("Conference not found"));
        
        // Check authorization
        if (!conference.getChairId().equals(chairId)) {
            throw new UnauthorizedException("Only conference chair can delete assignments");
        }
        
        assignmentRepository.delete(assignment);
    }
    
    public List<AssignmentResponseDTO> getAssignmentsBySubmission(Long submissionId, Long chairId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        
        Conference conference = conferenceRepository.findById(submission.getConferenceId())
                .orElseThrow(() -> new NotFoundException("Conference not found"));
        
        // Check authorization
        if (!conference.getChairId().equals(chairId)) {
            throw new UnauthorizedException("Only conference chair can view assignments");
        }
        
        return assignmentRepository.findBySubmissionId(submissionId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AssignmentResponseDTO> getMyAssignments(Long reviewerId) {
        return assignmentRepository.findByReviewerId(reviewerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public AssignmentResponseDTO getAssignment(Long assignmentId, Long userId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        
        Submission submission = submissionRepository.findById(assignment.getSubmissionId())
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        
        Conference conference = conferenceRepository.findById(submission.getConferenceId())
                .orElseThrow(() -> new NotFoundException("Conference not found"));
        
        // Check authorization - reviewer or chair
        if (!assignment.getReviewerId().equals(userId) && !conference.getChairId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to view this assignment");
        }
        
        return mapToDTO(assignment);
    }
    
    private AssignmentResponseDTO mapToDTO(Assignment assignment) {
        Submission submission = submissionRepository.findById(assignment.getSubmissionId())
                .orElse(null);
        User reviewer = userRepository.findById(assignment.getReviewerId())
                .orElse(null);
        
        return AssignmentResponseDTO.builder()
                .id(assignment.getId())
                .submissionId(assignment.getSubmissionId())
                .submissionTitle(submission != null ? submission.getTitle() : null)
                .reviewerId(assignment.getReviewerId())
                .reviewerEmail(reviewer != null ? reviewer.getEmail() : null)
                .reviewerName(reviewer != null ? reviewer.getFullName() : null)
                .status(assignment.getStatus().name())
                .isPrimary(assignment.getIsPrimary())
                .assignedAt(assignment.getAssignedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}

