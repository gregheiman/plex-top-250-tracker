package org.gregh.PlexTop250Tracker;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Uses the ArrayLists from ScrapeIMDBMovieNames and the Plex API to search
 * through the Plex library to determine the existence of movies in the Plex
 * library
 *
 * @author Greg Heiman
 */
public class GrabAllMovieNamesInPlex {
    // TODO: Create a logging system that tracks what URL's were used, what movies
    // we already have, and what movies we
    // need to get
    private String plexBaseURL;

    public GrabAllMovieNamesInPlex(String plexBaseURL) {
        this.plexBaseURL = plexBaseURL;
    }

    public String getPlexBaseURL() {
        return plexBaseURL;
    }

    public void setPlexBaseURL(String plexBaseURL) {
        this.plexBaseURL = plexBaseURL;
    }

    /**
     * Create the full URL needed to search Plex for the existence of a cetain title
     *
     * @param IMDBMovies - The array list that houses the IMDB top 250
     */
    public String createNewPlexURLWithMovieTitle(String movieTitle) {
        String plexURLWithMovieName = (getPlexBaseURL() + "&title=" + movieTitle).replace(" ", "%20");
        return plexURLWithMovieName;
    }

    /**
     * Parses through the XML the Plex returns to verify that the title of the movie
     * is present within the XML
     *
     * @param finalPlexURL - The URL which the program used to fetch the movie
     *                     information from
     * @param titleOfMovie - The title of the movie that is being verified
     * @return - true if the movie is actually present in the Plex library - false
     *         if the movie is not present
     */
    public boolean verifyTitleOfMovieWithPlex(String plexURLWithMovieName, String titleOfMovie) {
        // Checks for the matching of the title field to make sure that the movie
        // actually exists in the Plex
        // Media Server
        Document movieCheck = null;

        try {
            URL finalPlexURL = new URL(plexURLWithMovieName);
            movieCheck = Jsoup.connect(String.valueOf(finalPlexURL)).get();
        } catch (IOException e) {
            System.out.println(
                    "An error occurred when trying to connect to the following Plex URL: " + plexURLWithMovieName);
            e.printStackTrace();
        }

        Elements titleVerify;
        if (movieCheck != null) {
            // Grabs the correct Tag on the Plex XML page
            titleVerify = movieCheck.getElementsByTag("Video");
        } else {
            return false;
        }

        // Assess whether the title attribute inside of the Video tag equals the title
        // of the movie from IMDB
        if (titleVerify.attr("title").equals(titleOfMovie)) {
            System.out.println(
                    "The program was able to successfully retrieve the following movie: " + plexURLWithMovieName);
            return true;
        } else {
            System.out.println("The title didn't match for the following title: " + titleOfMovie);
            return false;
        }
    }
}
