# JFxVNC.org
JFxVNC is a Java VNC viewer based on [Netty framework](https://github.com/netty/netty) with JavaFX 8 UI. The VNC viewer app (jfxvnc-ui) use a lot of great [ControlsFX goodies](https://bitbucket.org/controlsfx/controlsfx/).

[![Build Status](https://travis-ci.org/comtel2000/jfxvnc.png)](https://travis-ci.org/comtel2000/jfxvnc)

![Splash] (https://raw.githubusercontent.com/comtel2000/jfxvnc/gh-pages/images/screen_info.png)
![Splash] (https://raw.githubusercontent.com/comtel2000/jfxvnc/gh-pages/images/screen_rasp.png)

## Bundles
- jfxvnc-net (netty based VNC / RFB protocol)
- jfxvnc-ui (JavaFX VNC client)

## Supports
- RFB 3.3 to 3.8 protocol
- RAW, Copy Rect, Cursor, Desktop Resize Encoding
- VNC Auth security and SSL
- true/full color pixel format only (24 depth)
- Server/Client clipboard transfer

## Roadmap
- Hextile, RRE, ZLIB, Tight, Turbo, etc. encodings
- more than 'VNC Auth' security
- Touch gesture support
- ...

## How to build and run
What is required:

* Latest stable [Oracle JDK 8](http://www.oracle.com/technetwork/java/)
* Latest stable [Apache Maven](http://maven.apache.org/)

```shell
mvn clean install
java -jar jfxvnc-ui/target/jfxvnc-ui-0.0.1-SNAPSHOT.jar
```

## Links
- [Netty](https://github.com/netty/netty)
- [ControlsFX](https://bitbucket.org/controlsfx/controlsfx/)
- [afterburner.fx](https://github.com/AdamBien/afterburner.fx)

## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)