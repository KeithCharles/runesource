package server.model.player.storage;

public class ItemContainer {

	private int slot;
	private int item;
	private int amount;
	
	public ItemContainer() {
		
	}
	
	public ItemContainer(int slot, int item, int amount) {
		this.setSlot(slot);
		this.setItem(item);
		this.setAmount(amount);
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public int getItem() {
		return item;
	}

	public void setItem(int item) {
		this.item = item;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
}
