package mods.gregtechmod.objects.items.tools;

import buildcraft.api.tools.IToolWrench;
import com.google.common.collect.Multimap;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.tile.IWrenchable;
import ic2.api.transport.IPipe;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.util.RotationUtil;
import mods.gregtechmod.api.GregTechAPI;
import mods.gregtechmod.api.util.Reference;
import mods.gregtechmod.compat.ModHandler;
import mods.gregtechmod.core.GregTechConfig;
import mods.gregtechmod.util.GtUtil;
import mods.gregtechmod.util.ICustomItemModel;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Optional.Interface(modid = "buildcraftlib", iface = "buildcraft.api.tools.IToolWrench")
public class ItemWrench extends ItemToolWrench implements ICustomItemModel, IToolWrench, IEnhancedOverlayProvider {
    public final String name;
    protected final int entityDamage;
    protected int rotateDamage = 1;
    protected int removeDamage = 10;
    protected List<String> effectiveEntities = Arrays.asList("minecraft:villager_golem", "twilightforest:tower_golem", "thaumcraft:golem");
    protected boolean showDurability = true;

    public ItemWrench(String name, int entityDamage) {
        this(name, 28, entityDamage);
    }

    public ItemWrench(String name, int durability, int entityDamage) {
        super(null);
        this.name = name;
        this.entityDamage = entityDamage;
        setMaxDamage(durability - 1);
        GregTechAPI.instance().registerWrench(this);
    }

    @Override
    public boolean providesEnhancedOverlay(World world, BlockPos pos, EnumFacing facing, EntityPlayer player, ItemStack stack) {
        if (GregTechConfig.GENERAL.enhancedWrenchOverlay) {
            Block block = world.getBlockState(pos).getBlock();
            return block instanceof IWrenchable && (world.getTileEntity(pos) instanceof IPipe || Arrays.stream(EnumFacing.VALUES).anyMatch(side -> ((IWrenchable)block).canSetFacing(world, pos, side, player)));
        }
        return false;
    }

    @Override
    public String getTranslationKey() {
        return Reference.MODID + ".item." + name;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        Block block = world.getBlockState(pos).getBlock();
        ItemStack currentStack = player.getHeldItem(hand);

        if (block instanceof IWrenchable && this.canTakeDamage(currentStack, rotateDamage)) {
            EnumFacing face = ((IWrenchable) block).getFacing(world, pos);
            EnumFacing rotated = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
            if (rotated == face && !canTakeDamage(currentStack, removeDamage)) return EnumActionResult.FAIL;
            ItemToolWrench.wrenchBlock(world, pos, rotated, player, true);
            Block newBlock = world.getBlockState(pos).getBlock();
            this.damage(currentStack, world.getBlockState(pos).getBlock() == Blocks.AIR ? removeDamage : newBlock != block ? rotateDamage : 0, player);

            if (world.isRemote) IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(3, attacker);
        ResourceLocation entityName = EntityList.getKey(target);
        if (entityName != null && this.effectiveEntities.contains(entityName.toString())) GtUtil.damageEntity(target, attacker, this.entityDamage);
        return true;
    }

    @Override
    public ResourceLocation getItemModel() {
        return GtUtil.getModelResourceLocation(this.name, "tool");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (this.showDurability) tooltip.add((stack.getMaxDamage() - stack.getItemDamage() + 1) + " / " + (stack.getMaxDamage() + 1));
        if (ModHandler.buildcraftCore) tooltip.add(GtUtil.translateItem("wrench.description_bc"));
        tooltip.add(GtUtil.translateItem("wrench.description"));
        tooltip.add(GtUtil.translateItem("wrench.description_2"));
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        if (slot == EntityEquipmentSlot.MAINHAND) multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", this.entityDamage - 1, 0));

        return multimap;
    }

    @Override
    public boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        return true;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        wrench.damageItem(1, player);
        IC2.audioManager.playOnce(player, "Tools/wrench.ogg");
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        stack = stack.copy();
        if (stack.attemptDamageItem(8, GtUtil.RANDOM, null)) return ItemStack.EMPTY;
        return stack;
    }
}
