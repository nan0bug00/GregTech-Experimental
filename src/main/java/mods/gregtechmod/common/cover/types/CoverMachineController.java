package mods.gregtechmod.common.cover.types;

import ic2.core.IC2;
import mods.gregtechmod.common.core.GregtechMod;
import mods.gregtechmod.common.cover.ICoverable;
import mods.gregtechmod.common.util.IGregtechMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class CoverMachineController extends CoverGeneric {
    private byte mode;

    public CoverMachineController(ICoverable te, EnumFacing side, ItemStack stack) {
        super(te, side, stack);
    }

    @Override
    public void doCoverThings() {
        if (!(te instanceof IGregtechMachine)) return;

        boolean isPowered = ((TileEntity)te).getWorld().isSidePowered(((TileEntity)te).getPos().offset(side), side);
        if (isPowered == (mode == 0) && mode != 2) ((IGregtechMachine)te).enableWorking();
        else ((IGregtechMachine)te).disableWorking();
    }

    @Override
    public ResourceLocation getIcon() {
        return new ResourceLocation(GregtechMod.MODID, "blocks/covers/machine_controller");
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer player) {
        mode = (byte) ((mode + 1)%3);
        if (!player.world.isRemote) return false;

        switch (mode) {
            case 0:
                IC2.platform.messagePlayer(player, "Normal");
                break;
            case 1:
                IC2.platform.messagePlayer(player, "Inverted");
                break;
            case 2:
                IC2.platform.messagePlayer(player, "No work at all");
                break;
        }
        return true;
    }

    @Override
    public void onCoverRemoved() {
        if (te instanceof IGregtechMachine) ((IGregtechMachine)te).enableWorking();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setByte("mode", this.mode);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.mode = nbt.getByte("mode");
    }

    @Override
    public short getTickRate() {
        return 1;
    }

    @Override
    public boolean allowEnergyTransfer() {
        return true;
    }

    @Override
    public boolean letsLiquidsIn() {
        return true;
    }

    @Override
    public boolean letsLiquidsOut() {
        return true;
    }

    @Override
    public boolean letsItemsIn() {
        return true;
    }

    @Override
    public boolean letsItemsOut() {
        return true;
    }

    @Override
    public boolean acceptsRedstone() {
        return true;
    }
}
