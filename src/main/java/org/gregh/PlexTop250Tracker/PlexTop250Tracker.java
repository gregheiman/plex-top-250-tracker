package org.gregh.PlexTop250Tracker;

import java.util.Scanner;

public class PlexTop250Tracker {
    public static void main(String[] args) {
        // Create base versions of all the needed classes
        ScrapeIMDBMovieNames IMDBScraper = new ScrapeIMDBMovieNames();
        WriteMovieTitlesToExcel ExcelSheet = new WriteMovieTitlesToExcel();
        FetchPlexInfo plexInfoFetcher = new FetchPlexInfo();

        // Let the user decide whether they want to fetch their Plex info manually or automatically
        decideMethodOfFetchingPlexInfo(plexInfoFetcher);

        // Create a GrabAllMovieNamesInPlex with the newly fetch Plex info
        GrabAllMovieNamesInPlex PlexAPIHitter = new GrabAllMovieNamesInPlex(plexInfoFetcher);

        // Create the Plex URL
        PlexAPIHitter.setPlexBaseURL();
        // Crosscheck the IMDB list with the Plex library
        PlexAPIHitter.createNewPlexURLWithMovieTitle(IMDBScraper.getMovieTitles());
        // Have the user decide if they want a text file
        decideWhetherToWriteMoviesToTextFile(PlexAPIHitter);
        // Have the user decide if they want to send the missing movies to an excel file
        decideWhetherToWriteMoviesToExcelFile(ExcelSheet, PlexAPIHitter);
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

    private static void decideWhetherToWriteMoviesToTextFile(GrabAllMovieNamesInPlex PlexAPIHitter) {
        Scanner input = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("Would you like to print a list of the needed movies to a text file?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            int answer = input.nextInt();

            switch (answer) {
                case 1:
                    // Print out the movies that are missing from the Plex library
                   PlexAPIHitter.sendNeededMoviesToFile(PlexAPIHitter.getListOfNeededMovies());
                   run = false;
                   break;
                case 2:
                    System.out.println("The program will not write the names of missing movies to a text file.\n");
                    run = false;
                    break;
                default:
                    System.out.println("Please enter in a valid option");
            }
        }
    }

    private static void decideWhetherToWriteMoviesToExcelFile(WriteMovieTitlesToExcel ExcelSheet,
                                                              GrabAllMovieNamesInPlex PlexAPIHitter) {
        Scanner input = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("Would you like to print a list of the needed movies to an Excel file?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            int answer = input.nextInt();

            switch (answer) {
                case 1:
                    // Print out the movies that are missing from the Plex library
                    ExcelSheet.writeMissingMoviesToSpreadsheet(PlexAPIHitter.getListOfNeededMovies());
                    // Have user decide whether to send excel sheet through email
                    decideWhetherToSendEmail(ExcelSheet);
                    run = false;
                    break;
                case 2:
                    System.out.println("The program will not write the names of missing movies to an Excel file.\n");
                    run = false;
                    break;
                default:
                    System.out.println("Please enter in a valid option");
            }
        }
    }

    private static void decideWhetherToSendEmail(WriteMovieTitlesToExcel excelSheet) {
        EmailExcelToUser emailExcelToUser = new EmailExcelToUser(excelSheet);
        Scanner input = new Scanner(System.in);
        boolean run = true;

        while (run) {
            System.out.println("Would you like to email the newly created excel file to an email through Gmail?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            String answer = input.nextLine();

            switch (answer) {
                case "1":
                    emailExcelToUser.askUserForSenderEmail();
                    run = false;
                    break;
                case "2":
                    run = false;
                    break;
                default:
                    System.out.println("Please select a valid option");
            }

        }

    }
}
