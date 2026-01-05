package com.uth.confms.pc.service;

import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.entity.Paper;
import java.util.*;

/**
 * Service goi y reviewer dua tren topic/keyword (AI feature)
 * So khop keywords cua paper voi research topics cua PC members
 * Chi goi y, khong tu dong assign
 */
public class ReviewerSuggestionService {
    
    private static final int EXACT_MATCH_SCORE = 2;
    private static final int PARTIAL_MATCH_SCORE = 1;
    private static final int DEFAULT_SUGGESTION_COUNT = 5;
    
    public List<PCMember> suggestReviewers(Paper paper, List<PCMember> pcMembers, int topN) {
        if (paper == null || pcMembers == null || paper.getKeywords().isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<PCMember, Integer> scoreMap = new HashMap<>();
        
        List<String> paperKeywords = new ArrayList<>();
        for (String keyword : paper.getKeywords()) {
            paperKeywords.add(keyword.toLowerCase().trim());
        }
        
        for (PCMember pcMember : pcMembers) {
            if (!pcMember.isActive()) {
                continue;
            }
            
            int score = calculateMatchScore(paperKeywords, pcMember.getResearchTopics());
            if (score > 0) {
                scoreMap.put(pcMember, score);
            }
        }
        
        List<Map.Entry<PCMember, Integer>> sortedEntries = new ArrayList<>(scoreMap.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        List<PCMember> suggestions = new ArrayList<>();
        int count = 0;
        for (Map.Entry<PCMember, Integer> entry : sortedEntries) {
            if (count >= topN) {
                break;
            }
            suggestions.add(entry.getKey());
            count++;
        }
        
        return suggestions;
    }
    
    private int calculateMatchScore(List<String> paperKeywords, List<String> researchTopics) {
        int score = 0;
        
        List<String> topicsLower = new ArrayList<>();
        for (String topic : researchTopics) {
            topicsLower.add(topic.toLowerCase().trim());
        }
        
        for (String keyword : paperKeywords) {
            if (topicsLower.contains(keyword)) {
                score += EXACT_MATCH_SCORE;
                continue;
            }
            
            for (String topic : topicsLower) {
                if (keyword.contains(topic) || topic.contains(keyword)) {
                    score += PARTIAL_MATCH_SCORE;
                    break;
                }
            }
        }
        
        return score;
    }
    
    public List<PCMember> suggestReviewers(Paper paper, List<PCMember> pcMembers) {
        return suggestReviewers(paper, pcMembers, DEFAULT_SUGGESTION_COUNT);
    }
    
    public void printSuggestions(List<PCMember> suggestions, Paper paper) {
        if (suggestions == null || suggestions.isEmpty()) {
            System.out.println("❌ Khong tim thay reviewer phu hop cho paper: " + paper.getTitle());
            return;
        }
        
        System.out.println("\n🤖 AI Goi y Reviewer cho paper: " + paper.getTitle());
        System.out.println("   Keywords: " + String.join(", ", paper.getKeywords()));
        System.out.println("   So luong goi y: " + suggestions.size());
        System.out.println("   Danh sach:");
        
        for (int i = 0; i < suggestions.size(); i++) {
            PCMember member = suggestions.get(i);
            System.out.println("   " + (i + 1) + ". " + member.getName() + 
                             " (" + member.getEmail() + ")");
            System.out.println("      Topics: " + String.join(", ", member.getResearchTopics()));
        }
        System.out.println("   ⚠️  Luu y: Day chi la goi y, can kiem tra COI truoc khi assign!\n");
    }
}
