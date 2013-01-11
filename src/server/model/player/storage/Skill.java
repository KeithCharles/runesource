package server.model.player.storage;

public class Skill {
	
	private int level;
	private int xp;
	
	public Skill() {
		
	}
	
	public Skill(int level, int xp) {
		this.setLevel(level);
		this.setXp(xp);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

}
