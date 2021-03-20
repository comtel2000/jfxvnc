# JFX VNC
[![Build Status](https://travis-ci.org/comtel2000/jfxvnc.png)](https://travis-ci.org/comtel2000/jfxvnc) [![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcomtel2000%2Fjfxvnc.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcomtel2000%2Fjfxvnc?ref=badge_shield)
 [![License](https://img.shields.io/badge/license-Apache_2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jfxvnc/jfxvnc-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jfxvnc/jfxvnc-parent)

[JFX VNC](http://jfxvnc.org) is a Java VNC remote desktop client based on JavaFX and divided into two parts. One module (jfxvnc-net) is a RFB/VNC remote desktop protocol based on [Netty](https://github.com/netty/netty). The other module (jfxvnc-ui) contains the JavaFX based VNC viewer build with the just-enough MVP framework [afterburner.fx](https://github.com/AdamBien/afterburner.fx)

[![video](http://img.youtube.com/vi/hbsgvLNvPCc/0.jpg)](http://youtu.be/hbsgvLNvPCc)


[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcomtel2000%2Fjfxvnc.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcomtel2000%2Fjfxvnc?ref=badge_large)

## Modules
- jfxvnc-net (netty based VNC / RFB protocol)
- jfxvnc-ui (JavaFX VNC components)
- jfxvnc-swing (Java Swing VNC components)
- jfxvnc-app (JavaFX VNC client app)

## Supports
- RFB 3.3 to 3.8 protocol
- RAW, Copy Rect, Zlib (0.0.2), Cursor, Desktop Resize Encoding, Hextile (1.0.2)
- VNC Auth security and SSL
- true/full color pixel format (24 depth) + 8bpp
- Server/Client clipboard transfer
- Zoom, Full screen (0.0.2)
- Touch gesture support
- VNC listening mode (0.0.3)

## Roadmap
- TRLE, ZRLE, Tight, Turbo, etc. encodings
- more than 'VNC Auth' security
- ...

## How to build and run
What is required:

* Latest stable [Oracle JDK 8](http://www.oracle.com/technetwork/java)
* Latest stable [Apache Maven](http://maven.apache.org)

```shell
mvn clean install
java -jar jfxvnc-app/target/jfxvnc-jar-with-dependencies.jar
```

## Maven central repository

```xml

<dependency>
  <groupId>org.jfxvnc</groupId>
  <artifactId>jfxvnc-net</artifactId>
  <version>[LATEST_RELEASE]</version>
</dependency>

<dependency>
  <groupId>org.jfxvnc</groupId>
  <artifactId>jfxvnc-ui</artifactId>
  <version>[LATEST_RELEASE]</version>
</dependency>

<dependency>
  <groupId>org.jfxvnc</groupId>
  <artifactId>jfxvnc-swing</artifactId>
  <version>[LATEST_RELEASE]</version>
</dependency>

<dependency>
  <groupId>org.jfxvnc</groupId>
  <artifactId>jfxvnc-app</artifactId>
  <version>[LATEST_RELEASE]</version>
</dependency>

```

## Links
- [Netty](https://github.com/netty/netty)
- [afterburner.fx](https://github.com/AdamBien/afterburner.fx)

## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Trademarks:

VNC® and RFB® are trademarks of RealVNC Limited and are protected by trademark registrations and/or pending trademark applications in the European Union, United States of America and other jurisdictions. Other trademarks and logos are the property of their respective owners.

This project is an unofficial pice of software and has nothing in common with the companies above.

THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW. EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.