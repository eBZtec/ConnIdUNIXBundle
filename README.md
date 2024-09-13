Connector Unix
==============

This UNIX Connector is a fork of the [ConnId](https://github.com/Evolveum/ConnIdUNIXBundle) project.

## Dependencies

[![GitHub release](https://img.shields.io/github/v/tag/mwiede/jsch.svg)](https://github.com/mwiede/jsch/tree/jsch-0.2.19)

## How to build

### Maven

* download UNIX connector source code from Github
* build connector with maven:
```
mvn clean install -DskipTests=true
```
* find org.connid.bundles.unix-{version}.jar in ```/target``` folder

## Installation

* put org.connid.bundles.unix-{version}.jar to ```{midPoint_home}/icf-connectors/``` or ```{midPoint_home}/connid-connectors/``` directory

### Run tests

Fillout the properties file ```src/test/resoruces/unix.properties``` with your unix configuration:

```
unix.admin=
unix.password=
unix.hostname=
unix.port=22
unix.base.home.directory=/home
unix.user.shell=/bin/bash
unix.user.root=true
unix.pty=true
unix.ptytype=
```

and then run:

```bash
mvn clean install
```
