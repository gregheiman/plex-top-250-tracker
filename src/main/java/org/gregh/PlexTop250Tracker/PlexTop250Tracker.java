package org.gregh.PlexTop250Tracker;

import java.util.ArrayList;
import java.util.Scanner;

import org.gregh.PlexTop250Tracker.FetchPlexInfo.PlexInfoFetchingMethod;

public class PlexTop250Tracker {
    public static void main(String[] args) {
        // Let the user decide whether they want to fetch their Plex info manually or
        // automatically
        PlexInfoFetchingMethod plexFetchInfoMethod = decideMethodOfFetchingPlexInfo();
        FetchPlexInfo plexInfoFetcher = new FetchPlexInfo(plexFetchInfoMethod);
        plexInfoFetcher.fetchPlexInfo();

        // Create the Plex URL
        String plexBaseURL = "http://" + plexInfoFetcher.getPlexIP() + ":" + plexInfoFetcher.getPlexPort()
                + "/library/sections/" + plexInfoFetcher.getPlexLibraryNum() + "/all?X-Plex-Token="
                + plexInfoFetcher.getPlexAuthToken();
        GrabAllMovieNamesInPlex PlexAPIHitter = new GrabAllMovieNamesInPlex(plexBaseURL);
        ScrapeIMDBMovieNames IMDBScraper = new ScrapeIMDBMovieNames();
        // Retrieve the top 250 movies from IMDB
        IMDBScraper.retrieveTop250MoviesFromIMDB();
        ArrayList<String> neededMovies = new ArrayList<String>();

        // Crosscheck the IMDB list with the Plex library. Add missing movies to list
        for (String movieTitle : IMDBScraper.getTop250MovieTitles()) {
            String plexUrl = PlexAPIHitter.createNewPlexURLWithMovieTitle(movieTitle);
            if (!PlexAPIHitter.verifyTitleOfMovieWithPlex(plexUrl, movieTitle)) {
                neededMovies.add(movieTitle);
            }
        }

        // Have the user decide if they want a text file with missing movies
        if (decideWhetherToWriteMoviesToTextFile()) {
            FileWriter.sendNeededMoviesToTextFile(neededMovies);
        }

        // Have the user decide if they want to send the missing movies to an excel file
        // and send that Excel file through email
        if (decideWhetherToWriteMoviesToExcelFile()) {
            WriteMovieTitlesToExcel ExcelSheet = new WriteMovieTitlesToExcel();
            ExcelSheet.writeMissingMoviesToSpreadsheet(neededMovies);
            if (decideWhetherToSendEmail()) {
                EmailExcelToUser emailExcelToUser = new EmailExcelToUser("plexTop250@javamail.com");
                emailExcelToUser.sendEmailWithExcelSheetAttached(ExcelSheet.getFileOutName());
            }
        }
    }

    private static PlexInfoFetchingMethod decideMethodOfFetchingPlexInfo() {
        Scanner input = new Scanner(System.in);
        boolean run = true;
        PlexInfoFetchingMethod pifm = PlexInfoFetchingMethod.MANUAL;

        while (run) {
            System.out.println("How would you like to fetch the Plex data? (Defaults to Manual)");
            System.out.println("1. Automatically");
            System.out.println("2. Manually");
            String answer = input.nextLine();

            switch (answer) {
            case "1":
                pifm = PlexInfoFetchingMethod.AUTOMATIC;
                run = false;
                break;
            case "2":
                pifm = PlexInfoFetchingMethod.MANUAL;
                run = false;
                break;
            }
        }

        return pifm;
    }

    private static boolean decideWhetherToWriteMoviesToTextFile() {
        Scanner input = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("Would you like to print a list of the needed movies to a text file?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            String answer = input.nextLine();

            switch (answer) {
            case "1":
                // Print out the movies that are missing from the Plex library
                return true;
            case "2":
                System.out.println("The program will not write the names of missing movies to a text file.\n");
                return false;
            default:
                System.out.println("Please enter in a valid option");
            }
        }

        return false;
    }

    private static boolean decideWhetherToWriteMoviesToExcelFile() {
        Scanner input = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("Would you like to print a list of the needed movies to an Excel file?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            int answer = input.nextInt();

            switch (answer) {
            case 1:
                return true;
            case 2:
                return false;
            default:
                System.out.println("Please enter in a valid option");
            }
        }
        return false;
    }

    private static boolean decideWhetherToSendEmail() {
        Scanner input = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("Would you like to email the newly created excel file to an email through Gmail?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            String answer = input.nextLine();

            switch (answer) {
            case "1":
                return true;
            case "2":
                return false;
            default:
                System.out.println("Please select a valid option");
            }
        }
        return false;
    }
}
