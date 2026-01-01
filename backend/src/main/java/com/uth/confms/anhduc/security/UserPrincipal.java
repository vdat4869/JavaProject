package com.uth.confms.anhduc.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * User principal chứa thông tin người dùng đang đăng nhập.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String email;
    private String fullName;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    /**
     * Các vai trò của user trong từng hội nghị.
     * Key: conferenceId, Value: List of roles (CHAIR, TRACK_CHAIR, REVIEWER, etc.)
     */
    private Map<UUID, List<String>> conferenceRoles;
    
    /**
     * Danh sách paper IDs mà user là tác giả.
     */
    private Set<UUID> authoredPapers;
    
    /**
     * Danh sách paper IDs mà user là corresponding author.
     */
    private Set<UUID> correspondingAuthorPapers;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Kiểm tra user có vai trò trong hội nghị không.
     */
    public boolean hasRoleInConference(UUID conferenceId, String role) {
        if (conferenceRoles == null || !conferenceRoles.containsKey(conferenceId)) {
            return false;
        }
        return conferenceRoles.get(conferenceId).contains(role);
    }

    /**
     * Kiểm tra user có phải là Chair của hội nghị không.
     */
    public boolean isChairOfConference(UUID conferenceId) {
        return hasRoleInConference(conferenceId, "CHAIR");
    }

    /**
     * Kiểm tra user có phải là Track Chair của hội nghị không.
     */
    public boolean isTrackChairOfConference(UUID conferenceId) {
        return hasRoleInConference(conferenceId, "TRACK_CHAIR");
    }

    /**
     * Kiểm tra user có phải là tác giả của paper không.
     */
    public boolean isAuthorOfPaper(UUID paperId) {
        return authoredPapers != null && authoredPapers.contains(paperId);
    }

    /**
     * Kiểm tra user có phải là corresponding author của paper không.
     */
    public boolean isCorrespondingAuthorOfPaper(UUID paperId) {
        return correspondingAuthorPapers != null && correspondingAuthorPapers.contains(paperId);
    }
}
