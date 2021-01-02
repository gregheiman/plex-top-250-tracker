package org.gregh.PlexTop250Tracker;

import org.apache.logging.log4j.LogManager;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.Scanner;

import org.apache.logging.log4j.Logger;
/**
 * Class to fetch the needed info to run the program from the user, either automatically or manually.
 * @author Greg Heiman
 */
public class FetchPlexInfo {
    private String plexIPAddress;
    private String plexPortNum;
    private String plexLibraryNum;
    private String plexAuthToken;
    static Logger logger = LogManager.getLogger(FetchPlexInfo.class);

    public FetchPlexInfo() {
    }

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

                logger.warn("The program was unable to access Plex with the information that the " +
                        "user entered in." + "\nThe information the user entered in was as follows: " + "\nPlex Login: " +
                        plexLogin + "\nPlex Password: " + plexPassword + "\n");
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

        // User selects which server they want to use and which library in that server they want to use
        while (true) {
            // If any of these fail it prompts the user to select a different server
            int selectedServer = 0;
            Elements connections = new Elements();

            try {
                // Select the server number from the list of servers
                selectedServer = userSelectsWhichServer(connectToServerList());
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                System.out.println("Please enter in a valid option.");
            } catch (IOException e) {
                System.out.println("There was an IO error in selecting which server to use. Please try again.");
            } catch (Exception e) {
                System.out.println("There was an error in selecting which server to use. Please try again.");
            }

            try {
                // Grab the connection tag from the unfiltered xml as this contains the IP address and port number
                connections = connectToServerList().getElementsByTag("Connection");
            } catch (IOException e) {
                System.out.println("There was an IO error fetching the list of Plex servers using the information provided");
            } catch (Exception e) {
                System.out.println("There was an error in fetching the list of Plex servers.");
            }

            try {
                // Set the IP address and port number
                setPlexIPAddress(grabServerIPAddress(connections, selectedServer));
            } catch (Exception e) {
                System.out.println("There was an error fetching the IP address of the chosen server. Please select a new" +
                        " server.");
            }

            try {
                // Set the IP address and port number
                setPlexPortNumber(grabServerPortAddress(connections, selectedServer));
            } catch (Exception e) {
                System.out.println("There was an error fetching the port number of the chosen server. Please select a new" +
                        " server.");
            }

            try {
                // User selects which library to use
                setPlexLibraryNum(userSelectsWhichLibrary(connectToSelectedServer()));
                // User must select a library in order for the program to work
                break;
            } catch (NoRouteToHostException e) {
                System.out.println("The program was unable to find a valid route to the selected host. Please select" +
                        " a different host or fix any network related problems.");
            } catch (ConnectException e) {
                System.out.println("The program was unable to connect to the specified server. Please select " +
                        " a different server.");
            } catch (IOException e) {
                System.out.println("There was an IO error in fetching the libraries of the server that you selected." +
                        " Please select a different server.");
                logger.fatal("The program ran into an IO error when fetching the libraries of the following server." +
                        "\nServer IP: " + getPlexIP() + "\nServer Port: " + getPlexPort() + "\nLibrary Num: " + getPlexLibraryNum() + "\n");
            } catch (Exception e) {
                System.out.println("An error occurred fetching the libraries for that server please select a different " +
                        " server.");
                logger.fatal("The program ran into an issue grabbing the libraries for the selected server." +
                        "\nServer IP: " + getPlexIP() + "\nServer Port: " + getPlexPort() + "\nLibrary Num: " + getPlexLibraryNum() + "\n");
            }
        }
    }

    /**
     * Hit the API endpoint that lists the devices serving as servers for Plex
     * @return - The unfiltered xml from hitting the api
     */
    private Document connectToServerList() throws IOException {
        Document listOfPlexServers = null;

        try {
            listOfPlexServers = Jsoup.connect("https://plex.tv/api/resources?X-Plex-Token=" + getPlexAuthToken()).get();
        } catch (IOException e) {
            System.out.println("There was a problem fetching the list of Plex servers using the information provided");

            logger.fatal("The program was unable to fetch the list of servers for the user  from the following" +
                    " URL: " + "\nPlex URL: https://plex.tv/api/resources?X-Plex-Token=" + getPlexAuthToken() + "\n");
        }

        return listOfPlexServers;
    }

    /**
     * User selects which server they would like to use from the list provided by the Plex API
     * @param listOfPlexServers - the unfiltered xml from hitting the Plex API for server devices
     * @return - the selected server from the list
     */
    private int userSelectsWhichServer(Document listOfPlexServers) {
        Scanner input = new Scanner(System.in);
        int selectedServer;
        Elements serverNames = new Elements();

        // Dynamic input based on the output of the server list
        while (true) {
            if (listOfPlexServers != null) {
                System.out.println("\nServers:");
                serverNames = listOfPlexServers.getElementsByTag("Device");

                // Print out a list of all of the users servers
                for (int i = 0; i < serverNames.size(); i++) {
                    System.out.println((i + 1) + ". " + serverNames.get(i).attr("name"));
                }
            } else {
                System.out.println("The program was unable to find any servers connected to the specified account.");
            }

            try {
                System.out.println("Select which of the servers above is the one you would like to use.");
                selectedServer = input.nextInt();

                if (selectedServer > (serverNames.size() + 1)) {
                    System.out.println("Please enter in a valid option");
                } else {
                    System.out.println(serverNames.get(selectedServer - 1).attr("name"));
                    return selectedServer;
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException();
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    /**
     * Grabs the IP address from the selected server
     * @param connections - Filtered part of the xml that contains the IP address for the selected server
     * @param serverNum - The number of the selected server in the list
     * @return - The IP address in String format
     */
    private String grabServerIPAddress(Elements connections, int serverNum) {
        String ipAddress = "";

        if (connections != null) {
            ipAddress = connections.get(serverNum - 1).attr("address");
            System.out.println("The IP address for the server is " + ipAddress);
        } else {
            System.out.println("Unable to get a list of the connection tag");
        }

        return ipAddress;
    }

    /**
     * Grabs the port number from the selected server
     * @param connections - Filtered part of the xml that contains the port number for the selected server
     * @param serverNum - The number of the selected server in the list
     * @return - The port number in String format
     */
    private String grabServerPortAddress(Elements connections, int serverNum) {
        String portNum= "";

        if (connections != null) {
            portNum = connections.get(serverNum - 1).attr("port");
            System.out.println("The port number for the server is: " + portNum);
        } else {
            System.out.println("Unable to get a list of the connection tag");
        }

        return portNum;
    }

    /**
     * User connects to the newly selected server and fetches a list of the libraries that server contains
     * @return - The document containing unfiltered XML of all the libraries inside the server
     * @throws Exception - Catch any exception that the method may throw
     */
    private Document connectToSelectedServer() throws Exception {
        Document listOfPlexLibraries = null;

        try {
            listOfPlexLibraries = Jsoup.connect("http://" + getPlexIP() + ":" + getPlexPort() + "/library/sections" +
                    "?X-Plex-Token=" + getPlexAuthToken()).get();
        } catch (NoRouteToHostException e) {
            throw new NoRouteToHostException();
        } catch (ConnectException e) {
            throw new ConnectException();
        } catch (IOException e) {
            throw new IOException();
        } catch (Exception e) {
            throw new Exception();
        }

        return listOfPlexLibraries;
    }

    /**
     * User selects which library they would like to use from a list provided by the XML from connectToSelectedServer()
     * @param listOfPlexLibraries - Unfiltered XML containing information about the libraries inside of the server
     * @return - The number of the library the user would like to use
     */
    private String userSelectsWhichLibrary(Document listOfPlexLibraries) {
        Scanner input = new Scanner(System.in);
        String libraryNumber = "";
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
                        libraryNumber = libraryNames.get(libraryNum - 1).attr("key");
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Please enter in a valid option.");
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Please enter in a valid option.");
                }
            }
        }
        
        return libraryNumber;
    }
}
