package universalcoins.net;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import universalcoins.tile.TileUCSign;

public class UCTileSignMessage implements IMessage, IMessageHandler<UCTileSignMessage, IMessage> {
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private String[] signText;
	private String blockOwner;
	private String blockIcon;

	public UCTileSignMessage() {
	}

	public UCTileSignMessage(int x, int y, int z, String[] signText, String blockOwner, String blockIcon) {
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
		this.signText = new String[] { signText[0], signText[1], signText[2], signText[3] };
		this.blockOwner = blockOwner;
		this.blockIcon = blockIcon;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.xCoord = buf.readInt();
		this.yCoord = buf.readShort();
		this.zCoord = buf.readInt();
		this.signText = new String[4];
		for (int i = 0; i < 4; ++i) {
			this.signText[i] = ByteBufUtils.readUTF8String(buf);
		}
		this.blockOwner = ByteBufUtils.readUTF8String(buf);
		this.blockIcon = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.xCoord);
		buf.writeShort(this.yCoord);
		buf.writeInt(this.zCoord);

		for (int i = 0; i < 4; ++i) {
			ByteBufUtils.writeUTF8String(buf, this.signText[i]);
		}
		ByteBufUtils.writeUTF8String(buf, this.blockOwner);
		ByteBufUtils.writeUTF8String(buf, this.blockIcon);
	}

	@SideOnly(Side.CLIENT)
	public String[] getTranslatedText()
	{
		String[] lines = new String[4];
		for(int i = 0; i < 4; i++)
		{
			String str = signText[i];
			if(!str.isEmpty() && str.charAt(0) == '{')
				try
				{
					IChatComponent component = IChatComponent.Serializer.func_150699_a(str);
					str = component.getFormattedText();
					str = str.replaceFirst("^\u00A7([0-9a-fl-r])", "&\\1");
					str = str.replaceAll("\u00A7[0-9a-fl-r]", "");
				}
				catch (Exception ignored)
				{}

			lines[i] = str;
		}

		return lines;
	}

	@Override
	public IMessage onMessage(UCTileSignMessage message, MessageContext ctx) {
		TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld.getTileEntity(message.xCoord,
				message.yCoord, message.zCoord);

		if (tileEntity != null && tileEntity instanceof TileUCSign) {
			((TileUCSign) tileEntity).signText = message.getTranslatedText();
			((TileUCSign) tileEntity).blockOwner = message.blockOwner;
			((TileUCSign) tileEntity).blockIcon = message.blockIcon;
		}
		return null;
	}
}