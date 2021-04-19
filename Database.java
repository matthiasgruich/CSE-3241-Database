package osu.cse3241;

import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class Database {

    private static String DATABASE = "test.db";

    public static Connection initializeDB(String databaseFileName) {
        /**
         * The "Connection String" or "Connection URL".
         *
         * "jdbc:sqlite:" is the "subprotocol".
         * (If this were a SQL Server database it would be "jdbc:sqlserver:".)
         */
        String url = "jdbc:sqlite:" + databaseFileName;
        Connection conn = null; // If you create this variable inside the Try block it will be out of scope
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                // Provides some positive assurance the connection and/or creation was successful.
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("The connection to the database was successful.");
            } else {
                // Provides some feedback in case the connection failed but did not throw an exception.
                System.out.println("Null Connection");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("There was a problem connecting to the database.");
        }
        return conn;
    }

    public static void displayResultSet(ResultSet result, Statement statement, Connection conn) throws SQLException {

        try {
            ResultSetMetaData metadata = result.getMetaData();
            int column = metadata.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= column; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = result.getString(i);
                    System.out.print(columnValue + " " + metadata.getColumnName(i));
                }
                System.out.println("");
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (result != null) {
                result.close();
                statement.close();
                conn.close();
            }
        }

    }

    public static int checkOrAddPerson(Connection conn, String fname, String lname) throws SQLException {

        // Create variables
        String query = null;
        PreparedStatement statement = null;
        ResultSet result;

        // Check if Person exists in PEOPLE
        query = "SELECT * FROM PEOPLE WHERE fname = ? AND lname = ?";
        statement = conn.prepareStatement(query);
        statement.setString(1, fname);
        statement.setString(2, lname);
        result = statement.executeQuery();

        // Add Person if does not exist
        if(!result.next()) {
            query = "INSERT INTO PEOPLE VALUES (NULL, ?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, fname);
            statement.setString(2, lname);
            statement.executeUpdate();
            System.out.println("Person Not Found. Adding to Database.");
        } else {
            System.out.println("Person Already Exists in Database.");
        }

        // Get PersonID
        query = "SELECT PersonID FROM PEOPLE WHERE fname = ? AND lname = ?;";
        statement = conn.prepareStatement(query);
        statement.setString(1, fname);
        statement.setString(2, lname);
        result = statement.executeQuery();
        result.next();
        return result.getInt(1);
    }

    public static int insertIntoMedia(Connection conn, String title, String date, String genre, String type) throws SQLException {

        // Create variables
        String query = null;
        PreparedStatement statement = null;
        ResultSet result;

        // Insert into MEDIA
        query = "INSERT INTO MEDIA VALUES (NULL, ?, ?, ?, ?)";
        statement = conn.prepareStatement(query);
        statement.setString(1, title);
        statement.setString(2, date);
        statement.setString(3, genre);
        statement.setString(4, type);
        statement.executeUpdate();
        System.out.println("Media Added to Database.");

        // Get MediaID
        query = "SELECT MediaID FROM MEDIA WHERE title = ? AND ReleaseDate = ? AND genre = ? AND type = ?;";
        statement = conn.prepareStatement(query);
        statement.setString(1, title);
        statement.setString(2, date);
        statement.setString(3, genre);
        statement.setString(4, type);
        result = statement.executeQuery();
        result.next();
        return result.getInt(1);
    }

    public static void main(String[] args) {

        Connection conn = initializeDB(DATABASE);
        System.out.println(conn);

        // display menu
    	System.out.println("Select Option:\n");
    	System.out.println("\t1. Search database");
    	System.out.println("\t2. Add new media item to database");
    	System.out.println("\t3. Order items");
    	System.out.println("\t4. Edit records");
    	System.out.println("\t5. Useful reports");

        Scanner in = new Scanner(System.in);
        int num = in.nextInt();

        try {
            switch (num) {
                case 1:
                    searchItem(conn);
                    break;
                case 2:
                    addItem(conn);
                    break;
                case 3:
                    break;
                case 4:
                    editItem(conn);
                    break;
                case 5:
                    usefulReports(conn);
                    break;
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    public static void searchItem(Connection conn) throws SQLException {

        System.out.println("Select Search:\n");
        System.out.println("\t1. Search by artist");
        System.out.println("\t2. Search by track");

        Scanner in = new Scanner(System.in);
        int num = in.nextInt();
        in.nextLine();

        // create variables used for query creation
        PreparedStatement statement = null;
        ResultSet result = null;
        String query, select, from, where;

        switch (num) {

            case 1:

                // Search by artist
                System.out.println("Enter artist first name:");
                String fname = in.nextLine();
                System.out.println("Enter artist last name:");
                String lname = in.nextLine();

                select = "SELECT MEDIA.*, TRACKS.* ";
                from = "FROM MEDIA, TRACKS, PEOPLE ";
                where = "WHERE MEDIA.MediaID = TRACKS.TrackID AND TRACKS.Artist = PEOPLE.PersonID AND PEOPLE.Fname = ? AND PEOPLE.Lname = ?;";
                query = select + from + where;

                try {
                    statement = conn.prepareStatement(query);
                    statement.setString(1, fname);
                    statement.setString(2, lname);

                    result = statement.executeQuery();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;

            case 2:

                // Search by track
                System.out.println("Enter track title:");
                String track = in.nextLine();
                select = "SELECT MEDIA.*, TRACKS.* ";
                from = "FROM MEDIA, TRACKS ";
                where = "WHERE MEDIA.Title = ? AND MEDIA.MediaID = TRACKS.TrackID;";
                query = select + from + where;

                try {

                    statement = conn.prepareStatement(query);
                    statement.setString(1, track);
                    result = statement.executeQuery();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;
        }

        // print result set
        try {
            ResultSetMetaData metadata = result.getMetaData();
            int column = metadata.getColumnCount();
            while (result.next()) {
                for (int i = 1; i <= column; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = result.getString(i);
                    System.out.print(columnValue + " " + metadata.getColumnName(i));
                }
                System.out.println("");
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (result != null) {
                result.close();
                statement.close();
                conn.close();
            }
        }

        in.close();
    }

    public static void addItem(Connection conn) throws SQLException {

        System.out.println("Select Add:\n");
        System.out.println("\t1. Add Track");
        System.out.println("\t2. Add Movie");
        System.out.println("\t3. Add Book");
        System.out.println("\t4. Add Person");

        Scanner in = new Scanner(System.in);
        int num = in.nextInt();
        in.nextLine();

        // create variables used for query creation
        PreparedStatement statement;
        String query, insert, values;
        String artistFname, artistLname, directorFname, directorLname, starFname, starLname, authorFname, authorLname,
                title, date, genre, length, inAlbum, albumTitle = null, bookType, bookLength = null;
        int artistPersonID, directorPersonID, starPersonID, authorPersonID, MediaID, pages;
        float rating;

        switch (num) {

            case 1:

                // Get Track data
                System.out.println("Enter Track Title");
                title = in.nextLine();
                System.out.println("Enter Track Release Date (DD-MM-YYYY)");
                date = in.nextLine();
                System.out.println("Enter Track Genre");
                genre = in.nextLine();
                System.out.println("Enter Track Length (HH:MM:SS)");
                length = in.nextLine();
                System.out.println("Enter Artist First Name");
                artistFname = in.nextLine();
                System.out.println("Enter Artist Last Name");
                artistLname = in.nextLine();
                System.out.println("Is Track part of an album (y/n)?");
                inAlbum  = in.nextLine();

                if(inAlbum.equals("y")){
                    System.out.println("Enter Album Title");
                    albumTitle = in.nextLine();
                }

                // Add Media
                MediaID = insertIntoMedia(conn, title, date, genre, "Track");

                // Ensure artist is in database
                artistPersonID = checkOrAddPerson(conn, artistFname, artistLname);

                // Add Track
                insert = "INSERT INTO TRACKS ";
                values = "VALUES (?, ?, ?, ?);";
                query = insert + values;

                statement = conn.prepareStatement(query);
                statement.setInt(1, MediaID);
                statement.setString(2, length);
                statement.setInt(4, artistPersonID);


                if(inAlbum.equals("y")){
                    statement.setString(3, albumTitle);
                } else {
                    statement.setString(3, "NULL");
                }

                statement.executeUpdate();
                System.out.println("Track Added to Database");

                break;

            case 2:

                // Get Movie data
                System.out.println("Enter Movie Title");
                title = in.nextLine();
                System.out.println("Enter Movie Release Date (DD-MM-YYYY)");
                date = in.nextLine();
                System.out.println("Enter Movie Genre");
                genre = in.nextLine();
                System.out.println("Enter Movie Length");
                length = in.nextLine();
                System.out.println("Enter Movie Rating");
                rating = in.nextFloat();
                in.nextLine();
                System.out.println("Enter Director First Name");
                directorFname = in.nextLine();
                System.out.println("Enter Director Last Name");
                directorLname = in.nextLine();
                System.out.println("Enter Star First Name");
                starFname = in.nextLine();
                System.out.println("Enter Star Last Name");
                starLname = in.nextLine();

                // Ensure artist is in database
                directorPersonID = checkOrAddPerson(conn, directorFname, directorLname);
                starPersonID = checkOrAddPerson(conn, starFname, starLname);

                // Add Media
                MediaID = insertIntoMedia(conn, title, date, genre, "Movie");

                // Add Track
                insert = "INSERT INTO MOVIES ";
                values = "VALUES (?, ?, ?, ?, ?);";
                query = insert + values;

                statement = conn.prepareStatement(query);
                statement.setInt(1, MediaID);
                statement.setString(2, length);
                statement.setFloat(3, rating);
                statement.setInt(4, starPersonID);
                statement.setInt(3, directorPersonID);
                statement.executeUpdate();
                System.out.println("Movie Added to Database");

                break;

            case 3:

                // Get Book data
                System.out.println("Enter Book Title");
                title = in.nextLine();
                System.out.println("Enter Book Release Date (DD-MM-YYYY)");
                date = in.nextLine();
                System.out.println("Enter Book Genre");
                genre = in.nextLine();
                System.out.println("Enter Author First Name");
                authorFname = in.nextLine();
                System.out.println("Enter Author Last Name");
                authorLname = in.nextLine();
                System.out.println("Is the Book printed or digital (p/d)?");
                bookType = in.nextLine();

                // Ensure author is in database
                authorPersonID = checkOrAddPerson(conn, authorFname, authorLname);

                // Add Media
                MediaID = insertIntoMedia(conn, title, date, genre, "Movie");

                // Add Book
                insert = "INSERT INTO BOOKS ";
                values = "VALUES (?, ?);";
                query = insert + values;
                statement = conn.prepareStatement(query);
                statement.setInt(1, MediaID);
                statement.setInt(2, authorPersonID);
                statement.executeUpdate();

                // Add either Printed or AudioBook
                if(bookType.equals("p")){
                    System.out.println("Enter number of pages");
                    pages = in.nextInt();
                    in.nextLine();

                    insert = "INSERT INTO PRINTED ";
                    values = "VALUES (?, ?);";
                    query = insert + values;
                    statement = conn.prepareStatement(query);
                    statement.setInt(1, MediaID);
                    statement.setInt(2, pages);
                } else {
                    System.out.println("Enter audiobook duration (HH:MM:SS)");
                    bookLength = in.nextLine();

                    insert = "INSERT INTO AUDIOBOOK ";
                    values = "VALUES (?, ?);";
                    query = insert + values;
                    statement = conn.prepareStatement(query);
                    statement.setInt(1, MediaID);
                    statement.setString(2, bookLength);
                }

                statement.executeUpdate();
                System.out.println("Book Added to Database");

                break;
        }

        // print result set
        in.close();
    }

    public static void editItem(Connection conn) throws SQLException {

    	Scanner in = new Scanner(System.in);
		PreparedStatement statement = null;
		ResultSet result = null;
		int index = 1;

		System.out.println("Enter artist first name:");
    	String Fname = in.nextLine();
		System.out.println("Enter artist last name:");
		String Lname = in.nextLine();

		String retrieveData = "SELECT * FROM PEOPLE WHERE Fname = ? and Lname = ?;";
		statement = conn.prepareStatement(retrieveData);
		statement.setString(1, Fname);
		statement.setString(2, Lname);
        result = statement.executeQuery();

        String oldPersonID = result.getString("PersonID");

		String query = "UPDATE PEOPLE SET PersonID = ?, Fname = ?, Lname = ? WHERE Fname = ? AND Lname = ?;";

		System.out.println("Enter new PersonID, or hit enter to skip:");
		String newPersonID = in.nextLine();
		System.out.println("Enter new first name, or hit enter to skip:");
		String newFname = in.nextLine();
		System.out.println("Enter new first name, or hit enter to skip:");
		String newLname = in.nextLine();

        statement = conn.prepareStatement(query);

        // Append Update
		if(!newPersonID.isEmpty()){
            statement.setString(1, newPersonID);
        } else {
            statement.setString(1, oldPersonID);
        }
        if(!newFname.isEmpty()){
            statement.setString(2, newFname);
        } else {
            statement.setString(2, Fname);
        }
        if(!newLname.isEmpty()){
            statement.setString(3, newLname);
        } else {
            statement.setString(3, Lname);
        }

        statement.setString(4, Fname);
        statement.setString(5, Lname);
        statement.executeUpdate();

	}

    public static void usefulReports(Connection conn) throws SQLException {

        System.out.println("Select report:\n");
        System.out.println("\t1. Track by ARTIST released before YEAR");
        System.out.println("\t2. Number of albums checked out by a single patron");
        System.out.println("\t3. Most popular actor in the database");
        System.out.println("\t4. Most listened to artist in the database");
        System.out.println("\t5. Patron who has checked out the most videos");

        Scanner in = new Scanner(System.in);
        int num = in.nextInt();
        in.nextLine();

        // create variables used for query creation
        PreparedStatement statement = null;
        ResultSet result = null;
        String query, select, from, where, group, order, limit;

        switch (num) {

            case 1:

                // Find the titles of all tracks by ARTIST released before YEAR
                System.out.println("Enter artist first name:");
                String fname = in.nextLine();
                System.out.println("Enter artist last name:");
                String lname = in.nextLine();
                System.out.println("Enter year:");
                int year = in.nextInt();

                select = "SELECT MEDIA.Title, CAST(STRFTIME('%Y', MEDIA.ReleaseDate) AS INT) AS Year ";
                from = "FROM MEDIA, TRACKS, PEOPLE ";
                where = "WHERE MEDIA.MediaID = TRACKS.TrackID AND TRACKS.Artist = PEOPLE.PersonID AND PEOPLE.Fname = ? AND PEOPLE.Lname = ? AND Year < ?;";
                query = select + from + where;

                try {
                    statement = conn.prepareStatement(query);
                    statement.setString(1, fname);
                    statement.setString(2, lname);
                    statement.setInt(3, year);

                    result = statement.executeQuery();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;

            case 2:

                // Find the total number of albums checked out by a single patron
                System.out.println("Enter patron email:");
                String email = in.nextLine();
                select = "SELECT COUNT(DISTINCT TRACKS.AlbumID) AS Albums ";
                from = "FROM MEDIABORROWED, MEDIA, TRACKS ";
                where = "WHERE MEDIABORROWED.CheckedOutBy = ? AND MEDIABORROWED.MediaID = MEDIA.MediaID AND MEDIA.MediaID = TRACKS.TrackID;";
                query = select + from + where;

                try {

                    statement = conn.prepareStatement(query);
                    statement.setString(1, email);
                    result = statement.executeQuery();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;

            case 3:

                // Most popular actor in the database
                select = "SELECT PEOPLE.Fname, PEOPLE.Lname, COUNT(PEOPLE.PersonID) as Occurrence ";
                from = "FROM MEDIABORROWED, MEDIA, MOVIES, PEOPLE ";
                where = "WHERE MEDIABORROWED.MediaID = MEDIA.MediaID AND MEDIA.MediaID = MOVIES.MovieID AND MOVIES.Actor = PEOPLE.PersonID ";
                group = "GROUP BY PEOPLE.PersonID ";
                order = "ORDER BY Occurrence DESC ";
                limit = "LIMIT 1;";
                query = select + from + where + group + order + limit;

                try {

                    statement = conn.prepareStatement(query);
                    result = statement.executeQuery();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;

            case 4:

            	// Most listened to artist in the database
				break;

            case 5:

                // Patron who has checked out the most videos
                select = "SELECT PATRON.*, COUNT(PATRON.Email) as Videos";
                from = "FROM PATRON, MEDIABORROWED, MEDIA, MOVIES ";
                where = "WHERE MEDIABORROWED.CheckedOutBy = PATRON.Email AND MEDIABORROWED.MediaID = MEDIA.MediaID AND MEDIA.MediaID = MOVIES.MovieID ";
                group = "GROUP BY PATRON.Email ";
                order = "ORDER BY Videos DESC ";
                limit = "LIMIT 1;";

                query = select + from + where + group + order + limit;

                try {
                    statement = conn.prepareStatement(query);
                    result = statement.executeQuery();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;
        }

        // print result set
        displayResultSet(result, statement, conn);

        in.close();
    }
}



