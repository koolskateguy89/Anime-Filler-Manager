# Anime Filler Manager

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e43cf0251bdf4f49aae15f7e82808a01)](https://www.codacy.com/gh/koolskateguy89/Anime-Filler-Manager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=koolskateguy89/Anime-Filler-Manager&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/774d8a80335d28beb533/maintainability)](https://codeclimate.com/github/koolskateguy89/Anime-Filler-Manager/maintainability)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/koolskateguy89/Anime-Filler-Manager.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/koolskateguy89/Anime-Filler-Manager/context:java)

## Table of Contents

- [Introduction](#introduction)
- [Built With](#built-with)
- [How Does It Work?](#how-does-it-work)
- [Getting Started](#getting-started)
    - [Dependencies](#dependencies)
    - [Installing](#installing)
    - [Building and running using Maven](#building-and-running-using-maven)
- [Things to note](#some-things-to-know-when-running)
- [TODO](#todo)
- [License](#license)

## Introduction

Anime Filler Manager is a Java application to search for anime from
 [MyAnimeList](https://myanimelist.net/)
 and find out which episodes of that anime are **filler** (unnecessary to watch as they do not contribute to the
 main plot), according to the amazing community-led
 [Anime Filler List](https://www.animefillerlist.com/).

<sub><sup><sub><sup>
Should have called this MyFillerList
</sup></sub></sup></sub>

Tbh this has a lot of problems, mostly due to it being coursework and thus having to implement cOmPLeX feAtUrEs.

## Built With

- Java 17
- [Maven](https://maven.apache.org/)
- [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/)
    - [FXML](https://en.wikipedia.org/wiki/FXML)
- [SQLite](https://www.sqlite.org/index.html)

## How does it work?

This application mainly uses [web scraping](https://jsoup.org/) to enable you to find the anime of your dreams and to
find out which episodes are not worth watching. It also employs [database management](https://github.com/xerial/sqlite-jdbc)
to be able to store the anime you're currently watching/want to watch.

## Getting Started

### Dependencies

- [JavaFX](https://openjfx.io/)
- [ControlsFX](https://github.com/controlsfx/controlsfx)
- [Guava](https://github.com/google/guava)
- [jsoup](https://jsoup.org/)
- [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)

### Installing

Clone this repository:
```
git clone https://github.com/koolskateguy89/Anime-Filler-Manager
```

### Building and running using Maven

See the [res](res) folder for some bash & batch files to help build and run.

<!-- <br/> -->

To build:
1.  Run the command `mvn package` in the repository folder

To run:
2.  Open the target directory e.g. `cd target`
3.  Run the command `java -jar "AFM.jar"` OR 'open' the executable JAR at `[REPO_FOLDER]/target/AFM.jar`

## Some things to know when running

1. To search for an anime, you HAVE to have selected an anime
2. The anime database has to have the tables MyList & ToWatch, in a specific format (just use the `Create new` button
   on the Settings screen, it's easier)
   - There is a [blank database](res/blank.db) with these in the [res](res) folder
   - Tbh if you're actually gonna use this program, I **strongly recommend** using an external database so you can update without losing your data
3. I cba

## TODO
Key:

_italicized = WIP/next-to-do_

- [x] Add option to skip loading screen
- [x] Redesign menu panel (make it a VBox with icons on buttons - make buttons 'seamless')
- [x] Add always-on-top option
  - [x] Add opacity option
  - [x] Add opacity option for when not focused
- [x] Make subwindows open on top of current window (middle) - infoWindows
- [x] Make settings screen button highlight when open
- [x] Add option to use external database for MyList and ToWatch
  - [x] Add option to make new blank database
- [x] Add color themes option (use different stylesheets which the user picks from)
  - [ ] _Light theme_ - icons are then messed up because they're white :/ but mostly done
  - [ ] Idk maybe Dracula/Monokai/etc
- [x] Use ControlsFX a lot more
- [ ] Enable use of ControlsFX.TableFilter in MyList & ToWatch
- [ ] Switch to use Jikan API if this is actually gonna be used because web-scraping is so long, plus the minimum 13 results thing is a bit 🥴 - **quite long**
- [ ] Add a global keybind to minimize/maximize - **no idea**
- [ ] Use [Apache log4j](https://logging.apache.org/log4j/2.x/) to log errors
  - [ ] Use SLF4J to allow swapping in case of more Log4j bugs lol
- [ ] Have no window resizing
- [ ] Add episodes column to all tablescreens - **almost-long**
- [ ] Resize screens to all be same size (SceneBuilder) - **long*
- [ ] Add hyperlink to the next watch episode

## License

Coming soon
