package net.gazeplay.games.creampie;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by schwab on 12/08/2016.
 *
 */
@Slf4j
public class CreamPie implements GameLifeCycle {

    private final IGameContext gameContext;

    private final Stats stats;

    private final Hand hand;

    private final Target target;

    private final ReplayablePseudoRandom randomGenerator;


    public static Timer timer;



    public CreamPie(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        final ImageLibrary imageLibrary = Portrait.createImageLibrary(randomGenerator);
        final RandomPositionGenerator randomPositionGenerator = gameContext.getRandomPositionGenerator();
        randomPositionGenerator.setRandomGenerator(randomGenerator);

        hand = new Hand();

        Scene scene = gameContext.getPrimaryScene();
        int radius = (int) Math.min(scene.getHeight()/12, scene.getWidth()/12);

        target = new Target(randomPositionGenerator, hand, stats, gameContext, imageLibrary, this, radius);
        gameContext.getChildren().add(target);
        gameContext.getChildren().add(hand);
    }

    public CreamPie(IGameContext gameContext, Stats stats, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        final ImageLibrary imageLibrary = Portrait.createImageLibrary(randomGenerator);
        final RandomPositionGenerator randomPositionGenerator = gameContext.getRandomPositionGenerator();
        randomPositionGenerator.setRandomGenerator(randomGenerator);
        //randomPositionGenerator.
        hand = new Hand();

        Scene scene = gameContext.getPrimaryScene();

        int radius = (int) Math.min(scene.getHeight()/12, scene.getWidth()/12);

        target = new Target(randomPositionGenerator, hand, stats, gameContext, imageLibrary, this,radius);

    }

    @Override
    public void launch() {
        timer = new Timer();
        timer.schedule(new RemindTask(),6000);
        gameContext.getChildren().clear();
        gameContext.getChildren().add(target);
        gameContext.getChildren().add(hand);
        gameContext.setLimiterAvailable();
        hand.recomputePosition();

        gameContext.getRoot().widthProperty().addListener((obs, oldVal, newVal) -> hand.recomputePosition());
        gameContext.getRoot().heightProperty().addListener((obs, oldVal, newVal) -> hand.recomputePosition());

        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {
        stats.setTargetAOIList(target.getTargetAOIList());
    }


      public class RemindTask extends TimerTask {

        public void run() {
            timer.cancel();
            target.animationEnded = false;
            target.enter();
            target.gameContext.start();
            //gameContext.getGazeDeviceManager().addEventFilter(this);
            //this.addEventFilter(MouseEvent.ANY, enterEvent);
            //this.addEventFilter(GazeEvent.ANY, enterEvent);
        }
    }

}
