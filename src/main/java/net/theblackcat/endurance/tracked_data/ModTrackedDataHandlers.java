package net.theblackcat.endurance.tracked_data;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.Vec3d;
import net.theblackcat.endurance.Endurance;

public class ModTrackedDataHandlers {
    public static final TrackedDataHandler<Vec3d> VECTOR_3D = new TrackedDataHandler<>() {
        @Override
        public PacketCodec<? super RegistryByteBuf, Vec3d> codec() {
            return Vec3d.PACKET_CODEC;
        }

        @Override
        public Vec3d copy(Vec3d value) {
            return new Vec3d(value.getX(), value.getY(), value.getZ());
        }
    };

    public static void Register() {
        FabricTrackedDataRegistry.register(Endurance.Id("tracked_data.vec3d"), VECTOR_3D);
    }
}
