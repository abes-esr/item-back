package fr.abes.item.security;

public class User
{
    private String userNum;

    private String userKey;

    private String userGroup;

    private String role;

    private String library;

    private String shortName;

    private String loginAllowed;

    private String iln;

    private String libRcr;

    private String mail;

    public void setUserNum(String userNum){
        this.userNum = userNum;
    }
    public String getUserNum(){
        return this.userNum;
    }
    public void setUserKey(String userKey){
        this.userKey = userKey;
    }
    public String getUserKey(){
        return this.userKey;
    }
    public void setUserGroup(String userGroup){
        this.userGroup = userGroup;
    }
    public String getUserGroup(){
        return this.userGroup;
    }
    public void setLibrary(String library){
        this.library = library;
    }
    public String getLibrary(){
        return this.library;
    }
    public void setShortName(String shortName){
        this.shortName = shortName;
    }
    public String getShortName(){
        return this.shortName;
    }
    public void setLoginAllowed(String loginAllowed){
        this.loginAllowed = loginAllowed;
    }
    public String getLoginAllowed(){
        return this.loginAllowed;
    }
    public void setIln(String iln){
        this.iln = iln;
    }
    public String getIln(){
        return this.iln;
    }
    public void setLibRcr(String libRcr){
        this.libRcr = libRcr;
    }
    public String getLibRcr(){
        return this.libRcr;
    }
    public String getRole() {
        if (role == null || role.isEmpty()) {
            if (this.userGroup.toLowerCase().trim().equals("coordinateur"))
                role = "USER";
            if (this.userGroup.toLowerCase().trim().equals("abes"))
                role = "ADMIN";
        }
        return this.role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}

