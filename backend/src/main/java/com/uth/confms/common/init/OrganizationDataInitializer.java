package com.uth.confms.common.init;

import com.uth.confms.common.entity.Organization;
import com.uth.confms.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrganizationDataInitializer implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (organizationRepository.count() > 0) {
            return;
        }

        log.info("Initializing organization data...");

        List<Organization> organizations = Arrays.asList(
                Organization.builder().name("Trường Đại học Giao thông vận tải TP.HCM").code("UTH").city("TP.HCM")
                        .build(),
                Organization.builder().name("Đại học Bách khoa Hà Nội").code("HUST").city("Hà Nội").build(),
                Organization.builder().name("Trường Đại học Bách khoa - ĐHQG TP.HCM").code("HCMUT").city("TP.HCM")
                        .build(),
                Organization.builder().name("Trường Đại học Khoa học Tự nhiên - ĐHQG TP.HCM").code("HCMUS")
                        .city("TP.HCM").build(),
                Organization.builder().name("Trường Đại học Công nghệ Thông tin - ĐHQG TP.HCM").code("UIT")
                        .city("TP.HCM").build(),
                Organization.builder().name("Trường Đại học Quốc tế - ĐHQG TP.HCM").code("IU").city("TP.HCM").build(),
                Organization.builder().name("Đại học Quốc gia Hà Nội").code("VNU-HN").city("Hà Nội").build(),
                Organization.builder().name("Đại học Quốc gia TP.HCM").code("VNU-HCM").city("TP.HCM").build(),
                Organization.builder().name("Trường Đại học Cần Thơ").code("CTU").city("Cần Thơ").build(),
                Organization.builder().name("Trường Đại học Đà Nẵng").code("UD").city("Đà Nẵng").build(),
                Organization.builder().name("Trường Đại học FPT").code("FPTU").city("Toàn quốc").build(),
                Organization.builder().name("Trường Đại học Tôn Đức Thắng").code("TDTU").city("TP.HCM").build(),
                Organization.builder().name("Trường Đại học Công nghiệp TP.HCM").code("IUH").city("TP.HCM").build(),
                Organization.builder().name("Trường Đại học Sư phạm Kỹ thuật TP.HCM").code("HCMUTE").city("TP.HCM")
                        .build(),
                Organization.builder().name("Học viện Công nghệ Bưu chính Viễn thông").code("PTIT")
                        .city("Hà Nội & TP.HCM").build(),
                Organization.builder().name("Other").code("OTHER").city("Other").build());

        organizationRepository.saveAll(organizations);
        log.info("Saved {} organizations.", organizations.size());
    }
}
