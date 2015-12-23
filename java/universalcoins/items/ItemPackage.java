package universalcoins.items;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;

public class ItemPackage extends Item {

	public ItemPackage() {
		super();
		this.maxStackSize = 1;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister
				.registerIcon(UniversalCoins.MODID + ":" + this.getUnlocalizedName().substring(5));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if (stack.stackTagCompound != null) {
			NBTTagList tagList = stack.stackTagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
				// byte slot = tag.getByte("Slot");
				int itemCount = ItemStack.loadItemStackFromNBT(tag).stackSize;
				String itemName = ItemStack.loadItemStackFromNBT(tag).getDisplayName();
				list.add(itemCount + " " + itemName);
			}

			if(stack.stackTagCompound.hasKey("sender", Constants.NBT.TAG_STRING))
				list.add(EnumChatFormatting.BLUE+ StatCollector.translateToLocal("item.package.by")+" "+stack.stackTagCompound.getString("sender"));
			if(stack.stackTagCompound.hasKey("sent", Constants.NBT.TAG_LONG))
				list.add(EnumChatFormatting.BLUE+ StatCollector.translateToLocal("item.package.on")+" "+
						DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.forLanguageTag(
								Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().replace("_","-")
						)).format(stack.stackTagCompound.getLong("sent")));
			if(stack.stackTagCompound.hasKey("received", Constants.NBT.TAG_LONG))
				list.add(EnumChatFormatting.BLUE+ StatCollector.translateToLocal("item.package.received")+" "+
						DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.forLanguageTag(
								Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().replace("_","-")
						)).format(stack.stackTagCompound.getLong("received")));
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side,
			float px, float py, float pz) {
		if (itemstack.stackTagCompound != null && !world.isRemote) {
			// TODO unpack items and destroy package
			NBTTagList tagList = itemstack.stackTagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
				if (player.inventory.getFirstEmptyStack() != -1) {
					player.inventory.addItemStackToInventory(ItemStack.loadItemStackFromNBT(tag));
				} else {
					// spawn in world
					EntityItem entityItem = new EntityItem(world, x, y, z, ItemStack.loadItemStackFromNBT(tag));
					world.spawnEntityInWorld(entityItem);
				}
			}
			// destroy package
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		}

		return false;
	}

}
