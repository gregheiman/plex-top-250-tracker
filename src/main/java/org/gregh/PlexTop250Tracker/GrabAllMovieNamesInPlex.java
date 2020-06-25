package org.gregh.PlexTop250Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Uses the ArrayLists from ScrapeIMDBMovieNames and the Plex API to search through the Plex library to determine the
 * existence of movies in the Plex library
 * @author Greg Heiman
 */
public class GrabAllMovieNamesInPlex {
    //TODO: Create a logging system that tracks what URL's were used, what movies we already have, and what movies we
    // need to get
    private URL plexBaseURL;
    private String plexIP;
    private String plexPort;
    private String plexLibraryNum;
    private String plexAuthToken;
    private ArrayList<String> listOfNeededMovies;

    public GrabAllMovieNamesInPlex() {
        listOfNeededMovies = new ArrayList<String>();
    }

    public ArrayList<String> getListOfNeededMovies() {
        return listOfNeededMovies;
    }

    public void setListOfNeededMovies(ArrayList<String> listOfNeededMovies) {
        this.listOfNeededMovies = listOfNeededMovies;
    }

    //TODO: Add verification for each of the following items
    public String getPlexIP() {
        return plexIP;
    }

    public void setPlexIP(String plexIP) {
        if (verifyIPAddress(plexIP)) {
            this.plexIP = plexIP;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getPlexPort() {
        return plexPort;
    }

    public void setPlexPort(String plexPort) {
        if (verifyPortNum(plexPort)) {
            this.plexPort = plexPort;
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

    public URL getPlexBaseURL() {
        return plexBaseURL;
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
     * Take in the users answers in order to form a complete base URL for their Plex server
     */
    public void setBasePlexURL() {
        Scanner input = new Scanner(System.in);

        // Take in the user's Plex IP address
        while (true) {
            try {
                System.out.println("What is the IP address of your local Plex server?");
                setPlexIP(input.nextLine());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter in a valid IP address");
            }
        }

        // Take in the user's Plex port number
        while (true) {
            try {
                System.out.println("What is the port number that your local Plex server runs on?");
                setPlexPort(input.nextLine());
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

        try {
            plexBaseURL = new URL("http://" + getPlexIP() + ":" + getPlexPort() + "/library/sections/" + getPlexLibraryNum()
                    + "/all?X-Plex-Token=" + getPlexAuthToken());
        } catch (MalformedURLException e) {
            System.out.println("There was an error in creating the base URL for the Plex");
            e.printStackTrace();
        }
    }

    /**
     * Create the full URL needed to search Plex for the existence of a cetain title
     * @param IMDBMovies - The array list that houses the IMDB top 250
     */
    public void createNewPlexURLWithMovieTitle(ArrayList<String> IMDBMovies) {
        for (int i = 0; i < IMDBMovies.size(); i++) {
            String plexURLWithMovieName = (getPlexBaseURL() + "&title=" + IMDBMovies.get(i));

            try {
                grabPlexMovieNames(plexURLWithMovieName.replace(" ", "%20"),
                        ScrapeIMDBMovieNames.getMovieTitlesWithSpaces().get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Hits the Plex API to see if a certain movie exists
     * @param plexURLWithMovieName - the final URL in the format of a string
     * @throws IOException - Thrown with buffered reader and InputStreamReader
     */
    private void grabPlexMovieNames(String plexURLWithMovieName, String titleOfMovie) throws IOException {
        URL finalPlexURL = new URL(plexURLWithMovieName);
        boolean movieVerified;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(finalPlexURL.openStream()));

            if (in.readLine() != null) {
                movieVerified = verifyTitleOfMovieWithPlex(finalPlexURL, titleOfMovie);

                if (!movieVerified) {
                    // Send the movie name to a file
                    listOfNeededMovies.add(titleOfMovie);
                }
            }

            in.close();
        } catch (ConnectException e) {
            System.out.println("The connection to the Plex server timed out. Make sure that the Plex server is on and " +
                    "running on the correct IP and Port number");
            System.out.println("Entered in IP address:" + getPlexIP() + "\nEntered in Port number:" + getPlexPort());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Something happened in verifying the existence of " + titleOfMovie +
                    " using the following URL: " + finalPlexURL);
            System.out.println("Entered in IP address:" + getPlexIP() + "\nEntered in Port number:" + getPlexPort());
            e.printStackTrace();
        }
    }

    /**
     * Parses through the XML the Plex returns to verify that the title of the movie is present within the XML
     * @param finalPlexURL - The URL which the program used to fetch the movie information from
     * @param titleOfMovie - The title of the movie that is being verified
     * @return - true if the movie is actually present in the Plex library - false if the movie is not present
     */
    private boolean verifyTitleOfMovieWithPlex(URL finalPlexURL, String titleOfMovie) {
        // Checks for the matching of the title field to make sure that the movie actually exists in the Plex
        // Media Server
        Document movieCheck = null;

        try {
            movieCheck = Jsoup.connect(String.valueOf(finalPlexURL)).get();
        } catch (IOException e) {
            System.out.println("An error occurred when trying to connect to the following Plex URL: " + finalPlexURL);
            e.printStackTrace();
        }

        Elements titleVerify;
        if (movieCheck != null) {
            // Grabs the correct Tag on the Plex XML page
            titleVerify = movieCheck.getElementsByTag("Video");
        } else {
            return false;
        }

        // Assess whether the title attribute inside of the Video tag equals the title of the movie from IMDB
        if (titleVerify.attr("title").equals(titleOfMovie)) {
            System.out.println("The program was able to successfully retrieve the following movie: " + finalPlexURL);
            return true;
        } else {
            System.out.println("The title didn't match for the following title: " + titleOfMovie);
            return false;
        }
    }

    /**
     * Send the list of needed movies to a text file
     * @param neededMovies - An arraylist that contains the names of all the missing movies
     */
    public void sendNeededMoviesToFile(ArrayList<String> neededMovies) {
        try {
            PrintWriter neededMoviesFile = new PrintWriter("neededMovies.txt");

            try {
                // Write the name of every movie onto the neededMovies.txt file
                for (String movie: neededMovies) {
                    neededMoviesFile.println(movie);
                }
            } finally {
                // Print out the date and time the files was written to
                neededMoviesFile.println("\nThis list was written on: " + LocalDateTime.now());
                // Close the PrintWriter
                neededMoviesFile.close();
                // Log the successful writting of the neededMovies.txt file
                System.out.println("Successfully printed to the neededMovies.txt file on: " + LocalDateTime.now());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the neededMovies.txt file");
            e.printStackTrace();
        }
    }
}
