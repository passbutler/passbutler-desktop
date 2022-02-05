<div align="center">
    <img alt="Pass Butler – Private Cloud Password Manager" src="./projectcover.jpg" width="600">
</div>

# Pass Butler for desktop

This repository contains the source code of Pass Butler for desktop platforms.

## Installation

Add APT repository:

    $ echo "deb https://apt.passbutler.de bullseye main" | sudo tee /etc/apt/sources.list.d/passbutler.list

Add APT repository signing key and update package index:

    $ wget -q -O - https://apt.passbutler.de/signing-key.gpg | sudo apt-key add -
    $ sudo apt update

Install the package:

    $ sudo apt install passbutler

## Development setup

The following steps are tested with Ubuntu 20.04.

### Install Git

Git is needed for a build task that generates the version information for the build:

    $ sudo apt install git

### Install IntelliJ

Recommended version:
- IntelliJ IDEA 2021.3.2 (Community Edition)

Required plugin:
- TornadoFX (https://plugins.jetbrains.com/plugin/8339-tornadofx)

Recommended plugin:
- SQLDelight (https://plugins.jetbrains.com/plugin/8191-sqldelight)

### Install Java Development Kit (JDK)

An open JDK in version 14 is recommended.

Unfortunately, the OpenJDK 14 available in Ubuntu repositories is broken in terms of packaging. There are already known issues [[1](https://bugs.launchpad.net/ubuntu/+source/openjdk-14/+bug/1868699), [2](https://github.com/AdoptOpenJDK/openjdk-support/issues/165)] for problems when using `jpackage`:

    java.lang.module.FindException: Hash of jdk.management.jfr (e02522b71006de2e5f5cfb4b62b3ed4af7659bef52f6ec55df0c1abce130e286) differs to expected hash (9ac607490568ac60259af12ac34079b00adeac59f154d5bb90dd9e2ecc8f022f) recorded in java.base

To work around this, the [AdoptOpenJDK 14](https://adoptopenjdk.net) is used instead.

Add APT repository:

    $ echo "deb https://adoptopenjdk.jfrog.io/adoptopenjdk/deb focal main" | sudo tee /etc/apt/sources.list.d/adoptopenjdk.list

Add APT repository signing key and update package index:

    $ wget -q -O - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
    $ sudo apt update

Install the package:

    $ sudo apt install adoptopenjdk-14-hotspot

Set default `java` and `jpackage` to AdoptOpenJDK 14:

    $ sudo update-alternatives --config java

Choose `/usr/lib/jvm/adoptopenjdk-14-hotspot-amd64/bin/java`

    $ sudo update-alternatives --config jpackage

Choose `/usr/lib/jvm/adoptopenjdk-14-hotspot-amd64/bin/jpackage`

In IntelliJ, change the "Project SDK" to the AdoptOpenJDK 14 in File -> Project Structure -> Project Settings -> Project.

### Clone project

Clone repository:

    $ git clone ssh://git@github.com/passbutler/passbutler-desktop.git
    $ cd ./passbutler-desktop/

Clone submodules:

    $ git submodule update --init

### Packaging

Currently, only the distribution with "deb" package is supported!

#### Package for Debian/Ubuntu

Configuration for the distribution:

    $ export PASSBUTLER_VERSION="1.0.0" # Same value as `version` in `build.gradle.kts` file
    $ export PASSBUTLER_BUILD_TYPE="debug" # Valid values are "debug" or "release"

Create the distribution files:

    $ ./gradlew installDist

Build the package:

    $ jpackage \
        --name "PassButler" \
        --app-version "$PASSBUTLER_VERSION" \
        --copyright "Example copyright" \
        --description "Example description" \
        --vendor "Bastian Raschke" \
        --input ./build/install/PassButlerDesktop/lib/ \
        --main-jar PassButlerDesktop-${PASSBUTLER_VERSION}.jar \
        --icon ./src/main/resources/drawables/logo.png \
        --file-associations ./file-associations.properties \
        --type deb \
        --linux-package-name "passbutler" \
        --linux-deb-maintainer "bastian.raschke@posteo.de" \
        --linux-app-category "utils"

## License

Pass Butler is licensed under the GNU Affero General Public License 3:

    Copyright (c) 2019-2022 Bastian Raschke

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.

The full license can be found in [`LICENSE.txt`](LICENSE.txt).
