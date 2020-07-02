package mods.gregtechmod.common.cover;

import ic2.core.block.BlockTileEntity;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.model.AbstractModel;
import ic2.core.model.BasicBakedBlockModel;
import ic2.core.model.ModelUtil;
import ic2.core.model.VdUtil;
import ic2.core.util.Util;
import mods.gregtechmod.common.util.GtProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.IntBuffer;
import java.util.*;
import java.util.function.Function;

import static ic2.core.model.VdUtil.generateBlockVertex;

@SideOnly(Side.CLIENT)
public class RenderTeBlock extends AbstractModel {
    private final HashMap<EnumFacing, ResourceLocation> initialTextures;
    private final Map<ResourceLocation, TextureAtlasSprite> textures;
    private final ResourceLocation particle;

    public RenderTeBlock(HashMap<EnumFacing, ResourceLocation> map, ResourceLocation particle) {
        this.initialTextures = map;
        this.particle = particle;
        this.textures = generateTextureLocations();
        textures.put(this.particle, null);
    }

    public Collection<ResourceLocation> getTextures() {
        return this.textures.keySet();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        for (Map.Entry<ResourceLocation, TextureAtlasSprite> entry : this.textures.entrySet()) {
            entry.setValue(bakedTextureGetter.apply(entry.getKey()));
        }
        return this;
    }

    private HashMap<ResourceLocation, TextureAtlasSprite> generateTextureLocations() {
        HashMap<ResourceLocation, TextureAtlasSprite> ret = new HashMap<>();
        for (ResourceLocation loc : initialTextures.values()) {
            ret.put(loc, null);
        }
        return ret;
    }

    public List<BakedQuad> getQuads(IBlockState rawState, EnumFacing side, long rand) {
        if (!(rawState instanceof Ic2BlockState.Ic2BlockStateInstance)) return ModelUtil.getMissingModel().getQuads(rawState, side, rand);

        return generateModel(rawState).getQuads(rawState, side, rand);
    }

    private IBakedModel generateModel(IBlockState rawState) {
        System.out.println("generating model");
        HashMap<EnumFacing, ResourceLocation> covers = new HashMap<>();
        EnumFacing face = rawState.getValue(BlockTileEntity.facingProperty);
        Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
        if (state.hasValue(CoverHandler.coverHandlerProperty)) {
            HashMap<EnumFacing, ICover> coverMap = state.getValue(CoverHandler.coverHandlerProperty).covers;
            for (Map.Entry<EnumFacing, ICover> entry : coverMap.entrySet()) {
                EnumFacing aSide = entry.getKey();
                covers.put(aSide, coverMap.get(aSide).getIcon());
            }
        }
        float th = 1;
        float sp = 0;
        List<BakedQuad>[] arrayOfList = new List[EnumFacing.VALUES.length];
        for (int i = 0; i < arrayOfList.length; i++) arrayOfList[i] = new ArrayList<>();
        List<BakedQuad> generalQuads = new ArrayList<>();
        for (EnumFacing side : EnumFacing.VALUES) {
            EnumFacing currentFacing = side;
            //Rotation TODO: Unlock UVs?
            if (!covers.containsKey(side) && face != EnumFacing.NORTH) {
                if (face == side) currentFacing = EnumFacing.NORTH;
                else if (Util.verticalFacings.contains(face)) {
                    if (side == EnumFacing.NORTH) currentFacing = EnumFacing.SOUTH;
                    else if (side == EnumFacing.SOUTH) currentFacing = EnumFacing.UP;
                }
                else if (!Util.verticalFacings.contains(side)) {
                    if (face == EnumFacing.SOUTH) currentFacing = side.getOpposite();
                    else currentFacing = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? side.rotateY() : side.rotateY().getOpposite();
                }
            }
            TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
            TextureAtlasSprite sprite;
            GtProperties.AnimationSpeed prop;
            GtProperties.TextureOverride overrides;

            if (covers.containsKey(side)) sprite = map.getAtlasSprite(covers.get(currentFacing).toString());
            else if ((prop = state.getValue(GtProperties.ANIMATION_SPEED_PROPERTY)) != null && prop.getSides().contains(currentFacing) && prop.getValue() > 1) {
                sprite = map.getAtlasSprite(initialTextures.get(currentFacing).toString() + prop.getValue());
            }
            else if ((overrides = state.getValue(GtProperties.TEXTURE_OVERRIDE_PROPERTY)) != null && overrides.hasOverride(currentFacing)) {
                sprite = map.getAtlasSprite(overrides.getTextureOverride(currentFacing).toString());
            }
            else sprite = textures.get(initialTextures.get(currentFacing));

            float zS = sp, yS = zS, xS = yS;
            float zE = sp + th, yE = zE, xE = yE;
            IntBuffer buffer = VdUtil.getQuadBuffer();
            generateQuad(xS, yS, zS, xE, yE, zE, side, face, sprite, state.hasValue(GtProperties.UV_LOCK_PROPERTY) ? state.getValue(GtProperties.UV_LOCK_PROPERTY) : true, buffer);
            BakedQuad quad = new BakedQuad(Arrays.copyOf(buffer.array(), buffer.position()), -1, side, sprite, false, VdUtil.vertexFormat);
            arrayOfList[side.ordinal()].add(quad);
            generalQuads.add(quad);
            buffer.rewind();
        }
        int used = 0;
        for (int j = 0; j < arrayOfList.length; j++) {
            if (arrayOfList[j].isEmpty()) {
                arrayOfList[j] = Collections.emptyList();
            } else {
                used++;
            }
        }
        if (used == 0) arrayOfList = null;
        if (generalQuads.isEmpty()) generalQuads = Collections.emptyList();
        return new BasicBakedBlockModel(arrayOfList, generalQuads, null);
    }

    private static void generateQuad(float xS, float yS, float zS, float xE, float yE, float zE, EnumFacing side, EnumFacing face, TextureAtlasSprite sprite, boolean uvlock, IntBuffer quadBuffer) {
        float spriteU = sprite.getMinU();
        float spriteV = sprite.getMinV();

        float spriteWidth = sprite.getMaxU() - spriteU;
        float spriteHeight = sprite.getMaxV() - spriteV;

        float reverseU = sprite.getMaxU();
        float reverseWidth = sprite.getMinU() - reverseU;

        float xA = xS;
        float zA = zS;
        float yA = yS;
        float xB = xE;
        float zB = zE;
        float yB = yE;

        float xC = xS;
        float zC = zS;
        float yC = yS;
        float xD = xE;
        float zD = zE;
        float yD = yE;

        if (!uvlock) {
            if (face == EnumFacing.SOUTH) {
                xA = zA = xC = zC = xE;
                xB = zB = xD = zD = xS;
            }
            else if (face == EnumFacing.EAST) {
                xA = zA = yA = xE;
                xB = zB = yB = xS;
            }
            else if (face == EnumFacing.WEST) {
                xC = zC = yC = xE;
                xD = zD = yD = xS;
            }
        }

        switch(side) {
            case DOWN:
                generateBlockVertex(xS, yS, zS, spriteU + spriteWidth * xA, spriteV + spriteHeight * zA, side, quadBuffer); // 0 0
                generateBlockVertex(xE, yS, zS, spriteU + spriteWidth * xB, spriteV + spriteHeight * zA, side, quadBuffer); // 1 0
                generateBlockVertex(xE, yS, zE, spriteU + spriteWidth * xB, spriteV + spriteHeight * zB, side, quadBuffer); // 1 1
                generateBlockVertex(xS, yS, zE, spriteU + spriteWidth * xA, spriteV + spriteHeight * zB, side, quadBuffer); // 0 1
                break;
            case UP:
                generateBlockVertex(xS, yE, zS, spriteU + spriteWidth * xA, spriteV + spriteHeight * zA, side, quadBuffer); // 0 0
                generateBlockVertex(xS, yE, zE, spriteU + spriteWidth * xC, spriteV + spriteHeight * zD, side, quadBuffer); // 0 1
                generateBlockVertex(xE, yE, zE, spriteU + spriteWidth * xB, spriteV + spriteHeight * zB, side, quadBuffer); // 1 1
                generateBlockVertex(xE, yE, zS, spriteU + spriteWidth * xD, spriteV + spriteHeight * zC, side, quadBuffer); // 1 0
                break;
            case NORTH:
                generateBlockVertex(xS, yS, zS, spriteU + spriteWidth * xD, spriteV + spriteHeight * yD, side, quadBuffer); // 1 1
                generateBlockVertex(xS, yE, zS, spriteU + spriteWidth * xD, spriteV + spriteHeight * yC, side, quadBuffer); // 1 0
                generateBlockVertex(xE, yE, zS, spriteU + spriteWidth * xC, spriteV + spriteHeight * yC, side, quadBuffer); // 0 0
                generateBlockVertex(xE, yS, zS, spriteU + spriteWidth * xC, spriteV + spriteHeight * yD, side, quadBuffer); // 0 1
                break;
            case SOUTH:
                generateBlockVertex(xS, yS, zE, reverseU + reverseWidth * xE, spriteV + spriteHeight * yE, side, quadBuffer); // 1 1
                generateBlockVertex(xE, yS, zE, reverseU + reverseWidth * xS, spriteV + spriteHeight * yE, side, quadBuffer); // 0 1
                generateBlockVertex(xE, yE, zE, reverseU + reverseWidth * xS, spriteV + spriteHeight * yS, side, quadBuffer); // 0 0
                generateBlockVertex(xS, yE, zE, reverseU + reverseWidth * xE, spriteV + spriteHeight * yS, side, quadBuffer); // 1 0
                break;
            case WEST:
                generateBlockVertex(xS, yS, zS, reverseU + reverseWidth * zE, spriteV + spriteHeight * yE, side, quadBuffer); // 1 1
                generateBlockVertex(xS, yS, zE, reverseU + reverseWidth * zS, spriteV + spriteHeight * yE, side, quadBuffer); // 0 1
                generateBlockVertex(xS, yE, zE, reverseU + reverseWidth * zS, spriteV + spriteHeight * yS, side, quadBuffer); // 0 0
                generateBlockVertex(xS, yE, zS, reverseU + reverseWidth * zE, spriteV + spriteHeight * yS, side, quadBuffer); // 1 0
                break;
            case EAST:
                generateBlockVertex(xE, yS, zS, spriteU + spriteWidth * zD, spriteV + spriteHeight * yD, side, quadBuffer); // 1 1
                generateBlockVertex(xE, yE, zS, spriteU + spriteWidth * zD, spriteV + spriteHeight * yC, side, quadBuffer); // 1 0
                generateBlockVertex(xE, yE, zE, spriteU + spriteWidth * zC, spriteV + spriteHeight * yC, side, quadBuffer); // 0 0
                generateBlockVertex(xE, yS, zE, spriteU + spriteWidth * zC, spriteV + spriteHeight * yD, side, quadBuffer); // 0 1
                break;
            default:
                throw new IllegalArgumentException("Unexpected facing: " + side);
        }
    }

    @Override
    public void onReload() {
    }

    //TODO: Return particle depending on side
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.textures.get(particle);
    }
}
