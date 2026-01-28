package com.uth.confms.common.controller;

import com.uth.confms.common.entity.Organization;
import com.uth.confms.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    @GetMapping
    public List<Organization> getAll() {
        return organizationRepository.findAll();
    }
}
