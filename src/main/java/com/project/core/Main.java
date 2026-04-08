package com.project.core;

import com.jme3.system.AppSettings;
import com.project.utils.Constants;

/**
 * Application entry point.
 *
 * <p>Creates an {@link AppSettings} object to configure the window, then
 * hands control to {@link GameApp}.
 *
 * <p>Run this class directly or build a fat JAR with {@code mvn package} and
 * run:
 * <pre>
 *   java -jar target/project-game-1.0-SNAPSHOT-jar-with-dependencies.jar
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        GameApp app = new GameApp();

        AppSettings settings = new AppSettings(true);
        settings.setTitle(Constants.GAME_TITLE);
        settings.setWidth(Constants.WINDOW_WIDTH);
        settings.setHeight(Constants.WINDOW_HEIGHT);
        settings.setFrameRate(Constants.TARGET_FPS);
        settings.setVSync(true);
        settings.setSamples(4); // 4× MSAA for smoother edges
        settings.setResizable(true);

        app.setSettings(settings);
        app.setShowSettings(false); // skip the display-settings dialog on startup
        app.start();
    }
}
