package view;

public enum GameOverMenu {
	START_GAME(0), START_SCREEN(1);

	private final int lineNumber;
	GameOverMenu(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public GameOverMenu getSelection(int number) {
		if (number == 0)
			return START_GAME;
		else if (number == 1)
			return START_SCREEN;
		else
			return null;
	}

	public GameOverMenu select(boolean toUp) {
		int selection;

		if (lineNumber > -1 && lineNumber < 2) {
			selection = lineNumber - (toUp ? 1 : -1);
			if (selection == -1)
				selection = 1;
			else if (selection == 2)
				selection = 0;
			return getSelection(selection);
		}

		return null;
	}

	public int getLineNumber() {
		return lineNumber;
	}
}
