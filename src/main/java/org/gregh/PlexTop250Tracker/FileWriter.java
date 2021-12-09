package org.gregh.PlexTop250Tracker;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Write the needed movied to several file types.
 *
 * @author Greg Heiman
 */
public class FileWriter {
    /**
     * Send the list of needed movies to a text file
     * @param neededMovies - An arraylist that contains the names of all the missing movies
     */
    public static void sendNeededMoviesToTextFile(ArrayList<String> neededMovies) {
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
