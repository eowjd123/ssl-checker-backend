package dto;

public class SSLCertificateInfo {
    private String domain;
    private String ipAddress;
    private String pemKey;  // 추가
    private String subjectCN;
    private String subjectO;
    private String subjectOU;
    private String issuerCN;
    private String issuerO;
    private String notBefore;
    private String notAfter;
    private long daysUntilExpiry;
    private String serialNumber;
    private String signatureAlgorithm;
    private String error;

    // Getters and Setters
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getPemKey() { return pemKey; }
    public void setPemKey(String pemKey) { this.pemKey = pemKey; }

    public String getSubjectCN() { return subjectCN; }
    public void setSubjectCN(String subjectCN) { this.subjectCN = subjectCN; }

    public String getSubjectO() { return subjectO; }
    public void setSubjectO(String subjectO) { this.subjectO = subjectO; }

    public String getSubjectOU() { return subjectOU; }
    public void setSubjectOU(String subjectOU) { this.subjectOU = subjectOU; }

    public String getIssuerCN() { return issuerCN; }
    public void setIssuerCN(String issuerCN) { this.issuerCN = issuerCN; }

    public String getIssuerO() { return issuerO; }
    public void setIssuerO(String issuerO) { this.issuerO = issuerO; }

    public String getNotBefore() { return notBefore; }
    public void setNotBefore(String notBefore) { this.notBefore = notBefore; }

    public String getNotAfter() { return notAfter; }
    public void setNotAfter(String notAfter) { this.notAfter = notAfter; }

    public long getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(long daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}