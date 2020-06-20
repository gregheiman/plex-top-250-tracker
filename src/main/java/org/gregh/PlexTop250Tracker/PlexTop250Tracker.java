package org.gregh.PlexTop250Tracker;

public class PlexTop250Tracker {
    public static void main(String[] args) {
        ScrapeIMDBMovieNames IMDBScraper = new ScrapeIMDBMovieNames();
        GrabAllMovieNamesInPlex PlexAPIHitter = new GrabAllMovieNamesInPlex();

        PlexAPIHitter.createNewPlexURLWithMovieTitle(IMDBScraper.getMovieTitles());
    }
}
