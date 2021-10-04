package mods.gregtechmod.objects.blocks.teblocks.container;

import ic2.core.slot.SlotInvSlot;
import ic2.core.util.StackUtil;
import mods.gregtechmod.inventory.SlotInvSlotHolo;
import mods.gregtechmod.objects.blocks.teblocks.TileEntityAdvancedSafe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public class ContainerAdvancedSafe extends ContainerGtBase<TileEntityAdvancedSafe> {

    public ContainerAdvancedSafe(EntityPlayer player, TileEntityAdvancedSafe base) {
        super(player, base);
        
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new SlotInvSlot(base.content, x + y * 9, 8 + x * 18, 23 + y * 18));
            }
        }
        
        addSlotToContainer(new SlotInvSlotHolo(base.filter, 0, 80, 5));
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (slotId == 63 && dragType == 1 && clickType == ClickType.PICKUP) {
            ItemStack stack = player.inventory.getItemStack();
            this.base.filter.put(!stack.isEmpty() ? StackUtil.copyWithSize(stack, 1) : ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        
        return super.slotClick(slotId, dragType, clickType, player);
    }
}
