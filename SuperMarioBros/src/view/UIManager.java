package view;

import manager.GameEngine;
import manager.GameStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class UIManager extends JPanel {

	private GameEngine engine;
	private Font gameFont;
	private BufferedImage startScreenImage, aboutScreenImage, helpScreenImage;
	private BufferedImage heartIcon;
	private BufferedImage coinIcon;
	private BufferedImage selectIcon;
	private MapSelection mapSelection;

	public UIManager(GameEngine engine, int width, int height) {
		setPreferredSize(new Dimension(width, height));
		setMaximumSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));

		this.engine = engine;
		ImageLoader loader = engine.getImageLoader();

		mapSelection = new MapSelection();

		BufferedImage sprite = loader.loadImage("/sprite.png");
		this.heartIcon = loader.loadImage("/heart-icon.png");
		this.coinIcon = loader.getSubImage(sprite, 1, 5, 48, 48);
		this.selectIcon = loader.loadImage("/select-icon.png");
		this.startScreenImage = loader.loadImage("/start-screen.png");
		this.helpScreenImage = loader.loadImage("/help-screen.png");
		this.aboutScreenImage = loader.loadImage("/about-screen.png");

		try {
			InputStream in = getClass()
					.getResourceAsStream("/media/font/mario-font.ttf");
			gameFont = Font.createFont(Font.TRUETYPE_FONT, in);
		} catch (FontFormatException | IOException e) {
			gameFont = new Font("Verdana", Font.PLAIN, 12);
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g.create();
		GameStatus gameStatus = engine.getGameStatus();

		if (gameStatus == GameStatus.START_SCREEN) {
			drawStartScreen(g2);
		} else if (gameStatus == GameStatus.MAP_SELECTION) {
			drawMapSelectionScreen(g2);
		} else if (gameStatus == GameStatus.ABOUT_SCREEN) {
			drawAboutScreen(g2);
		} else if (gameStatus == GameStatus.HELP_SCREEN) {
			drawHelpScreen(g2);
		} else if (gameStatus == GameStatus.GAME_OVER) {
			drawGameOverScreen(g2);
		} else {
			Point camLocation = engine.getCameraLocation();
			g2.translate(-camLocation.x, -camLocation.y);
			engine.drawMap(g2);
			g2.translate(camLocation.x, camLocation.y);

			drawPoints(g2);
			drawRemainingLives(g2);
			drawAcquiredCoins(g2);
			drawRemainingTime(g2);

			if (gameStatus == GameStatus.PAUSED) {
				drawPauseScreen(g2);
			} else if (gameStatus == GameStatus.MISSION_PASSED) {
				if (engine.getSelectedMap() < mapSelection.getMapSize() - 1) {
					drawMissionPassedScreen(g2);
				} else {
					engine.setGameStatus(GameStatus.VICTORY);
				}
			} else if (gameStatus == GameStatus.VICTORY) {
				drawVictoryScreen(g2);
			}
		}

		g2.dispose();
	}

	private void drawRemainingTime(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(25f));
		g2.setColor(Color.WHITE);
		String displayedStr = "TIME: " + engine.getRemainingTime();
		g2.drawString(displayedStr, 750, 50);
	}

	private void drawVictoryScreen(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(50f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, 1280, 720);

		String title = "YOU WON";
		int x_location = (1280 - g2.getFontMetrics().stringWidth(title)) / 2;
		g2.setColor(Color.WHITE);
		g2.drawString(title, x_location, 150);

		String acquiredPoints = "Score: " + engine.getScore();
		int stringLength = g2.getFontMetrics().stringWidth(acquiredPoints);
		int stringHeight = g2.getFontMetrics().getHeight();
		g2.drawString(acquiredPoints, (getWidth() - stringLength) / 2,
				getHeight() - stringHeight * 2);

		int width = g2.getFontMetrics().stringWidth("HOME");
		g2.drawString("HOME", (1280 - width) / 2, 300);
		int y_location = 300 - selectIcon.getHeight();
		g2.drawImage(selectIcon, 375, y_location, null);
	}

	private void drawMissionPassedScreen(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(50f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, 1280, 720);

		String title = "MISSION PASSED!";
		int x_location = (1280 - g2.getFontMetrics().stringWidth(title)) / 2;
		g2.setColor(Color.YELLOW);
		g2.drawString(title, x_location, 150);

		String acquiredPoints = "Score: " + engine.getScore();
		int stringLength = g2.getFontMetrics().stringWidth(acquiredPoints);
		int stringHeight = g2.getFontMetrics().getHeight();
		g2.drawString(acquiredPoints, (getWidth() - stringLength) / 2,
				getHeight() - stringHeight * 2);

		g2.setColor(Color.WHITE);
		int width = g2.getFontMetrics().stringWidth("Home");
		g2.drawString("Home", (1280 - width) / 2, 1 * 100 + 200);
		width = g2.getFontMetrics().stringWidth("Next Level");
		g2.drawString("Next Level", (1280 - width) / 2, 2 * 100 + 200);

		int row = engine.getMissionPassedSelection().getLineNumber();
		int y_location = row * 100 + 300 - selectIcon.getHeight();
		g2.drawImage(selectIcon, 335, y_location, null);
	}

	private void drawHelpScreen(Graphics2D g2) {
		g2.drawImage(helpScreenImage, 0, 0, null);
	}

	private void drawAboutScreen(Graphics2D g2) {
		g2.drawImage(aboutScreenImage, 0, 0, null);
	}

	private void drawGameOverScreen(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(50f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, 1280, 720);

		String title = "GAME OVER";
		int x_location = (1280 - g2.getFontMetrics().stringWidth(title)) / 2;
		g2.setColor(Color.YELLOW);
		g2.drawString(title, x_location, 150);

		String acquiredPoints = "Score: " + engine.getScore();
		int stringLength = g2.getFontMetrics().stringWidth(acquiredPoints);
		int stringHeight = g2.getFontMetrics().getHeight();
		g2.drawString(acquiredPoints, (getWidth() - stringLength) / 2,
				getHeight() - stringHeight * 2);

		g2.setColor(Color.WHITE);
		int width = g2.getFontMetrics().stringWidth("Restart");
		g2.drawString("Restart", (1280 - width) / 2, 1 * 100 + 200);
		width = g2.getFontMetrics().stringWidth("Home");
		g2.drawString("Home", (1280 - width) / 2, 2 * 100 + 200);

		int row = engine.getGameOverMenu().getLineNumber();
		int y_location = row * 100 + 300 - selectIcon.getHeight();
		g2.drawImage(selectIcon, 375, y_location, null);
	}

	private void drawPauseScreen(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(50f));
		g2.setColor(Color.WHITE);
		String displayedStr = "PAUSED";
		int stringLength = g2.getFontMetrics().stringWidth(displayedStr);
		g2.drawString(displayedStr, (getWidth() - stringLength) / 2,
				getHeight() / 2);
	}

	private void drawAcquiredCoins(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(30f));
		g2.setColor(Color.WHITE);
		String displayedStr = "" + engine.getCoins();
		g2.drawImage(coinIcon, getWidth() - 115, 10, null);
		g2.drawString(displayedStr, getWidth() - 65, 50);
	}

	private void drawRemainingLives(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(30f));
		g2.setColor(Color.WHITE);
		String displayedStr = "" + engine.getRemainingLives();
		g2.drawImage(heartIcon, 50, 10, null);
		g2.drawString(displayedStr, 100, 50);
	}

	private void drawPoints(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(25f));
		g2.setColor(Color.WHITE);
		String displayedStr = "Points: " + engine.getScore();
		int stringLength = g2.getFontMetrics().stringWidth(displayedStr);;
		// g2.drawImage(coinIcon, 50, 10, null);
		g2.drawString(displayedStr, 300, 50);
	}

	private void drawStartScreen(Graphics2D g2) {
		int row = engine.getStartScreenSelection().getLineNumber();
		g2.drawImage(startScreenImage, 0, 0, null);
		g2.drawImage(selectIcon, 375, row * 70 + 440, null);
	}

	private void drawMapSelectionScreen(Graphics2D g2) {
		g2.setFont(gameFont.deriveFont(50f));
		g2.setColor(Color.WHITE);
		mapSelection.draw(g2);
		int row = engine.getSelectedMap();
		int y_location = row * 100 + 300 - selectIcon.getHeight();
		g2.drawImage(selectIcon, 375, y_location, null);
	}

	public String selectMapViaMouse(Point mouseLocation) {
		return mapSelection.selectMap(mouseLocation);
	}

	public String selectMapViaKeyboard(int index) {
		return mapSelection.selectMap(index);
	}

	public int changeSelectedMap(int index, boolean up) {
		return mapSelection.changeSelectedMap(index, up);
	}
}