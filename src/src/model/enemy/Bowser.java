package model.enemy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Timer;
import java.util.TimerTask;

import view.ImageLoader;

public class Bowser extends Enemy {
	private BufferedImage rightImage;
	private BufferedImage atkImage;
	private int remainingLives;
	private Timer timer = new Timer();
	private boolean isInvincible = false;
	private int maxLives;
	private BufferedImage fireballStyle;
	private long lastFireTime = 0;

	public Bowser(double x, double y, BufferedImage style) {
		super(x, y, style);
		this.setVelX(5.0);
		this.maxLives = 10; // Set the maximum number of lives
		this.remainingLives = maxLives;
		ImageLoader imageLoader = new ImageLoader();
		BufferedImage fireball = imageLoader.loadImage("/sprite.png");
		fireballStyle = imageLoader.getSubImage(fireball, 3, 4, 24, 24);
	}

	public void draw(Graphics g) {
		if (!isInvincible || System.currentTimeMillis() % 100 < 50) {
			if (this.getVelX() > 0.0) {
				g.drawImage(this.rightImage, (int) this.getX(),
						(int) this.getY(), (ImageObserver) null);
			} else {
				super.draw(g);
			}
		}
	}
	public void setRightImage(BufferedImage rightImage) {
		this.rightImage = rightImage;
	}
	public void setAtkImage(BufferedImage atkImage) {
		this.atkImage = atkImage;
	}

	public int getRemainingLives() {
		return this.remainingLives;
	}
	public int getMaxLives() {
		return this.maxLives;
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

	public BowserFireBall fire() {
		long currentTime = System.currentTimeMillis();
		// Fire each 1second
		if (currentTime - lastFireTime >= 1000) {
			lastFireTime = currentTime;
			double x = Bowser.this.getX();
			x += Bowser.this.getVelX() > 0.0 ? this.getBounds().width : 0;
			return new BowserFireBall(x,
					Bowser.this.getY() + 96, fireballStyle,
					Bowser.this.getVelX() > 0.0);
		}
		return null;
	}
}
