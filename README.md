# About

This Gradle Plugin generates and merges Eclipse JDT core preferences from an
exported `formatter.xml` file.

## Usage

```
plugins {
    id 'eclipse'
    id 'de.topobyte.eclipse-formatter-prefs-gradle-plugin' version '0.1.0'
}

eclipseFormatter {
    formatterXml = layout.projectDirectory.file("config/eclipse/formatter.xml")
    // If your formatter.xml file contains more than one profile:
    // profileName = "My Profile Name"
    // If you do not want to add the setting 'org.eclipse.jdt.core.javaFormatter=org.eclipse.jdt.core.defaultJavaFormatter'
    // forceDefaultJavaFormatter = false
}
```

# License

This software is released under the terms of the GNU Lesser General Public
License.

See  [LGPL.md](LGPL.md) and [GPL.md](GPL.md) for details.

# Development and testing

Before publishing the plugin to the Gradle plugin portal or the Topobyte Maven
repository, it should be tested locally.
To help with that, there are a few testing-projects available within
this repository that use the locally built version. In order to test them, first
build the current version:

    ./gradlew -PpublishForTesting publish

This will put the plugin artifacts and metadata into a local Maven repository in
directory `maven-repo`.
The testing projects reside in the `test-local` directory and are configured to
prioritize this local repository over the official plugin portal repository.
Have a look at what the script `test.sh` does.
It tests the plugin in different scenarios, especially using different
combinations of JDK and Gradle versions.
