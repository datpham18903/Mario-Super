package manager;

import model.GameObject;
import model.Map;
import model.brick.Brick;
import model.brick.OrdinaryBrick;
import model.enemy.*;
import model.hero.Fireball;
import model.hero.Mario;
import model.prize.BoostItem;
import model.prize.Coin;
import model.prize.Prize;
import view.ImageLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class MapManager {

    private Map map;

    public MapManager() {}

    public void updateLocations() {
        if (map == null)
            return;

        map.updateLocations();
    }

    public void resetCurrentMap(GameEngine engine) {
        Mario mario = getMario();
        mario.resetLocation();
        engine.resetCamera();
        createMap(engine.getImageLoader(), map.getPath());
        map.setMario(mario);
    }

    public boolean createMap(ImageLoader loader, String path) {
        MapCreator mapCreator = new MapCreator(loader);
        map = mapCreator.createMap("/maps/" + path, 400);

        return map != null;
    }

    public void acquirePoints(int point) {
        map.getMario().acquirePoints(point);
    }

    public Mario getMario() {
        return map.getMario();
    }

    public void fire(GameEngine engine) {
        Fireball fireball = getMario().fire();
        if (fireball != null) {
            map.addFireball(fireball);
            engine.playFireball();
        }
    }

    public boolean isGameOver() {
        return getMario().getRemainingLives() == 0 || map.isTimeOver();
    }

    public int getScore() {
        return getMario().getPoints();
    }

    public int getRemainingLives() {
        return getMario().getRemainingLives();
    }

    public int getCoins() {
        return getMario().getCoins();
    }

    public void drawMap(Graphics2D g2) {
        map.drawMap(g2);
    }

    public int passMission(int requiredPoint) {
        // Check if Bowser has been defeated
        if (map.getBowser() != null && map.getBowser().getRemainingLives() > 0) {
        	System.out.println(map.getBowser());
        	System.out.println(map.getBowser().getRemainingLives());
            return -1;  // Bowser is still alive, so the player cannot pass the level
        }

        // Check if Mario has enough points
        if (getMario().getPoints() < requiredPoint) {
            return -1;  // Mario does not have enough points, so the player cannot pass the level
        }

        // Check if Mario has reached the end point and if the end point hasn't been touched yet
        if (getMario().getX() >= map.getEndPoint().getX() && !map.getEndPoint().isTouched()) {
            map.getEndPoint().setTouched(true);
            int height = (int) getMario().getY();
            return height * 2;
        }

        return -1;  // The player cannot pass the level
    }




    public boolean endLevel(){
        return getMario().getX() >= map.getEndPoint().getX() + 320;
    }

    public void checkCollisions(GameEngine engine) {
        if (map == null) {}

        checkBottomCollisions(engine);
        checkTopCollisions(engine);
        checkMarioHorizontalCollision(engine);
        checkEnemyCollisions(engine);
        checkPrizeCollision();
        checkPrizeContact(engine);
        checkFireballContact();
    }

    private void checkBottomCollisions(GameEngine engine) {
        Mario mario = this.getMario();
        ArrayList<Brick> bricks = this.map.getAllBricks();
        ArrayList<Enemy> enemies = this.map.getEnemies();
        ArrayList<GameObject> toBeRemoved = new ArrayList();
        Rectangle marioBottomBounds = mario.getBottomBounds();
        if (!mario.isJumping()) {
            mario.setFalling(true);
        }
        for (Brick brick : bricks) {
            Rectangle brickTopBounds = brick.getTopBounds();
            if (marioBottomBounds.intersects(brickTopBounds)) {
                mario.setY(brick.getY() - mario.getDimension().height + 1);
                mario.setFalling(false);
                mario.setVelY(0);
            }
        }

        for (Enemy enemy : enemies) {
            Rectangle enemyTopBounds = enemy.getTopBounds();
            if (marioBottomBounds.intersects(enemyTopBounds)) {
                mario.acquirePoints(100);
                toBeRemoved.add(enemy);
                engine.playStomp();
            }
        }

        if (mario.getY() + mario.getDimension().height >= map.getBottomBorder()) {
            mario.setY(map.getBottomBorder() - mario.getDimension().height);
            mario.setFalling(false);
            mario.setVelY(0);
        }

        removeObjects(toBeRemoved);
    }

    private void checkTopCollisions(GameEngine engine) {
        Mario mario = getMario();
        ArrayList<Brick> bricks = map.getAllBricks();

        Rectangle marioTopBounds = mario.getTopBounds();
        for (Brick brick : bricks) {
            Rectangle brickBottomBounds = brick.getBottomBounds();
            if (marioTopBounds.intersects(brickBottomBounds)) {
                mario.setVelY(0);
                mario.setY(brick.getY() + brick.getDimension().height);
                Prize prize = brick.reveal(engine);
                if(prize != null)
                    map.addRevealedPrize(prize);
            }
        }
    }

    private void checkMarioHorizontalCollision(GameEngine engine){
        Mario mario = getMario();
        ArrayList<Brick> bricks = map.getAllBricks();
        ArrayList<Enemy> enemies = map.getEnemies();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        boolean marioDies = false;
        boolean toRight = mario.getToRight();

        Rectangle marioBounds = toRight ? mario.getRightBounds() : mario.getLeftBounds();

        for (Brick brick : bricks) {
            Rectangle brickBounds = !toRight ? brick.getRightBounds() : brick.getLeftBounds();
            if (marioBounds.intersects(brickBounds)) {
                mario.setVelX(0);
                if(toRight)
                    mario.setX(brick.getX() - mario.getDimension().width);
                else
                    mario.setX(brick.getX() + brick.getDimension().width);
            }
        }

        for(Enemy enemy : enemies){
            Rectangle enemyBounds = !toRight ? enemy.getRightBounds() : enemy.getLeftBounds();
            if (marioBounds.intersects(enemyBounds)) {
                marioDies = mario.onTouchEnemy(engine);
                toBeRemoved.add(enemy);
            }
        }
        removeObjects(toBeRemoved);


        if (mario.getX() <= engine.getCameraLocation().getX() && mario.getVelX() < 0) {
            mario.setVelX(0);
            mario.setX(engine.getCameraLocation().getX());
        }

        if(marioDies) {
            resetCurrentMap(engine);
        }
    }

    // Assuming 'engine' is a field in your class

    private void checkEnemyCollisions(GameEngine engine) {
        ArrayList<Brick> bricks = this.map.getAllBricks();
        ArrayList<Enemy> enemies = this.map.getEnemies();
        ArrayList<GameObject> toBeRemoved = new ArrayList();
        Mario mario = this.getMario();
        Rectangle marioBounds = mario.getBounds();
        Iterator var7 = enemies.iterator();

        while(true) {
            Enemy enemy;
            do {
                if (!var7.hasNext()) {
                    this.removeObjects(toBeRemoved);
                    return;
                }

                enemy = (Enemy)var7.next();
                boolean standsOnBrick = false;
                Rectangle enemyBounds = enemy.getBounds();
                if (marioBounds.intersects(enemyBounds)) {
                    boolean marioDies = mario.onTouchEnemy(engine);
                    toBeRemoved.add(enemy);
                    if (marioDies) {
                        this.resetCurrentMap(engine);
                        return;
                    }
                }

                Iterator var17 = bricks.iterator();

                while(var17.hasNext()) {
                    Brick brick = (Brick)var17.next();
                    Rectangle brickBounds = brick.getRightBounds();
                    Rectangle enemyBottomBounds = enemy.getBottomBounds();
                    Rectangle brickTopBounds = brick.getTopBounds();
                    if (enemy.getVelX() > 0.0) {
                        enemyBounds = enemy.getRightBounds();
                        brickBounds = brick.getLeftBounds();
                    }

                    if (enemyBounds.intersects(brickBounds)) {
                        enemy.setVelX(-enemy.getVelX());
                    }

                    if (enemyBottomBounds.intersects(brickTopBounds)) {
                        enemy.setFalling(false);
                        enemy.setVelY(0.0);
                        enemy.setY(brick.getY() - (double)enemy.getDimension().height);
                        standsOnBrick = true;
                    }
                }

                if (enemy.getY() + (double)enemy.getDimension().height > this.map.getBottomBorder()) {
                    enemy.setFalling(false);
                    enemy.setVelY(0.0);
                    enemy.setY(this.map.getBottomBorder() - (double)enemy.getDimension().height);
                }

                if (!standsOnBrick && enemy.getY() < this.map.getBottomBorder()) {
                    enemy.setFalling(true);
                }
            } while(!(enemy instanceof Bowser));

            Bowser bowser = (Bowser)enemy;
            Rectangle bowserBounds = bowser.getBounds();
            if (marioBounds.intersects(bowserBounds)) {
                this.handleBowserCollision(bowser);
            }

            ArrayList<Fireball> fireballs = this.map.getFireballs();
            Iterator var21 = fireballs.iterator();

            while(var21.hasNext()) {
                Fireball fireball = (Fireball)var21.next();
                Rectangle fireballBounds = fireball.getBounds();
                if (fireballBounds.intersects(bowserBounds)) {
                    this.handleBowserCollision(bowser);
                    toBeRemoved.add(fireball);
                }
            }
        }
    }

    private void handleBowserCollision(Bowser bowser) {
        bowser.reduceLives();
        if (bowser.getRemainingLives() <= 0) {
            this.map.removeEnemy(bowser);
        }

    }





    private void checkPrizeCollision() {
        ArrayList<Prize> prizes = map.getRevealedPrizes();
        ArrayList<Brick> bricks = map.getAllBricks();

        for (Prize prize : prizes) {
            if (prize instanceof BoostItem) {
                BoostItem boost = (BoostItem) prize;
                Rectangle prizeBottomBounds = boost.getBottomBounds();
                Rectangle prizeRightBounds = boost.getRightBounds();
                Rectangle prizeLeftBounds = boost.getLeftBounds();
                boost.setFalling(true);

                for (Brick brick : bricks) {
                    Rectangle brickBounds;

                    if (boost.isFalling()) {
                        brickBounds = brick.getTopBounds();

                        if (brickBounds.intersects(prizeBottomBounds)) {
                            boost.setFalling(false);
                            boost.setVelY(0);
                            boost.setY(brick.getY() - boost.getDimension().height + 1);
                            if (boost.getVelX() == 0)
                                boost.setVelX(2);
                        }
                    }

                    if (boost.getVelX() > 0) {
                        brickBounds = brick.getLeftBounds();

                        if (brickBounds.intersects(prizeRightBounds)) {
                            boost.setVelX(-boost.getVelX());
                        }
                    } else if (boost.getVelX() < 0) {
                        brickBounds = brick.getRightBounds();

                        if (brickBounds.intersects(prizeLeftBounds)) {
                            boost.setVelX(-boost.getVelX());
                        }
                    }
                }

                if (boost.getY() + boost.getDimension().height > map.getBottomBorder()) {
                    boost.setFalling(false);
                    boost.setVelY(0);
                    boost.setY(map.getBottomBorder() - boost.getDimension().height);
                    if (boost.getVelX() == 0)
                        boost.setVelX(2);
                }

            }
        }
    }

    private void checkPrizeContact(GameEngine engine) {
        ArrayList<Prize> prizes = map.getRevealedPrizes();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        Rectangle marioBounds = getMario().getBounds();
        for(Prize prize : prizes){
            Rectangle prizeBounds = prize.getBounds();
            if (prizeBounds.intersects(marioBounds)) {
                prize.onTouch(getMario(), engine);
                toBeRemoved.add((GameObject) prize);
            } else if(prize instanceof Coin){
                prize.onTouch(getMario(), engine);
            }
        }

        removeObjects(toBeRemoved);
    }

    private void checkFireballContact() {
        ArrayList<Fireball> fireballs = map.getFireballs();
        ArrayList<Enemy> enemies = map.getEnemies();
        ArrayList<Brick> bricks = map.getAllBricks();
        ArrayList<GameObject> toBeRemoved = new ArrayList<>();

        for(Fireball fireball : fireballs){
            Rectangle fireballBounds = fireball.getBounds();

            for(Enemy enemy : enemies){
                Rectangle enemyBounds = enemy.getBounds();
                if (fireballBounds.intersects(enemyBounds)) {
                    acquirePoints(100);
                    toBeRemoved.add(enemy);
                    toBeRemoved.add(fireball);
                }
            }

            for(Brick brick : bricks){
                Rectangle brickBounds = brick.getBounds();
                if (fireballBounds.intersects(brickBounds)) {
                    toBeRemoved.add(fireball);
                }
            }
        }

        removeObjects(toBeRemoved);
    }

    private void removeObjects(ArrayList<GameObject> list){
        if(list == null)
            return;

        for(GameObject object : list){
            if(object instanceof Fireball){
                map.removeFireball((Fireball)object);
            }
            else if(object instanceof Enemy){
                map.removeEnemy((Enemy)object);
            }
            else if(object instanceof Coin || object instanceof BoostItem){
                map.removePrize((Prize)object);
            }
        }
    }

    public void addRevealedBrick(OrdinaryBrick ordinaryBrick) {
        map.addRevealedBrick(ordinaryBrick);
    }

    public void updateTime(){
        if(map != null)
            map.updateTime(1);
    }

    public int getRemainingTime() {
        return (int)map.getRemainingTime();
    }
}
