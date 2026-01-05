package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.PCMember;
import java.util.*;

/**
 * Repository quan ly viec luu tru va truy xuat PCMember entities
 */
public class PCMemberRepository {
    private Map<String, PCMember> members;

    public PCMemberRepository() {
        this.members = new HashMap<>();
    }

    public PCMember save(PCMember member) {
        if (member == null || member.getId() == null) {
            return null;
        }
        members.put(member.getId(), member);
        return member;
    }

    public Optional<PCMember> findById(String id) {
        return Optional.ofNullable(members.get(id));
    }

    public List<PCMember> findAll() {
        return new ArrayList<>(members.values());
    }

    public List<PCMember> findByStatus(com.uth.confms.pc.entity.enums.InvitationStatus status) {
        List<PCMember> result = new ArrayList<>();
        for (PCMember member : members.values()) {
            if (member.getInvitationStatus() == status) {
                result.add(member);
            }
        }
        return result;
    }

    public List<PCMember> findActiveMembers() {
        List<PCMember> result = new ArrayList<>();
        for (PCMember member : members.values()) {
            if (member.isActive()) {
                result.add(member);
            }
        }
        return result;
    }

    public boolean existsById(String id) {
        return members.containsKey(id);
    }

    public void deleteById(String id) {
        members.remove(id);
    }

    public void deleteAll() {
        members.clear();
    }
}

