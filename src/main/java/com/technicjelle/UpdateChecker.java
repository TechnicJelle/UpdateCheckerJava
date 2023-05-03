package com.technicjelle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Checks for updates on a GitHub repository
 */
public final class UpdateChecker {
	private static boolean updateAvailable = false;
	private static URL url = null;
	private static String latestVersion = null;
	private static String curVer = null;

	/**
	 * Checks for updates from a GitHub repository's releases<br>
	 * <i>This method blocks the thread it is called from</i>
	 *
	 * @param author         GitHub Username
	 * @param name           GitHub Repository Name
	 * @param currentVersion Current version of the program. This must be in the same format as the version tags on GitHub
	 * @throws IOException If an IO exception occurs
	 * @see #checkAsync(String, String, String)
	 */
	public static void check(@NotNull String author, @NotNull String name, @NotNull String currentVersion) throws IOException {
		curVer = removePrefix(currentVersion);
		url = new URL("https://github.com/" + author + "/" + name + "/releases/latest");

		// Connect to GitHub website
		HttpURLConnection con;
		con = (HttpURLConnection) url.openConnection();
		con.setInstanceFollowRedirects(false);

		// Check if the response is a redirect
		String newUrl = con.getHeaderField("Location");

		if (newUrl == null) {
			throw new IOException("Did not get a redirect");
		}

		// Get the latest version tag from the redirect url
		String[] split = newUrl.split("/");
		latestVersion = removePrefix(split[split.length - 1]);

		// Check if the latest version is not the current version
		if (!latestVersion.equals(curVer)) updateAvailable = true;
	}

	/**
	 * Checks for updates from a GitHub repository's releases<br>
	 * <i>This method does not block the thread it is called from</i>
	 *
	 * @param author         GitHub Username
	 * @param name           GitHub Repository Name
	 * @param currentVersion Current version of the program. This must be in the same format as the version tags on GitHub
	 * @see #check(String, String, String)
	 */
	public static void checkAsync(@NotNull String author, @NotNull String name, @NotNull String currentVersion) {
		curVer = removePrefix(currentVersion);
		new Thread(() -> {
			try {
				check(author, name, currentVersion);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, name + "-Update-Checker").start();
	}

	/**
	 * This method logs a message to the console if an update is available<br>
	 *
	 * @param logger Logger to a potential update notification to
	 * @throws IllegalStateException If {@link #check(String, String, String)} has not been called
	 */
	public static void logUpdateMessage(Logger logger) throws IllegalStateException {
		if (curVer == null) throw new IllegalStateException("UpdateChecker.check() has not been called");
		if (updateAvailable) {
			logger.warning("New version available: v" + latestVersion + " (current: v" + curVer + ")");
			logger.warning("Download it at " + url);
		}
	}

	/**
	 * Removes a potential <code>v</code> prefix from a version
	 *
	 * @param version Version to remove the prefix from
	 * @return The version without the prefix
	 */
	@Contract(pure = true)
	private static @NotNull String removePrefix(@NotNull String version) {
		return version.replaceFirst("^v", "");
	}
}
