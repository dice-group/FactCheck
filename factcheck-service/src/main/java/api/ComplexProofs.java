package api;

/**
 * @author FAHAD ANWAR
 * @since 5/27/2018
 */
public class ComplexProofs {
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getProofPhrase() {
        return proofPhrase;
    }

    public void setProofPhrase(String proofPhrase) {
        this.proofPhrase = proofPhrase;
    }

    private String website;
    private String proofPhrase;
    public ComplexProofs() {
    }

    public ComplexProofs(String website, String proofPhrase) {
        super();
        this.website = website;
        this.proofPhrase = proofPhrase;
    }
}
