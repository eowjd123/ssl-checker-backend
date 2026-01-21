package service;

import dto.SSLCertificateInfo;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class SSLCertificateService {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SSLCertificateInfo checkCertificate(String domain) {
        SSLCertificateInfo info = new SSLCertificateInfo();
        info.setDomain(domain);

        try {
            domain = domain.replace("https://", "").replace("http://", "");
            if (domain.contains("/")) {
                domain = domain.substring(0, domain.indexOf("/"));
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