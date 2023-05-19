upgate
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.upgate/com.io7m.upgate.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.upgate%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/com.io7m.upgate/com.io7m.upgate.svg?style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/upgate/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m/upgate.svg?style=flat-square)](https://codecov.io/gh/io7m/upgate)

![upgate](./src/site/resources/upgate.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/upgate/main.linux.temurin.current.yml)](https://github.com/io7m/upgate/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/upgate/main.linux.temurin.lts.yml)](https://github.com/io7m/upgate/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/upgate/main.windows.temurin.current.yml)](https://github.com/io7m/upgate/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/upgate/main.windows.temurin.lts.yml)](https://github.com/io7m/upgate/actions?query=workflow%3Amain.windows.temurin.lts)|

## Usage

Create a configuration file containing a set of users and groups:

```
<?xml version="1.0" encoding="UTF-8" ?>

<Configuration xmlns="urn:com.io7m.upgate:1">
  <Users>
    <User ID="1001" GID="1001" Name="_registry" Shell="/sbin/nologin"/>
    <User ID="1002" GID="1002" Name="_nexus" Shell="/sbin/nologin"/>
    <User ID="1003" GID="1003" Name="_jenkins" Shell="/sbin/nologin"/>
    <User ID="1004" GID="1004" Name="_jenkins_node" Shell="/sbin/nologin"/>
    <User ID="1005" GID="1005" Name="_idstore_db" Shell="/sbin/nologin"/>
    <User ID="1006" GID="1006" Name="_idstore" Shell="/sbin/nologin"/>
    <User ID="1007" GID="1007" Name="_gtyrell" Shell="/sbin/nologin"/>
  </Users>
  <Groups>
    <Group ID="1001" Name="_registry">
      <GroupMember User="_registry"/>
    </Group>
    <Group ID="1002" Name="_nexus">
      <GroupMember User="_nexus"/>
    </Group>
    <Group ID="1003" Name="_jenkins">
      <GroupMember User="_jenkins"/>
    </Group>
    <Group ID="1004" Name="_jenkins_node">
      <GroupMember User="_jenkins_node"/>
    </Group>
    <Group ID="1005" Name="_idstore_db">
      <GroupMember User="_idstore_db"/>
    </Group>
    <Group ID="1006" Name="_idstore">
      <GroupMember User="_idstore"/>
    </Group>
    <Group ID="1007" Name="_gtyrell">
      <GroupMember User="_gtyrell"/>
    </Group>
  </Groups>
</Configuration>
```

Run `upgate apply --configuration config.xml`. The following actions will be
performed:

  * If a group does not exist, it will be created.
  * If a user does not exist, it will be created.
  * If a user exists but has an incorrect UID, the UID will be changed to
    match the configuration.
  * If a user exists but has an incorrect name, the name will be changed to
    match the configuration.
  * If a group exists but has an incorrect UID, the UID will be changed to
    match the configuration.
  * If a group exists but has an incorrect name, the name will be changed to
    match the configuration.
  * If a user is not in a specified group, it will be added to the group.

Users or groups not mentioned in the configuration file will be left
unmodified.

Use the `upgate schema` command to get an XSD schema against which the
configuration file can be validated.

