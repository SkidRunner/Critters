package karpathy.scriptsbots;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Group primaryGroup = new Group();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Canvas primaryCanvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
        primaryGroup.getChildren().add(primaryCanvas);

        Scene primaryScene = new Scene(primaryGroup, screenBounds.getWidth(), screenBounds.getHeight());
        primaryScene.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                }
            }

        });

        final View view = new View(primaryCanvas, new World());

        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.setScene(primaryScene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                view.handleIdle((int)now);
            }
        }.start();
    }
}
