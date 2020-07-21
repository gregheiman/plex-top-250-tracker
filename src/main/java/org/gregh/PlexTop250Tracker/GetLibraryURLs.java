package org.gregh.PlexTop250Tracker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetLibraryURLs {
    private String movieName;

    public GetLibraryURLs(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String createLibraryURL() {
        System.out.println("Creating the library URL for the following movie " + getMovieName());
        // Base URL for MCPL's REST based catalog. Shows only DVD's
        String baseURL = "https://mymcpl.bibliocommons.com/v2/search?f_FORMAT=DVD&query=";
        // Filters down the results to only show those that match the title
        String afterMovieNameBaseURL = "&searchType=title";

        // Changes spaces to be +, which is what the RESTful system MCPL uses for spaces
        String movieNameWithNoSpaces = getMovieName().replace(" ", "+");

        // Add all the pieces together to form a valid URL
        String finalURL = (baseURL + movieNameWithNoSpaces + afterMovieNameBaseURL);

        if (verifyURLExistence(finalURL)) {
            return finalURL;
        } else {
            return "Link-Could-Not-Be-Verified";
        }
    }

    private boolean verifyURLExistence(String urlToTest) {
        System.out.println("Verifying the following library URL: " + urlToTest);
        // Does not verify that the movie exists simply verifies that the link is not dead
        try {
            URL url = new URL(urlToTest);
            // Open a connection to the link that was created and grab its response code
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // If the response was valid return true
            if (connection.getResponseCode() == 200) {
                return true;
            } else {
                return false;
            }
        } catch (MalformedURLException e) {
            System.out.println("The URL for verifying the the existence of the movie at the MCPL was malformed.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("There was an IO error in verifying the existence of the movie link at MCPL");
            e.printStackTrace();
        }

        return false;
    }
}
