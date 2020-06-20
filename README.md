## Plex Top 250 Tracker

This program is designed to track what movies on the IMDB Top 250 list 
are missing from your Plex library. The program requires the use of the Plex
API which is undocumented and not very user-friendly. For more information on the
use of the API I recommend [this page](https://github.com/Arcanemagus/plex-api/wiki) 
where users have gathered information on how the API works.

## Requirements
1. Java 11
2. Maven
3. A Plex Server set up, and know the servers IP address
4. An understanding of how the Plex API works (recommended)

## Instructions on How to Run the Program
**First and foremost I recommend having an understanding of how the Plex API works
and more specifically how your personal Plex library is set up.**

1. Clone the repository onto your local machine

2. Figure out your personal Plex authentication token. To figure that out go 
[here.](https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/)

3. Figure out what the IP address of your Plex server is and what port the server is running on (Typically port 32400)

4. Figure out what the key of the library you want to check is
    1. To do that run hit the following API endpoint:
        ```
        http://IP:PORT/libary/sections?X-Plex-Token=Token 
        ```
    2. Look through the results till you find the right library and copy down the "key="
    number under the <Directory> tag.
    
5. Then go into GrabAllMovieNamesInPlex.java file and change the **plexBaseURL**
 under the constructor to the following URL:
    ```
    http://IP:PORT/libary/sections/key/all?X-Plex-Token=Token
    ```

6. Navigate to the repositories local location

7. Run the following command inside of the repo's local location in order
to install the Maven packages:
    ```
    mvn install
    ```
   
8. Then run the following command in order to run the program:
    ```
    mvn clean compile exec:java
    ```

