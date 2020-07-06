package org.gregh.PlexTop250Tracker;

import java.util.Scanner;

public class PlexTop250Tracker {
    public static void main(String[] args) {
        ScrapeIMDBMovieNames IMDBScraper = new ScrapeIMDBMovieNames();
        WriteMovieTitlesToExcel ExcelSheet = new WriteMovieTitlesToExcel();
        EmailExcelToUser emailExcelToUser = new EmailExcelToUser(ExcelSheet);
        FetchPlexInfo plexInfoFetcher = new FetchPlexInfo();

        decideMethodOfFetchingPlexInfo(plexInfoFetcher);

        GrabAllMovieNamesInPlex PlexAPIHitter = new GrabAllMovieNamesInPlex(plexInfoFetcher);
        // Create the Plex URL
        PlexAPIHitter.setPlexBaseURL();
        // Crosscheck the IMDB list with the Plex library
        PlexAPIHitter.createNewPlexURLWithMovieTitle(IMDBScraper.getMovieTitles());
        // Print out the movies that are missing from the Plex library
        PlexAPIHitter.sendNeededMoviesToFile(PlexAPIHitter.getListOfNeededMovies());
        // Add the movies to an Excel spreadsheet
        ExcelSheet.writeMissingMoviesToSpreadsheet(PlexAPIHitter.getListOfNeededMovies());

        // Send the newly created excel sheet to the users chosen email address
        emailExcelToUser.askUserForSenderEmail();
    }

    private static void decideMethodOfFetchingPlexInfo(FetchPlexInfo plexInfoFetcher) {
        Scanner input = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("How would you like to fetch Plex data?");
            System.out.println("1. Automatically");
            System.out.println("2. Manually");
            String answer = input.nextLine();

                switch (answer) {
                    case "1":
                        plexInfoFetcher.automaticallyFetchPlexInfo();
                        run = false;
                        break;
                    case "2":
                        plexInfoFetcher.manuallyFetchPlexInfo();
                        run = false;
                        break;
                    default:
                        System.out.println("Please select a valid option");
                }

        }
    }
}
