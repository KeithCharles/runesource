package server.model.player.storage;

/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

//@JsonIgnoreProperties
//@JsonIgnoreProperties(ignoreUnknown = true)
public class Details {

	private String password;
	private int rights;
	private Location location;
	private int gender;
	private int[] appearance;
	private int[] colors;
	private Skill[] skills;
	private long[] friends;
	private long[] ignores;
	private ItemContainer[] inventory;
	private ItemContainer[] equipment;
	
	private byte brightness = 1;
	private boolean mouseButtons;
	private boolean splitScreen = false;
	private boolean acceptAid = false;
	private boolean retaliate = false;
	private boolean chatEffects = false;
	private byte publicChat = 0;
	private byte privateChat = 0;
	private byte tradeCompete = 0;
	private boolean running;
	
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

	public byte getBrightness() {
		return brightness;
	}

	public void setBrightness(byte brightness) {
		this.brightness = brightness;
	}

	public boolean isMouseButtons() {
		return mouseButtons;
	}

	public void setMouseButtons(boolean mouseButtons) {
		this.mouseButtons = mouseButtons;
	}

	public boolean isSplitScreen() {
		return splitScreen;
	}

	public void setSplitScreen(boolean splitScreen) {
		this.splitScreen = splitScreen;
	}

	public boolean isAcceptAid() {
		return acceptAid;
	}

	public void setAcceptAid(boolean acceptAid) {
		this.acceptAid = acceptAid;
	}

	public boolean isRetaliate() {
		return retaliate;
	}

	public void setRetaliate(boolean retaliate) {
		this.retaliate = retaliate;
	}

	public boolean isChatEffects() {
		return chatEffects;
	}

	public void setChatEffects(boolean chatEffects) {
		this.chatEffects = chatEffects;
	}

	public byte getPublicChat() {
		return publicChat;
	}

	public void setPublicChat(byte pubicChat) {
		this.publicChat = pubicChat;
	}

	public byte getPrivateChat() {
		return privateChat;
	}

	public void setPrivateChat(byte privateChat) {
		this.privateChat = privateChat;
	}

	public byte getTradeCompete() {
		return tradeCompete;
	}

	public void setTradeCompete(byte tradeCompete) {
		this.tradeCompete = tradeCompete;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public long[] getFriends() {
		return friends;
	}

	public void setFriends(long[] friends) {
		this.friends = friends;
	}

	public long[] getIgnores() {
		return ignores;
	}

	public void setIgnores(long[] ignores) {
		this.ignores = ignores;
	}

}