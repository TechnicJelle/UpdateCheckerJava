# Java Update Checker
A simple Java update checker for your application.

## Installation
Visit https://jitpack.io/#TechnicJelle/UpdateCheckerJava for details on how to install this library.

## Usage
Simply instantiate a new `UpdateChecker` object with your GitHub username, repository name and current version.

Then call `.check()` or `.checkAsync()` on the instance to check for updates.

You can then log the update message with `logUpdateMessage()`.

```java
UpdateChecker updateChecker = new UpdateChecker("TechnicJelle", "UpdateCheckerJava", "2.0");
updateChecker.check();
updateChecker.logUpdateMessage(getLogger());
```

Full javadoc API reference: [technicjelle.com/UpdateCheckerJava](https://technicjelle.com/UpdateCheckerJava/com/technicjelle/UpdateChecker.html)

When using the async method, you, or your program's users, can override it to be synchronous anyway,
by passing `-Dtechnicjelle.updatechecker.noasync` as a JVM argument.\
Example: `java -Dtechnicjelle.updatechecker.noasync -jar server.jar`
