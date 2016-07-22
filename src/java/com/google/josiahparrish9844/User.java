/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.josiahparrish9844;

/**
 * POJO Class for the User. Used for access control and personalization stuff
 * @author jay-t
 */
public class User {
    
    private int Balance;
    private String Username;
    private String Bio;
    private String ProfilePic;
    
    public User(){
        Balance = 0;
        Username = "notLoggedIn";
    }

    public int getBalance() {
        return Balance;
    }

    public void setBalance(int Balance) {
        this.Balance = Balance;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }

    public String getBio() {
        return Bio;
    }

    public void setBio(String Bio) {
        this.Bio = Bio;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public void setProfilePic(String ProfilePic) {
        this.ProfilePic = ProfilePic;
    }
    
}
