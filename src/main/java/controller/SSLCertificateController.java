package controller;

import dto.SSLCertificateInfo;
import service.SSLCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SSLCertificateController {

    @Autowired
    private SSLCertificateService sslCertificateService;

    @GetMapping("/check-certificate")
    public SSLCertificateInfo checkCertificate(@RequestParam String domain) {
        System.out.println("Received request for domain: " + domain);
        return sslCertificateService.checkCertificate(domain);
    }

    // 새로 추가: 여러 도메인 일괄 체크
    @PostMapping("/check-certificates")
    public List<SSLCertificateInfo> checkCertificates(@RequestBody List<String> domains) {
        System.out.println("Received request for " + domains.size() + " domains");
        //병렬처리 메서드 호출
        return sslCertificateService.checkMultipleCertificates(domains);

        return results;
    }
}