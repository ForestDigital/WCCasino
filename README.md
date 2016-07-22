# WCCasino
Repo for Winners Circle Casino, JSF2+Primefaces Gambling Website

#Research Abstract
##Problem and motivation
Seeking the thrills and entertainment of participating in the lottery or going to the store to buy scratch cards is tedious and time consuming. Due to health concerns, the fun of a casino might be too difficult to travel to. Luckily, the Internet provides a great medium to bring this kind of environment to the personal computer.

Winners Circle Casino aims to provide the entertainment from a real casino, into a personalized environment of the user's choosing. With 3 different games to choose from, and a customizable user profile page, players can sit back at home or work and still be able to enjoy the fun of a casino!

##Background
Winners Circle Casino attempts to recreate a similar casino site from the MMO game EVE Online called iwantisk.com. On this site there are multiple games and an imaginary currency called 'isk'. Players participate in the luck based games like slots or scratchcards, and compete in the more competitive games like Raffles.


 ![Alt text](/web/resources/images/abstract1.png "Fig. 1: Iwantisk.com")
 
 Of the many games on the original website, for time constraint and difficulty reasons, we decided to implement 3 games from it: Raflles, Scratchcards, and a Lottery System. These games would be challenging to implement, but also fun and not too difficult as they are mostly simple procedural programming. Raffles and scratchcards are already on the original site, but the Lottery System is not, and was designed and implemented solely by us. Implementing this would be the most challenging facet of  the program, but using Java ScheduledExecutor service, it would still be possible. 
 
##Solution
The first endeavor of this project is to create a user interface that is both tasteful and informative, and shows the users all of the entertainment options provided by the site. The Landing Page needs to show the user the games available, and provide statistics about the site, to encourage the user to play by showing how much other users have won already. We adopted a design that was similar to the original site, but still proprietary to the games at our casino.

![Alt text](/web/resources/images/abstract2.png "Fig. 2: Homepage Wireframe")

Here the user can easily see the logo and what games are available, as well as their profile and user balance as to feel more personalized. A slideshow with promotions and events is visible to keep the user updated on what is going on in the site, as well as the various statistical data. The profile customization is an extremely important part of the design. We want the user to feel like they are in a sociable environment, and that they can interact with other users by personalizing their profile. 
    
Since there is a significant amount of currency flow on the site, there needs to be at least 2 types of users. A normal player should have access to all of the games, have their own balance and authentication information, and be able to customize their profile. There also needs to be an Administrator user type, who along with all of the functions of a normal user, also has increased control over the site and is able to moderate the site and update other users information. Control between these user types should be handled by JDBC Realm Authentication, so that even if a malicious user were to determine the URL's of the various Administrative Tasks, they wouldn't be able to access them. 
    
A basic user will be able to navigate to the site on the internet, engage minimally with the games, and be able to register by themselves for an account on the site. All users who register with the site online will be granted the basic user rights and then given their own profile.

![Alt text](/web/resources/images/abstract3.png "Fig. 3: ProfilePage Wireframe")

The profile page shows the user's selected username, as well as their profile picture, balance, and short biography. Upon first registering with the site, the user will have a stock profile image, and then have the option to upload any image of their choosing. Allowing the user to have a biography places another level of socialization to the site, letting the user feel more like they are part of a gambling community. The user can update their bio to say whatever they feel anyone else on the site should know about them. Their balance is the amount of tokens that they have to play the games with. It should be always updated with the database so that the user knows exactly how much they have. 
    
Administrators are the other type of user class and act as moderators for the site. Unlike a regular user, who can enroll on the site by themselves, an Administrator must be chosen by the site owner, and enrolled into the database manually. This prevents the possiblility of any malicious internet user from self enrolling in the database and getting unauthorized access to administrative tasks. 
    
Along with having all of the same rights and abilities as users, Administrative users also have required tasks at the beginning of the site launch, as well as moderation tasks, to make sure the site runs smoothly and users are playing fair. At the site launch, timers will need to be started manually to begin the more competitive games. This is control is granted to the administrator in case any part of the game execution fails and needs to be restarted. For example: An administrator has the ability to force the lottery to draw the winning numbers and select the winners, as well as the ability to stop and start the lottery execution. If for any reason the lottery were to stop running, the administrator would be able to draw the numbers that are already in the pot, to ensure that any paying customers money has not gone to waste. Then the Administrator would be able to stop and restart the lottery beginning again with a fresh cycle, resuming the flow of the site back to normal. Administrators also have moderation duties by looking through the databases both manually and at the administrative 'User Control' page. If a user's account activity looks suspicious, or if the administrator is getting notified that the user is being a nuisance on the site, then the administrator can then determine the users activity by monitoring the transactions in the Transactions database. From there an administrator can either update a user's balance to the correct value, or remove the user from the site altogether. 

Handling the statistics of the site without moderation would require each component to individually report their winnings after each win or each transaction. With 3 different games to work with and the user being able to add more tokens(the site currency) from their profile page, this could result in numerous race conditions as each game is trying to update the database and keep the user's balance in check. To get around this, instead of letting each game have access to the database, each game and each transaction be it a deposit or withdraws, must go through the cashier. Just like in a real casino or any commerce environment, the users do not pay or get money from the company itself, but rather the cashier. Making this cashier implement the Java Serializable class, we can prevent there from being any race conditions in the database. The Cashier bean handles the transactions between the user and the database, and issues the appropriate tickets if the user has enough tokens. Then if a user has won a game, issues payouts and updates the database and statistical data. Any flow of currency is handled by the Cashier.

![Alt text](/web/resources/images/abstract4.gif "Fig. 4: Cashier Bean Use Case")

Using this separation of duties allows us to add or remove games on the site freely and only requiring that a game let the cashier handle transactions. The Cashier Bean is in charge of checking the balance of the user before a transaction occurs, issuing payouts if a user wins a game, and updating the site totals after any payouts. For the games where a ticket is involved, like raffle tickets or lottery tickets, the cashier fills out these tickets and inserts them into the database so that the respective games can process them. 
    
The Raffle ticket system will be the simplest to implement. For each game, a prize will be chosen and split into twenty tickets. Players can buy any number of these tickets. After all twenty tickets have been bought, a single ticket will be chosen at random, and the player who bought this ticket will be awarded the full price of the prize.

![Alt text](/web/resources/images/abstract5.png "Fig. 5: Raffle Wireframe")

If a user attempts to buy a multiple of tickets, and there are not enough tickets left in the pot or the user has insufficient funds, the Raffle Bean will attempt to buy as many tickets as are left and that don't put the user negative. For Example: Imagine that there are 4 tickets left in a raffle at 200 tokens each, and a user with 600 tokens attempts to purchase 5 tickets. The max amount of tickets that the Raffle bean will attempt to buy is 4 tickets, since that is how many is remaining. The Bean should then see that the user only has enough for 3 tickets. At the end of the transaction, even though the user has attempted to purchase 5 tickets, only 3 tickets will be sold. All drawings of the tickets will be completely random, and the drawings will only occur once all tickets have been bought. An administrator should still be able to force draw the tickets and restart the game if anything goes wrong.

![Alt text](/web/resources/images/abstract6.png "Fig. 6: Scratchcard Wireframe")

Scratchcards are the next game on the difficulty scale. A user should be able to see a selection of appealing images with 'scratch off' areas on them. The different cards should have different prices and difficulty levels so that a user would feel the need to work up to the more expensive and profitable cards.  Once a user purchases a card, then they should need to click on each of the 'scratch off' areas to reveal what prize lays underneath. The hardest part of the development of this, will be getting the right prize numbers to match up with the corresponding cards. Then moderating how a bought card should appear to the user as opposed to a card that has not been bought yet will be challenging as well since simple HTML editing could reveal the prizes before the card is bought. 
    
The hardest game to develop on the site will be the lottery. This is another ticket based game, and each ticket will have the name of the user that bought the ticket, and 3 numbers that the user has chosen from the page. There can be any number of tickets or no tickets at all for each game, and even with a large amount of tickets there might not even be a winner. Each game lasts one minute in real time. After one minute has elapsed, the winning numbers will be chosen, and all the tickets will be read. If a ticket has any corresponding numbers, then that user will receive a prize. However, the game is only won if a ticket has all three of the winning numbers in the exact order that the winning numbers were drawn. The winning numbers are drawn at the end of each game instead of the beginning to prevent any malicious users from using HTML or SQL injection to discover the winning numbers before the end of the game. The winning numbers are drawn completely randomly, so it is possible to have the same winning numbers twice. After each game, if any user(s) have chosen the same numbers as the winning numbers in the correct order, then the user(s) will receive the full prize which is the ever increasing pool of money that includes the main pot along with any revenue from ticket sales. If no users have chosen the correct numbers, even if some users have gotten one or two numbers correctly, the current money pool will transfer over to the next game. Due to the difficulty of getting the correct numbers, this money pool can grow extremely large and be a big goal for users to try to win.
    
The lottery page itself should be rather simple. Three number selectors will be shown, and a button to purchase the lottery ticket using the three selected numbers. A timer will also be displayed showing the amount of seconds until the drawing. This timer needs to be correct and display the right value to all clients. The rules and payout amounts should also be visible, encouraging the user to purchase a ticket, even though the odds of winning a jackpot are slim.

![Alt text](/web/resources/images/abstract7.png "Fig. 7: Lottery Wireframe")

##Result


The user interface was very straightforward and we were able to work directly from the wireframes. We went with a tilt shifted poker background and poker table felt panels to give the feel of a casino or gambling. Buttons were accented in a tasteful red which is easy to see, and also invokes the feeling of a poker chip on a poker table. 
    
Each individual game was given it's own bean except for the scratchcard game. The raffle bean and lottery bean are application scoped since the win conditions of these games are independent on if a client is connected to the server or not. This way a client can purchase a few raffle or lottery tickets, and leave before the games end. If that user wins those games, they will still receive the prizes even though they are no longer connected. To keep up with the moderation utilities that the administrator has to execute, the lottery and raffle games begin in an off state. Once the product is deployed, the Administrator then has to activate these games to start execution of their processes. Once a user purchases a ticket for either game through the cashier bean, the corresponding game beans then use these tickets for the games.

![Alt text](/web/resources/images/abstract8.png "Fig. 8: Finished Homepage")

The raffle ticket and lottery games were able to be created almost verbatim from the planning phase, but the scratchcard game required more of a workaround. For cycling through the cards, a Prime faces tab view proved to be the best approach. Each tab could contain its on separate data, that way the cards and prized could not be mixed up. We attempted to implement a way where each card was given 6 small images as the 'scratch off' portions, but having a way for these small images to become a visible prize upon clicking proved to be too challenging. Instead, cards themselves are static, and when a user purchases a card, all of the squares are 'scratched off' and all 6 potential prizes are revealed. We found the best way to reveal these were through prime faces Growl Messages. This produces a visible message each time it is called in an overlay. For the scratchcards, a message is only produced if a prize is found from one of the six scratch areas. For Example: Since each scratchcard has 6 scratch areas, if a user only receives 4 Growl messages, then they can assume that the other two scratch areas had no prize underneath. 


# 
# 
# 
#Screenshots

![Alt text](/web/resources/images/screenshot1.png "")
![Alt text](/web/resources/images/screenshot2.png "")
![Alt text](/web/resources/images/screenshot3.png "")
![Alt text](/web/resources/images/screenshot4.png "")
![Alt text](/web/resources/images/screenshot5.png "")
![Alt text](/web/resources/images/screenshot6.png "")
