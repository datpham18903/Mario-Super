package manager;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JFrame;
import model.hero.Mario;
import view.*;


public class GameEngine implements Runnable {
    private static final int WIDTH = 1268;
    private static final int HEIGHT = 708;
    private MapManager mapManager;
    private UIManager uiManager;
    private SoundManager soundManager;
    private GameStatus gameStatus;
    private boolean isRunning;
    private Camera camera;
    private ImageLoader imageLoader;
    private Thread thread;
    private StartScreenSelection startScreenSelection= StartScreenSelection.START_GAME;;
    private GameOverMenu gameOverMenu = GameOverMenu.START_GAME;
    private MissionPassedSelection missionPassedSelection = MissionPassedSelection.START_SCREEN;
    private int selectedMap;

    private GameEngine() {init();}

    private void init() {
        this.imageLoader = new ImageLoader();
        InputManager inputManager = new InputManager(this);
        this.gameStatus = GameStatus.START_SCREEN;
        this.camera = new Camera();
        this.uiManager = new UIManager(this, 1268, 708);
        this.soundManager = new SoundManager();
        this.mapManager = new MapManager();

        JFrame frame = new JFrame("Super Mario Bros.");
        frame.add(this.uiManager);
        frame.addKeyListener(inputManager);
        frame.addMouseListener(inputManager);
        frame.pack();
        frame.setDefaultCloseOperation(3);
        frame.setResizable(false);
        frame.setLocationRelativeTo((Component)null);
        frame.setVisible(true);

        this.start();
    }

    private synchronized void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            this.thread = new Thread(this);
            this.thread.start();
        }
    }

    private void reset() {
        this.resetCamera();
        this.setGameStatus(GameStatus.START_SCREEN);
    }

    public void resetCamera() {
        this.camera = new Camera();
        this.soundManager.restartBackground();
    }

    public void selectMapViaMouse() {
        String path = this.uiManager.selectMapViaMouse(this.uiManager.getMousePosition());
        if (path != null) {
            this.createMap(path);
        }

    }

    public void selectMapViaKeyboard() {
        String path = this.uiManager.selectMapViaKeyboard(this.selectedMap);
        if (path != null) {
            this.createMap(path);
        }

    }
    public void nextMap() {
        selectedMap++;
        resetCamera();
        String path = uiManager.selectMapViaKeyboard(selectedMap);
        if (path != null) {
            createMap(path);
        }
    }
    public void changeSelectedMap(boolean up) {
        this.selectedMap = this.uiManager.changeSelectedMap(this.selectedMap, up);
    }

    private void createMap(String path) {
        boolean loaded = this.mapManager.createMap(this.imageLoader, path);
        if (loaded) {
            this.setGameStatus(GameStatus.RUNNING);
            this.soundManager.restartBackground();
        } else {
            this.setGameStatus(GameStatus.START_SCREEN);
        }

    }
@Override
    public void run() {
        while(this.isRunning && !this.thread.isInterrupted()) {
            try {
                long lastTime = System.nanoTime();
                double amountOfTicks = 60.0;
                double ns = 1.0E9 / amountOfTicks;
                double delta = 0.0;
                long timer = System.currentTimeMillis();

                while(this.isRunning && !this.thread.isInterrupted()) {
                    long now = System.nanoTime();
                    delta += (double)(now - lastTime) / ns;

                    for(lastTime = now; delta >= 1.0; --delta) {
                        if (this.gameStatus == GameStatus.RUNNING) {
                            this.gameLoop();
                        }
                    }

                    this.render();
                    if (this.gameStatus != GameStatus.RUNNING) {
                        timer = System.currentTimeMillis();
                    }

                    if (System.currentTimeMillis() - timer > 1000L) {
                        timer += 1000L;
                        this.mapManager.updateTime();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void render() {
        this.uiManager.repaint();
    }

    private void gameLoop() {
        this.updateLocations();
        this.checkCollisions();
        this.updateCamera();
        if (this.isGameOver()) {
            this.setGameStatus(GameStatus.GAME_OVER);
        }
        int missionPassed = this.passMission();
        if (missionPassed > -1) {
            this.mapManager.acquirePoints(missionPassed);
        } else if (this.mapManager.endLevel()) {
            this.setGameStatus(GameStatus.MISSION_PASSED);
        }
    }

    private void updateCamera() {
        Mario mario = mapManager.getMario();
        double marioVelocityX = mario.getVelX();
        double shiftAmount = 0;

        if (marioVelocityX > 0 && mario.getX() - 600 > camera.getX()) {
            shiftAmount = marioVelocityX;
        } else if (marioVelocityX < 0 ) {
            shiftAmount = marioVelocityX;
        }

        camera.moveCam(shiftAmount, 0);
    }

    private void updateLocations() {
        this.mapManager.updateLocations();
    }

    private void checkCollisions() {
        this.mapManager.checkCollisions(this);
    }

    public void receiveInput(ButtonAction input) {
        if (this.gameStatus == GameStatus.START_SCREEN) {
            if (input == ButtonAction.SELECT && this.startScreenSelection == StartScreenSelection.START_GAME) {
                this.startGame();
            } else if (input == ButtonAction.SELECT && this.startScreenSelection == StartScreenSelection.VIEW_ABOUT) {
                this.setGameStatus(GameStatus.ABOUT_SCREEN);
            } else if (input == ButtonAction.SELECT && this.startScreenSelection == StartScreenSelection.VIEW_HELP) {
                this.setGameStatus(GameStatus.HELP_SCREEN);
            } else if (input == ButtonAction.GO_UP) {
                this.selectOption(true);
            } else if (input == ButtonAction.GO_DOWN) {
                this.selectOption(false);
            }
        } else if (this.gameStatus == GameStatus.MAP_SELECTION) {
            if (input == ButtonAction.SELECT) {
                this.selectMapViaKeyboard();
            } else if (input == ButtonAction.GO_UP) {
                this.changeSelectedMap(true);
            } else if (input == ButtonAction.GO_DOWN) {
                this.changeSelectedMap(false);
            }
        } else if (this.gameStatus == GameStatus.RUNNING) {
            Mario mario = this.mapManager.getMario();
            if (input == ButtonAction.JUMP) {
                mario.jump(this);
            } else if (input == ButtonAction.M_RIGHT) {
                mario.move(true, this.camera);
            } else if (input == ButtonAction.M_LEFT) {
                mario.move(false, this.camera);
            } else if (input == ButtonAction.ACTION_COMPLETED) {
                mario.setVelX(0.0);
            } else if (input == ButtonAction.FIRE) {
                this.mapManager.fire(this);
            } else if (input == ButtonAction.PAUSE_RESUME) {
                this.pauseGame();
            }
        } else if (this.gameStatus == GameStatus.PAUSED) {
            if (input == ButtonAction.PAUSE_RESUME) {
                this.pauseGame();
            }
        }  else if (gameStatus == GameStatus.GAME_OVER) {
            if (input == ButtonAction.SELECT
                    && gameOverMenu == GameOverMenu.START_GAME) {
                resetCamera();
                selectMapViaKeyboard();
            } else if (input == ButtonAction.SELECT
                    && gameOverMenu == GameOverMenu.START_SCREEN) {
                reset();
            } else if (input == ButtonAction.GO_UP) {
                selectGameOverMenu(true);
            } else if (input == ButtonAction.GO_DOWN) {
                selectGameOverMenu(false);
            }
        } else if (gameStatus == GameStatus.MISSION_PASSED) {
            if (input == ButtonAction.SELECT
                    && missionPassedSelection == MissionPassedSelection.START_SCREEN) {
                reset();
            } else if (input == ButtonAction.SELECT
                    && missionPassedSelection == MissionPassedSelection.NEXT_MAP) {
                nextMap();
            } else if (input == ButtonAction.GO_UP) {
                selectMissionPassedOption(true);
            } else if (input == ButtonAction.GO_DOWN) {
                selectMissionPassedOption(false);
            }
        }

        if (input == ButtonAction.GO_TO_START_SCREEN) {
            this.setGameStatus(GameStatus.START_SCREEN);
        }

    }

    private void selectOption(boolean selectUp) {
        this.startScreenSelection = this.startScreenSelection.select(selectUp);
    }
    private void selectGameOverMenu(boolean selectUp) {
        gameOverMenu = gameOverMenu.select(selectUp);
    }

    private void selectMissionPassedOption(boolean selectUp) {
        missionPassedSelection = missionPassedSelection.select(selectUp);
    }
    private void startGame() {
        if (this.gameStatus != GameStatus.GAME_OVER) {
            this.setGameStatus(GameStatus.MAP_SELECTION);
        }

    }

    private void pauseGame() {
        if (this.gameStatus == GameStatus.RUNNING) {
            this.setGameStatus(GameStatus.PAUSED);
            this.soundManager.pauseBackground();
        } else if (this.gameStatus == GameStatus.PAUSED) {
            this.setGameStatus(GameStatus.RUNNING);
            this.soundManager.resumeBackground();
        }

    }

    public void shakeCamera() {
        this.camera.shakeCamera();
    }

    private boolean isGameOver() {
        return this.gameStatus == GameStatus.RUNNING ? this.mapManager.isGameOver() : false;
    }

    public ImageLoader getImageLoader() {
        return this.imageLoader;
    }

    public GameStatus getGameStatus() {
        return this.gameStatus;
    }
    public GameOverMenu getGameOverMenu() {
        return gameOverMenu;
    }

    public MissionPassedSelection getMissionPassedSelection() {
        return missionPassedSelection;
    }


    public StartScreenSelection getStartScreenSelection() {
        return this.startScreenSelection;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getScore() {
        return this.mapManager.getScore();
    }

    public int getRemainingLives() {
        return this.mapManager.getRemainingLives();
    }

    public int getCoins() {
        return this.mapManager.getCoins();
    }

    public int getSelectedMap() {
        return this.selectedMap;
    }

    public void drawMap(Graphics2D g2) {
        this.mapManager.drawMap(g2);
    }

    public Point getCameraLocation() {
        return new Point((int)this.camera.getX(), (int)this.camera.getY());
    }

    private int passMission() {
        return this.mapManager.passMission();
    }

    public void playCoin() {
        this.soundManager.playCoin();
    }

    public void playOneUp() {
        this.soundManager.playOneUp();
    }

    public void playSuperMushroom() {
        this.soundManager.playSuperMushroom();
    }

    public void playMarioDies() {
        this.soundManager.playMarioDies();
    }

    public void playJump() {
        this.soundManager.playJump();
    }

    public void playFireFlower() {
        this.soundManager.playFireFlower();
    }

    public void playFireball() {
        this.soundManager.playFireball();
    }

    public void playStomp() {
        this.soundManager.playStomp();
    }

    public MapManager getMapManager() {
        return this.mapManager;
    }

    public static void main(String... args) {
        new GameEngine();
    }

    public int getRemainingTime() {
        return this.mapManager.getRemainingTime();
    }
}
