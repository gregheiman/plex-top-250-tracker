package org.gregh.PlexTop250Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Class scrapes the IMDB top 250 movie titles and adds them to ArrayLists which are then used to search through and
 * verify the existence of said movie in the Plex library.
 * @author Greg Heiman
 */
public class ScrapeIMDBMovieNames {
    private ArrayList<String> top250MovieTitles;

    public ScrapeIMDBMovieNames() {}


    public ArrayList<String> getTop250MovieTitles() {
        return top250MovieTitles;
    }

    public void setTop250MovieTitles(ArrayList<String> top250MovieTitles) {
        this.top250MovieTitles = top250MovieTitles;
    }

    /**
     * Scrapes the IMDB top 250 web page and adds the names of the movies to an ArrayList that will be used for
     * creating Plex URL's to search through the Plex library.
     */
    public void retrieveTop250MoviesFromIMDB() {
        Document IMDBTop250 = null;

        try {
            // Connect JSOUP to the IMDB top 250 list
            IMDBTop250 = Jsoup.connect("https://www.imdb.com/chart/top").get();
        } catch(IOException e) {
            System.out.println("Could not fetch the Top 250 list from IMDB.");
        }

        try {
            // Grab the div which houses all of the movie info
            Element mainDiv = IMDBTop250.getElementById("main");
            // Search inside the main div to grab the column with the movie names
            Elements listOfMovies = mainDiv.getElementsByClass("titleColumn");

            for (Element row : listOfMovies) {
                // Add the movie names to the movieTitles arraylist
                String finalMovieTitle = filterIMDBTitles(row.text());
                top250MovieTitles.add(finalMovieTitle);
            }
        } catch(Exception e) {
            System.out.println("An exception occured in fetching the titles of the movies");
        }
    }

    /**
     * Filters out the number before to movie title and the year after the movie title leaving only the movies name
     * @param title - The title of the movie we are filtering
     * @return - just the name of the movie to add to the movieTitlesWithSpaceIdentifiers and movieTitlesWithSpaces
     *  ArrayLists
     */
    private static String filterIMDBTitles(String title) {
        String filteredTitle;

        // Get rid of the number before the title and the year after the title.
        filteredTitle = title.substring((title.indexOf(".") + 2), (title.indexOf("(") - 1));

        return filteredTitle;
    }
}
