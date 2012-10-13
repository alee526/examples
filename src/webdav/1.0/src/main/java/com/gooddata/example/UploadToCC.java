package com.gooddata.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.activation.FileDataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

/**
 * This is an example to show how to use Sardine lib to interact with
 * secure-di.gooddata.com WebDav over HTTPS services. Lock file is applied
 * instead of utilizing the lock in WebDav. The only reason is for portability
 * on future transfer protocol assuming each get/put is atomic.
 * 
 * @author a.lee@gooddata.com
 * 
 */

/**
 * A class to encapsulate the user profile
 * Kep it simple, no getter/setter here
 */
class UserProfile {
	String hostname = "secure-di.gooddata.com";
	String path = "";
	String username = "";
	String password = "";
	String projectid = "";
	String uploadfile = "";
}

public class UploadToCC {

	private final static String PROJECTID = "pid";
	private final static String USERNAME = "u";
	private final static String PASSWORD = "p";
	private final static String URLHOST = "host";
	private final static String URLPATH = "path";
	private final static String UPLOADFILE = "file";

	/**
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException,
			URISyntaxException {

		// Define all necessary variables
		Options options = createOptions();
		UserProfile profile = new UserProfile();

		validateInput(args, options, profile);

		// Just to show what is the current working directory to avoid file not
		// found exceptions
		System.out.println("Current working/running dir is "
				+ System.getProperty("user.dir"));

		/**
		 * A quick way to get the MIME type from javax.activation.FileDtaSource
		 * You can also use org.apache.tika/net.sf.jmimemagic/eu.medsea.mimeutil
		 */
		String listURI = constructURI(profile.hostname, profile.path
				+ "/waiting/", "");

		File uploadFile = new File(profile.uploadfile);
		String uploadfileName = uploadFile.getName();
		FileDataSource uploadfds = new FileDataSource(uploadFile);
		// Specify the destination URI including the file name
		String uploadURI = constructURI(profile.hostname, profile.path
				+ "/waiting/", uploadfileName);

		File lockFile = new File(profile.uploadfile + ".done");
		String lockfileName = lockFile.getName();
		FileDataSource lockfds = new FileDataSource(lockFile);
		// Specify the destination URI including the file name
		String lockURI = constructURI(profile.hostname, profile.path
				+ "/waiting/", lockfileName);

		/*********************************************************************
		 * Init Sadine Libs
		 *********************************************************************/
		Sardine sardine = SardineFactory.begin();
		// Disable buggy compression method
		sardine.disableCompression();
		// Enter your credential here, these are just examples
		sardine.setCredentials(profile.username, profile.password);

		/********************************************************************
		 * Specify the URL and Path to "list"
		 ********************************************************************/
		List<DavResource> resources = sardine.list(listURI);
		for (DavResource res : resources) {
			System.out.println(res); // calls the .toString() method.
		}

		/*********************************************************************
		 * LARGE FILE TRANSFER EXAMPLE
		 *********************************************************************/
		/*
		 * The following takes an InputStream and upload the file, limited
		 * memory and buffer is used in this case. Preferred.
		 */
		System.out.println("Uploading " + uploadfileName + " with MIME = "
				+ uploadfds.getContentType());
		InputStream fis = new FileInputStream(uploadFile);
		if (sardine.exists(uploadURI)) {
			// Conflict? check the lock file
			if (!sardine.exists(lockURI)) {
				// Previous upload may be corrupted or failed
				sardine.put(uploadURI, fis, uploadfds.getContentType());
			} else {
				// You can implement a force-override upload here if necessary
				System.err.println("We will not override a successful file "
						+ uploadURI + " on the server");
				System.exit(0);
			}
		} else {
			// Uploading the file, no override or conflict
			sardine.put(uploadURI, fis, uploadfds.getContentType());
		}

		/*********************************************************************
		 * SMALL FILE TRANSFER
		 *********************************************************************/
		/*********************************************************************
		 * WARNING: Do NOT use this example to upload large files. Only use this
		 * for the simplicity of creating some lock files on the remote server.
		 * Utilize for controlling purpose NOT for file transfer
		 **********************************************************************/
		// Upload a hard-coded lock file with a unix timestamp in it
		byte[] data = FileUtils.readFileToByteArray(lockFile);
		System.out.println("Uploading " + lockfileName + " with MIME = "
				+ lockfds.getContentType());
		// Specify the destination URI including file name
		sardine.put(lockURI, data, lockfds.getContentType());

	}

	private static void validateInput(String[] args, Options options,
			UserProfile profile) {

		// Just parsing the command line

		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption(URLPATH)) {
				// project specific path
				if (profile.path.startsWith("/project-uploads")) {
					if (line.hasOption(PROJECTID)) {
						profile.path = profile.path + "/"
								+ line.getOptionValue(PROJECTID);
						System.out.println("Applying project specific upload mode, path=" + profile.path);
					} else {
						System.err
								.println("Missing project ID, can't construct full projec specific path for GoodData project. Try -"
										+ PROJECTID);
						printHelpAndExit(options);
					}
				}
				// user specific
				else if (profile.path.startsWith("/uploads")) {
					System.out.println("Applying user specific upload mode, path=" + profile.path);
				} else {
					System.err
							.println("The path you are specifying is not a standard GoodData path");
					System.err
							.println("We don't gurantee this will work on a non-GoodData server");
					// Some special path does is not GD standard
				}
			}
			else {
				if (line.hasOption(PROJECTID)) {
					profile.path = "/project-uploads/" + line.getOptionValue(PROJECTID);
					System.out.println("Applying project specific upload mode, path=" + profile.path);
				} else {
					profile.path = "/uploads/";
					System.out.println("Applying user specific upload mode, path=" + profile.path);
				}
			}
			if (!line.hasOption(URLHOST)) {
				System.out.println("Applying default host " + profile.hostname);
			}
			if (!line.hasOption(USERNAME)) {
				System.err
						.println("Username option -"
								+ USERNAME
								+ " is not specified, applying \"anonymous\" as username");
				profile.username = "anonymous";
			}
			else {
				profile.username = line.getOptionValue(USERNAME);
			}
			if (!line.hasOption(PASSWORD)) {
				System.err.println("Password option -" + PASSWORD
						+ " is not specified, applying empty password");
			}
			else {

				profile.password = line.getOptionValue(PASSWORD);
			}
			if (!line.hasOption(UPLOADFILE)) {
				System.err.println("No file to upload, exiting");
				System.exit(0);
			} else {
				profile.uploadfile = line.getOptionValue(UPLOADFILE);
			}

		} catch (org.apache.commons.cli.ParseException exp) {

			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			printHelpAndExit(options);
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(PROJECTID, true, "Project ID");
		options.addOption(USERNAME, true, "Username");
		options.addOption(PASSWORD, true, "Password");
		options.addOption(URLHOST, true,
				"WebDav server host name e.g. secure-di.gooddata.com");
		options.addOption(
				URLPATH,
				true,
				"The destination folder on the WebDav server, replace the projectid with yours e.g. /project-uplodas/projetcid/wating/");
		options.addOption(UPLOADFILE, true, "Path to the source file");
		return options;
	}

	private static void printHelpAndExit(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(UploadToCC.class.getName(), options);
		System.exit(-1);
	}

	/**
	 * Does NOT support fragment
	 * @param host
	 * @param path
	 * @param filename
	 * @return
	 * @throws URISyntaxException
	 */
	private static String constructURI(String host, String path, String filename)
			throws URISyntaxException {
		// Always https
		URI u = new URI("https", host, path + "/" + filename, "");
		String normURL = u.normalize().toASCIIString();
		normURL = normURL.substring(0, normURL.length() - 1);
		System.out.println("Creating URL " + normURL);
		return normURL;
	}
}
