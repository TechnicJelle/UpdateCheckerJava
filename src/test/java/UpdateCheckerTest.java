import com.technicjelle.UpdateChecker;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UpdateCheckerTest {
	private static final String LATEST_VERSION;
	private static final Logger LOGGER;

	static {
		try {
			ProcessBuilder builder = new ProcessBuilder("git", "describe", "--tags", "--abbrev=0");
			Process p = builder.start();
			LATEST_VERSION = new String(p.getInputStream().readAllBytes()).trim();
			assertFalse("Could not get latest version from git", LATEST_VERSION.isEmpty());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		LOGGER = Logger.getLogger("UpdateCheckerJava");
	}

	private static void log(Throwable throwable) {
		LOGGER.log(Level.WARNING, "", throwable);
	}

	@Test
	public void testUpToDate() {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", LATEST_VERSION, UpdateCheckerTest::log);
		updateChecker.check();
		assertFalse("git latest version: " + LATEST_VERSION + ", update checker latest version: " + updateChecker.getLatestVersion(), updateChecker.isUpdateAvailable());
		updateChecker.logUpdateMessage(LOGGER);
	}

	@Test
	public void testAsync() throws InterruptedException {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1", UpdateCheckerTest::log);
		updateChecker.checkAsync();
		Thread.sleep(1000);
		assertTrue(updateChecker.isUpdateAvailable());
		updateChecker.logUpdateMessageAsync(LOGGER);
		Thread.sleep(1000);
	}

	@Test
	public void testAsyncAutomaticWait() {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1", UpdateCheckerTest::log);
		updateChecker.checkAsync();
		// No need to sleep, the isUpdateAvailable method will wait for the async check to finish
		assertTrue(updateChecker.isUpdateAvailable());
	}

	@Test
	public void testOutdated() {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1", UpdateCheckerTest::log);
		updateChecker.check();
		assertTrue(updateChecker.isUpdateAvailable());
	}

	@Test
	public void testRepoDoesNotExist() {
		AtomicBoolean thrown = new AtomicBoolean(false);
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "ThisRepoDoesNotExist", "42", throwable -> {
			if (throwable.getMessage().contains("404")) {
				thrown.set(true);
			}
			log(throwable);
		});
		updateChecker.check();
		assertTrue(thrown.get());
		assertFalse(updateChecker.isUpdateAvailable());
		assertTrue(updateChecker.getUpdateMessage().isEmpty());
	}

	@Test
	public void testInternetDown() {
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyPort", "1");
		try {
			AtomicBoolean thrown = new AtomicBoolean(false);
			UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1", throwable -> {
				thrown.set(true);
				log(throwable);
			});
			updateChecker.check();
			assertTrue(thrown.get());
			assertFalse(updateChecker.isUpdateAvailable());
			assertTrue(updateChecker.getUpdateMessage().isEmpty());
		} finally {
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
		}
	}

	private static final String DISABLED_PROPERTY = "technicjelle.updatechecker.disabled";

	@Test
	public void testDisabledUpToDate() {
		System.setProperty(DISABLED_PROPERTY, "");
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", LATEST_VERSION, UpdateCheckerTest::log);
		updateChecker.check();
		assertFalse(updateChecker.isUpdateAvailable()); //when disabled, there is never an update available
		updateChecker.logUpdateMessage(LOGGER);
		System.clearProperty(DISABLED_PROPERTY);
	}

	@Test
	public void testDisabledOutdated() throws InterruptedException {
		System.setProperty(DISABLED_PROPERTY, "");
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1", UpdateCheckerTest::log);
		updateChecker.check();
		assertFalse(updateChecker.isUpdateAvailable()); //when disabled, there is never an update available
		updateChecker.logUpdateMessageAsync(LOGGER);
		Thread.sleep(1000);
		System.clearProperty(DISABLED_PROPERTY);
	}
}
