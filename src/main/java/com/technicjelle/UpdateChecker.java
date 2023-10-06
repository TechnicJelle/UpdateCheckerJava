package com.technicjelle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.logging.Logger;

/**
 * Checks for updates on a GitHub repository
 */
public class UpdateChecker {
	private final String repoName;
	private final String currentVersion;
	private final URL url;

	private boolean updateAvailable = false;
	private String latestVersion = null;

	/**
	 * @param author         GitHub Username
	 * @param repoName       GitHub Repository Name
	 * @param currentVersion Current version of the program. This must be in the same format as the version tags on GitHub
	 */
	public UpdateChecker(@NotNull String author, @NotNull String repoName, @NotNull String currentVersion) {
		this.repoName = repoName;
		this.currentVersion = removePrefix(currentVersion);
		try {
			this.url = new URL("https://github.com/" + author + "/" + repoName + "/releases/latest");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks for updates from a GitHub repository's releases<br>
	 * <i>This method blocks the thread it is called from</i>
	 *
	 * @throws IOException If an IO exception occurs
	 * @see #checkAsync()
	 */
	public void check() throws IOException {
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
		if (!latestVersion.equals(currentVersion)) updateAvailable = true;
	}

	/**
	 * Checks for updates from a GitHub repository's releases<br>
	 * <i>This method does not block the thread it is called from</i><br>
	 * <br>Start your program with <code>-Dtechnicjelle.updatechecker.noasync</code> to disable async, and just always check synchronously
	 * @see #check()
	 */
	public void checkAsync() {
		String s = System.getProperty("technicjelle.updatechecker.noasync");

		if (s != null) {
			try {
				check();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return;
		}

		new Thread(() -> {
			try {
				check();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, repoName + "-Update-Checker").start();
	}

	/**
	 * This method logs a message to the console if an update is available<br>
	 *
	 * @param logger Logger to log a potential update notification to
	 * @throws IllegalStateException If {@link #check()} has not been called
	 */
	public void logUpdateMessage(@NotNull Logger logger) throws IllegalStateException {
		if (latestVersion == null) throw new IllegalStateException("check() has not been called");
		if (updateAvailable) {
			logger.warning("New version available: v" + latestVersion + " (current: v" + currentVersion + ")");
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
