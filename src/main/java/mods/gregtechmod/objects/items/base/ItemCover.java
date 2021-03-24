package mods.gregtechmod.objects.items.base;

import ic2.core.util.StackUtil;
import mods.gregtechmod.api.cover.CoverRegistry;
import mods.gregtechmod.api.cover.ICover;
import mods.gregtechmod.api.cover.ICoverable;
import mods.gregtechmod.util.GtUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCover extends ItemBase {
    private final String coverName;

    public ItemCover(String name, String coverName) {
        super(name, () -> GtUtil.translateItemDescription(coverName));
        this.coverName = coverName;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (player.isSneaking()) return EnumActionResult.PASS;
        TileEntity block = world.getTileEntity(pos);
        ItemStack stack = player.inventory.getCurrentItem();
        ItemStack coverStack = StackUtil.copyWithSize(stack, 1);
        if (block instanceof ICoverable) {
            ICover cover = CoverRegistry.constructCover(this.coverName, side, (ICoverable) block, coverStack);
            if (((ICoverable)block).placeCoverAtSide(cover, side, false)) {
                if (!player.capabilities.isCreativeMode) stack.shrink(1);
                return EnumActionResult.SUCCESS;
            }
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }
}
