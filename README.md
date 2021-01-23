# SANEScanFX

## Motivation
I have bought a raspberry pi. This serves in our network as a server for the printer, the scanner and works as a dns server with the [Pi hole](https://pi-hole.net/).
For scanning I have not found an application that I like, for this reason I started to develop the SANEScanFX.
The installation of a SANE service is not explained here, please use Google for that.

# Build
Make sure you have JAVA 15 on the path.

```
mvn clean compile javafx:run
```

# Kudos to lib and tool developers
## Libs
* [OpenJFX](https://github.com/openjdk/jfx)
* [Ikonli](https://github.com/kordamp/ikonli)
* [JFreeSane](https://github.com/sjamesr/jfreesane)

## Tools
* [Eclipse](https://www.eclipse.org/downloads/)
* [SceneBuilder](https://gluonhq.com/products/scene-builder/) 
* [SceneBuilder github](https://github.com/gluonhq/scenebuilder)
* [javafx-maven-plugin](https://github.com/openjfx/javafx-maven-plugin)
