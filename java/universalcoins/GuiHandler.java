package universalcoins;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;
import universalcoins.gui.BanditConfigGUI;
import universalcoins.gui.BanditGUI;
import universalcoins.gui.CardStationGUI;
import universalcoins.gui.PackagerGUI;
import universalcoins.gui.PowerBaseGUI;
import universalcoins.gui.PowerReceiverGUI;
import universalcoins.gui.SafeGUI;
import universalcoins.gui.SignalGUI;
import universalcoins.gui.TradeStationGUI;
import universalcoins.gui.UCSignEditGUI;
import universalcoins.gui.VendorBuyGUI;
import universalcoins.gui.VendorGUI;
import universalcoins.gui.VendorSellGUI;
import universalcoins.gui.VendorWrenchGUI;
import universalcoins.inventory.ContainerBandit;
import universalcoins.inventory.ContainerCardStation;
import universalcoins.inventory.ContainerPackager;
import universalcoins.inventory.ContainerPowerBase;
import universalcoins.inventory.ContainerPowerReceiver;
import universalcoins.inventory.ContainerSafe;
import universalcoins.inventory.ContainerSignal;
import universalcoins.inventory.ContainerTradeStation;
import universalcoins.inventory.ContainerVendor;
import universalcoins.inventory.ContainerVendorBuy;
import universalcoins.inventory.ContainerVendorSell;
import universalcoins.inventory.ContainerVendorWrench;
import universalcoins.tile.TileBandit;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TilePackager;
import universalcoins.tile.TilePowerBase;
import universalcoins.tile.TilePowerReceiver;
import universalcoins.tile.TileSafe;
import universalcoins.tile.TileSignal;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileUCSign;
import universalcoins.tile.TileVendor;

class GuiHandler implements IGuiHandler {
	public static final int GUI_TRADE_STATION = 1,
							GUI_VENDOR_WRENCH = 2,
							GUI_VENDOR_OWNER = 3,
							GUI_VENDOR_SELL = 4,
							GUI_VENDOR_BUY = 5,
							GUI_CARD_STATION = 6,
							GUI_SAFE = 7,
							GUI_BANDIT = 9,
							GUI_BANDIT_WRENCH = 10,
							GUI_SIGNAL = 11,
							GUI_PACKAGER = 12,
							GUI_POWER_BASE = 13,
							GUI_POWER_RECEIVER = 14,
							GUI_ADV_SIGN = 15;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		switch (ID) {
			case GUI_TRADE_STATION:
				if(tileEntity instanceof TileTradeStation)
					return new ContainerTradeStation(player.inventory, (TileTradeStation)tileEntity);
				else return null;
			case GUI_VENDOR_WRENCH:
			case GUI_VENDOR_OWNER:
			case GUI_VENDOR_SELL:
			case GUI_VENDOR_BUY:
				if(tileEntity instanceof TileVendor) {
					TileVendor tile = (TileVendor) tileEntity;
					switch (ID){
						case GUI_VENDOR_WRENCH: return new ContainerVendorWrench(player.inventory, tile);
						case GUI_VENDOR_OWNER: return new ContainerVendor(player.inventory, tile);
						case GUI_VENDOR_SELL: return new ContainerVendorSell(player.inventory, tile);
						default: return new ContainerVendorBuy(player.inventory, tile);
					}
				}
				else return null;
			case GUI_CARD_STATION:
				if(tileEntity instanceof TileCardStation)
					return new ContainerCardStation(player.inventory, (TileCardStation)tileEntity);
				else return null;
			case GUI_SAFE:
				if(tileEntity instanceof TileSafe)
					return new ContainerSafe(player.inventory, (TileSafe)tileEntity);
				else return null;
			case GUI_BANDIT:
			case GUI_BANDIT_WRENCH:
				if(tileEntity instanceof TileBandit) {
					TileBandit tile = (TileBandit) tileEntity;
					if(ID == GUI_BANDIT) return new ContainerBandit(player.inventory, tile);
					else return null;
				}
				else return null;
			case GUI_SIGNAL:
				if(tileEntity instanceof TileSignal)
					return new ContainerSignal(player.inventory, (TileSignal) tileEntity);
				else return null;
			case GUI_PACKAGER:
				if(tileEntity instanceof TilePackager)
					return new ContainerPackager(player.inventory, (TilePackager) tileEntity);
				else return null;
			case GUI_POWER_BASE:
				if(tileEntity instanceof TilePowerBase)
					return new ContainerPowerBase(player.inventory, (TilePowerBase) tileEntity);
				else return null;
			case GUI_POWER_RECEIVER:
				if(tileEntity instanceof TilePowerReceiver)
					return new ContainerPowerReceiver(player.inventory, (TilePowerReceiver) tileEntity);
				else return null;
			case GUI_ADV_SIGN:
				return null;
		}

		if (tileEntity instanceof TileTradeStation) {
			return new ContainerTradeStation(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
			if (player.getHeldItem() != null
					&& player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
				return new ContainerVendorWrench(player.inventory, (TileVendor) tileEntity);
			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getPersistentID().toString())) {
				return new ContainerVendor(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new ContainerVendorSell(player.inventory, (TileVendor) tileEntity);
			} else
				return new ContainerVendorBuy(player.inventory, (TileVendor) tileEntity);
		}
		if (tileEntity instanceof TileCardStation) {
			return new ContainerCardStation(player.inventory, (TileCardStation) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new ContainerSafe(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileBandit) {
			if (player.getHeldItem() != null
					&& player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
				return null;
			} else {
				return new ContainerBandit(player.inventory, (TileBandit) tileEntity);
			}
		}
		if (tileEntity instanceof TileSignal) {
			return new ContainerSignal(player.inventory, (TileSignal) tileEntity);
		}
		if (tileEntity instanceof TilePackager) {
			return new ContainerPackager(player.inventory, (TilePackager) tileEntity);
		}
		if (tileEntity instanceof TilePowerBase) {
			return new ContainerPowerBase(player.inventory, (TilePowerBase) tileEntity);
		}
		if (tileEntity instanceof TilePowerReceiver) {
			return new ContainerPowerReceiver(player.inventory, (TilePowerReceiver) tileEntity);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		switch (ID) {
			case GUI_TRADE_STATION:
				if(tileEntity instanceof TileTradeStation)
					return new TradeStationGUI(player.inventory, (TileTradeStation)tileEntity);
				else if(tileEntity instanceof TileUCSign)
					return new UCSignEditGUI((TileEntitySign) tileEntity);
			case GUI_VENDOR_WRENCH:
			case GUI_VENDOR_OWNER:
			case GUI_VENDOR_SELL:
			case GUI_VENDOR_BUY:
				if(tileEntity instanceof TileVendor) {
					TileVendor tile = (TileVendor) tileEntity;
					switch (ID){
						case GUI_VENDOR_WRENCH: return new VendorWrenchGUI(player.inventory, tile);
						case GUI_VENDOR_OWNER: return new VendorGUI(player.inventory, tile);
						case GUI_VENDOR_SELL: return new VendorSellGUI(player.inventory, tile);
						default: return new VendorBuyGUI(player.inventory, tile);
					}
				}
				else return null;
			case GUI_CARD_STATION:
				if(tileEntity instanceof TileCardStation)
					return new CardStationGUI(player.inventory, (TileCardStation)tileEntity);
				else return null;
			case GUI_SAFE:
				if(tileEntity instanceof TileSafe)
					return new SafeGUI(player.inventory, (TileSafe)tileEntity);
				else return null;
			case GUI_BANDIT:
			case GUI_BANDIT_WRENCH:
				if(tileEntity instanceof TileBandit) {
					TileBandit tile = (TileBandit) tileEntity;
					if(ID == GUI_BANDIT) return new BanditGUI(player.inventory, tile);
					else return null;
				}
				else return null;
			case GUI_SIGNAL:
				if(tileEntity instanceof TileSignal)
					return new SignalGUI(player.inventory, (TileSignal) tileEntity);
				else return null;
			case GUI_PACKAGER:
				if(tileEntity instanceof TilePackager)
					return new PackagerGUI(player.inventory, (TilePackager) tileEntity);
				else return null;
			case GUI_POWER_BASE:
				if(tileEntity instanceof TilePowerBase)
					return new PowerBaseGUI(player.inventory, (TilePowerBase) tileEntity);
				else return null;
			case GUI_POWER_RECEIVER:
				if(tileEntity instanceof TilePowerReceiver)
					return new PowerReceiverGUI(player.inventory, (TilePowerReceiver) tileEntity);
				else return null;
			case GUI_ADV_SIGN:
				if (tileEntity instanceof TileUCSign)
					return new UCSignEditGUI((TileUCSign) tileEntity);
				else return null;
		}

		if (tileEntity instanceof TileTradeStation) {
			return new TradeStationGUI(player.inventory, (TileTradeStation) tileEntity);
		}
		if (tileEntity instanceof TileVendor) {
			if (player.getHeldItem() != null
					&& player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
				return new VendorWrenchGUI(player.inventory, (TileVendor) tileEntity);
			}
			if (((TileVendor) tileEntity).blockOwner == null
					|| ((TileVendor) tileEntity).blockOwner.contentEquals(player.getPersistentID().toString())) {
				return new VendorGUI(player.inventory, (TileVendor) tileEntity);
			} else if (((TileVendor) tileEntity).sellMode) {
				return new VendorSellGUI(player.inventory, (TileVendor) tileEntity);
			} else
				return new VendorBuyGUI(player.inventory, (TileVendor) tileEntity);
		}
		if (tileEntity instanceof TileCardStation) {
			return new CardStationGUI(player.inventory, (TileCardStation) tileEntity);
		}
		if (tileEntity instanceof TileSafe) {
			return new SafeGUI(player.inventory, (TileSafe) tileEntity);
		}
		if (tileEntity instanceof TileBandit) {
			if (player.getHeldItem() != null
					&& player.getHeldItem().getItem() == UniversalCoins.proxy.itemVendorWrench) {
				return new BanditConfigGUI((TileBandit) tileEntity);
			} else {
				return new BanditGUI(player.inventory, (TileBandit) tileEntity);
			}
		}
		if (tileEntity instanceof TileSignal) {
			return new SignalGUI(player.inventory, (TileSignal) tileEntity);
		}
		if (tileEntity instanceof TileUCSign) {
			return new UCSignEditGUI((TileUCSign) tileEntity);
		}
		if (tileEntity instanceof TilePackager) {
			return new PackagerGUI(player.inventory, (TilePackager) tileEntity);
		}
		if (tileEntity instanceof TilePowerBase) {
			return new PowerBaseGUI(player.inventory, (TilePowerBase) tileEntity);
		}
		if (tileEntity instanceof TilePowerReceiver) {
			return new PowerReceiverGUI(player.inventory, (TilePowerReceiver) tileEntity);
		}
		return null;
	}
}
