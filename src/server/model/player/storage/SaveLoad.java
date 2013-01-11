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

import java.io.File;

import server.model.Position;
import server.model.player.Player;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Static utility methods for saving and loading players.
 * 
 * @author blakeman8192
 * @author Stuart Murphy
 */
public class SaveLoad {
	
	/**
	 * Jackson mapper, maps JSON data into java objects
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The directory where players are saved. */
	public static final String directory = "./data/characters/";

	/**
	 * Saves the player.
	 * 
	 * @param player
	 *            the player to save
	 * @return
	 */
	public static void save(Player player) throws Exception {
		Details details = new Details();
		
		details.setPassword(player.getPassword());
		details.setRights(player.getStaffRights());
		details.setLocation(new Location(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ()));
		details.setGender(player.getGender());
		details.setAppearance(player.getAppearance());
		details.setColors(player.getColors());
		details.setBrightness(player.getBrightness());
		details.setMouseButtons(player.mouseButtons());
		details.setSplitScreen(player.splitScreen());
		details.setAcceptAid(player.acceptAid());
		details.setRetaliate(player.retaliate());
		details.setChatEffects(player.chatEffects());
		details.setPublicChat(player.getPublicChat());
		details.setPrivateChat(player.getPrivateChat());
		details.setTradeCompete(player.getTradeCompete());
		details.setRunning(player.getMovementHandler().isRunToggled());
		details.setFriends(player.getFriends());
		details.setIgnores(player.getIgnores());
		
		details.setSkills(new Skill[player.getSkills().length]);
		for (int i = 0; i < player.getSkills().length; i++) {
			details.getSkills()[i] = new Skill(player.getSkills()[i], player.getExperience()[i]);
		}
		
		details.setInventory(new ItemContainer[player.getInventory().length]);
		for (int i = 0; i < player.getInventory().length; i++) {
			details.getInventory()[i] = new ItemContainer(player.getInventory()[i], player.getInventoryN()[i]);
		}
		
		details.setEquipment( new ItemContainer[player.getEquipment().length]);
		for (int i = 0; i < player.getEquipment().length; i++) {
			details.getEquipment()[i] = new ItemContainer(player.getEquipment()[i], player.getEquipmentN()[i]);
		}
	
		mapper.writeValue(new File(directory + player.getUsername() + ".json"), details);
	}

	/**
	 * Loads the player (and sets the loaded attributes).
	 * 
	 * @param player
	 *            the player to load.
	 * @return 0 for success, 1 if the player does not have a saved game, 2 for
	 *         invalid username/password
	 */
	public static int load(Player player) throws Exception {
		File file = new File(directory + player.getUsername() + ".json");
		if (!file.exists()) {
			return 1;
		}
		
		Details details = mapper.readValue(file, Details.class);
		
		if(!player.getPassword().equalsIgnoreCase(details.getPassword())) {
			return 1;
		}
	
		player.setStaffRights(details.getRights());
		player.setPosition(new Position(details.getLocation().getX(), details.getLocation().getY(), details.getLocation().getZ()));
		player.setGender(details.getGender());
		player.setBrightness(details.getBrightness());
		player.setMouseButtons(details.isMouseButtons());
		player.setSplitScreen(details.isSplitScreen());
		player.setAcceptAid(details.isAcceptAid());
		player.setRetaliate(details.isRetaliate());
		player.setChatEffects(details.isChatEffects());
		player.setPublicChat(details.getPublicChat());
		player.setPrivateChat(details.getPrivateChat());
		player.setTradeCompete(details.getTradeCompete());
		player.getMovementHandler().setRunToggled(details.isRunning());
		
		for(int i = 0; i < player.getAppearance().length; i++) {
			player.getAppearance()[i] = details.getAppearance()[i];
		}
		
		for(int i = 0; i < player.getColors().length; i++) {
			player.getColors()[i] = details.getColors()[i];
		}
	
		for(int i = 0; i < player.getSkills().length; i++) {
			player.getSkills()[i] = details.getSkills()[i].getLevel();
			player.getExperience()[i] = details.getSkills()[i].getXp();
		}
		
		for(int i = 0; i < player.getInventory().length; i++) {
			player.getInventory()[i] = details.getInventory()[i].getItem();
			player.getInventoryN()[i] = details.getInventory()[i].getAmount();
		}
		
		for(int i = 0; i < player.getEquipment().length; i++) {
			player.getEquipment()[i] = details.getEquipment()[i].getItem();
			player.getEquipment()[i] = details.getEquipment()[i].getAmount();
		}
		
		for(int i = 0; i < player.getFriends().length; i++) {
			player.getFriends()[i] = details.getFriends()[i];
		}
		
		for(int i = 0; i < player.getIgnores().length; i++) {
			player.getIgnores()[i] = details.getIgnores()[i];
		}
		
		return 0;
	}
	

}
