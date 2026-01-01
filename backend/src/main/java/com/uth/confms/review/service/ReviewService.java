package com.uth.confms.review.service;

import com.uth.confms.assignment.entity.Assignment;
import com.uth.confms.assignment.repository.AssignmentRepository;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.review.dto.ReviewSubmitDTO;
import com.uth.confms.review.dto.ReviewResponseDTO;
import com.uth.confms.review.entity.Review;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.repository.SubmissionRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý reviews (đánh giá bài nộp)
 * 
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 * <ul>
 *   <li>Tạo và cập nhật draft reviews</li>
 *   <li>Submit reviews</li>
 *   <li>Double-blind review (ẩn reviewer identity)</li>
 *   <li>Quản lý review status và scores</li>
 * </ul>
 * 
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ReviewResponseDTO createOrUpdateDraft(ReviewSubmitDTO dto, Long reviewerId) {
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        
        // Check authorization
        if (!assignment.getReviewerId().equals(reviewerId)) {
            throw new UnauthorizedException("You can only create reviews for your own assignments");
        }
        
        // Check assignment status
        if (assignment.getStatus() != Assignment.AssignmentStatus.ACCEPTED) {
            throw new BusinessException("Assignment must be accepted before creating review");
        }
        
        // Check if review already exists
        Review review = reviewRepository.findByAssignmentIdAndReviewerId(dto.getAssignmentId(), reviewerId)
                .orElse(null);
        
        if (review == null) {
            // Create new draft
            review = Review.builder()
                    .assignmentId(dto.getAssignmentId())
                    .submissionId(assignment.getSubmissionId())
                    .reviewerId(reviewerId)
                    .summary(dto.getSummary())
                    .strengths(dto.getStrengths())
                    .weaknesses(dto.getWeaknesses())
                    .comments(dto.getComments())
                    .score(Review.ReviewScore.valueOf(dto.getScore()))
                    .status(Review.ReviewStatus.DRAFT)
                    .isConfidential(dto.getIsConfidential())
                    .build();
        } else {
            // Update existing draft (only if still in DRAFT status)
            if (review.getStatus() != Review.ReviewStatus.DRAFT) {
                throw new BusinessException("Cannot update submitted review");
            }
            
            review.setSummary(dto.getSummary());
            review.setStrengths(dto.getStrengths());
            review.setWeaknesses(dto.getWeaknesses());
            review.setComments(dto.getComments());
            review.setScore(Review.ReviewScore.valueOf(dto.getScore()));
            review.setIsConfidential(dto.getIsConfidential());
        }
        
        review = reviewRepository.save(review);
        
        return mapToDTO(review, false); // false = don't show reviewer name (double-blind)
    }
    
    @Transactional
    public ReviewResponseDTO submitReview(Long reviewId, Long reviewerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        
        // Check authorization
        if (!review.getReviewerId().equals(reviewerId)) {
            throw new UnauthorizedException("You can only submit your own reviews");
        }
        
        // Check status
        if (review.getStatus() != Review.ReviewStatus.DRAFT) {
            throw new BusinessException("Review is already submitted");
        }
        
        // Validate required fields
        if (review.getSummary() == null || review.getSummary().trim().isEmpty()) {
            throw new BusinessException("Summary is required");
        }
        if (review.getComments() == null || review.getComments().trim().isEmpty()) {
            throw new BusinessException("Comments are required");
        }
        
        review.setStatus(Review.ReviewStatus.SUBMITTED);
        review.setSubmittedAt(LocalDateTime.now());
        review = reviewRepository.save(review);
        
        // Update assignment status to COMPLETED
        Assignment assignment = assignmentRepository.findById(review.getAssignmentId())
                .orElse(null);
        if (assignment != null) {
            assignment.setStatus(Assignment.AssignmentStatus.COMPLETED);
            assignmentRepository.save(assignment);
        }
        
        return mapToDTO(review, false); // false = don't show reviewer name (double-blind)
    }
    
    public ReviewResponseDTO getMyReview(Long assignmentId, Long reviewerId) {
        Review review = reviewRepository.findByAssignmentIdAndReviewerId(assignmentId, reviewerId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        
        // Check authorization
        if (!review.getReviewerId().equals(reviewerId)) {
            throw new UnauthorizedException("You can only view your own reviews");
        }
        
        return mapToDTO(review, true); // true = show reviewer name (own review)
    }
    
    public List<ReviewResponseDTO> getReviewsBySubmission(Long submissionId, Long userId, boolean isChairOrAdmin) {
        // Validate submission exists
        submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        
        List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);
        
        // For double-blind: only show reviewer names to chair/admin
        return reviews.stream()
                .map(review -> mapToDTO(review, isChairOrAdmin))
                .collect(Collectors.toList());
    }
    
    public ReviewResponseDTO getReview(Long reviewId, Long userId, boolean isChairOrAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        
        // Check authorization: reviewer can see own review, chair/admin can see all
        boolean canView = review.getReviewerId().equals(userId) || isChairOrAdmin;
        if (!canView) {
            throw new UnauthorizedException("You don't have permission to view this review");
        }
        
        // Show reviewer name only if it's own review or user is chair/admin
        boolean showReviewerName = review.getReviewerId().equals(userId) || isChairOrAdmin;
        
        return mapToDTO(review, showReviewerName);
    }
    
    private ReviewResponseDTO mapToDTO(Review review, boolean showReviewerName) {
        User reviewer = showReviewerName ? userRepository.findById(review.getReviewerId()).orElse(null) : null;
        
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .assignmentId(review.getAssignmentId())
                .submissionId(review.getSubmissionId())
                .reviewerId(review.getReviewerId())
                .reviewerName(reviewer != null ? reviewer.getFullName() : null)
                .summary(review.getSummary())
                .strengths(review.getStrengths())
                .weaknesses(review.getWeaknesses())
                .comments(review.getComments())
                .score(review.getScore().name())
                .status(review.getStatus().name())
                .isConfidential(review.getIsConfidential())
                .createdAt(review.getCreatedAt())
                .submittedAt(review.getSubmittedAt())
                .build();
    }
}

