package app;

import app.model.Job;
import app.model.Point;
import app.model.ServentInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */

//Ovo je prazan projekat, koji moze da vam koristi kao dobra pocetna tacka za projekat :D
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;
	//initialized in welcome message
	public static SystemState systemState;

	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}

	public static String BOOTSTRAP_IP_ADDRESS;
	public static int BOOTSTRAP_PORT;

	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * clique=false 			- is it a clique or not <br/>
	 * fifo=false				- should sending be fifo
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 * servent0.neighbors=1,2 	- if not a clique, who are the neighbors <br/>
	 * servent1.neighbors=0 <br/>
	 * servent2.neighbors=0 <br/>
	 * 
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. This is not a clique, and:<br/>
	 * servent 0 sees servent 1 and 2<br/>
	 * servent 1 sees servent 0<br/>
	 * servent 2 sees servent 0<br/>
	 * 
	 * @param configName name of configuration file
	 */

	public static void readBootstrapConfig(String configName){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));

		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			e.printStackTrace();
			System.exit(0);
		}

		BOOTSTRAP_IP_ADDRESS = properties.getProperty("bootstrap.ip");
		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bootstrap.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}


		myServentInfo = new ServentInfo(BOOTSTRAP_IP_ADDRESS, BOOTSTRAP_PORT);
	}

	public static void readServentConfig(String configName) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));

		} catch (IOException e) {
			e.printStackTrace();
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		systemState = new SystemState();

		try {
			String ipAddress = properties.getProperty("ip");
			int listenerPort = Integer.parseInt(properties.getProperty("port"));

			myServentInfo = new ServentInfo(ipAddress, listenerPort);
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading ip_address or port. Exiting...");
			System.exit(0);
		}

		BOOTSTRAP_IP_ADDRESS = properties.getProperty("bootstrap.ip");
		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bootstrap.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		if (properties.getProperty("job_count") == null) {
			return;
		}

		int jobsCount = Integer.parseInt(properties.getProperty("job_count"));
		for (int i = 0; i < jobsCount; i++) {
			String jobName = properties.getProperty("job" + i + ".name");

			String[] pointsCoordinates = properties.getProperty("job" + i + ".points.coordinates").split(";");
			List<Point> points = new ArrayList<>();
			try {
				for (String coordinates: pointsCoordinates) {
					String[] xy = coordinates.substring(1, coordinates.length() - 1).split(",");
					points.add(new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
				}
			} catch (NumberFormatException e) {
				timestampedErrorPrint("Problem reading points for the job. Exiting...");
				System.exit(0);
			}

			try {
				int pointsCount = Integer.parseInt(properties.getProperty("job" + i + ".points.count"));
				double proportion = Double.parseDouble(properties.getProperty("job" + i + ".proportion"));
				int width = Integer.parseInt(properties.getProperty("job" + i + ".width"));
				int height = Integer.parseInt(properties.getProperty("job" + i + ".height"));

				Job job = new Job(jobName, pointsCount, proportion, width, height, points);
				myServentInfo.addNewJob(job);
			} catch (NumberFormatException e) {
				timestampedErrorPrint("Problem reading integer or double properties for the job. Exiting...");
				System.exit(0);
			}
		}
	}

}
