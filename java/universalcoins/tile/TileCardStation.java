package universalcoins.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCCardStationServerCustomNameMessage;
import universalcoins.net.UCCardStationServerWithdrawalMessage;
import universalcoins.net.UCTileCardStationMessage;
import universalcoins.util.UniversalAccounts;

public class TileCardStation extends TileEntity implements IInventory, ISidedInventory {
	private ItemStack[] inventory = new ItemStack[2];
	public static final int itemCoinSlot = 0;
	public static final int itemCardSlot = 1;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
	public EntityPlayer player;
	public String playerName = "";
	public String playerUID = "";
	public boolean inUse = false;
	public boolean depositCoins = false;
	public boolean withdrawCoins = false;
	public boolean accountError = false;
	public int coinWithdrawalAmount = 0;
	public String cardOwner = "";
	public String accountNumber = "none";
	public int accountBalance = 0;
	public String customAccountName = "none";
	public String customAccountNumber = "none";

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
		inUse = false;
		withdrawCoins = false;
		depositCoins = false;
		accountNumber = "none";
		updateTE();
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot >= inventory.length) {
			return null;
		}
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= size) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(size);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		fillCoinSlot();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.inventory[slot] != null) {
			ItemStack itemstack = this.inventory[slot];
			this.inventory[slot] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			if (slot == itemCoinSlot && depositCoins && !accountNumber.contentEquals("none")) {
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - accountBalance) / itemValue);
					if (!worldObj.isRemote) {
						creditAccount(accountNumber, depositAmount * itemValue);
						accountBalance = getAccountBalance(accountNumber);
					}
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
				}
			}
			if (slot == itemCardSlot && !worldObj.isRemote) {
				if (!inventory[itemCardSlot].hasTagCompound()) {
					return;
				}
				if (inventory[itemCardSlot].stackTagCompound.getInteger("CoinSum") != 0
						&& inventory[itemCardSlot].stackTagCompound.getString("Owner").contentEquals(playerName)) {
					addPlayerAccount(playerUID);
					accountNumber = getPlayerAccount(playerUID);
					creditAccount(accountNumber, inventory[itemCardSlot].stackTagCompound.getInteger("CoinSum"));
					inventory[itemCardSlot].stackTagCompound.removeTag("CoinSum");
					inventory[itemCardSlot].stackTagCompound.setString("Account", accountNumber);
				}

				accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
				cardOwner = inventory[itemCardSlot].stackTagCompound.getString("Owner");
				if (getCustomAccount(playerUID) != "")
					customAccountName = getCustomAccount(playerUID);
				accountBalance = getAccountBalance(accountNumber);

				if(!cardOwner.equals(playerUID))
				{
					inventory[itemCardSlot] = null;
					accountNumber = "none";
					customAccountNumber = "none";
					customAccountName = "none";
					cardOwner = "";
					accountBalance = 0;
					if(player != null)
						player.closeScreen();
				}
			}
		}
	}

	@Override
	public String getInventoryName() {
		return UniversalCoins.proxy.blockCardStation.getLocalizedName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public void sendButtonMessage(int functionID, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, functionID, shiftPressed));
	}

	@Override
	public Packet getDescriptionPacket() {
		return UniversalCoins.snw.getPacketFrom(new UCTileCardStationMessage(this));
	}

	public void sendServerUpdatePacket(int withdrawalAmount) {
		UniversalCoins.snw
				.sendToServer(new UCCardStationServerWithdrawalMessage(xCoord, yCoord, zCoord, withdrawalAmount));
	}

	public void sendServerUpdatePacket(String customName) {
		UniversalCoins.snw.sendToServer(new UCCardStationServerCustomNameMessage(xCoord, yCoord, zCoord, customName));
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
			inUse = tagCompound.getBoolean("InUse");
		} catch (Throwable ex2) {
			inUse = false;
		}
		try {
			depositCoins = tagCompound.getBoolean("DepositCoins");
		} catch (Throwable ex2) {
			depositCoins = false;
		}
		try {
			withdrawCoins = tagCompound.getBoolean("WithdrawCoins");
		} catch (Throwable ex2) {
			withdrawCoins = false;
		}
		try {
			coinWithdrawalAmount = tagCompound.getInteger("CoinWithdrawalAmount");
		} catch (Throwable ex2) {
			coinWithdrawalAmount = 0;
		}
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
		tagCompound.setBoolean("InUse", inUse);
		tagCompound.setBoolean("DepositCoins", depositCoins);
		tagCompound.setBoolean("WithdrawCoins", withdrawCoins);
		tagCompound.setInteger("CoinWithdrawalAmount", coinWithdrawalAmount);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openInventory() {

	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}

	private int getCoinType(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
	}

	public void onButtonPressed(int functionId) {
		if (worldObj.isRemote)
			return;

		//System.out.println("funcionId:"+functionId+" playerId:"+playerUID+" cardOwner:"+cardOwner+" accountNumber:"+accountNumber+" " +
		//		"customAccountName:"+customAccountName+" customAccountNumber:"+customAccountNumber+" accountBalance:"+accountBalance+" " +
		//		"accountError:"+Boolean.toString(accountError));
		if(!cardOwner.isEmpty() && !cardOwner.equals(playerUID))
		{
			functionId = 6;
			cardOwner = "";
			accountNumber = "";
			customAccountName = "";
			customAccountNumber = "";
			accountBalance = -1;
			if(inventory[itemCardSlot] != null && player != null)
				player.closeScreen();
			inventory[itemCardSlot] = null;
			return;
		}

		accountError = false; // reset error state
		// handle function IDs sent from CardStationGUI
		// function1 - new card
		// function2 - transfer account
		// function3 - deposit
		// function4 - withdraw
		// function5 - get account info
		// function6 - destroy invalid card
		// function7 - new custom account
		// function8 - new custom card
		// function9 - transfer custom account
		// function10 - account error reset
		if (functionId == 1) {
			if (getPlayerAccount(playerUID) == "") {
				addPlayerAccount(playerUID);
				accountNumber = getPlayerAccount(playerUID);
			}
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].stackTagCompound = new NBTTagCompound();
			inventory[itemCardSlot].stackTagCompound.setString("Name", playerName);
			inventory[itemCardSlot].stackTagCompound.setString("Owner", playerUID);
			inventory[itemCardSlot].stackTagCompound.setString("Account", accountNumber);
			accountBalance = getAccountBalance(accountNumber);
		}
		if (functionId == 2) {
			if (getPlayerAccount(playerUID) == "") {
			} else {
				transferPlayerAccount(playerUID);
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
				inventory[itemCardSlot].stackTagCompound = new NBTTagCompound();
				inventory[itemCardSlot].stackTagCompound.setString("Name", playerName);
				inventory[itemCardSlot].stackTagCompound.setString("Owner", playerUID);
				inventory[itemCardSlot].stackTagCompound.setString("Account", getPlayerAccount(playerUID));
				accountBalance = getAccountBalance(accountNumber);
			}
		}
		if (functionId == 3) {
			// set to true if player presses deposit button, reset on any other
			// button press
			depositCoins = true;
			withdrawCoins = false;
			// set account number if not already set and we have a card present
			if (accountNumber.contentEquals("none") && inventory[itemCardSlot] != null) {
				accountNumber = inventory[itemCardSlot].stackTagCompound.getString("Account");
			}
		} else {
			depositCoins = false;
		}
		if (functionId == 4) {
			withdrawCoins = true;
			depositCoins = false;
			fillCoinSlot();
		} else
			withdrawCoins = false;
		if (functionId == 5) {
			String storedAccount = getPlayerAccount(playerUID);
			if (storedAccount != "") {
				accountNumber = storedAccount;
				cardOwner = playerUID; // needed for new card auth
				accountBalance = getAccountBalance(accountNumber);
				if (getCustomAccount(playerUID) != "") {
					customAccountName = getCustomAccount(playerUID);
					customAccountNumber = getPlayerAccount(customAccountName);
				}
			} else
				accountNumber = "none";
		}
		if (functionId == 6) {
			inventory[itemCardSlot] = null;
		}
		if (functionId == 7) {
			if (getPlayerAccount(customAccountName) != ""
					&& !getCustomAccount(playerUID).contentEquals(customAccountName)) {
				accountError = true;
				// we need to reset this so that that function 7 is called again
				// on next attempt at getting an account
				customAccountName = "none";
				return;
			} else if (getCustomAccount(playerUID) == "") {
				addCustomAccount(customAccountName);
			}
			customAccountName = getCustomAccount(playerUID);
			customAccountNumber = getPlayerAccount(customAccountName);
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].stackTagCompound = new NBTTagCompound();
			inventory[itemCardSlot].stackTagCompound.setString("Name", customAccountName);
			inventory[itemCardSlot].stackTagCompound.setString("Owner", playerUID);
			inventory[itemCardSlot].stackTagCompound.setString("Account", customAccountNumber);
		}
		if (functionId == 8) {
			inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
			inventory[itemCardSlot].stackTagCompound = new NBTTagCompound();
			inventory[itemCardSlot].stackTagCompound.setString("Name", customAccountName);
			inventory[itemCardSlot].stackTagCompound.setString("Owner", playerUID);
			inventory[itemCardSlot].stackTagCompound.setString("Account", customAccountNumber);
			accountBalance = getAccountBalance(customAccountNumber);
		}
		if (functionId == 9) {
			if (getCustomAccount(playerUID) == "" || getPlayerAccount(customAccountName) != "") {
				accountError = true;
			} else {
				accountError = false;
				transferCustomAccount();
				inventory[itemCardSlot] = new ItemStack(UniversalCoins.proxy.itemUCCard, 1);
				inventory[itemCardSlot].stackTagCompound = new NBTTagCompound();
				inventory[itemCardSlot].stackTagCompound.setString("Name", customAccountName);
				inventory[itemCardSlot].stackTagCompound.setString("Owner", playerUID);
				inventory[itemCardSlot].stackTagCompound.setString("Account", customAccountNumber);
				accountBalance = getAccountBalance(customAccountNumber);
			}
		}
	}

	public void fillCoinSlot() {
		if (inventory[itemCoinSlot] == null && coinWithdrawalAmount > 0) {
			// use logarithm to find largest cointype for coins being withdrawn
			int logVal = Math.min((int) (Math.log(coinWithdrawalAmount) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinWithdrawalAmount / Math.pow(9, logVal)), 64);
			inventory[itemCoinSlot] = (new ItemStack(coins[logVal], stackSize));
			coinWithdrawalAmount -= (stackSize * Math.pow(9, logVal));
			if (!worldObj.isRemote) {
				debitAccount(accountNumber, (int) (stackSize * Math.pow(9, logVal)));
				accountBalance = getAccountBalance(accountNumber);
			}
		}
		if (coinWithdrawalAmount <= 0) {
			withdrawCoins = false;
			coinWithdrawalAmount = 0;
		}
	}

	private String getCustomAccount(String playerUID2) {
		return UniversalAccounts.getInstance().getCustomAccount(playerUID2);
	}

	private String getPlayerAccount(String playerUID2) {
		return UniversalAccounts.getInstance().getPlayerAccount(playerUID2);
	}

	private void addPlayerAccount(String playerUID2) {
		UniversalAccounts.getInstance().addPlayerAccount(playerUID2);
	}

	private int getAccountBalance(String accountNumber2) {
		return UniversalAccounts.getInstance().getAccountBalance(accountNumber2);
	}

	private void creditAccount(String accountNumber2, int i) {
		UniversalAccounts.getInstance().creditAccount(accountNumber2, i);
	}

	private void transferCustomAccount() {
		UniversalAccounts.getInstance().transferCustomAccount(playerUID, customAccountName);
		customAccountNumber = UniversalAccounts.getInstance().getCustomAccount(playerUID);
	}

	private void addCustomAccount(String customAccountName2) {
		UniversalAccounts.getInstance().addCustomAccount(customAccountName2, playerUID);
	}

	private void transferPlayerAccount(String playerUID2) {
		UniversalAccounts.getInstance().transferPlayerAccount(playerUID2);
	}

	private void debitAccount(String accountNumber2, int i) {
		UniversalAccounts.getInstance().debitAccount(accountNumber2, i);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return null;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3) {
		return false;
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3) {
		return false;
	}
}
