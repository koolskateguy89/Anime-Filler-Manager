package afm.screens.infowindows;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputEvent;

import afm.Main;
import afm.anime.Genre;
import afm.common.utils.Utils;
import afm.screens.Menu;

public class GenreInfoWindow extends InfoWindow {

    // maybe this should be in Genre class
    private static final Map<Genre, String> genreDefMap;

    static {
        EnumMap<Genre, String> tempMap = new EnumMap<>(Genre.class);
        // genre definitions: https://myanimelist.net/anime/genre/info
        // TODO: not sure if all genres here

        tempMap.put(Genre.Action, """
        Plays out mainly through a clash of physical forces. Frequently these
        stories have fast cuts, tough characters making quick decisions and
        usually a beautiful girl nearby. Anything quick and most likely a thin
        storyline.
        """);

        tempMap.put(Genre.Adventure, """
        Travelling and undertaking an adventure in a certain place
        """);

        tempMap.put(Genre.AvantGarde, """
        Anime that have mind-twisting plots.
        """);

        tempMap.put(Genre.AwardWinning, """
        Won a major award.
        """);

        tempMap.put(Genre.BoysLove, """
        Gay.
        """);

        tempMap.put(Genre.CGDCT, """
        'Cute Girls Doing Cute Things'
        """);

        tempMap.put(Genre.Comedy, """
        Aims to make you laugh
        """);
        // tempMap.put(Genre.Comedy, """
        // Anime: *plays*
        // You: *laughs*
        // """);

        tempMap.put(Genre.Detective, """
        \uD83D\uDD75
        """);

        tempMap.put(Genre.Drama, """
        Anime that often show life or characters through conflict and
        emotions. In general, the different parts of the story tend to
        form a whole that is greater than the sum of the parts. In other
        words, the story has a message that is bigger than just the story
        line itself.
        """);

        tempMap.put(Genre.Ecchi, """
        Almost cartoon porn.
        """);

        tempMap.put(Genre.Erotica, """
        \uD83D\uDE33
        """);

        tempMap.put(Genre.Fantasy, """
        Based in a fanatical world.
        """);

        tempMap.put(Genre.GirlsLove, """
        Lesbian.
        """);

        tempMap.put(Genre.Gourmet, """
        Food.
        """);

        tempMap.put(Genre.Harem, """
        Not many guys, but many, many girls.
        """);

        tempMap.put(Genre.Hentai, """
        Cartoon porn.
        """);

        tempMap.put(Genre.Historical, """
        Based on past historical events.
        """);

        tempMap.put(Genre.Horror, """
        ooooo scarryyyy!
        """);

        tempMap.put(Genre.Josei, """
        Aimed towards young adult women.
        """);

        tempMap.put(Genre.Kids, """
        Aimed at kids
        
        
        ...yikes
        """);

        tempMap.put(Genre.MagicalSexShift, """
        One or more main characters in these stories identify as their biological sex,
        but their body is suddenly changed to the opposite sex through magical means.
        This Magical Sex Switch may be temporary, irreversible, or allow the character
        to change back and forth between the two. Since the character's original
        identity is not lost, coming to terms with the magical sex switch or finding a
        method to change back should play a central role in the plot.
        """);

        tempMap.put(Genre.MartialArts, """
        MARTIAL ART.
        """);

        tempMap.put(Genre.Mecha, """
        Involves robotic entities as a main feature.
        """);

        tempMap.put(Genre.Military, """
        An anime series/movie that has a heavy militaristic feel behind it.
        """);

        tempMap.put(Genre.Music, """
        Headphones and concerts etc.
        """);

        tempMap.put(Genre.Mystery, """
        ?
        """);

        tempMap.put(Genre.Mythology, """
        e
        """); // TODO

        tempMap.put(Genre.Parody, "Naruto -> Narudo" + "\n" + "Spiderman -> Spooderman.");

        tempMap.put(Genre.Psychological, """
        Brain.
        """);

        tempMap.put(Genre.Racing, """
        VROOM VROOM!
        """);

        tempMap.put(Genre.Romance, """
        Love.
        """);

        tempMap.put(Genre.Samurai, """
        Sword.
        """);

        tempMap.put(Genre.School, """
        Based in a school setting.
        """);

        tempMap.put(Genre.SciFi, """
        Science?
        """);

        tempMap.put(Genre.Seinen, """
        Aimed towards young adult men.
        """);

        tempMap.put(Genre.Shoujo, """
        Aimed at a "young female audience".
        """);

        tempMap.put(Genre.Shounen, """
        Aimed at a "young male audience".
        """);

        tempMap.put(Genre.SliceOfLife, """
        Aims to mimic real life through anime.
        """);

        tempMap.put(Genre.Space, """
        NASA.
        """);

        tempMap.put(Genre.Sports, """
        Literally sports, or rather, physical exertion...
        """);

        tempMap.put(Genre.StrategyGame, """
        Use brain in game 2 win.
        """);

        tempMap.put(Genre.SuperPower, """
        Usually based in a word where having superpowers is the norm.
        """);

        tempMap.put(Genre.Supernatural, """
        strange.
        """);

        tempMap.put(Genre.Survival, """
        """); // TODO

        tempMap.put(Genre.Suspense, """
        ...
        """);

        tempMap.put(Genre.TeamSports, """
        """); // TODO

        tempMap.put(Genre.TimeTravel, """
        """); // TODO

        tempMap.put(Genre.Vampire, """
        Draculaaaaa.
        """);

        tempMap.put(Genre.VideoGame, """
        """); // TODO

        tempMap.put(Genre.VisualArts, """
        """); // TODO

        tempMap.put(Genre.Workplace, """
        Working.
        """);

        genreDefMap = Collections.unmodifiableMap(tempMap);
    }

    public static void open(@Nonnull Button infoBtn) {
        Utils.setStyleClass(infoBtn, Menu.SELECTED);

        try {
            new GenreInfoWindow(infoBtn).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static final EventType<InputEvent> ALL_EVENTS = InputEvent.ANY;
    private static final EventHandler<InputEvent> IGNORE_EVENT = InputEvent::consume;

    private final Button helpBtn;

    @FXML
    private ComboBox<Genre> genreCombo;

    @FXML
    private Hyperlink explanationLink;

    @FXML
    private TextArea textArea;

    private GenreInfoWindow(Button helpBtn) throws IOException {
        super(null);
        this.helpBtn = helpBtn;

        // load FXML file into this object
        FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("infowindows/GenreInfoWindow"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();

        // Stop main screen receiving input
        Main.getStage().addEventFilter(ALL_EVENTS, IGNORE_EVENT);
    }

    @FXML
    void initialize() {
        setTitle("Genre Explanations");

        // add all Genres into the ComboBox
        genreCombo.getItems().addAll(Genre.values());

        // set it so when user selects a genre, it sets textArea as the definiton
        // of that Genre
        genreCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                textArea.setText(genreDefMap.get(newVal));
            } else {
                textArea.setText(null);
            }
        });

        explanationLink.setOnAction(event ->
                Main.getInstance().getHostServices().showDocument(explanationLink.getText()));

        super.afterInitialize();
    }

    @Override @FXML
    void closeWindow() {
        helpBtn.getStyleClass().setAll("button");
        helpBtn.setMouseTransparent(false);

        super.closeWindow();

        // allow main screen to receive input again
        Main.getStage().removeEventFilter(ALL_EVENTS, IGNORE_EVENT);
    }
}
