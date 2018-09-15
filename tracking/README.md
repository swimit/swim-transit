# SWIM Transit

A real-time view of the buses in the US (This uses the feed from [Nextbus](https://www.nextbus.com/xmlFeedDocs/NextBusXMLFeed.pdf))

# Installation

* Install [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). Ensure that your JAVA_HOME environment variable is pointed to the Java 8 installation location. Ensure that your PATH includes $JAVA_HOME.

# Run

## Run the application
Execute the command `./gradlew run` from a shell pointed to the application's home directory. This will start the Swim server on port 9001.
   ```console
    user@machine:~$ ./gradlew run
   ```

## Run the UI
For a real-time view of all the buses in the US, open the following URL on your browser: 'http://localhost:8090'

For a real-time view of all the buses in California, open the following URL on your browser: 'http://localhost:8090/CA.html'

 
