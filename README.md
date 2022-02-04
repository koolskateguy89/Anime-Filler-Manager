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
    - [Running using Maven](#running-using-maven)
    - [Building into an executable JAR](#building-into-an-executable-jar)
- [Things to note](#some-things-to-know-when-running)
- [License](#license)
- [TODO](#todo)


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
- Kotlin 1.5.31
- [Maven](https://maven.apache.org/)
- [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/)
    - [FXML](https://en.wikipedia.org/wiki/FXML)
- [SQLite](https://www.sqlite.org/index.html)


## How does it work?

This application mainly uses [web scraping](https://jsoup.org/) to enable you to find the anime of your dreams and to
find out which episodes are not worth watching. It also employs [database management](https://github.com/xerial/sqlite-jdbc)
to locally store the anime you're currently watching/want to watch.


## Getting Started

### Dependencies

- [Kotlin stdlib](https://kotlinlang.org/api/latest/jvm/stdlib/)
- [JavaFX](https://openjfx.io/)
- [Project Lombok](https://projectlombok.org/)
- [ControlsFX](https://github.com/controlsfx/controlsfx)
- [jsoup](https://jsoup.org/)
- [Guava](https://github.com/google/guava)
- [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)
- [slf4j-simple](https://github.com/qos-ch/slf4j)
- [kxtra-slf4j](https://github.com/kxtra/kxtra-slf4j) (compile-time only)

### Installing

Clone this repository:
```
git clone https://github.com/koolskateguy89/Anime-Filler-Manager
```

### Running with Maven

Run this command on the project root:

```
mvn clean compile && cd app && mvn exec:java
```

(`mvn -pl app exec:java` breaks the program for some reason)

### Building into an executable JAR

Run this command on the project root:
```
mvn clean package
```

There will be an executable JAR file (a fat JAR with all dependencies): 
`app/target/AFM.jar`

You can run this using:
```
java -jar app/target/AFM.jar
```


## Some things to know when running

1. To search for an anime, you HAVE to have selected an anime
2. The anime database has to have the tables MyList & ToWatch, in a specific format (just use the `Create new` button
   on the Settings screen, it's easier)
   - There is a [blank database](res/blank.db) with these in the [res](res) folder
   - Tbh if you're actually gonna use this program, I **strongly recommend** using an external database so you can update without losing your data
3. ...


## License

Distributed under the MIT License. See [LICENSE].txt for more information.


## TODO

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
  - [~] Light theme - icons are then messed up because they're white :/ but mostly done
  - [ ] Idk maybe Dracula/Monokai/etc
- [x] Use ControlsFX a lot more
- [x] Add hyperlink to the next watch episode
- [~] Use a logger (slf4j simple)
- [~] Split into `core` and `app` modules, `core` will basically just be `anime` package(?) - **long-ish**
- [~] `Filler` tests
- [ ] Switch to a lighter embedded db? (sqlite driver is 9.3MB!). Maybe h2?
- [ ] Switch to use Jikan API if this is actually gonna be used because web-scraping is so long, plus the minimum 13 results thing is a bit 🥴 - **quite long**
  - [ ] Just write REST API wrapper myself? not all of its features are needed, literally only searching for anime
    - NO. Ok maybe. Can use
    - I think I can use Jsoup for this, using `Connection.data(String...)` to set 
    - Or Ktor https://ktor.io/docs/request.html#parameters
  - [ ] Use it for filler? https://github.com/MALSync/MALSync/pull/689/commits/4c667e418a4340241f29211b66d8425885bc87e8#diff-7f379e3d9a0edcfa6f3d11771fbf08a97c786103ca087f384bc9acec82f1c0a0R1144
- [ ] Enable use of `ControlsFX.TableFilter` in MyList & ToWatch
- [ ] Add a global keybind to minimize/maximize - **no idea**
- [ ] Have no window resizing
  - [ ] Resize screens to all be same size (SceneBuilder) - **long**
- [ ] Add episodes column to all tablescreens? - **almost-long**
