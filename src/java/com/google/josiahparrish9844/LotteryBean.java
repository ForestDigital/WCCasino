/*
 * The Bean in charge of the lottery functionality of the site
 */
package com.google.josiahparrish9844;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.sql.DataSource;

/**
 *
 * @author jay-t
 */
@ManagedBean(name="lotteryBean")
@Eager
@ApplicationScoped
public class LotteryBean implements Serializable{
    
    private final int BOUND = 9;
    private final int PRICE_OF_TICKET = 500;
    private final int PRIZE_FOR_ONE_WIN = 1000;
    private final int PRIZE_FOR_TWO_WINS = 5000;
    
    private boolean DEBUG = true;
    
    @Resource(name="jdbc/wspFinal")
    private DataSource ds;
    
    @ManagedProperty(value="#{cashierBean}")
    private CashierBean cashier;
    
    private int currentNumber1, currentNumber2, currentNumber3;
    private int currentPot;
    private int currentRevenue;
    private Random rand;
    private int previousNumber1, previousNumber2, previousNumber3;
    private Timer timer;
    private int secondsLeft;
    private boolean started;
    
    private final ScheduledExecutorService scheduler =
     Executors.newScheduledThreadPool(1);

   public void startLotterySystems() {
     final Runnable beeper;
        beeper = new Runnable() {
            @Override
            public void run() {
                
                //This is the action that will occur
                //at every drawing;
                previousNumber1 = currentNumber1;
                previousNumber2 = currentNumber2;
                previousNumber3 = currentNumber3;
                
                currentNumber1 = rand.nextInt(BOUND);
                currentNumber2 = rand.nextInt(BOUND);
                currentNumber3 = rand.nextInt(BOUND);
                
                currentPot = currentPot + currentRevenue + 2500;
                
                try {
                    drawNumbers();
                } catch (SQLException ex) {
                    Logger.getLogger(LotteryBean.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                //Dump tickets and revenue
                currentRevenue = 0;
                
                secondsLeft = 60;
            }
        };
     final ScheduledFuture<?> beginLotto =
       scheduler.scheduleAtFixedRate(beeper, 1, 60, SECONDS);

   }
   
      public void startTimer() {
     final Runnable beeper = new Runnable() {
       public void run() { 
       
          if(secondsLeft > 1)              
              secondsLeft--;
       
       }
     };
     final ScheduledFuture<?> beeperHandle =
       scheduler.scheduleWithFixedDelay(beeper, 1, 1, SECONDS);

   }
   
    //@PostConstruct
    public void init() {
        rand = new Random();
        timer = new Timer();
        currentNumber1 = rand.nextInt(BOUND);
        currentNumber2 = rand.nextInt(BOUND);
        currentNumber3 = rand.nextInt(BOUND);
        secondsLeft = 60;
        currentPot = 10000;
        
        startLotterySystems();
        startTimer();
    }
    

    
        
    public ArrayList<lotteryTicket> getLottoTickets() throws SQLException {

        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();

        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        
        ArrayList<lotteryTicket> list = new ArrayList<>();

        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "select USERNAME, NUM1, NUM2, NUM3 from LOTTERY_TICKETS"
            );

            ResultSet result = ps1.executeQuery();

            while (result.next()) {
                lotteryTicket lt = new lotteryTicket(result.getString("USERNAME"),
                                                        result.getInt("NUM1"),
                                                        result.getInt("NUM2"),
                                                        result.getInt("NUM3"));

                list.add(lt);
            }

        } finally {
            conn.close();
        }
        
        return list;
    }
    
    public void drawNumbers() throws SQLException {
        
        List<lotteryTicket> allTickets = getLottoTickets();
        
        if(!allTickets.isEmpty()){
        boolean potNeedsReset = false;
        for(lotteryTicket ticket : allTickets){
            int totalWins = 0;
            if(DEBUG){
                if(ticket.getNumber1() == 1)
                    totalWins++;
                if(ticket.getNumber2() == 1)
                    totalWins++;
                if(ticket.getNumber3() == 1)
                    totalWins++;
            }
            else{
                if(ticket.getNumber1() == currentNumber1)
                    totalWins++;
                if(ticket.getNumber2() == currentNumber2)
                    totalWins++;
                if(ticket.getNumber3() == currentNumber3)
                    totalWins++;
            }
            
            if(totalWins == 1){
                cashier.issuePayout(ticket.getUsername(), PRIZE_FOR_ONE_WIN);
                cashier.addToLotteryTotal(PRIZE_FOR_ONE_WIN);
                //throws an exception for some reason
//                FacesContext context = FacesContext.getCurrentInstance();
//         
//                context.addMessage(null, new FacesMessage("You Won!",  "Lottery Payout: 1000 Tokens!"));
            }
            if(totalWins == 2){
                cashier.issuePayout(ticket.getUsername(), PRIZE_FOR_TWO_WINS);
                cashier.addToLotteryTotal(PRIZE_FOR_TWO_WINS);
                //throws an exception for some reason

//                FacesContext context = FacesContext.getCurrentInstance();
//         
//                context.addMessage(null, new FacesMessage("You Won!",  "Lottery Payout: 5000 Tokens!"));
            }
            if(totalWins == 3){ //JACKPOT!!
                cashier.issuePayout(ticket.getUsername(), currentPot);
                cashier.addToLotteryTotal(currentPot);
                potNeedsReset = true;
               //throws an exception for some reason
                //FacesContext context = FacesContext.getCurrentInstance();
                //context.addMessage(null, new FacesMessage("You Won!",  "Lottery Payout: "+currentPot+" Tokens!"));   
            }            
        }
        if(potNeedsReset)
            resetPot();
        }
    }
    
    public void resetPot() throws SQLException{
        currentPot = 10000;  
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        

        try { //"dump tickets"
            PreparedStatement ps1 = conn.prepareStatement(
                    "TRUNCATE TABLE LOTTERY_TICKETS"
            ); //reset table

           
            ps1.executeUpdate();

            conn.commit();
            

        } finally {
            conn.close();
        }
    }

    public CashierBean getCashier() {
        return cashier;
    }

    public void setCashier(CashierBean cashier) {
        this.cashier = cashier;
    }
    
    public int getCurrentNumber1() {
        return currentNumber1;
    }

    public void setCurrentNumber1(int currentNumber1) {
        this.currentNumber1 = currentNumber1;
    }

    public int getCurrentNumber2() {
        return currentNumber2;
    }

    public void setCurrentNumber2(int currentNumber2) {
        this.currentNumber2 = currentNumber2;
    }

    public int getCurrentNumber3() {
        return currentNumber3;
    }

    public void setCurrentNumber3(int currentNumber3) {
        this.currentNumber3 = currentNumber3;
    }

    public int getCurrentPot() {
        return currentPot;
    }

    public void setCurrentPot(int currentPot) {
        this.currentPot = currentPot;
    }

    public int getCurrentRevenue() {
        return currentRevenue;
    }

    public void setCurrentRevenue(int currentRevenue) {
        this.currentRevenue = currentRevenue;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public int getPreviousNumber1() {
        return previousNumber1;
    }

    public void setPreviousNumber1(int previousNumber1) {
        this.previousNumber1 = previousNumber1;
    }

    public int getPreviousNumber2() {
        return previousNumber2;
    }

    public void setPreviousNumber2(int previousNumber2) {
        this.previousNumber2 = previousNumber2;
    }

    public int getPreviousNumber3() {
        return previousNumber3;
    }

    public void setPreviousNumber3(int previousNumber3) {
        this.previousNumber3 = previousNumber3;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }
 
    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    public boolean isDEBUG() {
        return DEBUG;
    }

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }   
}
