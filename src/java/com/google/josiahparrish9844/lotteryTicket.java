
package com.google.josiahparrish9844;

/**
 * POJO class for the lottery tickets.
 * @author jay-t
 */
public class lotteryTicket {
    private String username;
    private int number1, number2, number3;
  
    
    public lotteryTicket(String uname, int num1, int num2, int num3){
        username = uname;
        number1 = num1;
        number2 = num2;
        number3 = num3;
    }

    public String getUsername() {
        return username;
    }

    public int getNumber1() {
        return number1;
    }

    public int getNumber2() {
        return number2;
    }

    public int getNumber3() {
        return number3;
    }
    
    
}
