package universalcoins;

import universalcoins.gui.HintGuiRenderer;
import universalcoins.items.ItemCoin;
import universalcoins.items.ItemCoinHeap;
import universalcoins.items.ItemLargeCoinBag;
import universalcoins.items.ItemLargeCoinStack;
import universalcoins.items.ItemSeller;
import universalcoins.items.ItemSmallCoinBag;
import universalcoins.items.ItemSmallCoinStack;
import universalcoins.items.ItemWrench;
import universalcoins.net.UCButtonMessage;
import universalcoins.net.UCTileCardStationMessage;
import universalcoins.net.UCTileTradeStationMessage;
import universalcoins.net.UCVendorServerMessage;
import universalcoins.proxy.CommonProxy;
import universalcoins.tile.TileCardStation;
import universalcoins.tile.TileTradeStation;
import universalcoins.tile.TileVendor;
import universalcoins.util.UCCommand;
import universalcoins.util.UCEventHandler;
import universalcoins.util.UCItemPricer;
import universalcoins.util.UCRecipeHelper;
import universalcoins.util.Vending;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * UniversalCoins, Sell all your extra blocks and buy more!!! Create a trading economy, jobs, whatever.
 * 
 * @author ted_996, notabadminer, AUTOMATIC_MAIDEN
 * 
 **/

@Mod(modid = UniversalCoins.modid, name = UniversalCoins.name, version = UniversalCoins.version)

public class UniversalCoins {
	@Instance("universalcoins")
	public static UniversalCoins instance;
	public static final String modid = "universalcoins";
	public static final String name = "Universal Coins";
	public static final String version = "1.7.2-1.5.5";
	
	public static Boolean autoModeEnabled;
	public static Boolean updateCheck;
	public static Boolean recipesEnabled;
	public static Boolean vendorRecipesEnabled;
	public static Boolean atmRecipeEnabled;
	public static Boolean cardSecurityEnabled;
	public static Boolean wrenchEnabled;
	public static Boolean collectCoinsInInfinite;
	public static Boolean mobsDropCoins;
	public static Boolean coinsInMineshaft;
	public static Boolean coinsInDungeon;
	
	public static SimpleNetworkWrapper snw;
	
	public static CreativeTabs tabUniversalCoins = new UCTab("tabUniversalCoins");
	
	@SidedProxy(clientSide="universalcoins.proxy.ClientProxy", serverSide="universalcoins.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		Property autoMode = config.get(config.CATEGORY_GENERAL, "Auto mode enabled", true);
		autoMode.comment = "Set to false to disable the ability to automatically buy or sell items.";
		autoModeEnabled = autoMode.getBoolean(true);
		Property recipes = config.get(config.CATEGORY_GENERAL, "CraftingRecipes enabled", true);
		recipes.comment = "Set to false to disable crafting recipes for selling catalog and trade station.";
		recipesEnabled = recipes.getBoolean(true);
		Property vendorRecipes = config.get(config.CATEGORY_GENERAL, "Vending Block Recipes", true);
		vendorRecipes.comment = "Set to false to disable crafting recipes for vending blocks.";
		vendorRecipesEnabled = vendorRecipes.getBoolean(true);
		Property atmRecipe = config.get(config.CATEGORY_GENERAL, "ATM Recipe", true);
		atmRecipe.comment = "Set to false to disable crafting recipes for ATM.";
		atmRecipeEnabled = atmRecipe.getBoolean(true);
		Property secureCard = config.get(config.CATEGORY_GENERAL, "Secure Card", true);
		secureCard.comment = "Set to false to disable card security. The UC Cards will be usable by any player.";
		cardSecurityEnabled = secureCard.getBoolean(true);
		Property wrench = config.get(config.CATEGORY_GENERAL, "Wrench enabled", true);
		wrench.comment = "Set to false to disable wrench. Use this if your world already has too many wrenches.";
		wrenchEnabled = wrench.getBoolean(true);
		Property collectInfinite = config.get(config.CATEGORY_GENERAL, "Collect infinite", true);
		collectInfinite.comment = "Set to false to disable collecting coins when vending blocks are set to infinite mode.";
		collectCoinsInInfinite = collectInfinite.getBoolean(true);
		Property mobDrops = config.get(config.CATEGORY_GENERAL, "Mob Drops", true);
		mobDrops.comment = "Set to false to disable mobs dropping coins on death.";
		mobsDropCoins = mobDrops.getBoolean(true);
		Property mineshaftCoins = config.get(config.CATEGORY_GENERAL, "Mineshaft CoinBag", true);
		mineshaftCoins.comment = "Set to false to disable coinbag spawning in mineshaft chests.";
		coinsInMineshaft = mineshaftCoins.getBoolean(true);
		Property dungeonCoins = config.get(config.CATEGORY_GENERAL, "Dungeon CoinBag", true);
		dungeonCoins.comment = "Set to false to disable coinbag spawning in dungeon chests.";
		coinsInDungeon = dungeonCoins.getBoolean(true);
		config.save();
		
		if (mobsDropCoins) {
			MinecraftForge.EVENT_BUS.register(new UCEventHandler());
		}
		
		//network packet handling
	    snw = NetworkRegistry.INSTANCE.newSimpleChannel(modid); 
	    snw.registerMessage(UCButtonMessage.class, UCButtonMessage.class, 0, Side.SERVER);
	    snw.registerMessage(UCVendorServerMessage.class, UCVendorServerMessage.class, 1, Side.SERVER);
	    snw.registerMessage(UCTileTradeStationMessage.class, UCTileTradeStationMessage.class, 2, Side.CLIENT);
	    snw.registerMessage(UCTileCardStationMessage.class, UCTileCardStationMessage.class, 3, Side.CLIENT);
	    
	    //update check using versionchecker
	    FMLInterModComms.sendRuntimeMessage(modid, "VersionChecker", "addVersionCheck", "https://www.dropbox.com/s/mn4mloo1vw79vdb/version.json");
	}
	
	@EventHandler
	public void postInitialise(FMLPostInitializationEvent event) {
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerBlocks();
		proxy.registerItems();
		proxy.registerRenderers();
		
		UCRecipeHelper.addCoinRecipes();
		if (recipesEnabled) {
			UCRecipeHelper.addTradeStationRecipe();
		}
		if (vendorRecipesEnabled){
			UCRecipeHelper.addVendingBlockRecipes();
		}
		if (atmRecipeEnabled){
			UCRecipeHelper.addCardStationRecipes();
		}
		if (wrenchEnabled) {
			UCRecipeHelper.addWrenchRecipe();
		}
		if (coinsInMineshaft) {
			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(proxy.itemLargeCoinBag), 1, 64, 20));
		}
		if (coinsInDungeon) {
			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(proxy.itemLargeCoinBag), 1, 64, 20));
		}
		
		GameRegistry.registerTileEntity(TileTradeStation.class, "TileTradeStation");
		GameRegistry.registerTileEntity(TileVendor.class, "TileVendor");
		GameRegistry.registerTileEntity(TileCardStation.class, "TileCardStation");
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	    UCItemPricer.initializeConfigs();
	    UCItemPricer.loadConfigs();	
	}
	
	@EventHandler
    public void serverStart(FMLServerStartingEvent event) {
		MinecraftServer server = MinecraftServer.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		manager.registerCommand(new UCCommand());
	}

}
