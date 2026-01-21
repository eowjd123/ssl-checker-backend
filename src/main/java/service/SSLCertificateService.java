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
import java.util.Map;

@Service
public class SSLCertificateService {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    public SSLCertificateInfo checkCertificate(String domain) {
        SSLCertificateInfo info = new SSLCertificateInfo();
        info.setDomain(domain);

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
            SSLSocket socket = (SSLSocket) factory.createSocket(domain, 443);
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

            socket.close();

        } catch (Exception e) {
            info.setError(e.getMessage());
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