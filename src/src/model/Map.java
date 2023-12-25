package model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Iterator;
import model.brick.Brick;
import model.brick.OrdinaryBrick;
import model.enemy.Enemy;
import model.hero.Fireball;
import model.hero.Mario;
import model.prize.BoostItem;
import model.prize.Coin;
import model.prize.Prize;
import model.enemy.Bowser;

public class Map {
    private double remainingTime;
    private Mario mario;
    private ArrayList<Brick> bricks = new ArrayList();
    private ArrayList<Enemy> enemies = new ArrayList();
    private ArrayList<Brick> groundBricks = new ArrayList();
    private ArrayList<Prize> revealedPrizes = new ArrayList();
    private ArrayList<Brick> revealedBricks = new ArrayList();
    private ArrayList<Fireball> fireballs = new ArrayList();
    private EndFlag endPoint;
    private BufferedImage backgroundImage;
    private double bottomBorder = 624.0;
    private String path;

    public Map(double remainingTime, BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
        this.remainingTime = remainingTime;
    }

    public Mario getMario() {
        return mario;
    }

    public void setMario(Mario mario) {
        this.mario = mario;
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }

    public ArrayList<Fireball> getFireballs() {
        return this.fireballs;
    }

    public ArrayList<Prize> getRevealedPrizes() {
        return this.revealedPrizes;
    }

    public ArrayList<Brick> getAllBricks() {
        ArrayList<Brick> allBricks = new ArrayList();
        allBricks.addAll(this.bricks);
        allBricks.addAll(this.groundBricks);
        return allBricks;
    }

    public void addBrick(Brick brick) {
        this.bricks.add(brick);
    }

    public void addGroundBrick(Brick brick) {
        this.groundBricks.add(brick);
    }

    public void addEnemy(Enemy enemy) {
        this.enemies.add(enemy);
    }

    public void drawMap(Graphics2D g2) {
        this.drawBackground(g2);
        this.drawPrizes(g2);
        this.drawBricks(g2);
        this.drawEnemies(g2);
        this.drawFireballs(g2);
        this.drawMario(g2);
        this.endPoint.draw(g2);
    }

    private void drawFireballs(Graphics2D g2) {
        Iterator var2 = this.fireballs.iterator();

        while(var2.hasNext()) {
            Fireball fireball = (Fireball)var2.next();
            fireball.draw(g2);
        }

    }

    private void drawPrizes(Graphics2D g2) {
        Iterator var2 = this.revealedPrizes.iterator();

        while(var2.hasNext()) {
            Prize prize = (Prize)var2.next();
            if (prize instanceof Coin) {
                ((Coin)prize).draw(g2);
            } else if (prize instanceof BoostItem) {
                ((BoostItem)prize).draw(g2);
            }
        }

    }

    private void drawBackground(Graphics2D g2) {
        g2.drawImage(this.backgroundImage, 0, 0, (ImageObserver)null);
    }

    private void drawBricks(Graphics2D g2) {
        Iterator var2 = this.bricks.iterator();

        Brick brick;
        while(var2.hasNext()) {
            brick = (Brick)var2.next();
            if (brick != null) {
                brick.draw(g2);
            }
        }

        var2 = this.groundBricks.iterator();

        while(var2.hasNext()) {
            brick = (Brick)var2.next();
            brick.draw(g2);
        }

    }

    private void drawEnemies(Graphics2D g2) {
        Iterator var2 = this.enemies.iterator();

        while(var2.hasNext()) {
            Enemy enemy = (Enemy)var2.next();
            if (enemy != null) {
                enemy.draw(g2);
            }
        }

    }

    private void drawMario(Graphics2D g2) {
        this.mario.draw(g2);
    }

    public synchronized void updateLocations() {
        this.mario.updateLocation();
        Iterator brickIterator = this.enemies.iterator();

        while(brickIterator.hasNext()) {
            Enemy enemy = (Enemy)brickIterator.next();
            enemy.updateLocation();
        }

        brickIterator = this.revealedPrizes.iterator();

        while(brickIterator.hasNext()) {
            Prize prize = (Prize)brickIterator.next();
            if (prize instanceof Coin) {
                ((Coin)prize).updateLocation();
                if ((double)((Coin)prize).getRevealBoundary() > ((Coin)prize).getY()) {
                    brickIterator.remove();
                }
            } else if (prize instanceof BoostItem) {
                ((BoostItem)prize).updateLocation();
            }
        }

        brickIterator = this.fireballs.iterator();

        while(brickIterator.hasNext()) {
            Fireball fireball = (Fireball)brickIterator.next();
            fireball.updateLocation();
        }

        brickIterator = this.revealedBricks.iterator();

        while(brickIterator.hasNext()) {
            OrdinaryBrick brick = (OrdinaryBrick)brickIterator.next();
            brick.animate();
            if (brick.getFrames() < 0) {
                this.bricks.remove(brick);
                brickIterator.remove();
            }
        }

        this.endPoint.updateLocation();
    }

    public double getBottomBorder() {
        return this.bottomBorder;
    }

    public void addRevealedPrize(Prize prize) {
        this.revealedPrizes.add(prize);
    }

    public void addFireball(Fireball fireball) {
        this.fireballs.add(fireball);
    }

    public void setEndPoint(EndFlag endPoint) {
        this.endPoint = endPoint;
    }

    public EndFlag getEndPoint() {
        return this.endPoint;
    }

    public void addRevealedBrick(OrdinaryBrick ordinaryBrick) {
        this.revealedBricks.add(ordinaryBrick);
    }

    public void removeFireball(Fireball object) {
        this.fireballs.remove(object);
    }

    public void removeEnemy(Enemy enemy) {
        if (this.enemies.contains(enemy)) {
            this.enemies.remove(enemy);
        }

    }

    public void removePrize(Prize object) {
        this.revealedPrizes.remove(object);
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void updateTime(double passed) {
        this.remainingTime -= passed;
    }

    public boolean isTimeOver() {
        return this.remainingTime <= 0.0;
    }

    public double getRemainingTime() {
        return this.remainingTime;
    }
    public boolean isBowserExist() {
        for (Enemy enemy : this.enemies) {
            if (enemy instanceof Bowser) {
                return true;
            }
        }
        return false;
    }

}
