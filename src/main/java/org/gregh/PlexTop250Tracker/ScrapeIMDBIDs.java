package org.gregh.PlexTop250Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ScrapeIMDBIDs {
    private static ArrayList<String> movieTitles = new ArrayList<String>();
    private static ArrayList<String> unFilteredMovieTitles = new ArrayList<String>();

    public ScrapeIMDBIDs() {
        movieTitles = getMovieTitlesFromIMDB();
    }

    public ArrayList<String> getMovieTitles() {
        return movieTitles;
    }

    public static ArrayList<String> getUnFilteredMovieTitles() {
        return unFilteredMovieTitles;
    }

    public ArrayList<String> getMovieTitlesFromIMDB() {
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
                movieTitles.add(finalMovieTitle);

                // Needed to verify that the movie actually exits in the plex library
                // TODO: Figure out a more elegant solution to this problem
                String filteredWithSpaces = filterButLeaveSpaces(row.text());
                unFilteredMovieTitles.add(filteredWithSpaces);
            }
        } catch(Exception e) {
            System.out.println("An exception occured in fetching the titles of the movies");
        }

        return movieTitles;
    }

    // TODO: Figure out how to do this better
    private String filterButLeaveSpaces(String title) {
        String filteredWithSpaces;

        filteredWithSpaces = title.substring((title.indexOf(".") + 2), (title.indexOf("(") - 1));

        return filteredWithSpaces;
    }

    private String filterIMDBTitles(String title) {
        String filteredTitle;

        // Get rid of the number before the title and the year after the title. Also change the spaces to valid identifiers.
        filteredTitle = title.substring((title.indexOf(".") + 2), (title.indexOf("(") - 1)).replace(" ", "%20");

        return filteredTitle;
    }
}
