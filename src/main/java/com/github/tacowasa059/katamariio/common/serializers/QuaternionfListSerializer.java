package com.github.tacowasa059.katamariio.common.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class QuaternionfListSerializer implements EntityDataSerializer<List<Quaternionf>> {

    @Override
    public void write(FriendlyByteBuf buf, List<Quaternionf> list) {
        buf.writeVarInt(list.size());
        for (Quaternionf q : list) {
            buf.writeFloat(q.x());
            buf.writeFloat(q.y());
            buf.writeFloat(q.z());
            buf.writeFloat(q.w());
        }
    }

    @Override
    public @NotNull List<Quaternionf> read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Quaternionf> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            float x = buf.readFloat();
            float y = buf.readFloat();
            float z = buf.readFloat();
            float w = buf.readFloat();
            list.add(new Quaternionf(x, y, z, w));
        }
        return list;
    }

    @Override
    public @NotNull List<Quaternionf> copy(List<Quaternionf> list) {
        List<Quaternionf> copy = new ArrayList<>(list.size());
        for (Quaternionf q : list) {
            copy.add(new Quaternionf(q));
        }
        return copy;
    }
}

