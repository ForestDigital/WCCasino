/*
 * Bean to handle all access control not already handled by JDBC Realms
 */
package com.google.josiahparrish9844;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 *
 * @author jay-t
 */
@SessionScoped
@Named
public class loginBean implements Serializable{

    @Resource(name="jdbc/wspFinal")
    private DataSource ds;
    
    FacesContext context2 = FacesContext.getCurrentInstance();
    HttpSession session = (HttpSession) context2.getExternalContext().getSession(true);
    
    @PostConstruct
    public void init() {
        session.setAttribute("user", "notLoggedIn");
        loggedIn = false;
        Imagename= "loading.gif";
    }
    
    public void testMessage() {
        FacesContext context = FacesContext.getCurrentInstance();
         
        context.addMessage(null, new FacesMessage("Successful",  "It Works! "));
    }
    
    public String registerUser(String username, String password) throws SQLException {
        
        
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "select USERNAME from USERS"
            );

            ResultSet result = ps1.executeQuery();

            Boolean doesNotExist = true;
            
            while (result.next()) {
              if(result.getString("USERNAME").equals(username)){
                  doesNotExist = false;
              }
            }
            
            if(doesNotExist){
                String insertBook = "INSERT INTO USERS"
                        + "(USERNAME, PASSWORD, IMAGENAME, BIO, BALANCE) VALUES"
                        + "(?,?,?,?,?)";

                PreparedStatement ps2 = conn.prepareStatement(insertBook);
                ps2.setString(1,username);
                ps2.setString(2,SHA256Encrypt.encrypt(password));
                ps2.setString(3,"stockProfilePhoto.png");
                ps2.setString(4, "Tell other patrons about yourself!!!!");
                ps2.setInt(5, 5000);

                ps2.executeUpdate();

                
                String addAuth = "INSERT INTO GROUP_TABLE"
                        + " (groupname, username) VALUES"
                        + " (?, ?)";
                
                PreparedStatement ps3 = conn.prepareStatement(addAuth);
                ps3.setString(1,"usergroup");
                ps3.setString(2,username);
                
                ps3.executeUpdate();    

                conn.commit();
                
            }else{
                FacesMessage javaTextMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Sorry, Username already taken", null);
            FacesContext.getCurrentInstance().addMessage("login:username", javaTextMsg);
            
            return null;
            }

        } finally {
            conn.close();
        }
        
        return "/registerSuccess";
    }
    
    public String loginAttempt(String username, String password) throws SQLException {
                
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "select USERNAME, PASSWORD, IMAGENAME, BIO, BALANCE from USERS"
            );

            ResultSet result = ps1.executeQuery();

            Boolean doesNotExist = true;
            
            while (result.next()) {
              if(result.getString("USERNAME").equals(username)){
                  if(result.getString("PASSWORD").equals(SHA256Encrypt.encrypt(password))){
                      doesNotExist = false;
                      session.setAttribute("user", username);
                  }
              }
            }
            
            if(doesNotExist){
                String repsonse = "The provided credentials cannot be determined to be authentic";
                return null;
            }
            
            String getUser = "select USERNAME, PASSWORD, IMAGENAME, BIO, BALANCE from USERS"
                        + " where USERNAME = ?";

                PreparedStatement ps2 = conn.prepareStatement(getUser);
                ps2.setString(1,username);
                
                ResultSet result2 = ps2.executeQuery();
                
                while (result2.next()) {
                    Balance = result2.getInt("BALANCE");
                    Bio = result2.getString("BIO");
                    Username = result2.getString("USERNAME");
                    Imagename = result2.getString("IMAGENAME");
                    
                    editable = false;
                    loggedIn = true;
            }

        } finally {
            conn.close();
        }
        
        loggedIn = true;
        return "/user/profile";
    }
    
    public String logout(){
        Username = "";
        Balance = 0;
        loggedIn = false;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login";
    }
    
    public String login() throws SQLException{
         if(FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal() == null)
        {
            
        }else{
             
            if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
            }

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            if (conn == null) {
                throw new SQLException("conn is null; Can't get db connection");
            }
            
            try {
            String username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
                
            String getUser = "select USERNAME, PASSWORD, IMAGENAME, BIO, BALANCE from USERS"
                        + " where USERNAME = ?";

                PreparedStatement ps2 = conn.prepareStatement(getUser);
                ps2.setString(1,username);
                
                ResultSet result2 = ps2.executeQuery();
                
                while (result2.next()) {
                    Balance = result2.getInt("BALANCE");
                    Bio = result2.getString("BIO");
                    Username = result2.getString("USERNAME");
                    Imagename = result2.getString("IMAGENAME");
                    
                    editable = false;
                    loggedIn = true;
            }

        } finally {
            conn.close();
        }
             
        }
        
        
        return " ";
    }
    
    public String checkPromoCode(String code) throws SQLException {
        
        
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "select * from PROMO_CODES"
            );

            ResultSet result = ps1.executeQuery();

            Boolean doesNotExist = true;
            
            while (result.next()) {
              if(result.getString("CODE").equals(code)){
                  doesNotExist = false;
              }
            }
            
            if(doesNotExist){
               FacesMessage javaTextMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Sorry, Promo Code does not exist!", null);
            FacesContext.getCurrentInstance().addMessage("promoEntry:promoCode", javaTextMsg);
            }else{
                
            Balance = Balance + 5000;
            
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE USERS set BALANCE = ? "
                            + "WHERE USERNAME = ?"
            );

            ps2.setInt(1,Balance);
            ps2.setString(2,Username);
            ps2.executeUpdate();

                conn.commit();
                testMessage();
            
            }

        } finally {
            conn.close();
        }
        
        return null;
    }
    
    private int Balance;
    private String Username;
    private String Imagename;
    private String Bio;
    private Boolean isAdmin;
    private Boolean editable;
    private Boolean loggedIn;

    public String getImagename() {
        return Imagename;
    }

    public void setImagename(String Imagename) {
        this.Imagename = Imagename;
    }

    
    public Boolean getLoggedIn() {
        if(FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal() == null)
        {
            Username = "";
        Balance = 0;
        loggedIn = false;
            return false;
        }else{
            //loggedIn = true;
            Username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
            return true;}
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }
    
    
    public int getBalance() throws SQLException {
        String username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();

        int userBalance = 0;
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }

        try {

            String getUser = "select BALANCE from USERS"
                    + " where USERNAME = ?";

            PreparedStatement ps = conn.prepareStatement(getUser);
            ps.setString(1, username);

            ResultSet result = ps.executeQuery();

            while (result.next()) {
                userBalance = result.getInt("BALANCE");
            }         

        } finally {
            conn.close();
        }
        return userBalance;
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

    public void setBio(String Bio) throws SQLException{
        this.Bio = Bio;
        editable = false;
        
         if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "UPDATE USERS set BIO = ? "
                            + "WHERE USERNAME = ?"
            );

            ps1.setString(1,Bio);
            ps1.setString(2,Username);
            ps1.executeUpdate();

                conn.commit();
            

        } finally {
            conn.close();
        }
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    
}
