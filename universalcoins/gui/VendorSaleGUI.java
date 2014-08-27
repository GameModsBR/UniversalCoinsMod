package universalcoins.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLLog;
import universalcoins.inventory.ContainerVendorSale;
import universalcoins.tile.TileVendor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class VendorSaleGUI extends GuiContainer{
	private TileVendor tileEntity;
	private GuiButton buyButton;
	private GuiCoinButton retrCoinButton, retrSStackButton, retrLStackButton, retrSBagButton, retrLBagButton;
	public static final int idBuyButton = 7;
	public static final int idCoinButton = 8;
	private static final int idSStackButton = 9;
	private static final int idLStackButton = 10;
	public static final int idSBagButton = 11;
	public static final int idLBagButton = 12;
	
	boolean shiftPressed = false;

	public VendorSaleGUI(InventoryPlayer inventoryPlayer, TileVendor tEntity) {
		super(new ContainerVendorSale(inventoryPlayer, tEntity));
		tileEntity = tEntity;
		
		xSize = 176;
		ySize = 200;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		buyButton = new GuiSlimButton(idBuyButton, 124 + (width - xSize) / 2, 42 + (height - ySize) / 2, 42, 12, "Buy");
		retrCoinButton = new GuiCoinButton(idCoinButton, 42 + (width - xSize) / 2, 84 + (height - ySize) / 2, 18, 18, "", 0);
		retrSStackButton = new GuiCoinButton(idSStackButton, 60 + (width - xSize) / 2, 84 + (height - ySize) / 2, 18, 18, "", 1);
		retrLStackButton = new GuiCoinButton(idLStackButton, 78 + (width - xSize) / 2, 84 + (height - ySize) / 2, 18, 18, "", 2);
		retrSBagButton = new GuiCoinButton(idSBagButton, 96 + (width - xSize) / 2, 84 + (height - ySize) / 2, 18, 18, "", 3);
		retrLBagButton = new GuiCoinButton(idLBagButton, 114 + (width - xSize) / 2, 84 + (height - ySize) / 2, 18, 18, "", 4);
		buttonList.clear();
		buttonList.add(buyButton);
		buttonList.add(retrCoinButton);
		buttonList.add(retrSStackButton);
		buttonList.add(retrLStackButton);
		buttonList.add(retrSBagButton);
		buttonList.add(retrLBagButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/vendor-sale.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		buyButton.enabled = tileEntity.buyButtonActive;
		retrCoinButton.enabled = tileEntity.uCoinButtonActive;
		retrSStackButton.enabled = tileEntity.uSStackButtonActive;
		retrLStackButton.enabled = tileEntity.uLStackButtonActive;
		retrSBagButton.enabled = tileEntity.uSBagButtonActive;
		retrLBagButton.enabled = tileEntity.uLBagButtonActive;		
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString("Vending Block", 6, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal(
				"container.inventory"), 6, ySize - 96 + 2, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.itemPrice), 46, 29, 4210752);
		fontRendererObj.drawString(String.valueOf(tileEntity.userCoinSum), 46, 71, 4210752);
	}
	
	protected void actionPerformed(GuiButton button) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			shiftPressed = true;
		}
		else {
			shiftPressed = false;
		}
		if (button.id == idBuyButton){
			if (shiftPressed) {
				tileEntity.onBuyMaxPressed();
			}
			else {
				tileEntity.onBuyPressed();
			}
		}
		else if (button.id <= idLBagButton) {
			tileEntity.onRetrieveButtonsPressed(button.id, shiftPressed);
		}
		tileEntity.sendButtonMessage(button.id, shiftPressed);
	}
}
