package server.model.player.storage;

public class ItemContainer {

	private int item;
	private int amount;
	
	public ItemContainer() {
		
	}
	
	public ItemContainer(int item, int amount) {
		this.setItem(item);
		this.setAmount(amount);
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
