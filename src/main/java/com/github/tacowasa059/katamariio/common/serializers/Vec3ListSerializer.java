package com.github.tacowasa059.katamariio.common.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Vec3ListSerializer implements EntityDataSerializer<List<Vec3>> {

    @Override
    public void write(FriendlyByteBuf buf, List<Vec3> list) {
        buf.writeVarInt(list.size());
        for (Vec3 v : list) {
            buf.writeDouble(v.x);
            buf.writeDouble(v.y);
            buf.writeDouble(v.z);
        }
    }

    @Override
    public @NotNull List<Vec3> read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Vec3> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            list.add(new Vec3(x, y, z));
        }
        return list;
    }

    @Override
    public @NotNull List<Vec3> copy(@NotNull List<Vec3> list) {
        return new ArrayList<>(list);
    }
}

