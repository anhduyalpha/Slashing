package com.main.slashing.fx;

import net.minecraft.world.phys.Vec3;

public final class FxMath {
    private FxMath() {}

    // THÊM forward vào Basis để dùng b.forward()
    public record Basis(Vec3 right, Vec3 up, Vec3 forward) {}

    public static Basis basisFromForward(Vec3 forward) {
        Vec3 f = forward.normalize();

        Vec3 worldUp = new Vec3(0, 1, 0);

        Vec3 right = f.cross(worldUp);
        if (right.lengthSqr() < 1.0e-6) right = new Vec3(1, 0, 0);
        right = right.normalize();

        Vec3 up = right.cross(f).normalize();

        return new Basis(right, up, f);
    }
}
