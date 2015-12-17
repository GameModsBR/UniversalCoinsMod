package universalcoins.net;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import universalcoins.tile.TileCardStation;

public class UCTileCardStationMessage implements IMessage, IMessageHandler<UCTileCardStationMessage, IMessage> {
	private int x, y, z, coinWithdrawalAmount, accountBalance, forcedMenuState = -1, resetBarProgress, lockBarProgress = -1;
	private boolean inUse, depositCoins, withdrawCoins, accountError;
	private String playerName, playerUID, accountNumber, cardOwner, groupAccountName, groupAccountNumber;

	public UCTileCardStationMessage() {
	}

	public UCTileCardStationMessage(TileCardStation tileEntity) {
		this.x = tileEntity.xCoord;
		this.y = tileEntity.yCoord;
		this.z = tileEntity.zCoord;
		this.coinWithdrawalAmount = tileEntity.coinWithdrawalAmount;
		this.accountBalance = tileEntity.accountBalance;
		this.inUse = tileEntity.inUse;
		this.depositCoins = tileEntity.depositCoins;
		this.withdrawCoins = tileEntity.withdrawCoins;
		this.accountError = tileEntity.accountError;
		this.playerName = tileEntity.playerName;
		this.playerUID = tileEntity.playerUID;
		this.accountNumber = tileEntity.accountNumber;
		this.cardOwner = tileEntity.cardOwner;
		this.groupAccountName = tileEntity.customAccountName;
		this.groupAccountNumber = tileEntity.customAccountNumber;
		this.forcedMenuState = tileEntity.forcedMenuState;
		this.resetBarProgress = tileEntity.resetBarProgress;
		this.lockBarProgress = tileEntity.lockBarProgress;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.coinWithdrawalAmount = buf.readInt();
		this.accountBalance = buf.readInt();
		this.inUse = buf.readBoolean();
		this.depositCoins = buf.readBoolean();
		this.withdrawCoins = buf.readBoolean();
		this.accountError = buf.readBoolean();
		this.playerName = ByteBufUtils.readUTF8String(buf);
		this.playerUID = ByteBufUtils.readUTF8String(buf);
		this.accountNumber = ByteBufUtils.readUTF8String(buf);
		this.cardOwner = ByteBufUtils.readUTF8String(buf);
		this.groupAccountName = ByteBufUtils.readUTF8String(buf);
		this.groupAccountNumber = ByteBufUtils.readUTF8String(buf);
		this.forcedMenuState = buf.readInt();
		this.resetBarProgress = buf.readInt();
		this.lockBarProgress = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(coinWithdrawalAmount);
		buf.writeInt(accountBalance);
		buf.writeBoolean(inUse);
		buf.writeBoolean(depositCoins);
		buf.writeBoolean(withdrawCoins);
		buf.writeBoolean(accountError);
		ByteBufUtils.writeUTF8String(buf, playerName);
		ByteBufUtils.writeUTF8String(buf, playerUID);
		ByteBufUtils.writeUTF8String(buf, accountNumber);
		ByteBufUtils.writeUTF8String(buf, cardOwner);
		ByteBufUtils.writeUTF8String(buf, groupAccountName);
		ByteBufUtils.writeUTF8String(buf, groupAccountNumber);
		buf.writeInt(forcedMenuState);
		buf.writeInt(resetBarProgress);
		buf.writeInt(lockBarProgress);
	}

	@Override
	public IMessage onMessage(UCTileCardStationMessage message, MessageContext ctx) {
		TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld.getTileEntity(message.x, message.y,
				message.z);

		if (tileEntity instanceof TileCardStation) {
			// FMLLog.info("UC: received TE packet");
			((TileCardStation) tileEntity).coinWithdrawalAmount = message.coinWithdrawalAmount;
			((TileCardStation) tileEntity).accountBalance = message.accountBalance;
			((TileCardStation) tileEntity).inUse = message.inUse;
			((TileCardStation) tileEntity).depositCoins = message.depositCoins;
			((TileCardStation) tileEntity).withdrawCoins = message.withdrawCoins;
			((TileCardStation) tileEntity).accountError = message.accountError;
			((TileCardStation) tileEntity).playerName = message.playerName;
			((TileCardStation) tileEntity).playerUID = message.playerUID;
			((TileCardStation) tileEntity).accountNumber = message.accountNumber;
			((TileCardStation) tileEntity).cardOwner = message.cardOwner;
			((TileCardStation) tileEntity).customAccountName = message.groupAccountName;
			((TileCardStation) tileEntity).customAccountNumber = message.groupAccountNumber;
			((TileCardStation) tileEntity).forcedMenuState = message.forcedMenuState;
			((TileCardStation) tileEntity).resetBarProgress = message.resetBarProgress;
			((TileCardStation) tileEntity).lockBarProgress = message.lockBarProgress;
		}
		return null;
	}
}