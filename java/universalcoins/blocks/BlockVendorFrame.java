package universalcoins.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tile.TileVendor;
import universalcoins.tile.TileVendorFrame;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockVendorFrame extends BlockContainer {
	
	IIcon blockIcon;
	
	public BlockVendorFrame() {
		super(new Material(MapColor.woodColor));
		setHardness(1.0f);
		setBlockTextureName("minecraft:planks_oak"); //fixes missing texture on block break
		setResistance(6000.0F);
		setBlockBounds(0, 0, 0, 0, 0, 0);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
	}
	
	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1, 0);
		TileEntity tentity = world.getTileEntity(x, y, z);
		if (tentity instanceof TileVendorFrame) {
			TileVendorFrame te = (TileVendorFrame) tentity;
			NBTTagList itemList = new NBTTagList();
			NBTTagCompound tagCompound = new NBTTagCompound();
			for (int i = 0; i < te.getSizeInventory(); i++) {
				ItemStack invStack = te.getStackInSlot(i);
				if (invStack != null) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setByte("Slot", (byte) i);
					invStack.writeToNBT(tag);
					itemList.appendTag(tag);
				}
			}tagCompound.setTag("Inventory", itemList);
			tagCompound.setInteger("CoinSum", te.coinSum);
			tagCompound.setInteger("UserCoinSum", te.userCoinSum);
			tagCompound.setInteger("ItemPrice", te.itemPrice);
			tagCompound.setString("BlockOwner", te.blockOwner);
			tagCompound.setBoolean("Infinite", te.infiniteMode);
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}

	@Override
	public boolean isOpaqueCube() {
	   return false;
	}
	
	public boolean renderAsNormalBlock() {
        return false;
    }
	
	public int getRenderType() {
        return -1;
    }
	
	/**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    public void setBlockBoundsBasedOnState(IBlockAccess block, int x, int y, int z) {
        this.getBlockBoundsFromMeta(block.getBlockMetadata(x, y, z));
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    public void getBlockBoundsFromMeta(int meta) {
        if (meta == 0) {
            this.setBlockBounds(0.12f, 0.12f, 0f, 0.88f, 0.88f, 0.07f);
        }
        if (meta == 1) {
            this.setBlockBounds(0.93f, 0.12f, 0.12f, 1.0f, 0.88f, 0.88f);
        }
        if (meta == 2) {
            this.setBlockBounds(0.12f, 0.12f, 0.93f, 0.88f, 0.88f, 1.00f);
        }
        if (meta == 3) {
            this.setBlockBounds(0.07f, 0.12f, 0.12f, 0f, 0.88f, 0.88f);
        }
    }
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (((TileVendor) tileEntity).inUse) {
			if (!world.isRemote) { player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.warning.inuse"))); }
			return true;
		} else {
			player.openGui(UniversalCoins.instance, 0, world, x, y, z);
			((TileVendor) tileEntity).playerName = player.getDisplayName();
			return true;
		}
	}
		
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		//set block meta so we can use it later for rotation
		int rotation = MathHelper.floor_double((double)((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
		world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
		if (world.isRemote) return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileVendorFrame) {
				TileVendorFrame tentity = (TileVendorFrame) te;
				NBTTagCompound tagCompound = stack.getTagCompound();
				if (tagCompound == null) {
					return;
				}
				NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < tagList.tagCount(); i++) {
					NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
					byte slot = tag.getByte("Slot");
					if (slot < tentity.getSizeInventory()) {
						tentity.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
					}
				}
				tentity.coinSum = tagCompound.getInteger("CoinSum");
				tentity.userCoinSum = tagCompound.getInteger("UserCoinSum");
				tentity.itemPrice = tagCompound.getInteger("ItemPrice");
				tentity.blockOwner = player.getCommandSenderName(); //always set to whomever place the block
				tentity.infiniteMode = tagCompound.getBoolean("Infinite");
				tentity.blockIcon = tagCompound.getString("blockIcon");
			}
			world.markBlockForUpdate(x, y, z);	
		}
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		String ownerName = ((TileVendorFrame)world.getTileEntity(x, y, z)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, player, x, y, z);
			return false;
		}
		if (player.getDisplayName().equals(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, x, y, z);
			EntityItem entityItem = new EntityItem(world, x, y, z, stack);
			world.spawnEntityInWorld(entityItem);
			super.removedByPlayer(world, player, x, y, z);
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileVendorFrame();
	}
}
