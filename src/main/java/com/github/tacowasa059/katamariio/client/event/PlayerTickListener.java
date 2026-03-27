package com.github.tacowasa059.katamariio.client.event;

import com.github.tacowasa059.katamariio.KatamariIO;
import com.github.tacowasa059.katamariio.common.accessors.ICustomPlayerData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KatamariIO.MODID, value = Dist.CLIENT)
public class PlayerTickListener {
    @SubscribeEvent
    public static void updateAABB(TickEvent.PlayerTickEvent event) {

        Player player = event.player;
        if(player.level().isClientSide && player.tickCount % 5 == 0) {
            ICustomPlayerData playerData = (ICustomPlayerData) player;
            if (playerData.katamariIO$getSize() != player.getBoundingBox().getYsize()) {
                player.refreshDimensions();
            }
        }
    }
}
