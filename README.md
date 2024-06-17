# Java Update Checker
A simple update checker for your Java application.

## Installation
Visit https://jitpack.io/#TechnicJelle/UpdateCheckerJava for details on how to install this library.

## Usage
Simply instantiate a new `UpdateChecker` object with your GitHub username, repository name and current version.

Then call `.check()` or `.checkAsync()` on the instance to check for updates.

You can then log the update message with `logUpdateMessage()` or `logUpdateMessageAsync()`.

```java
UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "2.0");
updateChecker.check();
updateChecker.logUpdateMessage(logger);
```

Full javadoc API reference: [technicjelle.com/UpdateCheckerJava](https://technicjelle.com/UpdateCheckerJava/com/technicjelle/UpdateChecker.html)

If you want to disable the update checker, you can do so
by passing `-Dtechnicjelle.updatechecker.disabled` as a JVM argument.\
Example: `java -Dtechnicjelle.updatechecker.disabled -jar server.jar`
