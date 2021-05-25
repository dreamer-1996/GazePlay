package net.gazeplay.games.egg;

import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;


@Slf4j
public class Egg extends Parent {

    private final double fixationlength;
    Timer timer;
    private final StackPane cards;

    private final IGameContext gameContext;

    private final EggGame gameInstance;

    private int turnNumber = 0;
    private final int totalNumberOfTurns;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;
    private final Stats stats;

    private final EventHandler<Event> enterEvent;

    public Egg(final IGameContext gameContext, final Stats stats,
               final EggGame gameInstance, final int fixationlength, final int numberOfTurn) {
        timer = new Timer();
        this.totalNumberOfTurns = numberOfTurn;
        Scene scene = gameContext.getPrimaryScene();

       double height = scene.getHeight() / 2;

        double width = 3. * height / 4.;

         //double width = 10 * height / 20;
         double positionX = scene.getWidth() / 2 - width / 2;
         double positionY = scene.getHeight() / 2 - height / 2;

        log.info("positionX : {} ; positionY : {} ; Height : {} ; Width: {}",(positionX) ,(positionY),height,width);

         this.cards = new StackPane();
        this.cards.setLayoutX(positionX);//positionX
        this.cards.setLayoutY(positionY); //positionY
        this.cards.setPrefWidth(width); //width
        this.cards.setPrefHeight(height); //height

        this.cards.prefHeightProperty().bind(scene.heightProperty().divide(2d));
        this.cards.prefWidthProperty().bind(this.cards.heightProperty().multiply(3d).divide(4d));
        this.cards.layoutXProperty().bind(scene.widthProperty().divide(2d).subtract(this.cards.widthProperty().divide(2d)));
        this.cards.layoutYProperty().bind(scene.heightProperty().divide(2d).subtract(this.cards.heightProperty().divide(2d)));

        /*this.cards.prefHeightProperty().bind(scene.heightProperty().divide(4d));
        this.cards.prefWidthProperty().bind(this.cards.heightProperty().multiply(5d).divide(8d));
        this.cards.layoutXProperty().bind(scene.widthProperty().divide(4d).subtract(this.cards.widthProperty().divide(4d)));
        this.cards.layoutYProperty().bind(scene.heightProperty().divide(4d).subtract(this.cards.heightProperty().divide(4d)));*/


        Rectangle image1 = new Rectangle(positionX, positionY, width,height );
        image1.setFill(new ImagePattern(new Image("data/egg/images/egg1.jpg"), 0, 0, 1, 1, true));
        image1.setMouseTransparent(true);
        /*Rectangle image2 = new Rectangle(positionX, positionY, width, height);
        image2.setFill(new ImagePattern(new Image("data/egg/images/egg3.jpg"), 0, 0, 1, 1, true));
        image2.setMouseTransparent(true);*/
        Rectangle image3 = new Rectangle(positionX, positionY, width, height);
        image3.setFill(new ImagePattern(new Image("data/egg/images/egg3.jpg"), 0, 0, 1, 1, true));

        image1.heightProperty().bind(scene.heightProperty().divide(4d));
        image1.widthProperty().bind(image1.heightProperty().multiply(5d).divide(8d));
        image1.xProperty().bind(scene.widthProperty().divide(4d).subtract(image1.widthProperty().divide(4d)));
        image1.yProperty().bind(scene.heightProperty().divide(4d).subtract(image1.heightProperty().divide(4d)));

        /*image2.heightProperty().bind(scene.heightProperty().divide(4));
        image2.widthProperty().bind(image2.heightProperty().multiply(5).divide(8));
        image2.xProperty().bind(scene.widthProperty().divide(4).subtract(image2.widthProperty().divide(4)));
        image2.yProperty().bind(scene.heightProperty().divide(4).subtract(image2.heightProperty().divide(4)));*/

        image3.heightProperty().bind(scene.heightProperty().divide(4));
        image3.widthProperty().bind(image3.heightProperty().multiply(5).divide(8));
        image3.xProperty().bind(scene.widthProperty().divide(4).subtract(image3.widthProperty().divide(4)));
        image3.yProperty().bind(scene.heightProperty().divide(4).subtract(image3.heightProperty().divide(4)));


        //this.cards.getChildren().addAll(image3, image2, image1);
        this.cards.getChildren().addAll(image3, image1);

        this.gameContext = gameContext;

        this.stats = stats;

        this.fixationlength = fixationlength;

        this.gameInstance = gameInstance;

        this.getChildren().add(cards);

        this.progressIndicator = createProgressIndicator();
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(image3);

        image3.addEventFilter(MouseEvent.ANY, enterEvent);
        image3.addEventFilter(GazeEvent.ANY, enterEvent);

    }

    private ProgressIndicator createProgressIndicator() {
        final ProgressIndicator indicator = new ProgressIndicator(0);

        indicator.minWidthProperty().bind(cards.widthProperty().divide(2));
        indicator.minHeightProperty().bind(cards.widthProperty().divide(2));

        indicator.translateXProperty().bind(cards.layoutXProperty().add(cards.widthProperty().divide(2).divide(2)));
        indicator.translateYProperty().bind(cards.layoutYProperty().add(cards.heightProperty().divide(2).divide(2)));

        indicator.setOpacity(0);
        indicator.setMouseTransparent(true);
        return indicator;
    }

    private EventHandler<Event> buildEvent() {
        final Egg that = this;
        timer.schedule(new RemindTask(),8000);
        return e -> {

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                if (turnNumber < totalNumberOfTurns) {

                    progressIndicator.setOpacity(0.5);
                    progressIndicator.setProgress(0);

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                        new KeyValue(progressIndicator.progressProperty(), 1)));

                    timelineProgressBar.setOnFinished(actionEvent -> {

                        if (turnNumber < totalNumberOfTurns - 1) {
                            stats.incrementNumberOfGoalsReached();
                            turnNumber++;
                            cards.getChildren().get(2).setOpacity(1 - turnNumber / (float) (totalNumberOfTurns - 1));
                            stats.incrementNumberOfGoalsToReach();
                            playSound(1);

                        } else if (turnNumber == totalNumberOfTurns - 1) {
                            timer.cancel();
                            turnNumber++;
                            gameContext.getGazeDeviceManager().removeEventFilter(cards);
                            that.removeEventFilter(MouseEvent.ANY, enterEvent);
                            that.removeEventFilter(GazeEvent.ANY, enterEvent);

                            cards.getChildren().get(1).setOpacity(0);

                            progressIndicator.setOpacity(0);
                            stats.incrementNumberOfGoalsReached();
                            playSound(2);

                            final PauseTransition t = new PauseTransition(Duration.seconds(1));

                            t.setOnFinished(actionEvent1 -> {

                                gameContext.updateScore(stats, gameInstance);

                                gameContext.playWinTransition(0, event -> {
                                    gameInstance.dispose();

                                    gameContext.clear();

                                    gameContext.showRoundStats(stats, gameInstance);

                                });

                            });

                            t.play();

                        }
                    });

                    timelineProgressBar.play();
                }
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                timelineProgressBar.stop();
                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);

            }
        };
    }

    class RemindTask extends TimerTask{
        //final Egg that = this;
        public void run(){
            //turnNumber++;
            timer.cancel();
            gameContext.getGazeDeviceManager().removeEventFilter(cards);
            //that.removeEventFilter(MouseEvent.ANY, enterEvent);
            //that.removeEventFilter(GazeEvent.ANY, enterEvent);
            cards.getChildren().get(1).setOpacity(0);
            progressIndicator.setOpacity(0);
            stats.incrementNumberOfGoalsReached();
            playSound(2);
            final PauseTransition t1 = new PauseTransition(Duration.seconds(1));
            t1.setOnFinished(actionEvent1 -> {

                gameContext.updateScore(stats, gameInstance);

                gameContext.playWinTransition(0, event -> {
                    gameInstance.dispose();

                    gameContext.clear();

                    gameContext.showRoundStats(stats, gameInstance);

                });

            });

            t1.play();
        }
    }

    public void playSound(final int i) {
        final String soundResource = "data/egg/sounds/" + i + ".mp3";
        gameContext.getSoundManager().add(soundResource);
    }

}
