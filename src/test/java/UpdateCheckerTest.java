import com.technicjelle.UpdateChecker;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UpdateCheckerTest {
	private static final String LATEST_VERSION;

	static {
		try {
			ProcessBuilder builder = new ProcessBuilder("git", "tag", "--sort=taggerdate");
			builder.directory(new File(System.getProperty("user.dir")));
			System.out.println(builder.directory().getAbsolutePath());
			Process p = builder.start();
			p.waitFor();
			String result = new String(p.getInputStream().readAllBytes());
			String[] versions = result.split("\n");
			LATEST_VERSION = versions[versions.length - 1].trim();
			assertFalse("Could not get latest version from git", LATEST_VERSION.isEmpty());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testUpToDate() {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", LATEST_VERSION);
		updateChecker.check();
		assertFalse("git latest version: " + LATEST_VERSION + ", update checker latest version: " + updateChecker.getLatestVersion(), updateChecker.isUpdateAvailable());
		Logger logger = Logger.getLogger("UpdateCheckerJava");
		updateChecker.logUpdateMessage(logger);
	}

	@Test
	public void testAsync() throws InterruptedException {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1");
		updateChecker.checkAsync();
		Thread.sleep(1000);
		assertTrue(updateChecker.isUpdateAvailable());
		Logger logger = Logger.getLogger("UpdateCheckerJava");
		updateChecker.logUpdateMessageAsync(logger);
		Thread.sleep(1000);
	}

	@Test
	public void testAsyncAutomaticWait() {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1");
		updateChecker.checkAsync();
		// No need to sleep, the isUpdateAvailable method will wait for the async check to finish
		assertTrue(updateChecker.isUpdateAvailable());
	}

	@Test
	public void testOutdated() {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1");
		updateChecker.check();
		assertTrue(updateChecker.isUpdateAvailable());
	}

	@Test
	public void testBroken() {
		assertThrows(CompletionException.class, () -> {
			UpdateChecker updateChecker = new UpdateChecker("akalscmlakmc", "knkasnckasnc", "42");
			updateChecker.check();
		});
	}

	private static final String DISABLED_PROPERTY = "technicjelle.updatechecker.disabled";

	@Test
	public void testDisabledUpToDate() {
		System.setProperty(DISABLED_PROPERTY, "");
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", LATEST_VERSION);
		updateChecker.check();
		assertFalse(updateChecker.isUpdateAvailable()); //when disabled, there is never an update available
		Logger logger = Logger.getLogger("UpdateCheckerJava");
		updateChecker.logUpdateMessage(logger);
		System.clearProperty(DISABLED_PROPERTY);
	}

	@Test
	public void testDisabledOutdated() throws InterruptedException {
		System.setProperty(DISABLED_PROPERTY, "");
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1");
		updateChecker.check();
		assertFalse(updateChecker.isUpdateAvailable()); //when disabled, there is never an update available
		Logger logger = Logger.getLogger("UpdateCheckerJava");
		updateChecker.logUpdateMessageAsync(logger);
		Thread.sleep(1000);
		System.clearProperty(DISABLED_PROPERTY);
	}
}
