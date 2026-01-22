package service;

import dto.SSLCertificateInfo;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class SSLCertificateService {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 스레드 풀: 동시에 10개 도메인 체크
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 타임아웃: 5초
    private static final int SOCKET_TIMEOUT = 5000;

    // 도메인별 PEM 키 매핑
    private static final Map<String, String> DOMAIN_PEM_MAP = Map.ofEntries(
            Map.entry("api.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("auth.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("my.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("school-api.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("school.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("metaware.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("metaware.world", "V2_VRWARE.pem"),
            Map.entry("stbuilder.vrware.us", "V2_VRWARE.pem"),
            Map.entry("storybuilder.vrware.world", "V2_VRWARE.pem"),
            Map.entry("club.vrware.us", "bookclub.pem"),
            Map.entry("caregiver360.mrware.us", "caregiver360-keypair.pem"),
            Map.entry("metaware.vrware.world", "V2_VRWARE.pem"),
            Map.entry("caregiver360-admin.mrware.world", "caregiver360-keypair.pem"),
            Map.entry("caregiver360.mrware.world", "caregiver360-keypair.pem"),
            Map.entry("rehab360.mrware.world", "Rehab360-key.pem"),
            Map.entry("caregiver360-lms.mrware.world", "caregiver360-keypair.pem"),
            Map.entry("rehab360-lms.mrware.world", "Rehab360-key.pem"),
            Map.entry("rehab360-admin.mrware.world", "Rehab360-key.pem"),
            Map.entry("nursing360.mrware.world", "toast-instance-ppk.ppk"),
            Map.entry("www.mrware.world", "total-admin-key.pem"),
            Map.entry("mini-story.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("mini-meta.vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("vrware.us", "toast-instance-ppk.ppk"),
            Map.entry("pixel.vrware.world", "V2_VRWARE.pem"),
            Map.entry("brittany.vrware.world", "V2_VRWARE.pem")
    );

    /**
     * 여러 도메인을 병렬로 체크 (성능 개선)
     */
    public List<SSLCertificateInfo> checkMultipleCertificates(List<String> domains) {
        // 각 도메인을 비동기로 처리
        List<CompletableFuture<SSLCertificateInfo>> futures = domains.stream()
                .map(domain -> CompletableFuture.supplyAsync(
                        () -> checkCertificate(domain),
                        executorService
                ))
                .collect(Collectors.toList());

        // 모든 결과가 완료될 때까지 대기 후 반환
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * 단일 도메인 SSL 인증서 체크
     */
    public SSLCertificateInfo checkCertificate(String domain) {
        SSLCertificateInfo info = new SSLCertificateInfo();
        info.setDomain(domain);

        SSLSocket socket = null;
        try {
            domain = domain.replace("https://", "").replace("http://", "");
            if (domain.contains("/")) {
                domain = domain.substring(0, domain.indexOf("/"));
            }

            // PEM 키 매칭
            String pemKey = DOMAIN_PEM_MAP.getOrDefault(domain, "-");
            info.setPemKey(pemKey);

            // IP 주소 가져오기
            try {
                InetAddress address = InetAddress.getByName(domain);
                info.setIpAddress(address.getHostAddress());
            } catch (Exception e) {
                info.setIpAddress("N/A");
            }

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) factory.createSocket(domain, 443);

            // 타임아웃 설정: 5초
            socket.setSoTimeout(SOCKET_TIMEOUT);

            socket.startHandshake();

            Certificate[] certificates = socket.getSession().getPeerCertificates();

            for (Certificate cert : certificates) {
                if (cert instanceof X509Certificate) {
                    X509Certificate x509cert = (X509Certificate) cert;

                    info.setSubjectCN(extractField(x509cert.getSubjectDN().getName(), "CN"));
                    info.setSubjectO(extractField(x509cert.getSubjectDN().getName(), "O"));
                    info.setSubjectOU(extractField(x509cert.getSubjectDN().getName(), "OU"));
                    info.setIssuerCN(extractField(x509cert.getIssuerDN().getName(), "CN"));
                    info.setIssuerO(extractField(x509cert.getIssuerDN().getName(), "O"));
                    info.setNotBefore(dateFormat.format(x509cert.getNotBefore()));
                    info.setNotAfter(dateFormat.format(x509cert.getNotAfter()));

                    long daysUntilExpiry = (x509cert.getNotAfter().getTime() - new Date().getTime())
                            / (1000 * 60 * 60 * 24);
                    info.setDaysUntilExpiry(daysUntilExpiry);

                    info.setSerialNumber(x509cert.getSerialNumber().toString(16).toUpperCase());
                    info.setSignatureAlgorithm(x509cert.getSigAlgName());

                    break;
                }
            }

        } catch (Exception e) {
            info.setError(e.getMessage());
        } finally {
            // 소켓 닫기
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // 무시
                }
            }
        }

        return info;
    }

    private String extractField(String dn, String field) {
        String[] parts = dn.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith(field + "=")) {
                return part.substring(field.length() + 1);
            }
        }
        return "-";
    }
}