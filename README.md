# Java Update Checker
[![Latest Release](https://repo.bluecolored.de/api/badge/latest/releases/com/technicjelle/UpdateChecker?name=Latest%20Release&prefix=v)](https://repo.bluecolored.de/#/releases/com/technicjelle/UpdateChecker)

A simple update checker for your Java application that checks GitHub releases.

## Install as dependency in Maven/Gradle
Visit https://repo.bluecolored.de/#/releases/com/technicjelle/UpdateChecker
for instructions on how to add this library as a dependency to your project.

You may want to shade the library!

## Usage
Simply instantiate a new `UpdateChecker` object with your GitHub username, repository name
and the current version of your program.  
It uses this to compare to the latest GitHub release tag.

Then call `.check()` or `.checkAsync()` on the instance to check for updates.

You can then log the update message with `logUpdateMessage()` or `logUpdateMessageAsync()`.

```java
UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "2.0");
updateChecker.check();
updateChecker.logUpdateMessage(logger);
```

Please see the javadoc for the full API reference:
- main (latest commit): https://technicjelle.com/UpdateCheckerJava
- latest release: https://repo.bluecolored.de/javadoc/releases/com/technicjelle/UpdateChecker/latest
  - Also has docs for previous releases (v2.5 and up)

### Disabling

If you want to disable the update checker, you can do so
by passing `-Dtechnicjelle.updatechecker.disabled` as a JVM argument.\
Example: `java -Dtechnicjelle.updatechecker.disabled -jar server.jar`
