# Anime Filler Manager

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e43cf0251bdf4f49aae15f7e82808a01)](https://www.codacy.com/gh/koolskateguy89/Anime-Filler-Manager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=koolskateguy89/Anime-Filler-Manager&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/774d8a80335d28beb533/maintainability)](https://codeclimate.com/github/koolskateguy89/Anime-Filler-Manager/maintainability)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/koolskateguy89/Anime-Filler-Manager.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/koolskateguy89/Anime-Filler-Manager/context:java)

## Table of Contents

- [Introduction](#introduction)
- [Built With](#built-with)
- [How Does It Work?](#how-does-it-work?)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installing](#installing)
    - [Building and running using Maven](#building-and-running-using-maven)
- [Important notes](#important-things-to-know-when-running)
- [TODO](#todo)
- [License](#license)

## Introduction

<sub><sup>
This is for my IB CS IA
</sup></sub>

Anime Filler Manager is a Java application to search for anime from
 [MyAnimeList](https://myanimelist.net/)
 and find out which episodes of that anime are **filler** (unnecessary to watch), according to the amazing community-led
 [Anime Filler List](https://www.animefillerlist.com/).

<sub><sup><sub><sup>
omg I should have called this MyFillerList
</sup></sub></sup></sub>

## Built With

- [Oracle JDK 15.0.1](https://www.oracle.com/uk/java/technologies/javase-jdk15-downloads.html) - Programming language
- [Maven](https://maven.apache.org/) - Project Management
- [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/) - GUI Development
    - [FXML](https://en.wikipedia.org/wiki/FXML)
- [SQLite](https://www.sqlite.org/index.html) - Database Management System

## How does it work?

This application mainly uses [web scraping](https://jsoup.org/) to enable you to find the anime of your dreams and to find out which episodes are not worth watching. It also employs [database management](https://github.com/xerial/sqlite-jdbc) to be able to store the anime you're currently watching/want to watch.

## Getting Started

### Prerequisites - ish

- [Java 15](https://www.oracle.com/uk/java/technologies/javase-downloads.html)
- [JavaFX 15](https://openjfx.io/)
- [ControlsFX 11.0.3](https://github.com/controlsfx/controlsfx)
- [Guava 30.1-jre](https://github.com/google/guava)
- [jsoup 1.13.1](https://jsoup.org/)
- [SQLite JDBC Driver 3.34.0](https://github.com/xerial/sqlite-jdbc)

### Installing

Clone this repository:
```
git clone https://github.com/koolskateguy89/Anime-Filler-Manager
```

### Building and running using Maven

Note: the only prerequisites needed for this are Java 15 & Maven.

See the [res](res) folder for some bash & batch files to help build and run.

<!-- <br/> -->

To build:
1.  Run the command `mvn package` in the repository folder

To run:

2.  Open the target directory e.g. `cd target`
3.  Run the command `java -jar "AFM.jar"` OR 'open' the executable JAR at `[REPO_FOLDER]/target/AFM.jar`

## Important things to know when running

1. To search for an anime, you HAVE to have selected an anime
2. I cba

## TODO

- [x] Add refresh button to 'filler container' to check for new filler episodes
    - This is particularly useful for long-running anime that are addedd to MyList/ToWatch 'early'
- [x] Restructure Main to have static `getInstance` and `getStage` to make getting Stage & stuff easier
- [x] Add asterisk to options that change upon restart
- [x] Make Settings use a Map<String,Boolean> mapping [setting-name]->[value] - **medium**
- [ ] Add episodes column to all tablescreens - **quick-ish**
- [ ] Resize screens to all be same size (SceneBuilder) - **long**
- [x] Add always on top option
  - [ ] Add infoWindows always on top option - **quick-ish**
- [ ] Make subwindows open on top of current window (middle) - infoWindows & Alerts - **long**

## License

Coming soon
