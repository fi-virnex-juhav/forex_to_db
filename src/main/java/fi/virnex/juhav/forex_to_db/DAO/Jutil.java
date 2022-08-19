package fi.virnex.juhav.forex_to_db.DAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.TimeZone;

public class Jutil {
	
	private static final String[] APPLICATION_PROPERTIES_PATH_COMPONENTS = { "src", "main", "resources", "application.properties" };
	
	private static final String ABSOLUTE_PATH_OF_APPLICATION_PROPERTIES_FILE = 
			System.getProperty("user.dir") 
			+ File.separator 
			+ String.join(File.separator, APPLICATION_PROPERTIES_PATH_COMPONENTS);
	
	private static final String RELATIVE_PATH_TO_APPLICATION_PROPERTIES_FILE = 
						String.join(File.separator, APPLICATION_PROPERTIES_PATH_COMPONENTS);	
	
	// read src/main/resources/application.properties file of the Eclipse project tree.
	// return Properties object.
	public static Properties getApplicationProperties() {
		Properties properties = new Properties();
		try {
			// FileInputStream fis = new FileInputStream(ABSOLUTE_PATH_OF_APPLICATION_PROPERTIES_FILE);
			FileInputStream fis = new FileInputStream(RELATIVE_PATH_TO_APPLICATION_PROPERTIES_FILE);
			properties.load(fis);
			fis.close();
		} catch (IOException ioe) {
			System.out.println("Exception " + ioe + " while trying to read file " + RELATIVE_PATH_TO_APPLICATION_PROPERTIES_FILE);
		}	
		
		return properties;
	}
	
	public static final String getRelativePath(String fromPath, String toPath) {
		String relativePath = null;
		try {

		      // Two absolute paths
		      File absoluteFromPath = new File(fromPath);
		      System.out.println("absoluteFromPath: " + absoluteFromPath);
		      File absoluteToPath   = new File(toPath);
		      System.out.println("absoluteToPath: " + absoluteToPath);

		      // convert the absolute path to URI
		      URI fromPathUri = absoluteFromPath.toURI();
		      URI toPathUri   = absoluteToPath.toURI();

		      // create a relative path from the two paths
		      URI relativePathUri = fromPathUri.relativize(toPathUri);

		      // convert the URI to string
		      relativePath = relativePathUri.getPath();

		      System.out.println("Relative Path: " + relativePath);
		      
		} catch (Exception e) {
		      System.out.println("Exception " + e + " in zutil.getRelativePath(" + fromPath + "," + toPath + ")" );
		      System.exit(1);
		}
		
	    return relativePath;
	}
	
	public static final LocalDateTime getDateTimeFromTimestamp(long timestamp) {
	    if (timestamp == 0)
	      return null;
	    return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), 
	    		TimeZone.getDefault().toZoneId());
	}
		
	public static final String getDateTimeAsString(LocalDateTime localDateTime) {
		return 		localDateTime.getYear() 
				+ "-" + IfLessThan10thenPrefix0(localDateTime.getMonthValue())
				+ "-" + IfLessThan10thenPrefix0(localDateTime.getDayOfMonth())
				+ " " + IfLessThan10thenPrefix0(localDateTime.getHour())
				+ ":" + IfLessThan10thenPrefix0(localDateTime.getMinute())
				+ ":" + IfLessThan10thenPrefix0(localDateTime.getSecond());
	}
	
	// convert e.g. "7" to "07" for month/dayOfMonth/hour/min/seconds
	public static final String IfLessThan10thenPrefix0(int h) {	
		if ( h < 10 ) {
			return "0" + h;		
		}
		return "" + h;
	}
	
	public static String getFileAsString(String pathToFile) {
		// Path filePath = Path.of("src/main/resources/cars_3_array.json");
		Path filePath = Path.of(pathToFile);
		String content = null;
		try {
			content = Files.readString(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
	
	/*
	public static void main(String args[]) {
		System.out.println(zutil.getApplicationProperties());
	
		long timestamp1 = 1658834404;
		long timestamp2 = 1658836744;
		
		System.out.println();
		System.out.println( getDateTimeAsString(getDateTimeFromTimestamp(timestamp1)) );
		System.out.println( getDateTimeAsString(getDateTimeFromTimestamp(timestamp2)) );
		System.out.println();	
		
	}
	*/

}
