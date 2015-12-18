package universalcoins.tile;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCBanditServerMessage;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;

public class TileBandit extends TileEntity implements IInventory {

	private ItemStack[] inventory = new ItemStack[3];
	public static final int itemCardSlot = 0;
	public static final int itemCoinSlot = 1;
	public static final int itemOutputSlot = 2;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
	public int coinSum = 0;
	public int spinFee = 1;
	public int fourMatchPayout = 0;
	public int fiveMatchPayout = 0;
	public boolean cardAvailable = false;
	public String customName = "";
	public String playerName = "";
	public EntityPlayer player;
	public boolean inUse = false;
	public int[] reelPos = { 0, 0, 0, 0, 0 };
	private int[] reelStops = { 0, 22, 44, 66, 88, 110, 132, 154, 176, 198 };

	public TileBandit() {
		super();
	}

	public void onButtonPressed(int buttonId) {
		if (buttonId == 0) {
			if (isCardAvailable()) {
				String account = inventory[itemCardSlot].getTagCompound().getString("Account");
				UniversalAccounts.getInstance().debitAccount(account, spinFee);
			} else {
				coinSum -= spinFee;
				coinSum = Math.max(coinSum, 0);
			}
			leverPull();
			checkCard();
		}
		if (buttonId == 1) {
			if (isCardAvailable() && inventory[itemCardSlot].getItem() == UniversalCoins.proxy.itemEnderCard) {
				String account = inventory[itemCardSlot].getTagCompound().getString("Account");
				UniversalAccounts.getInstance().creditAccount(account, coinSum);
				coinSum = 0;
			} else {
				fillOutputSlot();
			}
		}
		if (buttonId == 2) {
			checkMatch();
		}
	}

	public void checkMatch() {
		int matchCount = 0;
		for (int i = 0; i < reelStops.length; i++) {
			matchCount = 0;
			for (int j = 0; j < reelPos.length; j++) {
				if (reelStops[i] == reelPos[j]) {
					matchCount++;
				}
			}
			if (matchCount == 5) {
				coinSum += fiveMatchPayout;
				worldObj.playSound(xCoord, yCoord, zCoord, "universalcoins:winner", 1.0F, 1.0F, true);
			}
			if (matchCount == 4) {
				coinSum += fourMatchPayout;
				worldObj.playSound(xCoord, yCoord, zCoord, "universalcoins:winner", 1.0F, 1.0F, true);
			}
		}
	}

	public void playSound(int soundId) {
		if (soundId == 0) {
			worldObj.playSound(xCoord, yCoord, zCoord, "universalcoins:button", 0.4F, 1.0F, true);
		}
	}

	private void leverPull() {
		Random random = new Random();

		for (int i = 0; i < reelPos.length; i++) {
			int rnd = random.nextInt(reelStops.length);
			reelPos[i] = reelStops[rnd];
		}
	}

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
		inUse = false;
		updateTE();
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : UniversalCoins.proxy.blockBandit.getLocalizedName();
	}

	public void setInventoryName(String name) {
		customName = name;
	}

	public boolean isInventoryNameLocalized() {
		return false;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	private int getCoinType(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
	}

	public boolean isCardAvailable()
	{
		if(!worldObj.isRemote)
			checkCard();
		return cardAvailable;
	}

	public void checkCard() {
		if(worldObj.isRemote)
			return;

		cardAvailable = false;
		if (inventory[itemCardSlot] != null && inventory[itemCardSlot].hasTagCompound() && !worldObj.isRemote
				&& inventory[itemCardSlot].stackTagCompound.getString("Owner").equals(player.getPersistentID().toString())
				) {
			String account = inventory[itemCardSlot].getTagCompound().getString("Account");
			int accountBalance = UniversalAccounts.getInstance().getAccountBalance(account);
			if (accountBalance >= spinFee) {
				cardAvailable = true;
			}
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		boolean result = worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
		if(!result)
			entityplayer.closeScreen();
		return result;
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, button, shiftPressed));
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.func_148857_g());
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void sendServerUpdateMessage() {
		UniversalCoins.snw.sendToServer(
				new UCBanditServerMessage(xCoord, yCoord, zCoord, spinFee, fourMatchPayout, fiveMatchPayout));
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = inventory[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Inventory", itemList);
		tagCompound.setInteger("coinSum", Math.max(coinSum, 0));
		tagCompound.setInteger("spinFee", spinFee);
		tagCompound.setInteger("fourMatchPayout", fourMatchPayout);
		tagCompound.setInteger("fiveMatchPayout", fiveMatchPayout);
		tagCompound.setBoolean("cardAvailable", isCardAvailable());
		tagCompound.setString("customName", customName);
		tagCompound.setBoolean("inUse", inUse);
		tagCompound.setInteger("reelPos0", reelPos[0]);
		tagCompound.setInteger("reelPos1", reelPos[1]);
		tagCompound.setInteger("reelPos2", reelPos[2]);
		tagCompound.setInteger("reelPos3", reelPos[3]);
		tagCompound.setInteger("reelPos4", reelPos[4]);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
		try {
			coinSum = Math.max(tagCompound.getInteger("coinSum"),0);
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			spinFee = tagCompound.getInteger("spinFee");
		} catch (Throwable ex2) {
			spinFee = 1;
		}
		try {
			fourMatchPayout = tagCompound.getInteger("fourMatchPayout");
		} catch (Throwable ex2) {
			fourMatchPayout = UniversalCoins.fourMatchPayout;
		}
		try {
			fiveMatchPayout = tagCompound.getInteger("fiveMatchPayout");
		} catch (Throwable ex2) {
			fiveMatchPayout = UniversalCoins.fiveMatchPayout;
		}
		try {
			cardAvailable = tagCompound.getBoolean("cardAvailable");
		} catch (Throwable ex2) {
			cardAvailable = false;
		}
		try {
			customName = tagCompound.getString("customName");
		} catch (Throwable ex2) {
			customName = "";
		}
		try {
			reelPos[0] = tagCompound.getInteger("reelPos0");
		} catch (Throwable ex2) {
			reelPos[0] = 0;
		}
		try {
			reelPos[1] = tagCompound.getInteger("reelPos1");
		} catch (Throwable ex2) {
			reelPos[1] = 0;
		}
		try {
			reelPos[2] = tagCompound.getInteger("reelPos2");
		} catch (Throwable ex2) {
			reelPos[2] = 0;
		}
		try {
			reelPos[3] = tagCompound.getInteger("reelPos3");
		} catch (Throwable ex2) {
			reelPos[3] = 0;
		}
		try {
			reelPos[4] = tagCompound.getInteger("reelPos4");
		} catch (Throwable ex2) {
			reelPos[4] = 0;
		}
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i >= inventory.length) {
			return null;
		}
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= size) {
				inventory[slot] = null;
			} else {
				stack = stack.splitStack(size);
				if (stack.stackSize == 0) {
					inventory[slot] = null;
				}
			}
			if (slot == itemCardSlot) {
				checkCard();
			}
		}
		return stack;
	}

	public void fillOutputSlot() {
		inventory[itemOutputSlot] = null;
		if (coinSum > 0) {
			// use logarithm to find largest cointype for the balance
			int logVal = Math.min((int) (Math.log(coinSum) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinSum / Math.pow(9, logVal)), 64);
			// add a stack to the slot
			inventory[itemOutputSlot] = new ItemStack(coins[logVal], stackSize);
			int itemValue = multiplier[logVal];
			int debitAmount = 0;
			debitAmount = Math.min(stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
			if (!worldObj.isRemote) {
				coinSum -= debitAmount * itemValue;
				coinSum = Math.max(coinSum, 0);
			}
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			if (slot == itemCoinSlot) {
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
					coinSum += depositAmount * itemValue;
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
				}
			}
			if (slot == itemCardSlot) {
				checkCard();
			}
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}
}
