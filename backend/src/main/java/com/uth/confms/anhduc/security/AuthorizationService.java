package com.uth.confms.anhduc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Service kiểm tra quyền truy cập.
 * Được sử dụng với @PreAuthorize annotation.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Slf4j
@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    /**
     * Kiểm tra user có quyền truy cập paper không.
     * User có quyền nếu là tác giả hoặc là Chair/Track Chair.
     */
    public boolean canAccessPaper(UUID paperId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        if (user == null) {
            log.debug("Từ chối truy cập paper {}: người dùng chưa xác thực", paperId);
            return false;
        }
        
        if (user.isAuthorOfPaper(paperId)) {
            log.debug("Cho phép truy cập paper {}: user {} là tác giả", 
                    paperId, user.getId());
            return true;
        }
        
        log.debug("Từ chối truy cập paper {}: user {} không có quyền", 
                paperId, user.getId());
        return false;
    }

    /**
     * Kiểm tra user có phải là tác giả của paper không.
     */
    public boolean isAuthorOfPaper(UUID paperId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        return user != null && user.isAuthorOfPaper(paperId);
    }

    /**
     * Kiểm tra user có phải là corresponding author của paper không.
     */
    public boolean isCorrespondingAuthor(UUID paperId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        return user != null && user.isCorrespondingAuthorOfPaper(paperId);
    }

    /**
     * Kiểm tra user có quyền duyệt submission không.
     * User có quyền nếu là Chair hoặc Track Chair của conference.
     */
    public boolean canReviewSubmission(UUID conferenceId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        if (user == null) {
            log.warn("Từ chối review submission: người dùng chưa xác thực");
            return false;
        }
        
        boolean canReview = user.isChairOfConference(conferenceId) 
                || user.isTrackChairOfConference(conferenceId);
        
        log.debug("Kiểm tra quyền review cho user {} trên conference {}: {}", 
                user.getId(), conferenceId, canReview ? "CHO PHÉP" : "TỪ CHỐI");
        
        return canReview;
    }

    public boolean isChairOfConference(UUID conferenceId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        boolean isChair = user != null && user.isChairOfConference(conferenceId);
        
        if (isChair) {
            log.debug("Xác nhận user {} là Chair của conference {}", 
                    user.getId(), conferenceId);
        } else {
            log.debug("User {} không phải Chair của conference {}", 
                    user != null ? user.getId() : "null", conferenceId);
        }
        
        return isChair;
    }

    /**
     * Kiểm tra user có quyền quản lý camera-ready của conference không.
     */
    public boolean canManageCameraReady(UUID conferenceId, Authentication authentication) {
        boolean result = isChairOfConference(conferenceId, authentication);
        log.info("Kiểm tra quyền quản lý camera-ready cho conference {}: {}", 
                conferenceId, result ? "CHO PHÉP" : "TỪ CHỐI");
        return result;
    }

    /**
     * Kiểm tra user có quyền xuất kỷ yếu không.
     */
    public boolean canExportProceedings(UUID conferenceId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        if (user == null) return false;
        
        return user.isChairOfConference(conferenceId) 
                || user.isTrackChairOfConference(conferenceId);
    }

    private UserPrincipal getUserPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        
        return null;
    }
}
