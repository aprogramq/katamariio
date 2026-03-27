package com.github.tacowasa059.katamariio.client.event;

import com.github.tacowasa059.katamariio.KatamariIO;
import com.github.tacowasa059.katamariio.common.accessors.ICustomPlayerData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KatamariIO.MODID, bus= Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
public class SneakEventListener {
    @SubscribeEvent
    public static void onInputUpdate(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        ICustomPlayerData playerData = (ICustomPlayerData) player;
        if (player.onGround() && !player.isPassenger() && playerData.katamariIO$getFlag()) {
            event.getInput().shiftKeyDown = false;
        }
    }
}
