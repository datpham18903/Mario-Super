package model.enemy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Timer;
import java.util.TimerTask;

import view.ImageLoader;

public class Bowser extends Enemy {
	private BufferedImage rightImage;
	private BufferedImage atkImage, atkRightImage;
	private int remainingLives;
	private Timer timer = new Timer();
	private boolean isInvincible = false;
	private int maxLives;
	private BufferedImage fireballStyle;
	private long lastFireTime = 0;
	private boolean isAttacking = false;
	private boolean isRight = false;

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
			if (this.isAttacking) {
				if (this.isRight) {
					g.drawImage(this.atkRightImage, (int) this.getX() + 24,
							(int) this.getY(), (ImageObserver) null);
				} else {
					g.drawImage(this.atkImage, (int) this.getX() - 96,
							(int) this.getY(), (ImageObserver) null);
				}
			} else {
				if (this.isRight) {
					g.drawImage(this.rightImage, (int) this.getX(),
							(int) this.getY(), (ImageObserver) null);
				} else {
					super.draw(g);
				}
			}
		}
	}

	public void setRightImage(BufferedImage rightImage) {
		this.rightImage = rightImage;
	}

	public void setAtkImage(BufferedImage atkImage,
			BufferedImage atkRightImage) {
		this.atkImage = atkImage;
		this.atkRightImage = atkRightImage;
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
			this.isAttacking = true;
			this.timer.schedule(new TimerTask() {
				public void run() {
					Bowser.this.isAttacking = false;
				}
			}, 300L);
			lastFireTime = currentTime;
			double x = Bowser.this.getX();
			x += this.isRight ? this.getBounds().width : 0;
			return new BowserFireBall(x, this.getY() + 72, fireballStyle,
					this.isRight);
		}
		return null;
	}

	public void prepareAttack() {
		double velX = this.getVelX();
		if (velX == 0) {
			return;
		}
		if (velX > 0.0) {
			this.isRight = true;
		} else {
			this.isRight = false;
		}
		this.setVelX(0);
		this.timer.schedule(new TimerTask() {
			public void run() {
				Bowser.this.setVelX(velX);
			}
		}, 5000L);
	}
}
