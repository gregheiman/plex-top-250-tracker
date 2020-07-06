## Plex Top 250 Tracker

This program is designed to track what movies on the IMDB Top 250 list 
are missing from your Plex library. The program requires the use of the Plex
API which is undocumented and not very user-friendly. For more information on the
use of the API I recommend [this page](https://github.com/Arcanemagus/plex-api/wiki) 
where users have gathered information on how the API works.

## Requirements
1. Java 11
2. Maven
3. A Plex Server set up on the local network
4. An understanding of how the Plex API works (recommended)

## Instructions on How to Run the Program
**First and foremost I recommend having an understanding of how the Plex API works
and more specifically how your personal Plex library is set up.**

1. Clone the repository onto your local machine

2. Navigate to the repositories local location

3. You have two options for obtaining the needed information in order to access your Plex server. You can either do the automatic way which just needs your Plex login and Plex password, or if you have all the information already gathered you can use the manual way.
    * The needed information is as follows: 
        * the servers IP address
        * the server's port number
        * the Plex library's key
        * your Plex authentication token
    

4. Run the following command inside of the repo's local location in order
to install the Maven packages:
    ```
    mvn install
    ```
   
5. Then run the following command in order to run the program:
    ```
    mvn clean compile exec:java
    ```
   
10. If you would like to email the automatically created excel sheet to yourself or someone else
you must have a Gmail account and either allow less secure sign ins through the security panel, or
if you have 2-Factor authentication you need to create an app password to use with this program.
    * If you would not like to use Gmail or don't want to email the spreadsheet anywhere just end the program when you get to that point.

