# Anime-Filler

A Kotlin library to search for anime filler, according to animefillerlist.com.

## Add to project

Import to your project using [jitpack](https://jitpack.io):

### Maven

```xml
<repositories>
   <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
   </repository>
</repositories>
```

```xml
<dependency>
   <groupId>com.github.koolskateguy89.anime-filler-manager</groupId>
   <artifactId>anime-filler-list</artifactId>
   <version>modules-split-SNAPSHOT</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.koolskateguy89.anime-filler-manager:anime-filler-list:modules-split-SNAPSHOT")
}
```

## Usage

```kotlin
// TODO
val naruto: AnimeFiller = AnimeFillerList.fillerFor("naruto")
val fillerEpisodes: IntArray = naruto.filler
val allFillerEpisodes: IntArray = naruto.allFiller
```
