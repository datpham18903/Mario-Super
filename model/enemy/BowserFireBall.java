package model.enemy;

import java.awt.image.BufferedImage;

public class BowserFireBall extends Enemy {

	public BowserFireBall(double x, double y, BufferedImage style, boolean toRight) {
		super(x, y, style);
		setDimension(24, 24);
		setFalling(false);
		setJumping(false);
		setVelX(10);

		if (!toRight)
			setVelX(-10);
	}
}