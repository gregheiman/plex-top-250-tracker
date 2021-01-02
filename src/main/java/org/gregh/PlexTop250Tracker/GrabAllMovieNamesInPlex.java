package org.gregh.PlexTop250Tracker;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Uses the ArrayLists from ScrapeIMDBMovieNames and the Plex API to search through the Plex library to determine the
 * existence of movies in the Plex library
 * @author Greg Heiman
 */
public class GrabAllMovieNamesInPlex {
    //TODO: Create a logging system that tracks what URL's were used, what movies we already have, and what movies we
    // need to get
    private URL plexBaseURL;
    private ArrayList<String> listOfNeededMovies;
    private FetchPlexInfo plexInfo;
    private static Logger logger;

    public GrabAllMovieNamesInPlex(FetchPlexInfo plexInfo) {
        listOfNeededMovies = new ArrayList<String>();
        this.plexInfo = plexInfo;
        logger =  LogManager.getLogger(GrabAllMovieNamesInPlex.class);
    }

    public ArrayList<String> getListOfNeededMovies() {
        return listOfNeededMovies;
    }

    public URL getPlexBaseURL() {
        return plexBaseURL;
    }

    public void setListOfNeededMovies(ArrayList<String> listOfNeededMovies) {
        this.listOfNeededMovies = listOfNeededMovies;
    }

    /**
     *  Set the plexBaseURL based on the information gathered by FetchPlexInfo.java
     */
    public void setPlexBaseURL() {
        try {
            plexBaseURL = new URL("http://" + plexInfo.getPlexIP() + ":" + plexInfo.getPlexPort()
                    + "/library/sections/" + plexInfo.getPlexLibraryNum() + "/all?X-Plex-Token="
                    + plexInfo.getPlexAuthToken());

            logger.log(Level.DEBUG, "Plex URL is: " + plexBaseURL);
        } catch (MalformedURLException e) {
            logger.log(Level.ERROR, "MalformedURLException in creating the Plex URL. Plex URL is: " + plexBaseURL);
            System.out.println("There was an error in creating the base URL for the Plex");
        }
    }

    /**
     * Create the full URL needed to search Plex for the existence of a cetain title
     * @param IMDBMovies - The array list that houses the IMDB top 250
     */
    public void createNewPlexURLWithMovieTitle(ArrayList<String> IMDBMovies) {
        for (int i = 0; i < IMDBMovies.size(); i++) {
            String plexURLWithMovieName = (getPlexBaseURL() + "&title=" + IMDBMovies.get(i));
            logger.log(Level.DEBUG, "Plex URL with movie name is: " + plexURLWithMovieName);

            try {
                grabPlexMovieNames(plexURLWithMovieName.replace(" ", "%20"),
                        ScrapeIMDBMovieNames.getMovieTitlesWithSpaces().get(i));
            } catch (IOException e) {
                System.out.println("There was an error in creating the Plex URL with the movie name.");
                logger.log(Level.ERROR, "IOException in creating the Plex URL with the movie title." +
                        "Plex URL with movie name: " + plexURLWithMovieName);
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
        logger.log(Level.DEBUG, "Final Plex URL is: " + finalPlexURL);
        boolean movieVerified;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(finalPlexURL.openStream()));

            if (in.readLine() != null) {
                logger.log(Level.DEBUG, "BufferedReader to the final Plex URL stream not null");
                movieVerified = verifyTitleOfMovieWithPlex(finalPlexURL, titleOfMovie);

                if (!movieVerified) {
                    // Send the movie name to the list of needed movies if the movie comes back as not being present
                    listOfNeededMovies.add(titleOfMovie);
                }
            } else {
                System.out.println("No data from Plex URL to verify a movies existence in the Plex server.");
                logger.log(Level.WARN, "No data from Plex URL to verify a movies existence in the Plex server." +
                        "\nPlex URL: " + finalPlexURL);
            }

            in.close();
        } catch (ConnectException e) {
            System.out.println("The connection to the Plex server timed out. Make sure that the Plex server is on and " +
                    "running on the correct IP and Port number. Check log for more info.");
            System.out.println("Entered in IP address:" + plexInfo.getPlexIP() + "\nEntered in Port number:"
                    + plexInfo.getPlexPort());

            logger.log(Level.ERROR, "ConnectException in grabbing the movie name in Plex in order to verify" +
                    " the movies existence in the Plex server." + "\nEntered in IP address: " + plexInfo.getPlexIP() +
                    "\nEntered in Port number: " + plexInfo.getPlexPort());
        } catch (IOException e) {
            System.out.println("Something happened in verifying the existence of " + titleOfMovie +
                    " using the following URL: " + finalPlexURL);
            System.out.println("Entered in IP address:" + plexInfo.getPlexIP() + "\nEntered in Port number:"
                    + plexInfo.getPlexPort());

            logger.log(Level.ERROR, "IOException in BufferedReader or InputStreamReader. Likely due to an error" +
                    " in opening the stream to the Plex URL to verify the existence of a movie in the Plex server." +
                    "\nFinal Plex URL: " + finalPlexURL);
        }
    }

    /**
     * Parses through the XML the Plex URL returns to verify that the title of the movie is present within the XML
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

            logger.log(Level.ERROR, "IOException when trying to connect to the following Plex URl: " + finalPlexURL);
        }

        Elements titleVerify;
        if (movieCheck != null) {
            logger.log(Level.DEBUG, "Program was able to connect to " + finalPlexURL + " to begin scraping.");

            // Grabs the correct Tag on the Plex XML page
            titleVerify = movieCheck.getElementsByTag("Video");
        } else {
            return false;
        }

        // Assess whether the title attribute inside of the Video tag equals the title of the movie from IMDB
        if (titleVerify.attr("title").equals(titleOfMovie)) {
            logger.log(Level.DEBUG, "Program was able to verify " + titleOfMovie + " with Plex Server.");

            // Output in green and reset
            System.out.println((char)27 + "[32m" + titleOfMovie + " was successfully matched." + (char)27 + "[0m");
            return true;
        } else {
            logger.log(Level.WARN, "Program was not able to verify " + titleOfMovie + " with Plex Server.");

            // Output in red and reset
            System.out.println((char)27 + "[31m" + titleOfMovie + " was not successfully matched." + (char)27 + "[0m");
            return false;
        }
    }

    /**
     * Send the list of needed movies to a text file
     * @param neededMovies - An arraylist that contains the names of all the missing movies
     */
    public void sendNeededMoviesToFile(ArrayList<String> neededMovies) {
        // create the text file for the missing movies
        File neededMovieFile = new File(verifyExistenceOfOrCreateOutDirectory(), "neededMovies.txt");
        try {
            if (neededMovieFile.createNewFile()) {
                logger.log(Level.DEBUG, "Program was able to create the needed text file to output the " +
                        "needed movies to.");
            } else {
                logger.log(Level.WARN, "Program was not able to create the needed text file to output the " +
                        "needed movies to.");
            }
        } catch (IOException e) {
            logger.log(Level.ERROR, "IOException in creating the text file to output the needed movies to.");

            System.out.println("There was an error in creating the text file");
        }

        // print the list of movies from the array list to the neededMovieFile text file
        try {
            PrintWriter neededMoviesFile = new PrintWriter(neededMovieFile);

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
                // Log the successful writing of the neededMovies.txt file
                System.out.println("Successfully printed to the neededMovies.txt file on: " + LocalDateTime.now());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the neededMovies.txt file");
            e.printStackTrace();
        }
    }

    /**
     * Create or verify the existence of the ./out directory for the text files and excel files to go
     * @return the ./out directory
     */
    public static File verifyExistenceOfOrCreateOutDirectory() {
        // create a new directory called ./out
        File outDirectory = new File("./out");

        try {
            // check to see if the directory already exists
            if (outDirectory.exists()) {
               logger.log(Level.DEBUG, "The out directory for the output files already exists.");
            // if the directory doesn't exist try to create it
            } else if (outDirectory.mkdir()) {
                logger.log(Level.DEBUG, "The out directory for the output files was created successfully.");
            } else {
                logger.log(Level.WARN, "There was a problem in creating or verifying the existence of the out" +
                        " directory.");
            }
        } catch (Exception e) {
            System.out.println("There was an error in creating the output directory");
            logger.log(Level.ERROR, "There was an Exception in creating or verifying the existence of the out directory.");
        }

        // return the ./out directory
        return outDirectory;
    }
}
