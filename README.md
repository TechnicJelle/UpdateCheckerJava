# Java Update Checker
[![Latest Release](https://repo.bluecolored.de/api/badge/latest/releases/com/technicjelle/UpdateChecker?name=Latest%20Release&prefix=v)](https://repo.bluecolored.de/#/releases/com/technicjelle/UpdateChecker)

A simple update checker for your Java application that checks GitHub releases.

## Install as dependency in Maven/Gradle
Visit https://repo.bluecolored.de/#/releases/com/technicjelle/UpdateChecker
for instructions on how to add this library as a dependency to your project.

You may want to shade the library!

## Usage
Simply instantiate a new `UpdateChecker` object with:
1. Your GitHub username/org-name
2. Your repository name
3. The current version of your program,
   - It uses this to compare to the latest GitHub release tag. If they don't match, there is an update.
4. A handler for exceptions that may occur during the update check
   (Failures due to a spotty network, for example)

Then call `.check()` or `.checkAsync()` on the instance to check for updates.

You can then log the update message with `logUpdateMessage()` or `logUpdateMessageAsync()`.

```java
UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "v3.0.0", throwable -> logWarning(throwable));
updateChecker.check();
updateChecker.logUpdateMessage(logger);
```

Please see the javadoc for the full API reference:
- main (latest commit): https://technicjelle.com/UpdateCheckerJava
- latest release: https://repo.bluecolored.de/javadoc/releases/com/technicjelle/UpdateChecker/latest
  - Also has docs for previous releases (v2.5 and up)

### Disabling
If you want to disable the update checker, you can do so
by passing `-Dtechnicjelle.updatechecker.disabled` as a JVM argument.

Example: `java -Dtechnicjelle.updatechecker.disabled -jar server.jar`
