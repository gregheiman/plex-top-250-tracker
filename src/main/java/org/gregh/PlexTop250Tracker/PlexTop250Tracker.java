package org.gregh.PlexTop250Tracker;

import java.io.IOException;

public class PlexTop250Tracker {
    public static void main(String[] args) {
        ScrapeIMDBIDs IMDBScraper = new ScrapeIMDBIDs();
        GrabAllMovieNamesInPlex PlexAPIHitter = new GrabAllMovieNamesInPlex();

        PlexAPIHitter.createNewPlexURLWithMovieTitle(IMDBScraper.getMovieTitles());
    }
}
