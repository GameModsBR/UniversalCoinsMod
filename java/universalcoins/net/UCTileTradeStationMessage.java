package universalcoins.net;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tile.TileTradeStation;

public class UCTileTradeStationMessage implements IMessage, IMessageHandler<UCTileTradeStationMessage, IMessage> {
	public int x, y, z, coinSum, itemPrice;
	public String customName, playerName;
	private boolean buyButtonActive, sellButtonActive, autoModeButtonActive, coinButtonActive, isSStackButtonActive,
			isLStackButtonActive, isSBagButtonActive, isLBagButtonActive, inUse;

	public UCTileTradeStationMessage() {
	}

	public UCTileTradeStationMessage(TileTradeStation tileEntity) {
		this.x = tileEntity.xCoord;
		this.y = tileEntity.yCoord;
		this.z = tileEntity.zCoord;
		this.coinSum = tileEntity.coinSum;
		this.itemPrice = tileEntity.itemPrice;
		this.customName = tileEntity.getInventoryName();
		this.playerName = tileEntity.playerName;
		this.buyButtonActive = tileEntity.buyButtonActive;
		this.sellButtonActive = tileEntity.sellButtonActive;
		this.autoModeButtonActive = tileEntity.autoModeButtonActive;
		this.coinButtonActive = tileEntity.coinButtonActive;
		this.isSStackButtonActive = tileEntity.isSStackButtonActive;
		this.isLStackButtonActive = tileEntity.isLStackButtonActive;
		this.isSBagButtonActive = tileEntity.isSBagButtonActive;
		this.isLBagButtonActive = tileEntity.isLBagButtonActive;
		this.inUse = tileEntity.inUse;

	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.coinSum = buf.readInt();
		this.itemPrice = buf.readInt();
		this.customName = ByteBufUtils.readUTF8String(buf);
		this.playerName = ByteBufUtils.readUTF8String(buf);
		this.buyButtonActive = buf.readBoolean();
		this.sellButtonActive = buf.readBoolean();
		this.autoModeButtonActive = buf.readBoolean();
		this.coinButtonActive = buf.readBoolean();
		this.isSStackButtonActive = buf.readBoolean();
		this.isLStackButtonActive = buf.readBoolean();
		this.isSBagButtonActive = buf.readBoolean();
		this.isLBagButtonActive = buf.readBoolean();
		this.inUse = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(coinSum);
		buf.writeInt(itemPrice);
		ByteBufUtils.writeUTF8String(buf, customName);
		ByteBufUtils.writeUTF8String(buf, playerName);
		buf.writeBoolean(buyButtonActive);
		buf.writeBoolean(sellButtonActive);
		buf.writeBoolean(autoModeButtonActive);
		buf.writeBoolean(coinButtonActive);
		buf.writeBoolean(isSStackButtonActive);
		buf.writeBoolean(isLStackButtonActive);
		buf.writeBoolean(isSBagButtonActive);
		buf.writeBoolean(isLBagButtonActive);
		buf.writeBoolean(inUse);
	}

	@Override
	public IMessage onMessage(UCTileTradeStationMessage message, MessageContext ctx) {
		TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld.getTileEntity(message.x, message.y,
				message.z);

		if (tileEntity instanceof TileTradeStation) {
			// FMLLog.info("UC: received TE packet");
			((TileTradeStation) tileEntity).coinSum = message.coinSum;
			((TileTradeStation) tileEntity).itemPrice = message.itemPrice;
			((TileTradeStation) tileEntity).setInventoryName(message.customName);
			((TileTradeStation) tileEntity).playerName = message.playerName;
			((TileTradeStation) tileEntity).buyButtonActive = message.buyButtonActive;
			((TileTradeStation) tileEntity).sellButtonActive = message.sellButtonActive;
			((TileTradeStation) tileEntity).autoModeButtonActive = message.autoModeButtonActive;
			((TileTradeStation) tileEntity).coinButtonActive = message.coinButtonActive;
			((TileTradeStation) tileEntity).isSStackButtonActive = message.isSStackButtonActive;
			((TileTradeStation) tileEntity).isLStackButtonActive = message.isLStackButtonActive;
			((TileTradeStation) tileEntity).isSBagButtonActive = message.isSBagButtonActive;
			((TileTradeStation) tileEntity).isLBagButtonActive = message.isLBagButtonActive;
			((TileTradeStation) tileEntity).inUse = message.inUse;
		}
		return null;
	}
}