# Anime Filler Manager

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e43cf0251bdf4f49aae15f7e82808a01)](https://www.codacy.com/gh/koolskateguy89/Anime-Filler-Manager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=koolskateguy89/Anime-Filler-Manager&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/774d8a80335d28beb533/maintainability)](https://codeclimate.com/github/koolskateguy89/Anime-Filler-Manager/maintainability)

This is for my IB CS IA

Anime Filler Manager is a Java application to search for anime from \
[MyAnimeList](https://myanimelist.net/) \
and find out which episodes of that anime are **filler** (unnecessary to watch), according to the amazing community-led \
[Anime Filler List](https://www.animefillerlist.com/).

<sub><sup><sub><sup>
omg I should have called this MyFillerList
</sup></sub></sup></sub>

## Getting Started

### Prerequisites

-  [Java 14](https://www.oracle.com/uk/java/technologies/javase-downloads.html) (with preview features)
-  [JavaFX 15](https://openjfx.io/)
-  [ControlsFX 11.0.3](https://github.com/controlsfx/controlsfx)
-  [Guava 30.1-jre](https://github.com/google/guava)
-  [jsoup 1.13.1](https://jsoup.org/)
-  [SQLite JDBC Driver 3.34.0](https://github.com/xerial/sqlite-jdbc)

### Installing

Clone this repository:
```
git clone https://github.com/koolskateguy89/Anime-Filler-Manager
```

(p.s. if using Eclipse, you can import this as a Java Project, but you will probably have to configure the project build path to include all the prerequisites)

### Build and run using Maven

To build:\
1.  Run the command `mvn package` in the repository folder
To run:\
2.  Open the target directory e.g. `cd target`
3.  Run the command `java -jar --enable-preview "AFM.jar"`\
    OR double click executable JAR `[REPO_FOLDER]/target/AFM.jar`

## Built With

-  [Oracle JDK 15.0.1](https://www.oracle.com/uk/java/technologies/javase-jdk15-downloads.html) - Programming language
-  [Maven](https://maven.apache.org/) - Project Management
-  [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/) - GUI Development
-  [SQLite](https://www.sqlite.org/index.html) - Database Management System

## How does it work?

This application mainly uses web scraping to enable you to find the anime of your dreams and to find out which episodes are not worth watching. It also employs database management to be able to store the anime you're currently watching/want to watch.

## License

Coming soon
