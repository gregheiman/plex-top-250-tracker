package org.gregh.PlexTop250Tracker;

public class PlexTop250Tracker {
    public static void main(String[] args) {
        ScrapeIMDBMovieNames IMDBScraper = new ScrapeIMDBMovieNames();
        GrabAllMovieNamesInPlex PlexAPIHitter = new GrabAllMovieNamesInPlex();
        WriteMovieTitlesToExcel ExcelSheet = new WriteMovieTitlesToExcel();
        EmailExcelToUser emailExcelToUser = new EmailExcelToUser(ExcelSheet);

        // Create the Plex URL
        PlexAPIHitter.setBasePlexURL();
        // Crosscheck the IMDB list with the Plex library
        PlexAPIHitter.createNewPlexURLWithMovieTitle(IMDBScraper.getMovieTitles());
        // Print out the movies that are missing from the Plex library
        PlexAPIHitter.sendNeededMoviesToFile(PlexAPIHitter.getListOfNeededMovies());
        // Add the movies to an Excel spreadsheet
        ExcelSheet.writeMissingMoviesToSpreadsheet(PlexAPIHitter.getListOfNeededMovies());

        // Send the newly created excel sheet to the users chosen email address
        emailExcelToUser.askUserForSenderEmail();
    }
}
