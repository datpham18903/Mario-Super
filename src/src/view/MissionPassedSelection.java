package view;

public enum MissionPassedSelection {
	START_SCREEN(0), NEXT_MAP(1);

	private final int lineNumber;
	MissionPassedSelection(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public MissionPassedSelection getSelection(int number) {
		if (number == 0)
			return START_SCREEN;
		else if (number == 1)
			return NEXT_MAP;
		else
			return null;
	}

	public MissionPassedSelection select(boolean toUp) {
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
