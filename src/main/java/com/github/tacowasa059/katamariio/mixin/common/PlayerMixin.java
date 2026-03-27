package com.github.tacowasa059.katamariio.mixin.common;

import com.github.tacowasa059.katamariio.common.accessors.ICustomPlayerData;
import com.github.tacowasa059.katamariio.common.serializers.ModDataSerializers;
import com.github.tacowasa059.katamariio.common.utils.QuaternionUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * player data parameter
 */
@Mixin(Player.class)
public abstract class PlayerMixin implements ICustomPlayerData {


    @Unique
    private static final EntityDataAccessor<Float> sphericalPlayerMod$SIZE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Boolean> sphericalPlayerMod$FLAG = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Float> RESTITUTION_COEFFICIENT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<CompoundTag> sphericalPlayerMod$QUATERNION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
    @Unique
    private static final EntityDataAccessor<CompoundTag> CURRENT_POSITION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private static final EntityDataAccessor<List<Vec3>> SPHERICAL_PLAYER_POSITION_LIST = SynchedEntityData.defineId(Player.class, ModDataSerializers.VEC3_LIST);
    @Unique
    private static final EntityDataAccessor<List<Quaternionf>> SPHERICAL_PLAYER_QUATERNION_LIST = SynchedEntityData.defineId(Player.class, ModDataSerializers.QUATERNION_LIST);
    @Unique
    private static final EntityDataAccessor<List<Block>> SPHERICAL_PLAYER_BLOCK_LIST = SynchedEntityData.defineId(Player.class, ModDataSerializers.BLOCK_LIST);


    @Unique
    private boolean sphericalPlayerMod$initialized = false;
    @Unique
    private Quaternionf sphericalPlayerMod$quaternion = new Quaternionf(0, 0, 0, 1);
    @Unique
    private Quaternionf sphericalPlayerMod$prevQuaternion = new Quaternionf(0, 0, 0, 1);


    @Inject(method = "tick", at=@At("HEAD"))
    protected void tick(CallbackInfo ci){
        if(!sphericalPlayerMod$initialized){
            sphericalPlayerMod$quaternion = katamariIO$getValidQuaternion(katamariIO$getQuaternion());
            sphericalPlayerMod$prevQuaternion = katamariIO$getValidQuaternion(katamariIO$getQuaternion());
            sphericalPlayerMod$initialized = true;
        }



        //quaternion (client)
        Player player = (Player) (Object)this;
        if(player.level().isClientSide){
            sphericalPlayerMod$prevQuaternion = katamariIO$getValidQuaternion(new Quaternionf(sphericalPlayerMod$quaternion));

            Quaternionf quaternion = QuaternionUtils.getUpdatedQuaternion(player.position(),
                    katamariIO$getCurrentPosition(), (ICustomPlayerData) player);
            if(quaternion == null){
                quaternion = katamariIO$getQuaternion();
            }
            sphericalPlayerMod$quaternion = katamariIO$getValidQuaternion(quaternion);


        }

    }

    @Override
    public Quaternionf katamariIO$getInterpolatedQuaternion(float partialTicks){
        return QuaternionUtils.slerp(sphericalPlayerMod$prevQuaternion, sphericalPlayerMod$quaternion, partialTicks);
    }



    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void defineSynchedData(CallbackInfo ci) {
        Player entity = (Player)(Object)this;
        entity.getEntityData().define(sphericalPlayerMod$SIZE, 2.0f);
        entity.getEntityData().define(sphericalPlayerMod$FLAG, true);
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("x", 0f);
        nbt.putFloat("y", 0f);
        nbt.putFloat("z", 0f);
        nbt.putFloat("w", 1f);

        entity.getEntityData().define(sphericalPlayerMod$QUATERNION, nbt);
        entity.getEntityData().define(RESTITUTION_COEFFICIENT, 0.55f);

        CompoundTag nbt1 = new CompoundTag();
        nbt1.putFloat("x", 0);
        nbt1.putFloat("y", 0);
        nbt1.putFloat("z", 0);
        entity.getEntityData().define(CURRENT_POSITION, nbt1);

        entity.getEntityData().define(SPHERICAL_PLAYER_POSITION_LIST, new ArrayList<>());
        entity.getEntityData().define(SPHERICAL_PLAYER_QUATERNION_LIST, new ArrayList<>());
        entity.getEntityData().define(SPHERICAL_PLAYER_BLOCK_LIST, new ArrayList<>());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeAdditional(CompoundTag compound, CallbackInfo ci) {
        Player entity = (Player)(Object)this;
        SynchedEntityData dataManager = entity.getEntityData();
        compound.putFloat("SPM_Size", dataManager.get(sphericalPlayerMod$SIZE));
        compound.putBoolean("SPM_isBall", dataManager.get(sphericalPlayerMod$FLAG));
        compound.put("SPM_Quaternion", dataManager.get(sphericalPlayerMod$QUATERNION));
        compound.putFloat("SPM_RESTITUTION", dataManager.get(RESTITUTION_COEFFICIENT));
        compound.put("SPM_POSITION", dataManager.get(CURRENT_POSITION));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditional(CompoundTag compound, CallbackInfo ci) {
        Player entity = (Player)(Object)this;
        SynchedEntityData dataManager = entity.getEntityData();

        if (compound.contains("SPM_Size")) {
            katamariIO$setSize(compound.getFloat("SPM_Size"));
        }
        if (compound.contains("SPM_RESTITUTION")) {
            katamariIO$setRestitutionCoefficient(compound.getFloat("SPM_RESTITUTION"));
        }
        if (compound.contains("SPM_isBall")) {
            katamariIO$setFlag(compound.getBoolean("SPM_isBall"));
        }
        if (compound.contains("SPM_Quaternion")) {
            dataManager.set(sphericalPlayerMod$QUATERNION,compound.getCompound("SPM_Quaternion"));
        }
        if(compound.contains("SPM_POSITION")){
            dataManager.set(CURRENT_POSITION, compound.getCompound("SPM_POSITION"));
        }
    }
    @Inject(method="getStandingEyeHeight",at=@At("HEAD"),cancellable = true)
    public void getStandingEyeHeight(Pose p_213348_1_, EntityDimensions p_213348_2_, CallbackInfoReturnable<Float> cir) {
        if(katamariIO$getFlag()){
            cir.setReturnValue(0.85F* katamariIO$getSize());
        }
    }
    @Override
    public void katamariIO$setSize(float size) {
        Player entity = (Player)(Object)this;
        entity.getEntityData().set(sphericalPlayerMod$SIZE, size);
        entity.refreshDimensions();
    }

    @Override
    public float katamariIO$getSize() {
        Player entity = (Player)(Object)this;
        return entity.getEntityData().get(sphericalPlayerMod$SIZE);
    }

    @Override
    public void katamariIO$setRestitutionCoefficient(float value){
        Player entity = (Player)(Object)this;
        entity.getEntityData().set(RESTITUTION_COEFFICIENT, value);
    }
    @Override
    public float katamariIO$getRestitutionCoefficient(){
        Player entity = (Player)(Object)this;
        return entity.getEntityData().get(RESTITUTION_COEFFICIENT);
    }

    @Override
    public void katamariIO$setFlag(boolean flag) {
        Player entity = (Player)(Object)this;
        entity.getEntityData().set(sphericalPlayerMod$FLAG, flag);
        entity.refreshDimensions();
    }

    @Override
    public void katamariIO$setFlagAndSizeAndRestitution(boolean flag, float size, float value) {
        Player entity = (Player)(Object)this;
        entity.getEntityData().set(sphericalPlayerMod$FLAG, flag);
        entity.getEntityData().set(sphericalPlayerMod$SIZE, size);
        entity.getEntityData().set(RESTITUTION_COEFFICIENT, value);
        entity.refreshDimensions();
    }

    @Override
    public boolean katamariIO$getFlag() {
        Player entity = (Player)(Object)this;
        return entity.getEntityData().get(sphericalPlayerMod$FLAG);
    }

    @Override
    public void katamariIO$setQuaternion(Quaternionf quaternion) {
        Player entity = (Player)(Object)this;
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("x", quaternion.x());
        nbt.putFloat("y", quaternion.y());
        nbt.putFloat("z", quaternion.z());
        nbt.putFloat("w", quaternion.w());
        entity.getEntityData().set(sphericalPlayerMod$QUATERNION, nbt);
    }
    @Override
    public Quaternionf katamariIO$getQuaternion() {
        Player entity = (Player)(Object)this;
        CompoundTag quaternionNBT = entity.getEntityData().get(sphericalPlayerMod$QUATERNION);
        Quaternionf quaternion = new Quaternionf(
                quaternionNBT.getFloat("x"),
                quaternionNBT.getFloat("y"),
                quaternionNBT.getFloat("z"),
                quaternionNBT.getFloat("w")
        );
        return katamariIO$getValidQuaternion(quaternion);
    }

    @Unique
    private static Quaternionf katamariIO$getValidQuaternion(Quaternionf quaternion) {
        if(quaternion.x()==0 && quaternion.y()==0 && quaternion.z()==0 && quaternion.w()==0) {
            return new Quaternionf(0, 0, 0, 1);
        }
        return quaternion;
    }

    @Override
    public void katamariIO$setCurrentPosition(Vec3 pos){
        Player entity = (Player)(Object)this;
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("x", (float) pos.x());
        nbt.putFloat("y", (float) pos.y());
        nbt.putFloat("z", (float) pos.z());
        entity.getEntityData().set(CURRENT_POSITION, nbt);
    }
    @Override
    public Vec3 katamariIO$getCurrentPosition(){
        Player entity = (Player)(Object)this;
        CompoundTag compoundNBT = entity.getEntityData().get(CURRENT_POSITION);
        return new Vec3(
                compoundNBT.getFloat("x"),
                compoundNBT.getFloat("y"),
                compoundNBT.getFloat("z")
        );
    }

    @Inject(method = "getMyRidingOffset",at=@At("HEAD"),cancellable = true)
    public void getYOffset(CallbackInfoReturnable<Double> cir) {
        Entity entity =(Entity)(Object)this;
        ICustomPlayerData playerData =(ICustomPlayerData) entity;
        if(playerData.katamariIO$getFlag() )cir.setReturnValue(0.15);
    }

    @Inject(method = "getDimensions", at=@At("HEAD"), cancellable = true)
    public void getDimensions(Pose p_213305_1_, CallbackInfoReturnable<EntityDimensions> cir) {
        if(katamariIO$getFlag()) {
            EntityDimensions entityDimensions = EntityDimensions.scalable(katamariIO$getSize(), katamariIO$getSize());
            cir.setReturnValue(entityDimensions);
            cir.cancel();
        }
    }

    @Inject(method = "maybeBackOffFromEdge", at=@At("HEAD"), cancellable = true)
    protected void maybeBackOffFromEdge(Vec3 p_36201_, MoverType p_36202_, CallbackInfoReturnable<Vec3> cir) {
        if(katamariIO$getFlag()) {
            cir.setReturnValue(p_36201_);
        }
    }

    @Override
    public void katamariIO$addBlock(Block block, Quaternionf quaternionf, Vec3 vec3) {
        Player entity = (Player)(Object)this;
        List<Block> blocks = entity.getEntityData().get(SPHERICAL_PLAYER_BLOCK_LIST);
        List<Quaternionf> quaternionfs = entity.getEntityData().get(SPHERICAL_PLAYER_QUATERNION_LIST);
        List<Vec3> vec3s = entity.getEntityData().get(SPHERICAL_PLAYER_POSITION_LIST);

        blocks.add(block);
        quaternionfs.add(quaternionf);
        vec3s.add(vec3);

        entity.getEntityData().set(SPHERICAL_PLAYER_BLOCK_LIST, blocks, true);
        entity.getEntityData().set(SPHERICAL_PLAYER_POSITION_LIST, vec3s, true);
        entity.getEntityData().set(SPHERICAL_PLAYER_QUATERNION_LIST, quaternionfs, true);
    }

    @Override
    public List<Vec3> katamariIO$getSphericalPlayerPositions() {
        Player entity = (Player)(Object)this;
        return entity.getEntityData().get(SPHERICAL_PLAYER_POSITION_LIST);
    }
    @Override
    public List<Quaternionf> katamariIO$getSphericalPlayerQuaternions() {
        Player entity = (Player)(Object)this;
        return entity.getEntityData().get(SPHERICAL_PLAYER_QUATERNION_LIST);
    }
    @Override
    public List<Block> katamariIO$getSphericalPlayerBlocks() {
        Player entity = (Player)(Object)this;
        return entity.getEntityData().get(SPHERICAL_PLAYER_BLOCK_LIST);
    }

}
