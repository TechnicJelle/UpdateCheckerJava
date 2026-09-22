package com.technicjelle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Checks for updates on a GitHub repository
 */
public class UpdateChecker {

	private final String currentVersion;
	private final URL url;
	private final Consumer<Throwable> onError;
	private final boolean disabled;

	private transient CompletableFuture<String> latestVersionFuture = null;

	/**
	 * Start the program with <code>-Dtechnicjelle.updatechecker.disabled</code> to disable the update checker
	 *
	 * @param author         GitHub Username
	 * @param repoName       GitHub Repository Name
	 * @param currentVersion Current version of the program. This must be in the same format as the version tags on GitHub
	 * @param onError        Called when an internal error occurs. Use this to log the error using your logging system
	 */
	public UpdateChecker(@NotNull String author, @NotNull String repoName, @NotNull String currentVersion, @NotNull Consumer<Throwable> onError) {
		Objects.requireNonNull(author);
		Objects.requireNonNull(repoName);
		Objects.requireNonNull(currentVersion);
		Objects.requireNonNull(onError);

		this.currentVersion = removePrefix(currentVersion);
		this.disabled = System.getProperty("technicjelle.updatechecker.disabled") != null;
		this.onError = onError;
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
	 * @see #checkAsync()
	 */
	public void check() {
		checkAsync();
		latestVersionFuture.join();
	}

	/**
	 * Checks for updates from a GitHub repository's releases<br>
	 * <i>This method does <b>not</b> block the thread it is called from</i>
	 *
	 * @see #check()
	 */
	public void checkAsync() {
		latestVersionFuture = CompletableFuture.supplyAsync(this::fetchLatestVersion);
	}

	/**
	 * Checks if necessary and returns the latest available version
	 *
	 * @return the latest available version
	 */
	public synchronized String getLatestVersion() {
		if (latestVersionFuture == null) checkAsync();
		return latestVersionFuture.join();
	}

	private String fetchLatestVersion() {
		if (disabled) return currentVersion;
		try {
			// Connect to GitHub website
			HttpURLConnection con;
			con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);

			// Check if connection succeeded by getting the response code
			int code = con.getResponseCode();

			// Check if the response is a redirect
			String newUrl = con.getHeaderField("Location");

			if (newUrl == null) {
				throw new IOException("Did not get a redirect (" + code + ")");
			}

			// Get the latest version tag from the redirect url
			String[] split = newUrl.split("/");
			return removePrefix(split[split.length - 1]);
		} catch (IOException ex) {
			onError.accept(ex);
			return currentVersion;
		}
	}

	/**
	 * Checks if necessary and returns whether an update is available or not
	 *
	 * @return <code>true</code> if there is an update available or <code>false</code> otherwise.
	 */
	public boolean isUpdateAvailable() {
		return !getLatestVersion().equals(currentVersion);
	}

	/**
	 * Checks if necessary and returns a message if an update is available.<br>
	 * The message will contain the latest version and a link to the GitHub releases page.<br>
	 * <br>
	 * I recommend that you use one of the log methods if you want to log the update message:
	 * <ul>
	 * 	<li>{@link #logUpdateMessage(Logger)}</li>
	 * 	<li>{@link #logUpdateMessageAsync(Logger)}</li>
	 * 	<li>{@link #logUpdateMessage(Consumer)}</li>
	 * 	<li>{@link #logUpdateMessageAsync(Consumer)}</li>
	 * </ul>
	 * But this function remains available in case you want to use it manually.<br>
	 * <br>
	 * Example message:<br>
	 * <code>New version available: v2.5 (current: v2.4)<br>
	 * Download it at <a href="https://github.com/TechnicJelle/UpdateCheckerJava/releases/latest">https://github.com/TechnicJelle/UpdateCheckerJava/releases/latest</a></code>
	 *
	 * @return An optional containing the update message or an empty optional if there is no update available
	 */
	public Optional<String> getUpdateMessage() {
		if (isUpdateAvailable()) {
			return Optional.of("New version available: v" + getLatestVersion() + " (current: v" + currentVersion + ")\nDownload it at " + url);
		}
		return Optional.empty();
	}

	/**
	 * This method logs a message to the console if an update is available<br>
	 *
	 * @param logger Logger to log a potential update notification to
	 * @see #logUpdateMessageAsync(Logger)
	 * @see #logUpdateMessage(Consumer)
	 * @see #logUpdateMessageAsync(Consumer)
	 */
	public void logUpdateMessage(@NotNull java.util.logging.Logger logger) {
		getUpdateMessage().ifPresent(logger::warning);
	}

	/**
	 * This method logs a message to the console if an update is available, asynchronously<br>
	 *
	 * @param logger Logger to log a potential update notification to
	 * @see #logUpdateMessage(Logger)
	 * @see #logUpdateMessage(Consumer)
	 * @see #logUpdateMessageAsync(Consumer)
	 */
	public synchronized void logUpdateMessageAsync(@NotNull java.util.logging.Logger logger) {
		if (latestVersionFuture == null) checkAsync();
		latestVersionFuture.thenRun(() -> logUpdateMessage(logger));
	}

	/**
	 * This method logs a message to the console if an update is available<br>
	 * Useful if you don't use Java's own {@link java.util.logging.Logger} and you want to use your own.
	 *
	 * @param logger Logger to log a potential update notification to
	 * @see #logUpdateMessage(Logger)
	 * @see #logUpdateMessageAsync(Logger)
	 * @see #logUpdateMessageAsync(Consumer)
	 */
	public void logUpdateMessage(@NotNull Consumer<String> logger) {
		getUpdateMessage().ifPresent(logger);
	}

	/**
	 * This method logs a message to the console if an update is available, asynchronously<br>
	 * Useful if you don't use Java's own {@link java.util.logging.Logger} and you want to use your own.
	 *
	 * @param logger Logger to log a potential update notification to
	 * @see #logUpdateMessage(Logger)
	 * @see #logUpdateMessageAsync(Logger)
	 * @see #logUpdateMessage(Consumer)
	 */
	public synchronized void logUpdateMessageAsync(@NotNull Consumer<String> logger) {
		if (latestVersionFuture == null) checkAsync();
		latestVersionFuture.thenRun(() -> logUpdateMessage(logger));
	}


	/**
	 * Gets the current version of the program.<br>
	 * Useful in case you want to log a custom message.<br>
	 * <br>
	 * Does not actually check for updates
	 *
	 * @return The current version of the program
	 */
	public String getCurrentVersion() {
		return currentVersion;
	}

	/**
	 * Gets the URL to the GitHub releases page,
	 * where the latest version can be downloaded.<br>
	 * Useful in case you want to log a custom message.<br>
	 * <br>
	 * Does not actually check for updates
	 *
	 * @return The URL to the GitHub releases page
	 * @see #getUpdateUrl()
	 */
	public URL getUpdateURL() {
		return url;
	}

	/**
	 * Gets the URL to the GitHub releases page,
	 * where the latest version can be downloaded.<br>
	 * Useful in case you want to log a custom message.<br>
	 * <br>
	 * Does not actually check for updates
	 *
	 * @return The URL (as a String) to the GitHub releases page
	 * @see #getUpdateURL()
	 */
	public String getUpdateUrl() {
		return url.toString();
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

	/**
	 * Convenience function to convert a Throwable to a String, including the stacktrace.
	 * Useful for when logging it.
	 *
	 * @param throwable The Throwable to convert to a String
	 * @return The converted String
	 */
	@Contract(pure = true)
	public static @NotNull String trace(@NotNull Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		return sw.toString().trim();
	}
}
