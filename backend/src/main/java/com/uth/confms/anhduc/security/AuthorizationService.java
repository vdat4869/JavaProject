package com.uth.confms.anhduc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service kiểm tra quyền truy cập.
 * Được sử dụng với @PreAuthorize annotation.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    /**
     * Kiểm tra user có quyền truy cập paper không.
     * User có quyền nếu là tác giả hoặc là Chair/Track Chair.
     */
    public boolean canAccessPaper(UUID paperId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        if (user == null) return false;
        
        // Tác giả của paper
        if (user.isAuthorOfPaper(paperId)) {
            return true;
        }
        
        // TODO: Kiểm tra Chair/Track Chair dựa trên conferenceId của paper
        // Cần inject PaperRepository để lấy thông tin conference
        
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
        if (user == null) return false;
        
        return user.isChairOfConference(conferenceId) 
                || user.isTrackChairOfConference(conferenceId);
    }

    /**
     * Kiểm tra user có phải là Chair của conference không.
     */
    public boolean isChairOfConference(UUID conferenceId, Authentication authentication) {
        UserPrincipal user = getUserPrincipal(authentication);
        return user != null && user.isChairOfConference(conferenceId);
    }

    /**
     * Kiểm tra user có quyền quản lý camera-ready của conference không.
     */
    public boolean canManageCameraReady(UUID conferenceId, Authentication authentication) {
        return isChairOfConference(conferenceId, authentication);
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
