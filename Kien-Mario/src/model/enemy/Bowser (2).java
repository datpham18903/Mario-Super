package model.enemy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Timer;
import java.util.TimerTask;

public class Bowser extends Enemy {
    private BufferedImage rightImage;
    private int remainingLives;
    private Timer timer = new Timer();
    private boolean isAttacking = false;
    private boolean isInvincible = false;

    public Bowser(double x, double y, BufferedImage style) {
        super(x, y, style);
        this.scheduleAttack();
        this.setVelX(3.0);
        this.remainingLives = 10;
    }

    public void draw(Graphics g) {
        if (!isInvincible || System.currentTimeMillis() % 100 < 50) {
            // Draw Bowser only if not invincible or during the visible half of the invincible period
            if (this.getVelX() > 0.0) {
                g.drawImage(this.rightImage, (int) this.getX(), (int) this.getY(), (ImageObserver) null);
            } else {
                super.draw(g);
            }
        }

        // Visualize Bowser hitbox
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.RED);
        Rectangle bounds = getBounds();
        g2d.draw(bounds);
    }

    public void setRightImage(BufferedImage rightImage) {
        this.rightImage = rightImage;
    }

    public int getRemainingLives() {
        return this.remainingLives;
    }

    public void reduceLives() {
        if (!isInvincible) {
            --this.remainingLives;
            // Set Bowser invincible for a short period after being hit
            isInvincible = true;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isInvincible = false;
                }
            }, 100); // Set invincibility time to 100 milliseconds
        }
    }

    private void scheduleAttack() {
        this.timer.schedule(new TimerTask() {
            public void run() {
                if (!Bowser.this.isAttacking) {
                    Bowser.this.setVelX(Bowser.this.getVelX() * 2.0);
                    Bowser.this.isAttacking = true;
                    Bowser.this.timer.schedule(new TimerTask() {
                        public void run() {
                            Bowser.this.setVelX(Bowser.this.getVelX() / 2.0);
                            Bowser.this.isAttacking = false;
                            Bowser.this.scheduleAttack();
                        }
                    }, 2000L);
                }
            }
        }, 10000L);
    }


}
