package mods.gregtechmod.init;

import com.zuxelus.energycontrol.api.EnergyControlRegister;
import com.zuxelus.energycontrol.api.IItemCard;
import com.zuxelus.energycontrol.api.IItemKit;
import mods.gregtechmod.api.GregTechObjectAPI;
import mods.gregtechmod.objects.BlockItems;
import mods.gregtechmod.objects.blocks.BlockLightSource;
import mods.gregtechmod.objects.items.ItemCellClassic;
import mods.gregtechmod.objects.items.ItemSensorCard;
import mods.gregtechmod.objects.items.ItemSensorKit;
import mods.gregtechmod.util.GtUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockItemLoader {
    static final Set<net.minecraft.block.Block> BLOCKS = new LinkedHashSet<>();
    static final Set<Item> ITEMS = new LinkedHashSet<>();

    private static void registerItem(Item item) {
        if (!ITEMS.add(item)) throw new IllegalStateException("Duplicate registry entry: " + item.getRegistryName());
    }

    private static net.minecraft.block.Block registerBlock(net.minecraft.block.Block block) {
        if (!BLOCKS.add(block)) throw new IllegalStateException("Duplicate registry entry: " + block.getRegistryName());
        return block;
    }

    private static void registerBlockItem(Block block) {
        registerBlock(block);
        registerItem(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    static void init() {
        BlockItems.classicCells = Stream.<FluidLoader.IFluidProvider>concat(
                Arrays.stream(FluidLoader.Liquid.values()),
                Arrays.stream(FluidLoader.Gas.values())
        )
                .filter(FluidLoader.IFluidProvider::hasClassicCell)
                .collect(Collectors.toMap(FluidLoader.IFluidProvider::getName, 
                        provider -> new ItemCellClassic(provider.getName(), provider.getDescription(), provider.getFluid())));
        if (FluidRegistry.isFluidRegistered("biomass")) BlockItems.classicCells.put("biomass", new ItemCellClassic("biomass", null, FluidRegistry.getFluid("biomass")));
        if (FluidRegistry.isFluidRegistered("bio.ethanol")) BlockItems.classicCells.put("bio.ethanol", new ItemCellClassic("bio.ethanol", null, FluidRegistry.getFluid("bio.ethanol")));

        BlockItems.lightSource = registerBlock(new BlockLightSource());
        Arrays.stream(BlockItems.Block.values()).forEach(block -> registerBlockItem(block.getInstance()));
        Arrays.stream(BlockItems.Ore.values()).forEach(block -> registerBlockItem(block.getInstance()));
        Arrays.stream(BlockItems.Miscellaneous.values()).forEach(misc -> registerItem(misc.getInstance()));
        Arrays.stream(BlockItems.Ingot.values()).forEach(ingot -> registerItem(ingot.getInstance()));
        Arrays.stream(BlockItems.Plate.values()).forEach(plate -> registerItem(plate.getInstance()));
        Arrays.stream(BlockItems.Rod.values()).forEach(rod -> registerItem(rod.getInstance()));
        Arrays.stream(BlockItems.Dust.values()).forEach(dust -> registerItem(dust.getInstance()));
        Arrays.stream(BlockItems.Smalldust.values()).forEach(smallDust -> registerItem(smallDust.getInstance()));
        Arrays.stream(BlockItems.Nugget.values()).forEach(nugget -> registerItem(nugget.getInstance()));
        Arrays.stream(BlockItems.Cell.values()).forEach(cell -> registerItem(cell.getInstance()));
        BlockItems.classicCells.values().forEach(BlockItemLoader::registerItem);
        Arrays.stream(BlockItems.Cover.values()).forEach(coverItem -> registerItem(coverItem.getInstance()));
        Arrays.stream(BlockItems.Component.values()).forEach(component -> registerItem(component.getInstance()));
        Arrays.stream(BlockItems.Upgrade.values()).forEach(upgrade -> registerItem(upgrade.getInstance()));
        if (Loader.isModLoaded("energycontrol")) registerEnergyControlItems();
        Arrays.stream(BlockItems.Armor.values()).forEach(armor -> registerItem(armor.getInstance()));
        Arrays.stream(BlockItems.NuclearCoolantPack.values()).forEach(pack -> registerItem(pack.getInstance()));
        Arrays.stream(BlockItems.NuclearFuelRod.values()).forEach(nuclearRod -> registerItem(nuclearRod.getInstance()));
        Arrays.stream(BlockItems.JackHammer.values()).forEach(jackHammer -> registerItem(jackHammer.getInstance()));
        Arrays.stream(BlockItems.Tool.values()).forEach(tool -> registerItem(tool.getInstance()));
        Arrays.stream(BlockItems.Wrench.values()).forEach(wrench -> registerItem(wrench.getInstance()));
        Arrays.stream(BlockItems.Hammer.values()).forEach(hammer -> registerItem(hammer.getInstance()));
        Arrays.stream(BlockItems.SolderingMetal.values()).forEach(solderingMetal -> registerItem(solderingMetal.getInstance()));
        Arrays.stream(BlockItems.TurbineRotor.values()).forEach(rotor -> registerItem(rotor.getInstance()));
        Arrays.stream(BlockItems.File.values()).forEach(file -> registerItem(file.getInstance()));
        Arrays.stream(BlockItems.Saw.values()).forEach(saw -> registerItem(saw.getInstance()));
        Arrays.stream(BlockItems.ColorSpray.values()).forEach(spray -> registerItem(spray.getInstance()));

        Map<String, ItemStack> items = ITEMS.stream()
                .collect(Collectors.toMap(value -> value.getRegistryName().toString().split(":")[1], ItemStack::new));
        GtUtil.setPrivateStaticValue(GregTechObjectAPI.class, "items", items);
        
        Map<String, Block> blocks = BLOCKS.stream()
                .collect(Collectors.toMap(value -> value.getRegistryName().toString().split(":")[1], value -> value));
        GtUtil.setPrivateStaticValue(GregTechObjectAPI.class, "blocks", blocks);
    }

    @Optional.Method(modid = "energycontrol")
    public static void registerEnergyControlItems() {
        BlockItems.sensorKit = new ItemSensorKit();
        BlockItems.sensorCard = new ItemSensorCard();
        EnergyControlRegister.registerKit((IItemKit) BlockItems.sensorKit);
        EnergyControlRegister.registerCard((IItemCard) BlockItems.sensorCard);
    }
}
