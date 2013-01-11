package server.model.player.storage;

public class Details {

	private String password;
	private int rights;
	private Location location;
	private int gender;
	private int[] appearance;
	private int[] colors;
	private Skill[] skills;
	private ItemContainer[] inventory;
	private ItemContainer[] equipment;
	
	public Details() {
		
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getRights() {
		return rights;
	}

	public void setRights(int rights) {
		this.rights = rights;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int[] getAppearance() {
		return appearance;
	}

	public void setAppearance(int[] appearance) {
		this.appearance = appearance;
	}

	public int[] getColors() {
		return colors;
	}

	public void setColors(int[] colors) {
		this.colors = colors;
	}

	public Skill[] getSkills() {
		return skills;
	}

	public void setSkills(Skill[] skills) {
		this.skills = skills;
	}

	public ItemContainer[] getInventory() {
		return inventory;
	}

	public void setInventory(ItemContainer[] inventory) {
		this.inventory = inventory;
	}

	public ItemContainer[] getEquipment() {
		return equipment;
	}

	public void setEquipment(ItemContainer[] equipment) {
		this.equipment = equipment;
	}

}