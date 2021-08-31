package mods.gregtechmod.util;

import ic2.core.block.state.UnlistedBooleanProperty;
import ic2.core.block.state.UnlistedEnumProperty;
import ic2.core.block.state.UnlistedProperty;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Map;
import java.util.Set;

public class PropertyHelper {
    public static final IUnlistedProperty<Boolean> CONNECTED_DOWN = new UnlistedBooleanProperty("connected_down");
    public static final IUnlistedProperty<Boolean> CONNECTED_UP = new UnlistedBooleanProperty("connected_up");
    public static final IUnlistedProperty<Boolean> CONNECTED_NORTH = new UnlistedBooleanProperty("connected_north");
    public static final IUnlistedProperty<Boolean> CONNECTED_SOUTH = new UnlistedBooleanProperty("connected_south");
    public static final IUnlistedProperty<Boolean> CONNECTED_WEST = new UnlistedBooleanProperty("connected_west");
    public static final IUnlistedProperty<Boolean> CONNECTED_EAST = new UnlistedBooleanProperty("connected_east");
        
    public static final IUnlistedProperty<AnimationSpeed> ANIMATION_SPEED_PROPERTY = new UnlistedProperty<>("animationSpeed", AnimationSpeed.class);
    public static final IUnlistedProperty<TextureOverride> TEXTURE_OVERRIDE_PROPERTY = new UnlistedProperty<>("textureOverride", TextureOverride.class);
    public static final IUnlistedProperty<DimensionalTextureInfo> TEXTURE_INDEX_PROPERTY = new UnlistedProperty<>("textureInfo", DimensionalTextureInfo.class);
    public static final IUnlistedProperty<EnumFacing> OUTPUT_SIDE_PROPERTY = new UnlistedEnumProperty<>("outputSide", EnumFacing.class);

    public static class AnimationSpeed {
        private final Set<EnumFacing> sides;
        private final int value;

        public AnimationSpeed(Set<EnumFacing> sides, int value) {
            this.sides = sides;
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public Set<EnumFacing> getSides() {
            return this.sides;
        }
    }

    public static class TextureOverride {
        private final Map<EnumFacing, ResourceLocation> overrides;

        public TextureOverride(Map<EnumFacing, ResourceLocation> overrides) {
            this.overrides = overrides;
        }

        public boolean hasOverride(EnumFacing side) {
            return this.overrides.containsKey(side);
        }

        public ResourceLocation getTextureOverride(EnumFacing side) {
            return this.overrides.get(side);
        }
    }

    public static class DimensionalTextureInfo {
        public final Map<EnumFacing, EnumFacing> sideOverrides;
        public final DimensionType dimension;

        public DimensionalTextureInfo(Map<EnumFacing, EnumFacing> sideOverrides, DimensionType dimension) {
            this.sideOverrides = sideOverrides;
             this.dimension = dimension;
        }
    }
}
