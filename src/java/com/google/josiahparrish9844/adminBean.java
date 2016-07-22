/*
 * This bean handles the Administrative actions, primarily user control.
 */
package com.google.josiahparrish9844;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;

/**
 *
 * @author jay-t
 */
@Named(value = "adminBean")
@SessionScoped
public class adminBean implements Serializable {
    
    public adminBean() {
    }
    
    @Resource(name="jdbc/wspFinal")
    private DataSource ds;
    
    private ArrayList<User> userList;
    
    @PostConstruct
    public void init() {
        try {
            userList = getUsers();
        } catch (SQLException ex) {
            Logger.getLogger(adminBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public ArrayList<User> getUserList() throws SQLException {
        return userList;
    }
    
    public ArrayList<User> getUsers() throws SQLException {

        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();

        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        
        ArrayList<User> list = new ArrayList<>();

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "select USERNAME, IMAGENAME, BALANCE, BIO from USERS"
            );

            ResultSet result = ps1.executeQuery();

            while (result.next()) {
                User u = new User();
                u.setUsername(result.getString("USERNAME"));
                u.setBio(result.getString("BIO"));
                u.setBalance(result.getInt("BALANCE"));
                u.setProfilePic(result.getString("IMAGENAME"));

                list.add(u);
            }

        } finally {
            conn.close();
        }
        
        return list;
    }
    
    public void updateBalance(String username, String bal) throws SQLException{
        int balance;
        balance = Integer.parseInt(bal);
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }       

        try {   
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE USERS set BALANCE = ? "
                            + "WHERE USERNAME = ?"
            );

            ps2.setInt(1,balance);
            ps2.setString(2,username);
            ps2.executeUpdate();

                conn.commit();        

        } finally {
            conn.close();
        }
    }
    
    public String removeUser(User u) throws SQLException {
        //Cannot delete root
        String username = u.getUsername();
        
        if(username.equals("root")){        
            //doNothing
            addMessage("Error", "Cannot Delete Admin");
        }
        else{

            if (ds == null) {
                throw new SQLException("ds is null; Can't get data source");
            }

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            if (conn == null) {
                throw new SQLException("conn is null; Can't get db connection");
            }       

            try {   
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM USERS "
                                + "WHERE USERNAME = ?"
                );

                ps.setString(1,username);
                ps.executeUpdate();

                    conn.commit();  
                    addMessage("Success", "Deleting " + username + "...");
                    userList.remove(u);

            } finally {
                conn.close();
            }
        }
        return null;
    }
    
    public void addMessage(String summary, String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
        
        
    }
    
}
