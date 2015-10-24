package universalcoins.gui;

import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.inventory.ContainerPackager;
import universalcoins.tile.TilePackager;

public class PackagerGUI extends GuiContainer {

	private TilePackager tEntity;
	private GuiButton smallButton, medButton, largeButton, buyButton, coinButton, modeButton;
	public static final int idBuyButton = 0;
	public static final int idCoinButton = 1;
	public static final int idSmallButton = 2;
	public static final int idMedButton = 3;
	public static final int idLargeButton = 4;
	public static final int idModeButton = 5;
	private int[] defaultSlotCoord = { 8, 26, 44, 62 };
	private int hideCoord = Integer.MAX_VALUE;
	private boolean buttonHover = false;
	private boolean sendMode = false;
	private GuiTextField packageReceiverField;

	public PackagerGUI(InventoryPlayer inventoryPlayer, TilePackager tileEntity) {
		super(new ContainerPackager(inventoryPlayer, tileEntity));
		tEntity = tileEntity;

		xSize = 176;
		ySize = 190;
	}

	@Override
	public void initGui() {
		super.initGui();
		modeButton = new GuiSlimButton(idModeButton, 110 + (width - xSize) / 2, 4 + (height - ySize) / 2, 60, 12,
				StatCollector.translateToLocal("packager.button.mode.buy"));
		smallButton = new GuiSlimButton(idSmallButton, 87 + (width - xSize) / 2, 20 + (height - ySize) / 2, 50, 12,
				StatCollector.translateToLocal("packager.button.small"));
		medButton = new GuiSlimButton(idMedButton, 87 + (width - xSize) / 2, 32 + (height - ySize) / 2, 50, 12,
				StatCollector.translateToLocal("packager.button.medium"));
		largeButton = new GuiSlimButton(idLargeButton, 87 + (width - xSize) / 2, 44 + (height - ySize) / 2, 50, 12,
				StatCollector.translateToLocal("packager.button.large"));

		buyButton = new GuiSlimButton(idBuyButton, 123 + (width - xSize) / 2, 58 + (height - ySize) / 2, 46, 12,
				StatCollector.translateToLocal("general.button.buy"));
		coinButton = new GuiSlimButton(idCoinButton, 123 + (width - xSize) / 2, 91 + (height - ySize) / 2, 46, 12,
				StatCollector.translateToLocal("general.button.coin"));
		buttonList.clear();
		buttonList.add(modeButton);
		buttonList.add(smallButton);
		buttonList.add(medButton);
		buttonList.add(largeButton);
		buttonList.add(buyButton);
		buttonList.add(coinButton);

		// update slots to current mode
		updateSlots(tEntity.packageSize);

		packageReceiverField = new GuiTextField(this.fontRendererObj, Integer.MAX_VALUE, 20, 138, 13);
		packageReceiverField.setFocused(false);
		packageReceiverField.setMaxStringLength(24);
		packageReceiverField.setText("Enter Player Name");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString(tEntity.getInventoryName(), 8, 5, 4210752);
		// draws "Inventory" or your regional equivalent
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 96, 4210752);

		// draw package price if button hover true
		buttonHover = false;
		if (smallButton.func_146115_a()) {
			String cost = "Cost: " + tEntity.packageCost[0];
			int stringWidth = fontRendererObj.getStringWidth(cost);
			fontRendererObj.drawString(cost, 144 - stringWidth, 78, 4210752);
			buttonHover = true;
		}
		if (medButton.func_146115_a()) {
			String cost = "Cost: " + tEntity.packageCost[1];
			int stringWidth = fontRendererObj.getStringWidth(cost);
			fontRendererObj.drawString(cost, 144 - stringWidth, 78, 4210752);
			buttonHover = true;
		}
		if (largeButton.func_146115_a()) {
			String cost = "Cost: " + tEntity.packageCost[2];
			int stringWidth = fontRendererObj.getStringWidth(cost);
			fontRendererObj.drawString(cost, 144 - stringWidth, 78, 4210752);
			buttonHover = true;
		}
		if (!buttonHover) {
			// draw coin sum right aligned
			DecimalFormat formatter = new DecimalFormat("#,###,###,###");
			String coinSumString = String.valueOf(formatter.format(tEntity.coinSum));
			int stringWidth = fontRendererObj.getStringWidth(coinSumString);
			fontRendererObj.drawString(coinSumString, 144 - stringWidth, 78, 4210752);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		// disable if player has no money
		if (sendMode) {
			buyButton.displayString = StatCollector.translateToLocal("general.button.send");
			buyButton.enabled = canSend();
		} else {
			buyButton.displayString = StatCollector.translateToLocal("general.button.buy");
			buyButton.enabled = tEntity.coinSum >= tEntity.packageCost[tEntity.packageSize] || tEntity.cardAvailable;		
		}
		coinButton.enabled = tEntity.coinSum > 0;

		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/packager.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		// draw highlight over currently selected package mode
		int yHighlight[] = { 24, 36, 48 };
		// check tileEntity for mode and highlight proper mode
		this.drawTexturedModalRect(x + 80, y + yHighlight[tEntity.packageSize], 176, 0, 6, 6);

		// draw background for slots
		if (tEntity.packageSize == 1) {
			this.drawTexturedModalRect(x + 25, y + 21, 176, 6, 18, 36);
		}
		if (tEntity.packageSize == 2) {
			this.drawTexturedModalRect(x + 7, y + 21, 176, 6, 36, 36);
		}
		if (sendMode) {
			this.drawTexturedModalRect(x + 3, y + 20, 0, 190, 169, 40);
			fontRendererObj.drawString(packageReceiverField.getText(), x + 30, y + 30, 4210752);
		}
	}

	private void updateSlots(int mode) {
		if (mode == 0) {
			// hide slot 0-3
			Slot slot0 = super.inventorySlots.getSlot(0);
			slot0.xDisplayPosition = hideCoord;
			Slot slot1 = super.inventorySlots.getSlot(1);
			slot1.xDisplayPosition = hideCoord;
			Slot slot2 = super.inventorySlots.getSlot(2);
			slot2.xDisplayPosition = hideCoord;
			Slot slot3 = super.inventorySlots.getSlot(3);
			slot3.xDisplayPosition = hideCoord;
			Slot slot4 = super.inventorySlots.getSlot(4);
			slot4.xDisplayPosition = defaultSlotCoord[2];
			Slot slot5 = super.inventorySlots.getSlot(5);
			slot5.xDisplayPosition = defaultSlotCoord[2];
			Slot slot6 = super.inventorySlots.getSlot(6);
			slot6.xDisplayPosition = defaultSlotCoord[3];
			Slot slot7 = super.inventorySlots.getSlot(7);
			slot7.xDisplayPosition = defaultSlotCoord[3];
			// package input slot
			Slot slot11 = super.inventorySlots.getSlot(11);
			slot11.xDisplayPosition = hideCoord;
		}
		if (mode == 1) {
			// hide slot 0-1 , show 2-3
			Slot slot0 = super.inventorySlots.getSlot(0);
			slot0.xDisplayPosition = hideCoord;
			Slot slot1 = super.inventorySlots.getSlot(1);
			slot1.xDisplayPosition = hideCoord;
			Slot slot2 = super.inventorySlots.getSlot(2);
			slot2.xDisplayPosition = defaultSlotCoord[1];
			Slot slot3 = super.inventorySlots.getSlot(3);
			slot3.xDisplayPosition = defaultSlotCoord[1];
			Slot slot4 = super.inventorySlots.getSlot(4);
			slot4.xDisplayPosition = defaultSlotCoord[2];
			Slot slot5 = super.inventorySlots.getSlot(5);
			slot5.xDisplayPosition = defaultSlotCoord[2];
			Slot slot6 = super.inventorySlots.getSlot(6);
			slot6.xDisplayPosition = defaultSlotCoord[3];
			Slot slot7 = super.inventorySlots.getSlot(7);
			slot7.xDisplayPosition = defaultSlotCoord[3];
			// package input slot
			Slot slot11 = super.inventorySlots.getSlot(11);
			slot11.xDisplayPosition = hideCoord;
		}
		if (mode == 2) {
			// show slot 0-3
			Slot slot0 = super.inventorySlots.getSlot(0);
			slot0.xDisplayPosition = defaultSlotCoord[0];
			Slot slot1 = super.inventorySlots.getSlot(1);
			slot1.xDisplayPosition = defaultSlotCoord[0];
			Slot slot2 = super.inventorySlots.getSlot(2);
			slot2.xDisplayPosition = defaultSlotCoord[1];
			Slot slot3 = super.inventorySlots.getSlot(3);
			slot3.xDisplayPosition = defaultSlotCoord[1];
			Slot slot4 = super.inventorySlots.getSlot(4);
			slot4.xDisplayPosition = defaultSlotCoord[2];
			Slot slot5 = super.inventorySlots.getSlot(5);
			slot5.xDisplayPosition = defaultSlotCoord[2];
			Slot slot6 = super.inventorySlots.getSlot(6);
			slot6.xDisplayPosition = defaultSlotCoord[3];
			Slot slot7 = super.inventorySlots.getSlot(7);
			slot7.xDisplayPosition = defaultSlotCoord[3];
			// package input slot
			Slot slot11 = super.inventorySlots.getSlot(11);
			slot11.xDisplayPosition = hideCoord;
		}
		if (mode == 3) {
			// hide all slots
			Slot slot0 = super.inventorySlots.getSlot(0);
			slot0.xDisplayPosition = hideCoord;
			Slot slot1 = super.inventorySlots.getSlot(1);
			slot1.xDisplayPosition = hideCoord;
			Slot slot2 = super.inventorySlots.getSlot(2);
			slot2.xDisplayPosition = hideCoord;
			Slot slot3 = super.inventorySlots.getSlot(3);
			slot3.xDisplayPosition = hideCoord;
			Slot slot4 = super.inventorySlots.getSlot(4);
			slot4.xDisplayPosition = hideCoord;
			Slot slot5 = super.inventorySlots.getSlot(5);
			slot5.xDisplayPosition = hideCoord;
			Slot slot6 = super.inventorySlots.getSlot(6);
			slot6.xDisplayPosition = hideCoord;
			Slot slot7 = super.inventorySlots.getSlot(7);
			slot7.xDisplayPosition = hideCoord;
			// package input slot
			Slot slot11 = super.inventorySlots.getSlot(11);
			slot11.xDisplayPosition = 8;
		}
	}

	protected void actionPerformed(GuiButton button) {
		if (button.id == idModeButton) {
			sendMode = !sendMode;
			if (sendMode) {
				modeButton.displayString = StatCollector.translateToLocal("packager.button.mode.send");
				// move package size buttons
				smallButton.xPosition = hideCoord;
				medButton.xPosition = hideCoord;
				largeButton.xPosition = hideCoord;
				// move package slots
				updateSlots(3);
				packageReceiverField.setFocused(true);
			} else {
				modeButton.displayString = StatCollector.translateToLocal("packager.button.mode.buy");
				// move package size buttons
				smallButton.xPosition = 87 + (width - xSize) / 2;
				medButton.xPosition = 87 + (width - xSize) / 2;
				largeButton.xPosition = 87 + (width - xSize) / 2;
				// move package slots to current mode
				updateSlots(tEntity.packageSize);
				packageReceiverField.setFocused(false);
			}
		}
		if (button.id == idSmallButton) {
			updateSlots(0);
		}
		if (button.id == idMedButton) {
			updateSlots(1);
		}
		if (button.id == idLargeButton) {
			updateSlots(2);
		}
		if (button.id == idBuyButton && sendMode) {
			button.id = 10; //set to 10 so nothing happens in tileEntity
			tEntity.sendServerUpdateMessage(packageReceiverField.getText());
		}

		tEntity.sendPacket(button.id, isShiftKeyDown());
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (packageReceiverField.isFocused()) {
			if (packageReceiverField.getText().contains("Enter ")) {
				packageReceiverField.setText("");
			}
			packageReceiverField.textboxKeyTyped(c, i);
		} else {
			super.keyTyped(c, i);
		}
		if (c == KeyEvent.VK_ESCAPE) {
			super.keyTyped(c, i);
		}
		if (c == KeyEvent.VK_TAB) {
			List<String> players = new ArrayList<String>();
			for (EntityPlayer p : (List<EntityPlayer>) tEntity.getWorldObj().playerEntities) {
				players.add(p.getDisplayName());
			}
			String test[] = new String[1];
			test[0] = packageReceiverField.getText();
			List match = CommandBase.getListOfStringsFromIterableMatchingLastWord(test, players);
			if (match.size() > 0)
				packageReceiverField.setText(match.get(0).toString());
		}
	}

	private boolean canSend() {
		if (tEntity.getStackInSlot(tEntity.itemPackageInputSlot) != null
				&& tEntity.getWorldObj().getPlayerEntityByName(packageReceiverField.getText()) != null) {
			return true;
		}
		return false;
	}
}
