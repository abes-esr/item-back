package fr.abes.item.security;


public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String userNum;
    private String shortName;
    private String iln;
    private String role;
    private String email;

	public JwtAuthenticationResponse() {
    }
    
    public JwtAuthenticationResponse(String accessToken, String userNum, String shortName, String iln, String role, String email) {
        this.accessToken = accessToken;
        this.userNum = userNum;
        this.shortName = shortName;
        this.iln = iln;
        this.role = role;
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }
    public String getTokenType() {
        return tokenType;
    }
    public String getUserNum() {
        return userNum;
    }
    public String getShortName() {
        return shortName;
    }
    public String getIln() {
        return iln;
    }
    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }
}
