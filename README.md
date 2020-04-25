# Pass Butler desktop client

## Development setup (tested on Ubuntu 16.04)

Install OpenJDK and JavaFX

    $ sudo apt install openjdk-8-jdk openjfx

Install OpenJDK and JavaFX sources (optional)

    $ sudo apt install openjdk-8-jdk-source openjfx-source

Add libraries to project

File > Project Structure > Platform Settings > SDKs > 1.8 > Classpath:

- /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/jfxrt.jar
- /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jfxswt.jar
- /usr/lib/jvm/java-8-openjdk-amd64/lib/ant-javafx.jar
- /usr/lib/jvm/java-8-openjdk-amd64/lib/javafx-mx.jar
