# Java Update Checker
A simple Java update checker for your application.

## Installation
Visit the https://jitpack.io/#TechnicJelle/UpdateCheckerJava for details on how to install this library.

## Usage
This library is very simple to use. Just call the `UpdateChecker.check()` method with your GitHub username,
repository name and current version to check for updates.

You can then log the update message with `UpdateChecker.logUpdateMessage()`.

There is also an async way to check for updates: `UpdateChecker.checkAsync()`.

```java
UpdateChecker.check("TechnicJelle", "UpdateChecker", "v1.0");

UpdateChecker.logUpdateMessage(getLogger());
```