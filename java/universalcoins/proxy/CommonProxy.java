package universalcoins.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import universalcoins.blocks.BlockBase;
import universalcoins.blocks.BlockCardStation;
import universalcoins.blocks.BlockSafe;
import universalcoins.blocks.BlockTradeStation;
import universalcoins.blocks.BlockVendor;
import universalcoins.blocks.BlockVendorFrame;
import universalcoins.items.ItemBlockVendor;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemEnderCard;
import universalcoins.items.ItemLargeCoinBag;
import universalcoins.items.ItemLargeCoinStack;
import universalcoins.items.ItemSeller;
import universalcoins.items.ItemSmallCoinBag;
import universalcoins.items.ItemSmallCoinStack;
import universalcoins.items.ItemUCCard;
import universalcoins.items.ItemVendorWrench;
import universalcoins.util.Vending;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {
	public static Item itemCoin;
	public static Item itemSmallCoinStack;
	public static Item itemLargeCoinStack;
	public static Item itemSmallCoinBag;
	public static Item itemLargeCoinBag;
	public static Item itemSeller;
	public static Item itemUCCard;
	public static Item itemEnderCard;
	public static Item itemVendorWrench;
	
	public static Block blockTradeStation;
	public static Block blockVendor;
	public static Block blockVendorFrame;
	public static Block blockCardStation;
	public static Block blockBase;
	public static Block blockSafe;
	
	
	public void registerBlocks() {
		blockTradeStation = new BlockTradeStation().setBlockName("blockTradeStation");
		blockVendor = new BlockVendor(Vending.supports).setBlockName("blockVendor");
		blockVendorFrame = new BlockVendorFrame().setBlockName("blockVendorFrame");
		blockCardStation = new BlockCardStation().setBlockName("blockCardStation");
		blockBase = new BlockBase().setBlockName("blockBase");
		blockSafe = new BlockSafe().setBlockName("blockSafe");

		
		GameRegistry.registerBlock(blockTradeStation, "blockTradeStation").getUnlocalizedName();
		GameRegistry.registerBlock(blockVendor, ItemBlockVendor.class, "blockVendor");
		GameRegistry.registerBlock(blockVendorFrame, "blockVendorFrame").getUnlocalizedName();
		GameRegistry.registerBlock(blockCardStation, "blockCardStation").getUnlocalizedName();
		GameRegistry.registerBlock(blockBase, "blockBase").getUnlocalizedName();
		GameRegistry.registerBlock(blockSafe, "blockSafe").getUnlocalizedName();
	}
	
	public void registerItems() {
		itemCoin = new ItemCoin().setUnlocalizedName("itemCoin");
		itemSmallCoinStack = new ItemSmallCoinStack().setUnlocalizedName("itemSmallCoinStack");
		itemLargeCoinStack = new ItemLargeCoinStack().setUnlocalizedName("itemLargeCoinStack");
		itemSmallCoinBag = new ItemSmallCoinBag().setUnlocalizedName("itemSmallCoinBag");
		itemLargeCoinBag = new ItemLargeCoinBag().setUnlocalizedName("itemLargeCoinBag");
		itemUCCard = new ItemUCCard().setUnlocalizedName("itemUCCard");
		itemEnderCard = new ItemEnderCard().setUnlocalizedName("itemEnderCard");
		itemSeller = new ItemSeller().setUnlocalizedName("itemSeller");
		itemVendorWrench = new ItemVendorWrench().setUnlocalizedName("itemVendorWrench");
		
		
		GameRegistry.registerItem(itemCoin, itemCoin.getUnlocalizedName());
		GameRegistry.registerItem(itemSmallCoinStack, itemSmallCoinStack.getUnlocalizedName());
		GameRegistry.registerItem(itemLargeCoinStack, itemLargeCoinStack.getUnlocalizedName());
		GameRegistry.registerItem(itemSmallCoinBag, itemSmallCoinBag.getUnlocalizedName());
		GameRegistry.registerItem(itemLargeCoinBag, itemLargeCoinBag.getUnlocalizedName());
		GameRegistry.registerItem(itemUCCard, itemUCCard.getUnlocalizedName());
		GameRegistry.registerItem(itemEnderCard, itemEnderCard.getUnlocalizedName());
		GameRegistry.registerItem(itemSeller, itemSeller.getUnlocalizedName());
		GameRegistry.registerItem(itemVendorWrench, itemVendorWrench.getUnlocalizedName());
	}

	public void registerRenderers() {
		//blank since we don't do anything on the server
	}

}
