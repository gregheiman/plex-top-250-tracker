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
    private FetchPlexInfo plexInfo;

    public GrabAllMovieNamesInPlex() {
        listOfNeededMovies = new ArrayList<String>();
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

    public void setBasePlexURL() {
        try {
            plexBaseURL = new URL("http://" + plexInfo.getPlexIP() + ":" + plexInfo.getPlexPort()
                    + "/library/sections/" + plexInfo.getPlexLibraryNum() + "/all?X-Plex-Token="
                    + plexInfo.getPlexAuthToken());
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
            System.out.println("Entered in IP address:" + plexInfo.getPlexIP() + "\nEntered in Port number:"
                    + plexInfo.getPlexPort());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Something happened in verifying the existence of " + titleOfMovie +
                    " using the following URL: " + finalPlexURL);
            System.out.println("Entered in IP address:" + plexInfo.getPlexIP() + "\nEntered in Port number:"
                    + plexInfo.getPlexPort());
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
