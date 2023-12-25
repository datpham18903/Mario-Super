
package model.hero;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import manager.Camera;
import manager.GameEngine;
import model.GameObject;
import view.Animation;
import view.ImageLoader;
import model.enemy.Bowser;
import model.enemy.Enemy;

public class Mario extends GameObject {
    private int remainingLives;
    private int coins;
    private int points;
    private double invincibilityTimer;
    private MarioForm marioForm;
    private boolean toRight = true;
    private long lastFireTime;

    public Mario(double x, double y) {
        super(x, y, (BufferedImage)null);
        this.setDimension(48, 48);
        this.remainingLives = 3;
        this.points = 0;
        this.coins = 0;
        this.invincibilityTimer = 0.0;
        this.lastFireTime = System.currentTimeMillis();
        ImageLoader imageLoader = new ImageLoader();
        BufferedImage[] leftFrames = imageLoader.getLeftFrames(MarioForm.SMALL);
        BufferedImage[] rightFrames = imageLoader
                .getRightFrames(MarioForm.SMALL);
        Animation animation = new Animation(leftFrames, rightFrames);
        this.marioForm = new MarioForm(animation, false, false);
        this.setStyle(this.marioForm.getCurrentStyle(this.toRight, false, false));
    }
@Override
    public void draw(Graphics g) {
        boolean movingInX = this.getVelX() != 0.0;
        boolean movingInY = this.getVelY() != 0.0;
        this.setStyle(this.marioForm.getCurrentStyle(this.toRight, movingInX, movingInY));
        super.draw(g);
    }

    public void jump(GameEngine engine) {
        byte jumpHeight;
        if (this.marioForm.isFire()) {
            jumpHeight = 13;
        } else if (this.marioForm.isSuper()) {
            jumpHeight = 15;
        } else {
            jumpHeight = 11;
        }

        if (!this.isJumping() && !this.isFalling()) {
            this.setJumping(true);
            this.setVelY((double)jumpHeight);
            engine.playJump();
        }

    }

    public void move(boolean toRight, Camera camera) {
        double speed;
        if (this.marioForm.isFire() || this.marioForm.isSuper()) {
            speed = 5.0; // Super Mario and Fire Mario move at speed 5.0
        } else {
            speed = 6.0; // Normal Mario moves faster at speed 7.0
        }
        if (toRight) {
            this.setVelX(speed);
        } else if (camera.getX() < this.getX()) {
            this.setVelX(-speed);
        }
        this.toRight = toRight;
    }
    public boolean onTouchEnemy(GameEngine engine, Enemy enemy) {
        if (enemy instanceof Bowser) {
                engine.shakeCamera();
                --this.remainingLives;
                this.marioForm = this.marioForm.onTouchEnemy(engine.getImageLoader());
                this.setDimension(48, 48);
                return true;
            }
         else {
            if (!this.marioForm.isSuper() && !this.marioForm.isFire()) {
                --this.remainingLives;
                engine.playMarioDies();
                return true;
            } else {
                engine.shakeCamera();
                this.marioForm = this.marioForm.onTouchEnemy(engine.getImageLoader());
                this.setDimension(48, 48);
                return false;
            }
        }
    }
    public Fireball fire() {
        long currentTime = System.currentTimeMillis();
        long delay;
        if (this.marioForm.isFire()) {
            delay = 250; // Fire Mario fires every 0.25 seconds
        } else if (this.marioForm.isSuper()) {
            delay = 400; // Super Mario fires every 0.5 seconds
        } else {
            return null; // Small Mario can't fire
        }
        if (currentTime - lastFireTime >= delay) {
            lastFireTime = currentTime;
            return this.marioForm.fire(this.toRight, this.getX(), this.getY());
        }
        return null;
    }

    public void acquireCoin() {
        ++this.coins;
    }

    public void acquirePoints(int point) {
        this.points += point;
    }

    public int getRemainingLives() {
        return this.remainingLives;
    }

    public void setRemainingLives(int remainingLives) {
        this.remainingLives = remainingLives;
    }

    public int getPoints() {
        return this.points;
    }

    public int getCoins() {
        return this.coins;
    }

    public MarioForm getMarioForm() {
        return this.marioForm;
    }

    public void setMarioForm(MarioForm marioForm) {
        this.marioForm = marioForm;
    }

    public boolean isSuper() {
        return this.marioForm.isSuper();
    }

    public boolean getToRight() {
        return this.toRight;
    }

    public void resetLocation() {
        this.setVelX(0.0);
        this.setVelY(0.0);
        this.setX(50.0);
        this.setJumping(false);
        this.setFalling(true);
    }
}
