package server.model.player.storage;

public class Skill {
	
	private int id;
	private int level;
	private int xp;
	
	public Skill() {
		
	}
	
	public Skill(int id, int level, int xp) {
		this.setId(id);
		this.setLevel(level);
		this.setXp(xp);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
