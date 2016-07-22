/*
 * This Managed Bean handles most of the transactions between the user and server. 
 * Acts as a 'Cashier' for the casino
 */
package com.google.josiahparrish9844;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;

/**
 *
 * @author jay-t
 */
@ManagedBean(name = "cashierBean")
//@Named(value = "cashierBean")
@ApplicationScoped
public class CashierBean implements Serializable {

    @Resource(name = "jdbc/wspFinal")
    private DataSource ds;

    final int LOTTERY_TICKET_PRICE = 500;
    final int SCRATCHER_1_PRICE = 500;
    final int SCRATCHER_2_PRICE = 1000;
    final int SCRATCHER_3_PRICE = 3000;

    private int totalPayouts = 0;
    private int totalLottery = 0;
    private int totalScratchers = 0;
    private int totalRaffles = 0;

    /**
     * Creates a new instance of CashierBean
     */
    public CashierBean() {

    }

    public int buyRaffleTicket(String username, int howMany, int price) throws SQLException {
        int userBalance = 0;
        int ticketsBought = 0;

        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }

        for (int i = 0; i < howMany; i++) {
            try {

                String getUser = "select BALANCE from USERS"
                        + " where USERNAME = ?";

                PreparedStatement ps = conn.prepareStatement(getUser);
                ps.setString(1, username);

                ResultSet result = ps.executeQuery();

                while (result.next()) {
                    userBalance = result.getInt("BALANCE");
                }
                if (userBalance >= price) {
                    userBalance -= price;

                    //Must update user balance in database first before proceeding
                    PreparedStatement ps2 = conn.prepareStatement(
                            "UPDATE USERS set BALANCE = ? "
                            + "WHERE USERNAME = ?"
                    );

                    ps2.setInt(1, userBalance);
                    ps2.setString(2, username);
                    ps2.executeUpdate();

                    conn.commit();

                    FacesContext context = FacesContext.getCurrentInstance();

                    context.addMessage(null, new FacesMessage("Successful!", "Raffle Ticket Purchased!"));
                } else {
                    FacesContext context = FacesContext.getCurrentInstance();

                    context.addMessage(null, new FacesMessage("Error", "Insufficient Funds!"));
                    return ticketsBought; //finally will still be called
                }

                PreparedStatement ps1 = conn.prepareStatement(
                        "INSERT INTO RAFFLE_TICKETS"
                        + "(USERNAME) VALUES"
                        + "(?)"
                );

                ps1.setString(1, username);
                ps1.executeUpdate();
                ticketsBought++;
                //printReciept(username, "Raffle Ticket", price);
                conn.commit();
            } finally {

            }
        }
        conn.close();
        return ticketsBought;
    }

    public void buyLotteryTicket(int num1, int num2, int num3) throws SQLException {
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
            if (userBalance >= LOTTERY_TICKET_PRICE) {
                userBalance -= LOTTERY_TICKET_PRICE;

                //Must update user balance in database first before proceeding
                PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE USERS set BALANCE = ? "
                        + "WHERE USERNAME = ?"
                );

                ps2.setInt(1, userBalance);
                ps2.setString(2, username);
                ps2.executeUpdate();

                conn.commit();

                FacesContext context = FacesContext.getCurrentInstance();

                context.addMessage(null, new FacesMessage("Successful!", "Lottery Ticket Purchased!"));
            } else {
                FacesContext context = FacesContext.getCurrentInstance();

                context.addMessage(null, new FacesMessage("Error", "Insufficient Funds!"));
                return; //finally will still be called
            }

        } finally {

        }

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO LOTTERY_TICKETS"
                    + "(USERNAME, NUM1, NUM2, NUM3) VALUES"
                    + "(?,?,?,?)"
            );

            ps1.setString(1, username);
            ps1.setInt(2, num1);
            ps1.setInt(3, num2);
            ps1.setInt(4, num3);
            ps1.executeUpdate();

            printReciept(username, "Lottery Ticket", LOTTERY_TICKET_PRICE);

            conn.commit();
        } finally {
            conn.close();
        }

    }

    public void printReciept(String username, String tranType, int amountSpent) throws SQLException {
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
                    "INSERT INTO TRANSACTIONS"
                    + "(USERNAME, PURCHASE_AMOUNT, TRANSACTION_TYPE, timeOfPurchase) VALUES"
                    + "(?,?,?,NOW())"
            );

            ps1.setString(1, username);
            ps1.setInt(2, amountSpent);
            ps1.setString(3, tranType);
            ps1.executeUpdate();

            conn.commit();

        } finally {
            conn.close();
        }
    }

    public void issuePayout(String username, int payoutAmount) throws SQLException {
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

            userBalance += payoutAmount;

            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE USERS set BALANCE = ? "
                    + "WHERE USERNAME = ?"
            );

            ps2.setInt(1, userBalance);
            ps2.setString(2, username);
            ps2.executeUpdate();

            conn.commit();

        } finally {
            conn.close();
        }
    }

    public void purchaseScratcher(int scratcherType) throws SQLException {
        String username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
        Random rand = new Random();
        int subtotal = 0;

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

            if (scratcherType == 1) {
                subtotal = SCRATCHER_1_PRICE;
            }
            if (scratcherType == 2) {
                subtotal = SCRATCHER_2_PRICE;
            }
            if (scratcherType == 3) {
                subtotal = SCRATCHER_3_PRICE;
            }

            if (userBalance >= subtotal) {
                userBalance -= subtotal;

                //Must update user balance in database first before proceeding
                PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE USERS set BALANCE = ? "
                        + "WHERE USERNAME = ?"
                );

                ps2.setInt(1, userBalance);
                ps2.setString(2, username);
                ps2.executeUpdate();

                conn.commit();

                FacesContext context = FacesContext.getCurrentInstance();

                context.addMessage(null, new FacesMessage("Scratcher Purchased!", "Good Luck!"));
            } else {
                FacesContext context = FacesContext.getCurrentInstance();

                context.addMessage(null, new FacesMessage("Error", "Insufficient Funds!"));
                return; //finally will still be called
            }

        } finally {
            conn.close();
        }

        int payout = 0;

        if (scratcherType == 1) {
            int numberOfWins = rand.nextInt(6);
            if (numberOfWins >= 3) {
                numberOfWins = rand.nextInt(6);
            }

            for (int i = 0; i < numberOfWins; i++) {
                int thisAward = rand.nextInt(1000);
                payout += thisAward;
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage("Award!", "Found " + thisAward + " tokens!"));

            }
        }
        if (scratcherType == 2) {
            int numberOfWins = rand.nextInt(6);
            if (numberOfWins >= 3) {
                numberOfWins = rand.nextInt(6);
            }

            for (int i = 0; i < numberOfWins; i++) {
                int thisAward = rand.nextInt(3300);
                payout += thisAward;
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage("Award!", "Found " + thisAward + " tokens!"));
            }
        }
        if (scratcherType == 3) {
            int numberOfWins = rand.nextInt(6);
            if (numberOfWins >= 3) {
                numberOfWins = rand.nextInt(6);
            }

            for (int i = 0; i < numberOfWins; i++) {
                int thisAward = rand.nextInt(6600);
                payout += thisAward;
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage("Award!", "Found " + thisAward + " tokens!"));
            }
        }

        issuePayout(username, payout);
        addToScratcherTotal(payout);
        printReciept(username, "Scratcher " + scratcherType, subtotal);

    }

    public int getTotalPayouts() {
        return totalLottery + totalScratchers + totalRaffles;
    }

    public void setTotalPayouts(int totalPayouts) {
        this.totalPayouts = totalPayouts;
    }

    public int getTotalLottery() {
        return totalLottery;
    }

    public void setTotalLottery(int totalLottery) {
        this.totalLottery = totalLottery;
    }

    public int getTotalScratchers() {
        return totalScratchers;
    }

    public void setTotalScratchers(int totalScratchers) {
        this.totalScratchers = totalScratchers;
    }

    public int getTotalRaffles() {
        return totalRaffles;
    }

    public void setTotalRaffles(int totalRaffles) {
        this.totalRaffles = totalRaffles;
    }

    public void addToRaffleTotal(int payout) {
        totalRaffles += payout;
    }

    public void addToScratcherTotal(int payout) {
        totalScratchers += payout;
    }

    public void addToLotteryTotal(int payout) {
        totalLottery += payout;
    }
}
