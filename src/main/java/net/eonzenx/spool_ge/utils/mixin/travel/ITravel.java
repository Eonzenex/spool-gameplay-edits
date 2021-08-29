package net.eonzenx.spool_ge.utils.mixin.travel;

import net.minecraft.util.math.Vec3d;

public interface ITravel
{
    void travelOther(Vec3d movementInput);
    void travel(Vec3d movementInput);
}
