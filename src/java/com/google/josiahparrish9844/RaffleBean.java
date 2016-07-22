/*
 * This bean controls the Raffle Ticket System
 *
*/
package com.google.josiahparrish9844;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.util.Random;
import javax.annotation.Resource;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;

@ManagedBean(name="raffleBean")
@ApplicationScoped
public class RaffleBean implements Serializable{
    
    @ManagedProperty(value="#{cashierBean}")
    private CashierBean cashier;
    
    @Resource(name="jdbc/wspFinal")
    private DataSource ds;

    private int currentPrize;
    private int rafflesLeft;
    private int pricePerTicket;
    private Random rand;
    
    private final int NUMBER_OF_TICKETS = 20;

    public void init(){
        currentPrize = 0;
        rafflesLeft = 0;
        pricePerTicket = 0;       
        rand = new Random();
        
        getNextPrize();
    }       

    public int getCurrentPrize() {
        return currentPrize;
    }
    
    public void getNextPrize() {
        currentPrize = rand.nextInt(12000);
        currentPrize += 3000;
        
        int remainder = currentPrize % 1000;
        
        currentPrize -= remainder;
        
        pricePerTicket = currentPrize/NUMBER_OF_TICKETS;
        
        rafflesLeft = NUMBER_OF_TICKETS;
    }
    
    public void endOfCurrentRaffle() throws SQLException {
        
        selectWinner();
        
        getNextPrize();
    
    }
    
    public void selectWinner() throws SQLException{
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        
        ArrayList<String> list = new ArrayList<>();

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "select USERNAME from RAFFLE_TICKETS"
            );

            ResultSet result = ps1.executeQuery();

            while (result.next()) {
                list.add(result.getString("USERNAME"));
            }

            PreparedStatement ps2 = conn.prepareStatement(
                "TRUNCATE TABLE RAFFLE_TICKETS");
            ps2.executeUpdate();
            
            conn.commit();
            
        } finally {
            conn.close();
        }
        
        String winner = list.get(rand.nextInt(NUMBER_OF_TICKETS));
        
        cashier.issuePayout(winner, currentPrize);
        cashier.addToRaffleTotal(currentPrize);
    }
    
    public void purchaseTickets(int howMany)throws SQLException{
        String username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
        int bought = 0;
        
        if(rafflesLeft >= howMany){
            bought = cashier.buyRaffleTicket(username, howMany, pricePerTicket);
            
        }            
        else{
            bought = cashier.buyRaffleTicket(username, rafflesLeft, pricePerTicket);
            
        }        
        rafflesLeft -= bought;
        
        if(rafflesLeft < 1){
            endOfCurrentRaffle();
        }
    }
    
    public void setCurrentPrize(int currentPrize) {
        this.currentPrize = currentPrize;
    }

    public int getRafflesLeft() {
        return rafflesLeft;
    }

    public void setRafflesLeft(int rafflesLeft) {
        this.rafflesLeft = rafflesLeft;
    }

    public int getPricePerTicket() {
        return pricePerTicket;
    }

    public void setPricePerTicket(int pricePerTicket) {
        this.pricePerTicket = pricePerTicket;
    }

    public CashierBean getCashier() {
        return cashier;
    }

    public void setCashier(CashierBean cashier) {
        this.cashier = cashier;
    }
    
    
}
