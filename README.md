# MangaCutter
![](./src/main/resources/net/macu/UI/MangaCutter.svg)
[![Java CI with Maven](https://github.com/MangaCutter/MangaCutter/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/MangaCutter/MangaCutter/actions/workflows/maven.yml)

Application for downloading and manipulating with manga scans. Easy way to cut scans automatically fast and accurate.

Guides will be added soon.

## Build from sources

You can build MangaCutter from sources using Maven

```shell
mvn compiler:compile@pack-compile dependency:copy@pack-copy dependency:unpack-dependencies@pack-unpack-dependencies resources:resources@pack-resources jar:jar@pack-jar
```

Compiled jar file will be in `target` directory
