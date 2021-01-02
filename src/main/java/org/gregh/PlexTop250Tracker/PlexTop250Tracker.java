package org.gregh.PlexTop250Tracker;

import java.util.Scanner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Entry point for the program. Moves user to other points in program.
 * @author Greg Heiman
 */
public class PlexTop250Tracker {
    // Scanner that takes in input for all functions
    static Scanner input = new Scanner(System.in);
    // Create logger for class
    static Logger logger = LogManager.getLogger(PlexTop250Tracker.class);

    // Create base versions of all the needed classes
    static ScrapeIMDBMovieNames  IMDBScraper = new ScrapeIMDBMovieNames();
    static WriteMovieTitlesToExcel ExcelSheet = new WriteMovieTitlesToExcel();
    static FetchPlexInfo plexInfoFetcher = new FetchPlexInfo();

    public static void main(String[] args) {
        // Let the user decide whether they want to fetch their Plex info manually or automatically
        decideMethodOfFetchingPlexInfo();

        // Create a GrabAllMovieNamesInPlex with the newly fetch Plex info
        GrabAllMovieNamesInPlex plexAPIHitter = new GrabAllMovieNamesInPlex(plexInfoFetcher);

        // Create the Plex URL
        plexAPIHitter.setPlexBaseURL();
        // Crosscheck the IMDB list with the Plex library
        plexAPIHitter.createNewPlexURLWithMovieTitle(IMDBScraper.getMovieTitles());
        // Have the user decide if they want a text file
        decideWhetherToWriteMoviesToTextFile(plexAPIHitter);
        // Have the user decide if they want to send the missing movies to an excel file
        decideWhetherToWriteMoviesToExcelFile(plexAPIHitter);

        input.close();
    }

    /**
     * Allows user to decided how they would like to fetch their Plex server info. User has the
     * option of either automatic retrieval using their Plex account or fully manual retrieval using
     * information such as server IP address and port number.
     */
    private static void decideMethodOfFetchingPlexInfo() {
        boolean run = true;

        while (run) {
            System.out.println("How would you like to fetch Plex data?");
            System.out.println("1. Automatically");
            System.out.println("2. Manually");
            String answer = input.nextLine();

            switch (answer) {
                case "1":
                    plexInfoFetcher.automaticallyFetchPlexInfo();
                    logger.log(Level.INFO, "User chose automatic fetching of Plex data.");
                    run = false;
                    break;
                case "2":
                    plexInfoFetcher.manuallyFetchPlexInfo();
                    logger.log(Level.INFO, "User chose manual fetching of Plex data.");
                    run = false;
                    break;
                default:
                    System.out.println("Please select a valid option");
            }

        }
    }

    /**
     * Allows user to decide whether or not they would like a text file with the names of the movies that the user
     * is missing.
     * @param plexAPIHitter - Instance of GrabAllMovieNamesInPlex. Used to run the sendNeededMoviesToFile() function.
     */
    private static void decideWhetherToWriteMoviesToTextFile(GrabAllMovieNamesInPlex plexAPIHitter) {
        boolean run = true;

        while (run) {
            System.out.println("Would you like to print a list of the needed movies to a text file?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            String answer = input.nextLine();

            switch (answer) {
                case "1":
                    // Print out the movies that are missing from the Plex library
                   plexAPIHitter.sendNeededMoviesToFile(plexAPIHitter.getListOfNeededMovies());
                   logger.log(Level.INFO, "User chose to send missing movies to a text file.");
                   run = false;
                   break;
                case "2":
                    System.out.println("The program will not write the names of missing movies to a text file.\n");
                    logger.log(Level.INFO, "User chose not to send missing movies to a text file.");
                    run = false;
                    break;
                default:
                    System.out.println("Please enter in a valid option");
            }
        }
    }

    /**
     * Allows user to decide whether or not they would like a spreadsheet file with the names of the movies that the user
     * is missing.
     * @param plexAPIHitter - Instance of GrabAllMovieNamesInPlex. Used to obtain the list of needed movies.
     */
    private static void decideWhetherToWriteMoviesToExcelFile(GrabAllMovieNamesInPlex plexAPIHitter) {
        boolean run = true;

        while (run) {
            System.out.println("Would you like to print a list of the needed movies to an Excel file?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            String answer = input.nextLine();

            switch (answer) {
                case "1":
                    // Print out the movies that are missing from the Plex library
                    ExcelSheet.writeMissingMoviesToSpreadsheet(plexAPIHitter.getListOfNeededMovies());
                    // Have user decide whether to send excel sheet through email
                    decideWhetherToSendEmail();
                    logger.log(Level.INFO, "User chose to write missing movies to a spreadsheet.");
                    run = false;
                    break;
                case "2":
                    System.out.println("The program will not write the names of missing movies to an Excel file.\n");
                    logger.log(Level.INFO, "User chose not to write missing movies to a spreadsheet.");
                    run = false;
                    break;
                default:
                    System.out.println("Please enter in a valid option");
            }
        }
    }

    /**
     * Allows user to decided whether or not they would like to send the spreadsheet to an email address.
     */
    private static void decideWhetherToSendEmail() {
        EmailExcelToUser emailExcelToUser = new EmailExcelToUser(ExcelSheet);
        boolean run = true;

        while (run) {
            System.out.println("Would you like to email the newly created excel file to an email through Gmail?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            String answer = input.nextLine();

            switch (answer) {
                case "1":
                    emailExcelToUser.askUserForSenderEmail();
                    logger.log(Level.INFO, "User chose to send email containing missing movies.");
                    run = false;
                    break;
                case "2":
                    logger.log(Level.INFO, "User chose not to send email containing missing movies.");
                    run = false;
                    break;
                default:
                    System.out.println("Please select a valid option");
            }
        }
    }
}
