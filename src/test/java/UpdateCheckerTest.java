import com.technicjelle.UpdateChecker;
import org.junit.Test;

import java.util.concurrent.CompletionException;

import static org.junit.Assert.*;

public class UpdateCheckerTest {
	@Test
	public void testUpToDate() {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "2.3");
		updateChecker.check();
		assertFalse(updateChecker.isUpdateAvailable());
	}

	@Test
	public void testAsync() throws InterruptedException {
		UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "0.1");
		updateChecker.checkAsync();
		Thread.sleep(1000);
		assertTrue(updateChecker.isUpdateAvailable());
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
		assertThrows(CompletionException.class , () -> {
			UpdateChecker updateChecker = new UpdateChecker("akalscmlakmc", "knkasnckasnc", "42");
			updateChecker.check();
		});
	}
}
