package org.gregh.PlexTop250Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GrabAllMovieNamesInPlex {
    //TODO: Create a logging system that tracks what URL's were used, what movies we already have, and what movies we
    // need to get
    private static ArrayList<String> plexMovieNames = new ArrayList<String>();
    private URL plexBaseURL;
    private String plexURLWithMovieName;

    public GrabAllMovieNamesInPlex() {
        try {
            plexBaseURL = new URL("http://192.168.9.160:32400/library/sections/4/all?X-Plex-Token=J8mb7J2wqBVvVKq5a2Ue");
        } catch (MalformedURLException e) {
            System.out.println("There was an error in creating the base URL for the Plex");
            e.printStackTrace();
        }

    }

    /**
     * Create the full URL needed to search Plex for the existence of a cetain title
     * @param IMDBMovies - The array list that houses the IMDB top 250
     * @return - return the final URL as a string that will be converted to a URL when needed
     */
    public String createNewPlexURLWithMovieTitle(ArrayList<String> IMDBMovies) {
        for (int i = 0; i < IMDBMovies.size(); i++) {
            plexURLWithMovieName = (plexBaseURL + "&title=" + IMDBMovies.get(i));

            try {
                grabPlexMovieNames(plexURLWithMovieName.replace(" ", "%20"),
                        ScrapeIMDBMovieNames.getMovieTitlesWithSpaces().get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "Successfully created new Plex URL's with movies titles in place";
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

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                movieVerified = verifyTitleOfMovieWithPlex(finalPlexURL, titleOfMovie);

                if (!movieVerified) {
                    // Send the movie name to a file
                } else {
                    // Log that the movie name is in the Plex library
                }
            }

            in.close();
        } catch (IOException e) {
           System.out.println("Something happened in verifying the existence of the following movie: " + titleOfMovie +
           " using the following URL: " + finalPlexURL);
           e.printStackTrace();
        }

    }

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

        // TODO: Send the non present title to a certain file and the present ones to a file
        // Assess whether the title attribute inside of the Video tag equals the title of the movie from IMDB
        if (titleVerify.attr("title").equals(titleOfMovie)) {
            System.out.println("The program was able to successfully retrieve the following movie: " + finalPlexURL);
            return true;
        } else {
            System.out.println("The title didn't match for the following title: " + titleOfMovie);
            return false;
        }
    }
}
