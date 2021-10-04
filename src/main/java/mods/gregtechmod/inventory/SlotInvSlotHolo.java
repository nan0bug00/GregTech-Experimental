package mods.gregtechmod.inventory;

import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;

public class SlotInvSlotHolo extends SlotInvSlot {

    public SlotInvSlotHolo(InvSlot invSlot, int index, int x, int y) {
        super(invSlot, index, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return false;
    }
}
