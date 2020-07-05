package org.gregh.PlexTop250Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

public class FetchPlexInfo {
    private String plexIPAddress;
    private String plexPortNum;
    private String plexLibraryNum;
    private String plexAuthToken;

    public String getPlexIP() {
        return plexIPAddress;
    }

    public void setPlexIPAddress(String plexIP) {
        if (verifyIPAddress(plexIP)) {
            this.plexIPAddress = plexIP;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getPlexPort() {
        return plexPortNum;
    }

    public void setPlexPortNumber(String plexPort) {
        if (verifyPortNum(plexPort)) {
            this.plexPortNum = plexPort;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getPlexLibraryNum() {
        return plexLibraryNum;
    }

    public void setPlexLibraryNum(String plexLibraryNum) {
        if (verifyPlexLibraryNum(plexLibraryNum)) {
            this.plexLibraryNum = plexLibraryNum;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getPlexAuthToken() {
        return plexAuthToken;
    }

    public void setPlexAuthToken(String plexAuthToken) {
        if (verifyPlexAuthToken(plexAuthToken)) {
            this.plexAuthToken = plexAuthToken;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Uses regex to verify that user's entered ip address is valid
     * @param ipAddress - The IP address the user inputted
     * @return - boolean of whether the ip address is valid or not
     */
    private boolean verifyIPAddress(String ipAddress) {
        if (ipAddress.isEmpty() || ipAddress.isBlank()) {
            return false;
        }

        String pattern = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ipAddress.matches(pattern);
    }

    /**
     * Uses regex to verify the users entered port number
     * @param portNumber - The port number that the user entered
     * @return - boolean of whether the port number is valid or not
     */
    private boolean verifyPortNum(String portNumber) {
        if (portNumber.isBlank() || portNumber.isEmpty()) {
            return false;
        }

        // Checks that the users has entered in no more than 5 digits
        String pattern = "^\\d{1,5}$";

        return portNumber.matches(pattern) && Integer.parseInt(portNumber) < 65535;
    }

    private boolean verifyPlexAuthToken(String plexAuthToken) {
        if (plexAuthToken.isEmpty() || plexAuthToken.isBlank()) {
            return false;
        }

        // plexAuthToken must be 20 characters long
        String pattern = "^\\w{20}$";

        return plexAuthToken.matches(pattern);
    }

    private boolean verifyPlexLibraryNum(String libraryNum) {
        // Checks that the user entered in digits
        String pattern = "^\\d$";

        if (libraryNum.isBlank() || libraryNum.isEmpty()) {
            return false;
        }

        return libraryNum.matches(pattern);
    }

    public void automaticallyFetchPlexInfo() {
        // Take in the users Plex login info
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter in the email or username that you use to login to your Plex server.");
        String plexLogin = input.nextLine();
        System.out.println("Please enter in the password that you use to login to your Plex server.");
        String plexPassword = input.nextLine();

        Document grabPlexAuthToken = null;

        // Create a random UUID for the X-Plex-Client-Identifier
        String randomUUID = UUID.randomUUID().toString();

        // Post to the Plex sign_in xml link
        try {
            grabPlexAuthToken = Jsoup.connect("https://plex.tv/users/sign_in.xml?X-Plex-Client-Identifier=" + randomUUID)
                    .data("user[login]", plexLogin)
                    .data("user[password]", plexPassword)
                    .userAgent("Mozilla")
                    .parser(Parser.xmlParser())
                    .post();

            System.out.println(grabPlexAuthToken);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse the returned XML to find the authentication-token value
        Elements authToken;
        if (grabPlexAuthToken != null) {
            authToken = grabPlexAuthToken.getElementsByTag("user");
            System.out.println(authToken.attr("authentication-token"));
        } else {
            System.out.println("Could not find the user tag");
        }

    }

    public void manuallyFetchPlexInfo() {
        Scanner input = new Scanner(System.in);

        // Take in the user's Plex IP address
        while (true) {
            try {
                System.out.println("What is the IP address of your local Plex server?");
                setPlexIPAddress(input.nextLine());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter in a valid IP address");
            }
        }

        // Take in the user's Plex port number
        while (true) {
            try {
                System.out.println("What is the port number that your local Plex server runs on?");
                setPlexPortNumber(input.nextLine());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter in a valid port number");
            }
        }

        // Take in the user's Plex library
        while (true) {
            try {
                System.out.println("What is the key of the library in which you store movies?");
                setPlexLibraryNum(input.nextLine());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter in a valid library number");
            }
        }

        while (true) {
            try {
                System.out.println("What is the auth token for your local Plex server?");
                setPlexAuthToken(input.nextLine());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter in a valid Plex auth token");
            }
        }
    }
}
