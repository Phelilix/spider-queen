package sq.core.radix;

import radixcore.network.AbstractPacketHandler;
import radixcore.packets.PacketDataContainer;
import sq.packet.PacketSleepC;
import sq.packet.PacketSleepS;
import cpw.mods.fml.relauncher.Side;

/**
 * Spider Queen's packet handler from RadixCore.
 */
public final class SpiderPacketHandler extends AbstractPacketHandler
{
	public SpiderPacketHandler(String modId) 
	{
		super(modId);
	}

	@Override
	public void registerPackets() 
	{
		registerPacket(PacketDataContainer.class, Side.CLIENT);
		registerPacket(PacketSleepC.class, Side.CLIENT);
		registerPacket(PacketSleepS.class, Side.SERVER);
	}
}
