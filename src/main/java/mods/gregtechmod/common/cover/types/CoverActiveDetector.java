package mods.gregtechmod.common.cover.types;

import ic2.core.IC2;
import mods.gregtechmod.common.core.GregtechMod;
import mods.gregtechmod.common.cover.ICoverable;
import mods.gregtechmod.common.util.IGregtechMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class CoverActiveDetector extends CoverGeneric {
    private byte mode;

    public CoverActiveDetector(ICoverable te, EnumFacing side, ItemStack stack) {
        super(te, side, stack);
    }

    @Override
    public ResourceLocation getIcon() {
        return new ResourceLocation(GregtechMod.MODID, "blocks/covers/active_detector");
    }

    @Override
    public void doCoverThings() {
        if (!(te instanceof IGregtechMachine)) return;
        byte strength = (byte) (((((IGregtechMachine)te).getProgress() + 4) / ((IGregtechMachine) te).getMaxProgress()) * 15);
        if (mode < 2) {
            if (strength > 0 && ((IGregtechMachine)te).isActive()) {
                ((IGregtechMachine) te).setRedstoneOutput(side, mode == 0 ? strength : (byte) (15 - strength));
            } else {
                ((IGregtechMachine)te).setRedstoneOutput(side, (byte) (mode == 0 ? 0 : 15));
            }
        } else {
            ((IGregtechMachine)te).setRedstoneOutput(side, ((mode==2)!=(((IGregtechMachine)te).getProgress() == 0))?(byte)0:15);
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer player) {
       mode = (byte) ((mode + 1)%4);
       if (!player.world.isRemote) return false;
       switch (mode) {
           case 0:
               IC2.platform.messagePlayer(player, "Normal");
               break;
           case 1:
               IC2.platform.messagePlayer(player, "Inverted");
               break;
           case 2:
               IC2.platform.messagePlayer(player, "Ready to work");
               break;
           case 3:
               IC2.platform.messagePlayer(player, "Not ready to work");
               break;
       }
       return true;
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
        return 5;
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
    public boolean overrideRedstoneOut() {
        return true;
    }

    @Override
    public void onCoverRemoved() {
        if (te instanceof IGregtechMachine) ((IGregtechMachine) te).setRedstoneOutput(side, (byte) 0);
    }
}
