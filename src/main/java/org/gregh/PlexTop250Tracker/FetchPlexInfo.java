package org.gregh.PlexTop250Tracker;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.NoRouteToHostException;
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
        Document grabPlexAuthToken = null;

        while (true) {
            System.out.println("Please enter in the email or username that you use to login to your Plex server.");
            String plexLogin = input.nextLine();
            System.out.println("Please enter in the password that you use to login to your Plex server.");
            String plexPassword = input.nextLine();

            try {
                // Post to the Plex sign_in xml link
                grabPlexAuthToken = Jsoup.connect("https://plex.tv/users/sign_in.xml?X-Plex-Client-Identifier=1")
                        .data("user[login]", plexLogin)
                        .data("user[password]", plexPassword)
                        .followRedirects(false)
                        .parser(Parser.xmlParser())
                        .post();
                break;
            } catch (HttpStatusException e) {
                System.out.println("The program was unable to access Plex with the information that you entered in.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Parse the returned XML to find the authentication-token value
        Elements authToken;
        if (grabPlexAuthToken != null) {
            authToken = grabPlexAuthToken.getElementsByTag("authentication-token");
            setPlexAuthToken(authToken.text());
        } else {
            System.out.println("Could not find the user tag");
        }

        // User selects which server to use as the IP address and Port num
        userSelectsWhichServer();
        // User selects which library to use
        userSelectsWhichLibrary();

    }

    /**
     * User goes through their set up Plex servers and selects which one to use
     */
    private void userSelectsWhichServer() {
        Scanner input = new Scanner(System.in);

        Document listOfPlexServers = null;

        try {
            listOfPlexServers = Jsoup.connect("https://plex.tv/api/resources?X-Plex-Token=" + getPlexAuthToken()).get();
        } catch (IOException e) {
            System.out.println("There was a problem fetching the list of Plex servers using the information provided");
            e.printStackTrace();
        }

        Elements serverNames = null;

        // Dynamic input based on the output of the server list
        while (true) {
            if (listOfPlexServers != null) {
                System.out.println("\nServers:");
                serverNames = listOfPlexServers.getElementsByTag("Device");

                // Print out a list of all of the users servers
                for (int i = 0; i < serverNames.size(); i++) {
                    System.out.println((i + 1) + ". " + serverNames.get(i).attr("name"));
                }
            }

            try {
                System.out.println("Select which of the servers above is the one you would like to use.");
                int serverNum = input.nextInt();

                if (serverNum > (serverNames.size() + 1)) {
                    System.out.println("Please enter in a valid option");
                } else {
                    System.out.println(serverNames.get(serverNum - 1).attr("name"));

                    Elements connections = null;

                    // Create a list of all the connection tags
                    if (listOfPlexServers != null) {
                        connections = listOfPlexServers.getElementsByTag("Connection");
                    } else {
                        System.out.println("The list of serves is null");
                    }

                    // Find the appropriate connection tag and assign the ip address and port number from that tag
                    if (connections != null) {
                        setPlexIPAddress(connections.get(serverNum - 1).attr("address"));
                        setPlexPortNumber(connections.get(serverNum - 1).attr("port"));
                        break;
                    } else {
                        System.out.println("Unable to get a list of the connection tag");
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter in a valid option.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Please enter in a valid option.");
            }
        }



    }

    /**
     * User goes through the selected server's libraries and selects which one to use
     */
    private void userSelectsWhichLibrary() {
        Scanner input = new Scanner(System.in);

        Document listOfPlexLibraries = null;

        try {
            listOfPlexLibraries = Jsoup.connect("http://" + getPlexIP() + ":" + getPlexPort() + "/library/sections" +
                    "?X-Plex-Token=" + getPlexAuthToken()).get();
        } catch (NoRouteToHostException e) {
            System.out.println("The program was unable to find a valid route to the selected host. Please select" +
                    " a different host or fix any network related problems.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements libraryNames = null;

        if (listOfPlexLibraries != null) {
            System.out.println("\nLibraries:");
            libraryNames = listOfPlexLibraries.getElementsByTag("Directory");

            for (int i = 0; i < libraryNames.size(); i++) {
                System.out.println((i + 1) + ". " + libraryNames.get(i).attr("title"));
            }

            // Dynamic input based on the output of the library list
            while (true) {
                try {
                    System.out.println("Select which of the libraries above is the one you would like to use.");
                    int libraryNum = input.nextInt();

                    if (libraryNum > (libraryNames.size() + 1)) {
                        System.out.println("Please enter in a valid option");
                    } else {
                        System.out.println(libraryNames.get(libraryNum - 1).attr("title"));

                        // Find the appropriate library and grab the key from the attributes
                        setPlexLibraryNum(libraryNames.get(libraryNum - 1).attr("key"));
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Please enter in a valid option.");
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Please enter in a valid option.");
                }
            }
        }
    }

    /**
     * If the user prefers to enter in all of the needed information manually or the automatic system doesn't work
     */
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
