package com.github.tacowasa059.katamariio.client.event;

import com.github.tacowasa059.katamariio.KatamariIO;
import com.github.tacowasa059.katamariio.common.accessors.ICustomPlayerData;
import com.github.tacowasa059.katamariio.common.accessors.IPlayerRendererAccessor;
import com.github.tacowasa059.katamariio.client.utils.SphereRenderer;
import com.github.tacowasa059.katamariio.common.utils.QuaternionUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

import java.util.List;

@Mod.EventBusSubscriber(modid = KatamariIO.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerRendererListener {

    public static double MaxSize = 0;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void render(RenderLivingEvent.Pre<Player, PlayerModel<Player>> event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player1) {
            AbstractClientPlayer player = (AbstractClientPlayer) entity;
            ICustomPlayerData playerData =(ICustomPlayerData)player1;

            boolean flag = playerData.katamariIO$getFlag();
            if(!flag) return;
            if (entity.isInvisible()){
                event.setCanceled(true);
                return;
            }
            float size = playerData.katamariIO$getSize();

            float partialTicks = event.getPartialTick();

            Quaternionf quaternion = playerData.katamariIO$getInterpolatedQuaternion(partialTicks);

            ResourceLocation texture = player.getSkinTextureLocation();
            PoseStack poseStack  = event.getPoseStack();

            MultiBufferSource buffer = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();
            poseStack.pushPose();
            poseStack.translate(0,size/2.0,0);
            if(player.getVehicle()==null) poseStack.mulPose(quaternion);
            else poseStack.mulPose(QuaternionUtils.getQuaternionFromEntity(entity));

            int overlay = OverlayTexture.NO_OVERLAY;
            if(player1.hurtTime > 0 || player1.deathTime > 0)overlay = OverlayTexture.pack(OverlayTexture.u(event.getPartialTick()),
                    OverlayTexture.v(player1.hurtTime > 0 || player1.deathTime > 0));

            poseStack.pushPose();
            SphereRenderer.drawTexturedSphere(poseStack, buffer, texture, (float) (size / 2.0), 12, 0, 0, packedLight,true, overlay);
            poseStack.popPose();

            List<Vec3> vec3List = playerData.katamariIO$getSphericalPlayerPositions();
            List<Block> blockList = playerData.katamariIO$getSphericalPlayerBlocks();
            List<Quaternionf> quaternionfList = playerData.katamariIO$getSphericalPlayerQuaternions();

            double tmp_Size = playerData.katamariIO$getSize();

            for(int i=0; i<vec3List.size(); i++){
                Vec3 vec = vec3List.get(i);
                Block block = blockList.get(i);
                Quaternionf quaternionf = quaternionfList.get(i);

                poseStack.pushPose();
                poseStack.translate(vec.x, vec.y, vec.z);
                poseStack.mulPose(quaternionf);

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),poseStack, buffer, packedLight, overlay, ModelData.EMPTY, null);

                poseStack.popPose();

                if(player.equals(Minecraft.getInstance().player)){
                    tmp_Size = Math.max(tmp_Size, vec.length()*2);
                }
            }

            if(player.equals(Minecraft.getInstance().player)){
                MaxSize = tmp_Size;
            }

            poseStack.popPose();

            EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);

            if (player instanceof RemotePlayer && renderer instanceof IPlayerRendererAccessor playerRendererAccessor) {
                if(playerRendererAccessor.katamariIO$shouldShowName(player))
                    playerRendererAccessor.katamariIO$callRenderName(player, player.getDisplayName(), poseStack, buffer, packedLight);
            }
            event.setCanceled(true);
        }
    }

}
