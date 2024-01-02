
package manager;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import model.GameObject;
import model.Map;
import model.brick.Brick;
import model.brick.OrdinaryBrick;
import model.enemy.Bowser;
import model.enemy.BowserFireBall;
import model.enemy.Enemy;
import model.hero.Fireball;
import model.hero.Mario;
import model.prize.BoostItem;
import model.prize.Coin;
import model.prize.Prize;
import view.ImageLoader;
import java.awt.Color;

public class MapManager {
	private Map map;

	public MapManager() {
	}

	public void updateLocations() {
		if (this.map != null) {
			this.map.updateLocations();
		}

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
		this.map = mapCreator.createMap("/maps/" + path, 400.0);
		return this.map != null;
	}
	public boolean createMap(ImageLoader loader, String path, Mario mario) {
		boolean result = this.createMap(loader, path);
		if (result) {
			mario.resetLocation();
			map.setMario(mario);
		}
		return result;
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
		return this.getMario().getRemainingLives() == 0
				|| this.map.isTimeOver();
	}

	public int getScore() {
		return this.getMario().getPoints();
	}

	public int getRemainingLives() {
		return this.getMario().getRemainingLives();
	}

	public int getCoins() {
		return this.getMario().getCoins();
	}

	public void drawMap(Graphics2D g2) {
		ArrayList<GameObject> addObjects = new ArrayList();
		this.map.drawMap(g2);
		// Draw Bowser's health bar
		for (Enemy enemy : map.getEnemies()) {
			if (enemy instanceof Bowser) {
				Bowser bowser = (Bowser) enemy;
				drawHealthBar(g2, bowser);
				if ((int)(bowser.getVelX()) == 0) {
					BowserFireBall fireball = bowser.fire();
					if (fireball != null) {
						addObjects.add(fireball);
					}
				}
			}
		}
		this.addObjects(addObjects);
	}

	private void drawHealthBar(Graphics2D g2, Bowser bowser) {
		int barWidth = 150;
		int barHeight = 10;
		int x = (int) bowser.getX();
		int y = (int) bowser.getY() - 10; // Position the health bar 20 pixels
											// above Bowser
		float healthPercentage = (float) bowser.getRemainingLives()
				/ bowser.getMaxLives();

		g2.setColor(Color.GRAY);
		g2.fillRect(x, y, barWidth, barHeight);
		g2.setColor(Color.RED);
		g2.fillRect(x, y, (int) (barWidth * healthPercentage), barHeight);
	}

	public int passMission() {
		if (this.getMario().getX() >= this.map.getEndPoint().getX()
				&& !this.map.getEndPoint().isTouched()) {
			if (this.map.isBowserExist()) {
				return -1; // Mario cannot pass the mission if Bowser still
							// exists
			} else {
				this.map.getEndPoint().setTouched(true);
				int height = (int) this.getMario().getY();
				return height * 2;
			}
		} else {
			return -1;
		}
	}

	public boolean endLevel() {
		if (!this.map.isBowserExist()) {
			return this.getMario().getX() >= this.map.getEndPoint().getX()
					+ 320.0;
		}
		return false;
	}

	public void checkCollisions(GameEngine engine) {
		if (this.map != null) {
			this.checkBottomCollisions(engine);
			this.checkTopCollisions(engine);
			this.checkMarioHorizontalCollision(engine);
			this.checkEnemyCollisions(engine);
			this.checkPrizeCollision();
			this.checkPrizeContact(engine);
			this.checkFireballContact();
		}

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

		Iterator var7 = bricks.iterator();

		Rectangle enemyTopBounds;
		while (var7.hasNext()) {
			Brick brick = (Brick) var7.next();
			enemyTopBounds = brick.getTopBounds();
			if (marioBottomBounds.intersects(enemyTopBounds)) {
				mario.setY(brick.getY() - (double) mario.getDimension().height
						+ 1.0);
				mario.setFalling(false);
				mario.setVelY(0.0);
			}
		}

		var7 = enemies.iterator();

		while (var7.hasNext()) {
			Enemy enemy = (Enemy) var7.next();
			enemyTopBounds = enemy.getTopBounds();
			if (marioBottomBounds.intersects(enemyTopBounds)) {
				mario.acquirePoints(100);
				toBeRemoved.add(enemy);
				engine.playStomp();
			}
		}

		if (mario.getY() + (double) mario.getDimension().height >= this.map
				.getBottomBorder()) {
			mario.setY(this.map.getBottomBorder()
					- (double) mario.getDimension().height);
			mario.setFalling(false);
			mario.setVelY(0.0);
		}

		this.removeObjects(toBeRemoved);
	}

	private void checkTopCollisions(GameEngine engine) {
		Mario mario = this.getMario();
		ArrayList<Brick> bricks = this.map.getAllBricks();
		Rectangle marioTopBounds = mario.getTopBounds();
		Iterator var5 = bricks.iterator();

		while (var5.hasNext()) {
			Brick brick = (Brick) var5.next();
			Rectangle brickBottomBounds = brick.getBottomBounds();
			if (marioTopBounds.intersects(brickBottomBounds)) {
				mario.setVelY(0.0);
				mario.setY(brick.getY() + (double) brick.getDimension().height);
				Prize prize = brick.reveal(engine);
				if (prize != null) {
					this.map.addRevealedPrize(prize);
				}
			}
		}

	}

	private void checkMarioHorizontalCollision(GameEngine engine) {
		Mario mario = this.getMario();
		ArrayList<Brick> bricks = this.map.getAllBricks();
		ArrayList<Enemy> enemies = this.map.getEnemies();
		ArrayList<GameObject> toBeRemoved = new ArrayList();
		boolean marioDies = false;
		boolean toRight = mario.getToRight();
		Rectangle marioBounds = toRight
				? mario.getRightBounds()
				: mario.getLeftBounds();
		Iterator var9 = bricks.iterator();

		Rectangle enemyBounds;
		while (var9.hasNext()) {
			Brick brick = (Brick) var9.next();
			enemyBounds = !toRight
					? brick.getRightBounds()
					: brick.getLeftBounds();
			if (marioBounds.intersects(enemyBounds)) {
				mario.setVelX(0.0);
				if (toRight) {
					mario.setX(
							brick.getX() - (double) mario.getDimension().width);
				} else {
					mario.setX(
							brick.getX() + (double) brick.getDimension().width);
				}
			}
		}

		var9 = enemies.iterator();

		while (var9.hasNext()) {
			Enemy enemy = (Enemy) var9.next();
			enemyBounds = !toRight
					? enemy.getRightBounds()
					: enemy.getLeftBounds();
			if (marioBounds.intersects(enemyBounds)) {
				marioDies = mario.onTouchEnemy(engine, enemy);
				toBeRemoved.add(enemy);
			}
		}

		this.removeObjects(toBeRemoved);
		if (mario.getX() <= engine.getCameraLocation().getX()
				&& mario.getVelX() < 0.0) {
			mario.setVelX(0.0);
			mario.setX(engine.getCameraLocation().getX());
		}

		if (marioDies) {
			this.resetCurrentMap(engine);
		}

	}

	private void checkEnemyCollisions(GameEngine engine) {
		ArrayList<Brick> bricks = this.map.getAllBricks();
		ArrayList<Enemy> enemies = this.map.getEnemies();
		ArrayList<GameObject> toBeRemoved = new ArrayList();
		Mario mario = this.getMario();
		Rectangle marioBounds = mario.getBounds();
		Iterator<Enemy> var7 = enemies.iterator();

		while (true) {
			Enemy enemy;
			do {
				if (!var7.hasNext()) {
					this.removeObjects(toBeRemoved);
					return;
				}
				enemy = var7.next();

				boolean standsOnBrick = false;
				Rectangle enemyBounds = enemy.getBounds();
				if (marioBounds.intersects(enemyBounds)) {
					boolean marioDies = mario.onTouchEnemy(engine, enemy);
					toBeRemoved.add(enemy);
					if (marioDies) {
						this.resetCurrentMap(engine);
						return;
					}
				}

				Iterator var17 = bricks.iterator();

				while (var17.hasNext()) {
					Brick brick = (Brick) var17.next();
					Rectangle brickBounds = brick.getRightBounds();
					Rectangle enemyBottomBounds = enemy.getBottomBounds();
					Rectangle brickTopBounds = brick.getTopBounds();
					if (enemy.getVelX() > 0.0) {
						enemyBounds = enemy.getRightBounds();
						brickBounds = brick.getLeftBounds();
					}

					if (enemyBounds.intersects(brickBounds)) {
						if (enemy instanceof BowserFireBall) {
							toBeRemoved.add(enemy);
						} else {
							enemy.setVelX(-enemy.getVelX());
							if (enemy instanceof Bowser) {
								((Bowser) enemy).prepareAttack();
							}
						}
					}

					if (enemyBottomBounds.intersects(brickTopBounds)) {
						enemy.setFalling(false);
						enemy.setVelY(0.0);
						enemy.setY(brick.getY()
								- (double) enemy.getDimension().height);
						standsOnBrick = true;
					}
				}

				if (enemy.getY()
						+ (double) enemy.getDimension().height > this.map
								.getBottomBorder()) {
					enemy.setFalling(false);
					enemy.setVelY(0.0);
					enemy.setY(this.map.getBottomBorder()
							- (double) enemy.getDimension().height);
				}

				if (!standsOnBrick && enemy.getY() < this.map.getBottomBorder()
						&& !(enemy instanceof BowserFireBall)) {
					enemy.setFalling(true);
				}
			} while (!(enemy instanceof Bowser));

			Bowser bowser = (Bowser) enemy;
			Rectangle bowserBounds = bowser.getBounds();
			if (marioBounds.intersects(bowserBounds)) {
				this.handleBowserCollision(bowser);
			}

			ArrayList<Fireball> fireballs = this.map.getFireballs();
			Iterator var21 = fireballs.iterator();

			while (var21.hasNext()) {
				Fireball fireball = (Fireball) var21.next();
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
			this.getMario().acquirePoints(1000);
			this.map.removeEnemy(bowser);
		}

	}

	private void checkPrizeCollision() {
		ArrayList<Prize> prizes = this.map.getRevealedPrizes();
		ArrayList<Brick> bricks = this.map.getAllBricks();
		Iterator var3 = prizes.iterator();

		while (true) {
			Prize prize;
			do {
				if (!var3.hasNext()) {
					return;
				}

				prize = (Prize) var3.next();
			} while (!(prize instanceof BoostItem));

			BoostItem boost = (BoostItem) prize;
			Rectangle prizeBottomBounds = boost.getBottomBounds();
			Rectangle prizeRightBounds = boost.getRightBounds();
			Rectangle prizeLeftBounds = boost.getLeftBounds();
			boost.setFalling(true);
			Iterator var9 = bricks.iterator();

			while (var9.hasNext()) {
				Brick brick = (Brick) var9.next();
				Rectangle brickBounds;
				if (boost.isFalling()) {
					brickBounds = brick.getTopBounds();
					if (brickBounds.intersects(prizeBottomBounds)) {
						boost.setFalling(false);
						boost.setVelY(0.0);
						boost.setY(brick.getY()
								- (double) boost.getDimension().height + 1.0);
						if (boost.getVelX() == 0.0) {
							boost.setVelX(2.0);
						}
					}
				}

				if (boost.getVelX() > 0.0) {
					brickBounds = brick.getLeftBounds();
					if (brickBounds.intersects(prizeRightBounds)) {
						boost.setVelX(-boost.getVelX());
					}
				} else if (boost.getVelX() < 0.0) {
					brickBounds = brick.getRightBounds();
					if (brickBounds.intersects(prizeLeftBounds)) {
						boost.setVelX(-boost.getVelX());
					}
				}
			}

			if (boost.getY() + (double) boost.getDimension().height > this.map
					.getBottomBorder()) {
				boost.setFalling(false);
				boost.setVelY(0.0);
				boost.setY(this.map.getBottomBorder()
						- (double) boost.getDimension().height);
				if (boost.getVelX() == 0.0) {
					boost.setVelX(2.0);
				}
			}
		}
	}

	private void checkPrizeContact(GameEngine engine) {
		ArrayList<Prize> prizes = this.map.getRevealedPrizes();
		ArrayList<GameObject> toBeRemoved = new ArrayList();
		Rectangle marioBounds = this.getMario().getBounds();
		Iterator var5 = prizes.iterator();

		while (var5.hasNext()) {
			Prize prize = (Prize) var5.next();
			Rectangle prizeBounds = prize.getBounds();
			if (prizeBounds.intersects(marioBounds)) {
				prize.onTouch(this.getMario(), engine);
				toBeRemoved.add((GameObject) prize);
			} else if (prize instanceof Coin) {
				prize.onTouch(this.getMario(), engine);
			}
		}

		this.removeObjects(toBeRemoved);
	}

	private void checkFireballContact() {
		ArrayList<Fireball> fireballs = this.map.getFireballs();
		ArrayList<Enemy> enemies = this.map.getEnemies();
		ArrayList<Brick> bricks = this.map.getAllBricks();
		ArrayList<GameObject> toBeRemoved = new ArrayList();
		Iterator var5 = fireballs.iterator();

		while (var5.hasNext()) {
			Fireball fireball = (Fireball) var5.next();
			Rectangle fireballBounds = fireball.getBounds();
			Iterator var8 = enemies.iterator();

			Rectangle brickBounds;
			while (var8.hasNext()) {
				Enemy enemy = (Enemy) var8.next();
				brickBounds = enemy.getBounds();
				if (fireballBounds.intersects(brickBounds)) {
					this.acquirePoints(100);
					toBeRemoved.add(enemy);
					toBeRemoved.add(fireball);
				}
			}

			var8 = bricks.iterator();

			while (var8.hasNext()) {
				Brick brick = (Brick) var8.next();
				brickBounds = brick.getBounds();
				if (fireballBounds.intersects(brickBounds)) {
					toBeRemoved.add(fireball);
				}
			}
		}

		this.removeObjects(toBeRemoved);
	}

	private void removeObjects(ArrayList<GameObject> list) {
		if (list != null) {
			Iterator var2 = list.iterator();

			while (true) {
				while (var2.hasNext()) {
					GameObject object = (GameObject) var2.next();
					if (object instanceof Fireball) {
						this.map.removeFireball((Fireball) object);
					} else if (object instanceof Enemy) {
						this.map.removeEnemy((Enemy) object);
					} else if (object instanceof Coin
							|| object instanceof BoostItem) {
						this.map.removePrize((Prize) object);
					}
				}

				return;
			}
		}
	}

	private void addObjects(ArrayList<GameObject> list) {
		if (list != null) {
			Iterator<GameObject> objects = list.iterator();

			while (true) {
				while (objects.hasNext()) {
					GameObject object = objects.next();
					if (object instanceof Enemy) {
						this.map.addEnemy((Enemy) object);
					}
				}
				return;
			}
		}
	}

	public void addRevealedBrick(OrdinaryBrick ordinaryBrick) {
		this.map.addRevealedBrick(ordinaryBrick);
	}

	public void updateTime() {
		if (this.map != null) {
			this.map.updateTime(1.0);
		}

	}

	public int getRemainingTime() {
		return (int) this.map.getRemainingTime();
	}
}
