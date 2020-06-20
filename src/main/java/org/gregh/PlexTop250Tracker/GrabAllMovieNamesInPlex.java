package org.gregh.PlexTop250Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GrabAllMovieNamesInPlex {
    private static ArrayList<String> plexMovieNames = new ArrayList<String>();
    private URL plexBaseURL;
    private String plexURLWithMovieName;

    public GrabAllMovieNamesInPlex() {
        try {
            plexBaseURL = new URL("http://192.168.9.160:32400/library/sections/4/all?X-Plex-Token=J8mb7J2wqBVvVKq5a2Ue");
        } catch (MalformedURLException e) { 
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
                grabPlexMovieNames(plexURLWithMovieName, ScrapeIMDBIDs.getUnFilteredMovieTitles().get(i));
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
    public static void grabPlexMovieNames(String plexURLWithMovieName, String titleOfMovie) throws IOException {
        URL finalPlexURL = new URL(plexURLWithMovieName);

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(finalPlexURL.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                // Checks for the matching of the title field to make sure that the movie actually exists in the Plex
                // Media Server
                Document movieCheck = Jsoup.connect(String.valueOf(finalPlexURL)).get();
                Elements titleVerify = movieCheck.getElementsByTag("Video");

                // TODO: Send the non present title to a certain file and the present ones to a file
                if (titleVerify.attr("title").equals(titleOfMovie)) {
                    System.out.println("The program was able to successfully retrieve the following movie: " + finalPlexURL);
                } else {
                    System.out.println("The title didn't match for the following title: " + titleOfMovie);
                }
            }

            in.close();
        } catch (IOException e) {
           System.out.println("System was unable to retrieve the following movie: " + finalPlexURL);
        }

    }
}
