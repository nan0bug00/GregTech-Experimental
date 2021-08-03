package mods.gregtechmod.init;

import ic2.api.item.IC2Items;
import ic2.api.recipe.Recipes;
import ic2.core.init.OreValues;
import ic2.core.util.StackUtil;
import mods.gregtechmod.api.GregTechAPI;
import mods.gregtechmod.api.recipe.CellType;
import mods.gregtechmod.api.util.Reference;
import mods.gregtechmod.compat.ModHandler;
import mods.gregtechmod.core.GregTechConfig;
import mods.gregtechmod.core.GregTechMod;
import mods.gregtechmod.objects.BlockItems;
import mods.gregtechmod.recipe.*;
import mods.gregtechmod.recipe.crafting.ToolCraftingRecipeShaped;
import mods.gregtechmod.recipe.crafting.ToolOreIngredient;
import mods.gregtechmod.recipe.ingredient.RecipeIngredientFluid;
import mods.gregtechmod.recipe.ingredient.RecipeIngredientItemStack;
import mods.gregtechmod.recipe.ingredient.RecipeIngredientOre;
import mods.gregtechmod.util.GtUtil;
import mods.gregtechmod.util.OreDictUnificator;
import mods.gregtechmod.util.ProfileDelegate;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.*;
import java.util.regex.Pattern;

public class OreDictHandler { // TODO Subject to huge cleanup
    public static final OreDictHandler INSTANCE;
    private static final Pattern granitePattern = Pattern.compile("\\bstone.*Granite");

    public static final Map<String, String> GT_ORE_NAMES = new HashMap<>();
    private static final Map<String, Integer> VALUABLE_ORES = new HashMap<>();

    private static final List<String> IGNORED_NAMES = Arrays.asList("blockClay", "dustClay", "blockPrismarine", "blockPrismarineBrick", "naquadah", "brickXyEngineering", "breederUranium", "diamondNugget", "infiniteBattery", "superconductor", "itemCharcoalSugar", "aluminumWire", "aluminiumWire", "silverWire",
            "tinWire", "eliteBattery", "advancedBattery", "transformer", "coil", "wireMill", "multimeter", "itemMultimeter", "chunkLazurite", "itemRecord", "aluminumNatural", "aluminiumNatural", "naturalAluminum", "naturalAluminium",
            "antimatterMilligram", "antimatterGram", "strangeMatter", "HSLivingmetalIngot", "oilMoving", "oilStill", "oilBucket", "orePetroleum", "dieselFuel", "lava", "water", "obsidianRod", "motor", "wrench", "coalGenerator",
            "electricFurnace", "ironTube", "netherTube", "obbyTube", "valvePart", "aquaRegia", "leatherSeal", "leatherSlimeSeal", "enrichedUranium", "batteryInfinite", "itemSuperconductor", "camoPaste", "CAMO_PASTE");
    private boolean activated = false;

    static {
        GT_ORE_NAMES.put("battery", "crafting10kEUStore");
        GT_ORE_NAMES.put("basicCircuit", "craftingCircuitTier02");
        GT_ORE_NAMES.put("circuitBasic", "craftingCircuitTier02");
        GT_ORE_NAMES.put("advancedCircuit", "craftingCircuitTier04");
        GT_ORE_NAMES.put("circuitAdvanced", "craftingCircuitTier04");
        GT_ORE_NAMES.put("eliteCircuit", "craftingCircuitTier06");
        GT_ORE_NAMES.put("circuitElite", "craftingCircuitTier06");
        GT_ORE_NAMES.put("basalt", "stoneBasalt");
        GT_ORE_NAMES.put("marble", "stoneMarble");
        GT_ORE_NAMES.put("mossystone", "stoneMossy");
        GT_ORE_NAMES.put("MonazitOre", "oreMonazit");
        GT_ORE_NAMES.put("blockQuickSilver", "blockQuicksilver");
        GT_ORE_NAMES.put("ingotQuickSilver", "ingotQuicksilver");
        GT_ORE_NAMES.put("ingotQuicksilver", "itemQuicksilver");
        GT_ORE_NAMES.put("dustQuickSilver", "dustQuicksilver");
        GT_ORE_NAMES.put("dustQuicksilver", "itemQuicksilver");
        GT_ORE_NAMES.put("itemQuickSilver", "itemQuicksilver");
        GT_ORE_NAMES.put("dustCharCoal", "dustCharcoal");
        GT_ORE_NAMES.put("quartzCrystal", "crystalQuartz");
        GT_ORE_NAMES.put("quartz", "crystalQuartz");
        GT_ORE_NAMES.put("woodGas", "gasWood");
        GT_ORE_NAMES.put("woodLog", "logWood");
        GT_ORE_NAMES.put("pulpWood", "dustWood");
        GT_ORE_NAMES.put("blockCobble", "stoneCobble");
        GT_ORE_NAMES.put("gemPeridot", "gemOlivine");
        GT_ORE_NAMES.put("dustPeridot", "dustOlivine");
        GT_ORE_NAMES.put("dustDiamond", "itemDiamond");
        GT_ORE_NAMES.put("gemDiamond", "itemDiamond");
        GT_ORE_NAMES.put("dustLapis", "itemLazurite");
        GT_ORE_NAMES.put("dustLapisLazuli", "itemLazurite");
        GT_ORE_NAMES.put("dustLazurite", "itemLazurite");
        GT_ORE_NAMES.put("craftingRawMachineTier01", "craftingRawMachineTier00");
        GT_ORE_NAMES.put("dustSulfur", "craftingSulfurToGunpowder");
        GT_ORE_NAMES.put("dustSaltpeter", "craftingSaltpeterToGunpowder");
        GT_ORE_NAMES.put("crystalQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("crystalNetherQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("crystalCertusQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("dustQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("dustCertusQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("dustNetherQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("ingotQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("ingotNetherQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("ingotCertusQuartz", "craftingQuartz");
        GT_ORE_NAMES.put("blockAluminum", "blockAluminium");
        GT_ORE_NAMES.put("ingotAluminum", "ingotAluminium");

        VALUABLE_ORES.put("soulsand", 1);
        VALUABLE_ORES.put("glowstone", 1);
        VALUABLE_ORES.put("oreLapis", 3);
        VALUABLE_ORES.put("oreSodalite", 3);
        VALUABLE_ORES.put("oreRedstone", 3);
        VALUABLE_ORES.put("oreQuartz", 4);
        VALUABLE_ORES.put("oreNikolite", 3);
        VALUABLE_ORES.put("oreIron", 3);
        VALUABLE_ORES.put("oreGold", 4);
        VALUABLE_ORES.put("oreSilver", 3);
        VALUABLE_ORES.put("oreLead", 3);
        VALUABLE_ORES.put("oreSilverLead", 4);
        VALUABLE_ORES.put("oreGalena", 4);
        VALUABLE_ORES.put("oreDiamond", 5);
        VALUABLE_ORES.put("oreEmerald", 5);
        VALUABLE_ORES.put("oreRuby", 4);
        VALUABLE_ORES.put("oreSapphire", 4);
        VALUABLE_ORES.put("oreGreenSapphire", 4);
        VALUABLE_ORES.put("oreOlivine", 4);
        VALUABLE_ORES.put("oreCoal", 1);
        VALUABLE_ORES.put("oreCopper", 3);
        VALUABLE_ORES.put("oreTin", 3);
        VALUABLE_ORES.put("oreZinc", 2);
        VALUABLE_ORES.put("oreCassiterite", 3);
        VALUABLE_ORES.put("oreTetrahedrite", 3);
        VALUABLE_ORES.put("oreAntimony", 3);
        VALUABLE_ORES.put("oreIridium", 10);
        VALUABLE_ORES.put("oreCooperite", 10);
        VALUABLE_ORES.put("oreSheldonite", 10);
        VALUABLE_ORES.put("orePlatinum", 7);
        VALUABLE_ORES.put("oreNickel", 4);
        VALUABLE_ORES.put("orePyrite", 1);
        VALUABLE_ORES.put("oreCinnabar", 3);
        VALUABLE_ORES.put("oreSphalerite", 2);
        VALUABLE_ORES.put("oreAluminium", 2);
        VALUABLE_ORES.put("oreAluminum", 2);
        VALUABLE_ORES.put("oreSteel", 4);
        VALUABLE_ORES.put("oreTitan", 5);
        VALUABLE_ORES.put("oreTitanium", 5);
        VALUABLE_ORES.put("oreChrome", 10);
        VALUABLE_ORES.put("oreChromium", 10);
        VALUABLE_ORES.put("oreElectrum", 5);
        VALUABLE_ORES.put("oreTungsten", 4);
        VALUABLE_ORES.put("oreTungstate", 4);
        VALUABLE_ORES.put("oreBauxite", 2);
        VALUABLE_ORES.put("oreApatite", 1);
        VALUABLE_ORES.put("oreSulfur", 1);
        VALUABLE_ORES.put("oreSaltpeter", 2);
        VALUABLE_ORES.put("orePhosphorite", 2);
        VALUABLE_ORES.put("oreMagnesium", 2);
        VALUABLE_ORES.put("oreManganese", 2);
        VALUABLE_ORES.put("oreMonazit", 3);
        VALUABLE_ORES.put("oreMonazite", 3);
        VALUABLE_ORES.put("oreFortronite", 3);
        VALUABLE_ORES.put("oreThorium", 5);
        VALUABLE_ORES.put("orePlutonium", 15);
        VALUABLE_ORES.put("oreOsmium", 20);
        VALUABLE_ORES.put("oreEximite", 3);
        VALUABLE_ORES.put("oreMeutoite", 3);
        VALUABLE_ORES.put("orePrometheum", 3);
        VALUABLE_ORES.put("oreDeepIron", 2);
        VALUABLE_ORES.put("oreInfuscolium", 3);
        VALUABLE_ORES.put("oreOureclase", 3);
        VALUABLE_ORES.put("oreAredrite", 3);
        VALUABLE_ORES.put("oreAstralSilver", 4);
        VALUABLE_ORES.put("oreCarmot", 4);
        VALUABLE_ORES.put("oreMithril", 4);
        VALUABLE_ORES.put("oreRubracium", 3);
        VALUABLE_ORES.put("oreOrichalcum", 3);
        VALUABLE_ORES.put("oreAdamantine", 5);
        VALUABLE_ORES.put("oreAtlarus", 3);
        VALUABLE_ORES.put("oreIgnatius", 3);
        VALUABLE_ORES.put("oreShadowIron", 4);
        VALUABLE_ORES.put("oreMidasium", 3);
        VALUABLE_ORES.put("oreVyroxeres", 3);
        VALUABLE_ORES.put("oreCeruclase", 3);
        VALUABLE_ORES.put("oreKalendrite", 3);
        VALUABLE_ORES.put("oreVulcanite", 3);
        VALUABLE_ORES.put("oreSanguinite", 3);
        VALUABLE_ORES.put("oreLemurite", 3);
        VALUABLE_ORES.put("oreAdluorite", 3);
        VALUABLE_ORES.put("oreNaquadah", 8);
        VALUABLE_ORES.put("oreBitumen", 2);
        VALUABLE_ORES.put("oreForce", 3);
        VALUABLE_ORES.put("oreCertusQuartz", 4);
        VALUABLE_ORES.put("oreVinteum", 3);
        VALUABLE_ORES.put("orePotash", 2);
        VALUABLE_ORES.put("oreArdite", 2);
        VALUABLE_ORES.put("oreCobalt", 2);
        VALUABLE_ORES.put("oreUranium", 5);

        INSTANCE = new OreDictHandler();
    }

    private OreDictHandler() {}

    public static void registerValuableOres() {
        VALUABLE_ORES.forEach((key, value) -> OreDictionary.getOres(key)
                .forEach(stack -> OreValues.add(stack, value)));
    }

    @SubscribeEvent
    public void registerOre(OreDictionary.OreRegisterEvent event) {
        if (event == null || !this.activated) return;

        registerOre(event.getName(), event.getOre());
    }

    public void registerOre(String name, ItemStack ore) {
        if (ore.isEmpty() || name.isEmpty() || IGNORED_NAMES.contains(name)) return;

        if (ore.getCount() != 1) GregTechMod.logger.error("'" + name + "' is either being misused by another mod or has been wrongly registered, as the stack size of the event stack is not 1");
        ore.setCount(1);

        if (name.toLowerCase(Locale.ROOT).contains("xych") || name.toLowerCase(Locale.ROOT).contains("xyore")) return;

        String unifiedName = GT_ORE_NAMES.get(name);
        if (unifiedName != null) OreDictUnificator.registerOre(unifiedName, ore);

        if (name.startsWith("denseOre")) {
            OreDictUnificator.registerOre(name.replaceFirst("denseOre", "oreDense"), ore);
            return;
        } else if (name.startsWith("netherOre")) {
            OreDictUnificator.registerOre(name.replaceFirst("netherOre", "oreNether"), ore);
            return;
        } else if (name.startsWith("endOre")) {
            OreDictUnificator.registerOre(name.replaceFirst("endOre", "oreEnd"), ore);
            return;
        } else if (name.startsWith("itemDrop")) {
            OreDictUnificator.registerOre(name.replaceFirst("itemDrop", "item"), ore);
            return;
        } else if (granitePattern.matcher(name).find()) OreDictUnificator.registerOre("stoneGranite", ore);

        if (name.startsWith("plate") || name.startsWith("ore") || name.startsWith("dust") || name.startsWith("gem")
                || name.startsWith("ingot") || name.startsWith("nugget") || name.startsWith("block") || name.startsWith("stick")) OreDictUnificator.addAssociation(name, ore.copy());

        registerRecipes(name, ore);
    }

    public void activateHandler() {
        this.activated = true;
        Arrays.stream(OreDictionary.getOreNames())
                .forEach(name -> OreDictionary.getOres(name)
                    .forEach(ore -> registerOre(name, ore)));
    }

    public void registerRecipes(String name, ItemStack ore) {
        if (ore.isEmpty()) return;

        ore.setCount(1);
        if (name.startsWith("plate") || name.startsWith("ore") || name.startsWith("dust") || name.startsWith("gem") || name.startsWith("ingot") || name.startsWith("nugget") || name.startsWith("block") || name.startsWith("stick")) OreDictUnificator.add(name, ore.copy());

        if (name.startsWith("drop")) name = name.replaceFirst("drop", "item");

        Item item = ore.getItem();
        int meta = ore.getMetadata();

        if (name.startsWith("stone")) {
            processStone(ore, name);
        } else if (name.startsWith("ore")) {
            processOre(ore, name);
        } else if (name.startsWith("dust")) {
            processDust(ore, name);
        } else if (name.startsWith("ingot")) {
            processIngot(ore, name);
        } else if (name.startsWith("block")) {
            ItemStack unified = OreDictUnificator.getUnifiedOre(name);
            if (!unified.isEmpty()) ore = unified;
            processBlock(ore, name);
        } else if (name.startsWith("nugget")) {
            ItemStack unified = OreDictUnificator.getUnifiedOre(name);
            if (!unified.isEmpty()) ore = unified;
            Recipes.recyclerBlacklist.add(Recipes.inputFactory.forOreDict(name));

            String ingotName = name.replaceFirst("nugget", "ingot");
            ItemStack ingot = OreDictUnificator.getFirstOre(ingotName);
            if (!ingot.isEmpty()) {
                if (!name.equals("nuggetIridium") && !name.equals("nuggetOsmium") && !name.equals("nuggetUranium") && !name.equals("nuggetPlutonium") && !name.equals("nuggetThorium")) {
                    DynamicRecipes.addAlloySmelterRecipe(RecipeAlloySmelter.create(Collections.singletonList(RecipeIngredientOre.create(name, 9)), OreDictUnificator.getFirstOre(ingotName), 200, 1, false));
                }
                Ingredient input = new OreIngredient(name);
                ModHandler.addShapelessRecipe(
                        name+"ToIngot",
                        OreDictUnificator.getFirstOre(ingotName),
                        input, input, input, input, input, input, input, input, input
                );
                ModHandler.addShapelessRecipe(
                        ingotName+"ToNuggets",
                        StackUtil.copyWithSize(ore, 9),
                        new OreIngredient(ingotName)
                );
                ModHandler.removeFactorizerRecipe(ingot, true);
                ModHandler.removeFactorizerRecipe(ore, false);
                ModHandler.addFactorizerRecipe(ingot, StackUtil.copyWithSize(ore, 9), true);
                ModHandler.addFactorizerRecipe(StackUtil.copyWithSize(ore, 9), ingot, false);
            }
        } else if (name.startsWith("plate")) {
            if (!name.startsWith("plateAlloy")) {
                ModHandler.removeCraftingRecipe(ore);

                if (name.startsWith("plateDense")) {
                    ItemStack ingot = OreDictUnificator.getFirstOre(name.replaceFirst("plateDense", "ingot"), 8);
                    if (!ingot.isEmpty()) DynamicRecipes.addSmeltingAndAlloySmeltingRecipe(name, ore, ingot);

                    ItemStack dust = OreDictUnificator.getFirstOre(name.replaceFirst("plateDense", "dust"), 8);
                    if (!dust.isEmpty()) DynamicRecipes.addPulverizerRecipe(RecipePulverizer.create(RecipeIngredientOre.create(name), dust));
                } else {
                    ItemStack plateDense = OreDictUnificator.getFirstOre(name.replaceFirst("plate", "plateDense"));
                    if (!plateDense.isEmpty()) DynamicRecipes.addCompressorRecipe(Recipes.inputFactory.forOreDict(name, 8), plateDense);

                    ItemStack ingot = OreDictUnificator.getFirstOre(name.replaceFirst("plate", "ingot"));
                    if (!ingot.isEmpty()) DynamicRecipes.addSmeltingAndAlloySmeltingRecipe(name, ore, ingot);

                    ItemStack dust = OreDictUnificator.getFirstOre(name.replaceFirst("plate", "dust"));
                    if (!dust.isEmpty()) DynamicRecipes.addPulverizerRecipe(RecipePulverizer.create(RecipeIngredientOre.create(name), dust));
                }
            }
            String ingotName = name.replaceFirst("plate", "ingot");
            if (!OreDictUnificator.getFirstOre(ingotName).isEmpty()) {
                ModHandler.addShapedRecipe(
                        name+"FromIngots",
                        ore,
                        "H", "I", "I", 'H', "craftingToolHardHammer", 'I', ingotName
                );
            }
        } else if (name.startsWith("paper") || name.startsWith("book") || name.equals("treeLeaves")) {
            OreDictUnificator.addAssociation(name, ore);
        } else if (name.equals("woodRubber") || name.equals("logRubber")) {
            DynamicRecipes.addSawmillRecipe(RecipeSawmill.create(RecipeIngredientOre.create(name), Arrays.asList(IC2Items.getItem("misc_resource", "resin"), new ItemStack(BlockItems.Dust.WOOD.getInstance(), 16)), 1, true));
        } else if (name.startsWith("log")) {
            if (meta == OreDictionary.WILDCARD_VALUE) {
                for (int i = 0; i < 16; i++) {
                    processLog(new ItemStack(item, 1, i), name);
                }
            } else processLog(ore, name);
        } else if (name.startsWith("slabWood")) {
            if (meta == OreDictionary.WILDCARD_VALUE) {
                for (int i = 0; i < 16; i++) {
                    processSlab(new ItemStack(item, 1, i), name);
                }
            } else processSlab(ore, name);
        } else if (name.startsWith("plankWood")) {
            if (meta == OreDictionary.WILDCARD_VALUE) {
                for (int i = 0; i < 16; i++) {
                    processPlank(new ItemStack(item, 1, i), name);
                }
            } else processPlank(ore, name);

            DynamicRecipes.addLatheRecipe(RecipeLathe.create(RecipeIngredientOre.create(name), Collections.singletonList(new ItemStack(Items.STICK, 2)), 25, 8));
            DynamicRecipes.addAssemblerRecipe(RecipeDualInput.create(Arrays.asList(RecipeIngredientOre.create(name, 8), RecipeIngredientOre.create("dustRedstone")), new ItemStack(Blocks.NOTEBLOCK), 800, 1));
            DynamicRecipes.addAssemblerRecipe(RecipeDualInput.create(Arrays.asList(RecipeIngredientOre.create(name, 8), RecipeIngredientOre.create("gemDiamond")), new ItemStack(Blocks.JUKEBOX), 1600, 1));
        } else if (name.startsWith("treeSapling")) {
            if (meta == OreDictionary.WILDCARD_VALUE) {
                for (int i = 0; i < 16; i++) {
                    processSapling(new ItemStack(item, 1, i), name);
                }
            } else processSapling(ore, name);
        } else if (name.startsWith("stick")) {
            OreDictUnificator.addAssociation(name, ore);

            ItemStack smallDust = OreDictUnificator.getFirstOre(name.replaceFirst("stick", "dustSmall"), 2);
            if (!smallDust.isEmpty()) {
                DynamicRecipes.addPulverizerRecipe(RecipePulverizer.create(RecipeIngredientOre.create(name), smallDust));
            }
        } else if (name.startsWith("seed")) {
            DynamicRecipes.addCentrifugeRecipe(RecipeCentrifuge.create(RecipeIngredientOre.create(name, 64), Collections.singletonList(ProfileDelegate.getCell("seed.oil")), 1, 200, CellType.CELL));
        } else if (name.startsWith("plant") || name.startsWith("flower")) {
            if (GregTechMod.classic) {
                DynamicRecipes.COMPRESSOR.addRecipe(Recipes.inputFactory.forOreDict(name, 8), IC2Items.getItem("crafting", "compressed_plants"));
            } else if (name.startsWith("plant")) {
                ModHandler.addShapedRecipe(
                        name+"ToPlantBall",
                        IC2Items.getItem("crafting", "plant_ball"),
                        "XXX", "X X", "XXX", 'X', name
                );
            }

            ItemStack dye = ModHandler.getCraftingResult(ore);
            if (!dye.isEmpty()) DynamicRecipes.addExtractorRecipe(Recipes.inputFactory.forOreDict(name), StackUtil.copyWithSize(dye, dye.getCount() + 1));
        }
    }

    private void processSapling(ItemStack stack, String name) {
        OreDictUnificator.addAssociation(name, stack);
        DynamicRecipes.addPulverizerRecipe(RecipePulverizer.create(RecipeIngredientOre.create(name), new ItemStack(BlockItems.Dust.WOOD.getInstance(), 2)));
    }

    private void processPlank(ItemStack stack, String name) {
        OreDictUnificator.addAssociation(name, stack);
        ModHandler.removeTEPulverizerRecipe(stack);
        DynamicRecipes.addPulverizerRecipe(RecipePulverizer.create(RecipeIngredientOre.create(name), new ItemStack(BlockItems.Dust.WOOD.getInstance())));
    }

    private void processSlab(ItemStack stack, String name) {
        OreDictUnificator.addAssociation(name, stack);

        DynamicRecipes.addPulverizerRecipe(RecipePulverizer.create(RecipeIngredientOre.create(name), new ItemStack(BlockItems.Smalldust.WOOD.getInstance(), 2)));
        if (!ModHandler.woodenTie.isEmpty()) {
            Fluid creosote = FluidRegistry.getFluid("creosote");
            if (creosote != null) {
                DynamicRecipes.addCannerRecipe(RecipeCanner.create(RecipeIngredientOre.create(name, 3), RecipeIngredientFluid.fromFluid(creosote, 1), Collections.singletonList(ModHandler.woodenTie), 200, 4));
            }
        }
    }

    private void processLog(ItemStack stack, String name) {
        OreDictUnificator.addAssociation(name, stack);

        IRecipe recipe = ModHandler.getCraftingRecipe(stack);
        if (recipe != null) {
            ItemStack result = recipe.getRecipeOutput();
            if (!result.isEmpty()) {
                ItemStack planks = StackUtil.copyWithSize(result, result.getCount() * 3 / 2);
                DynamicRecipes.addSawmillRecipe(RecipeSawmill.create(RecipeIngredientItemStack.create(stack), Arrays.asList(planks, new ItemStack(BlockItems.Dust.WOOD.getInstance())), 1, true));
                ModHandler.removeCraftingRecipeFromInputs(stack);
                String recipeName = recipe.getRegistryName().getPath();

                CraftingHelper.ShapedPrimer primer = CraftingHelper.parseShaped("S", "L", 'S', new ToolOreIngredient("craftingToolSaw", 1), 'L', stack);
                IRecipe sawingRecipe = new ToolCraftingRecipeShaped(
                        "",
                        primer.width,
                        primer.height,
                        primer.input,
                        StackUtil.copyWithSize(result, GregTechConfig.GENERAL.woodNeedsSawForCrafting ? result.getCount() : result.getCount() * 5 / 4),
                        Collections.singletonList(IC2Items.getItem("chainsaw")),
                        1
                ).setRegistryName(new ResourceLocation(Reference.MODID, recipeName+"_sawing"));
                ForgeRegistries.RECIPES.register(sawingRecipe);

                GameRegistry.addShapelessRecipe(
                        new ResourceLocation(Reference.MODID, recipeName),
                        null,
                        StackUtil.copyWithSize(result, result.getCount() / (GregTechConfig.GENERAL.woodNeedsSawForCrafting ? 2 : 1)),
                        Ingredient.fromStacks(stack)
                );
            }
        }
    }

    private void processStone(ItemStack stack, String name) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) GregTechAPI.JACK_HAMMER_MINABLE_BLOCKS.add(stack);

        if (name.equals("stoneObsidian") && item instanceof ItemBlock) {
            ((ItemBlock) item).getBlock().setResistance(20);
        }
    }

    private void processBlock(ItemStack stack, String name) {
        String ingotName = name.replaceFirst("block", "ingot");
        ItemStack ingot = OreDictUnificator.getFirstOre(ingotName).copy();
        String gemName = name.replaceFirst("block", "gem");
        ItemStack gem = OreDictUnificator.getFirstOre(gemName).copy();
        String dustName = name.replaceFirst("block", "dust");
        ItemStack dust = OreDictUnificator.getFirstOre(dustName).copy();

        ModHandler.removeCraftingRecipeFromInputs(stack);
        if (ingot.isEmpty() && gem.isEmpty() && dust.isEmpty()) return;

        boolean storageBlockCrafting = GregTechAPI.getDynamicConfig("storageBlockCrafting", name, false);
        boolean storageBlockDeCrafting = GregTechAPI.getDynamicConfig("storageBlockDeCrafting", name, !gem.isEmpty());

        if (!dust.isEmpty()) {
            ModHandler.removeCraftingRecipeFromInputs(dust, dust, dust, dust, dust, dust, dust, dust, dust);
            if (ingot.isEmpty() && gem.isEmpty()) {
                if (storageBlockCrafting) processBlockRecipe(stack, name+"FromDusts");
                DynamicRecipes.addIngotToBlockRecipe(dustName, dust, stack, storageBlockCrafting, storageBlockDeCrafting);
            }

            dust.setCount(9);
            if (storageBlockDeCrafting) ModHandler.addShapelessRecipe(name+"ToDust", dust, new OreIngredient(name));
            DynamicRecipes.addPulverizerRecipe(RecipePulverizer.create(RecipeIngredientOre.create(name), dust));
        }
        if (!gem.isEmpty()) {
            if (ModHandler.getCraftingResult(gem, gem, ItemStack.EMPTY, gem, gem).isItemEqual(stack)) return;

            ModHandler.removeCraftingRecipeFromInputs(gem, gem, gem, gem, gem, gem, gem, gem, gem);

            if (storageBlockCrafting) processBlockRecipe(stack, name+"FromGems");
            if (storageBlockDeCrafting) {
                ModHandler.addShapelessRecipe(name + "ToGem", StackUtil.setSize(gem, 9), new OreIngredient(name));
            }

            DynamicRecipes.addIngotToBlockRecipe(gemName, gem, stack, storageBlockCrafting, storageBlockDeCrafting);
        }
        if (!ingot.isEmpty()) {
            ModHandler.removeCraftingRecipeFromInputs(ingot, ingot, ingot, ingot, ingot, ingot, ingot, ingot, ingot);

            if (storageBlockCrafting) processBlockRecipe(stack, name+"FromIngots");

            ingot.setCount(9);
            DynamicRecipes.addSmeltingAndAlloySmeltingRecipe(name, stack, ingot);
            if (storageBlockDeCrafting) ModHandler.addShapelessRecipe(name+"ToIngot", ingot, new OreIngredient(name));
            DynamicRecipes.addIngotToBlockRecipe(ingotName, ingot, stack, storageBlockCrafting, storageBlockDeCrafting);
        }
    }

    private void processBlockRecipe(ItemStack stack, String name) {
        ModHandler.addShapedRecipe(
                name,
                stack,
                "XXX", "XXX", "XXX", 'X', name
        );
    }

    private void processIngot(ItemStack stack, String name) {
        String material = name.replace("ingot", "");
        String materialName = material.toLowerCase(Locale.ROOT);
        String plateName = name.replaceFirst("ingot", "plate");
        ItemStack plate = OreDictUnificator.get(plateName);
        if (!plate.isEmpty()) {
            DynamicRecipes.addBenderRecipe(RecipeSimple.create(RecipeIngredientOre.create(name), plate, 50, 20));
            ItemStack result;

            if (GregTechAPI.getDynamicConfig("recipes", "platesNeededForArmorMadeOf"+material, true)) {
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(stack, stack, stack, stack, ItemStack.EMPTY, stack)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_helmet_" + materialName,
                            result,
                            "XXX", "XTX", 'X', plateName, 'T', "craftingToolHardHammer"
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(stack, ItemStack.EMPTY, stack, stack, stack, stack, stack, stack, stack)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_chestplate_" + materialName,
                            result,
                            "XTX", "XXX", "XXX", 'X', plateName, 'T', "craftingToolHardHammer"
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(stack, stack, stack, stack, ItemStack.EMPTY, stack, stack, ItemStack.EMPTY, stack)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_leggings_" + materialName,
                            result,
                            "XXX", "XTX", "X X", 'X', plateName, 'T', "craftingToolHardHammer"
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(stack, ItemStack.EMPTY, stack, stack, ItemStack.EMPTY, stack)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_boots_" + materialName,
                            result,
                            "XTX", "X X", 'X', plateName, 'T', "craftingToolHardHammer"
                    );
                }
            }


            if (GregTechAPI.getDynamicConfig("recipes", "platesNeededForToolsMadeOf"+name.replace("ingot", ""), true)) {
                ItemStack stick = new ItemStack(Items.STICK);
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(ItemStack.EMPTY, stack, ItemStack.EMPTY, ItemStack.EMPTY, stack, ItemStack.EMPTY, ItemStack.EMPTY, stick)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_sword_" + materialName,
                            result,
                            " X ", "FXT", " S ", 'X', plateName, 'T', "craftingToolHardHammer", 'S', "stickWood", 'F', "craftingToolFile"
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(stack, stack, stack, ItemStack.EMPTY, stick, ItemStack.EMPTY, ItemStack.EMPTY, stick)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_pickaxe_" + materialName,
                            result,
                            "XII", "FST", " S ", 'X', plateName, 'T', "craftingToolHardHammer", 'S', "stickWood", 'F', "craftingToolFile", 'I', stack
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(ItemStack.EMPTY, stack, ItemStack.EMPTY, ItemStack.EMPTY, stick, ItemStack.EMPTY, ItemStack.EMPTY, stick)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_shovel_" + materialName,
                            result,
                            "FXT", " S ", " S ", 'X', plateName, 'T', "craftingToolHardHammer", 'S', "stickWood", 'F', "craftingToolFile"
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(stack, stack, ItemStack.EMPTY, stack, stick, ItemStack.EMPTY, ItemStack.EMPTY, stick)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_axe_" + materialName,
                            result,
                            "XIT", "XS ", "FS ", 'X', plateName, 'T', "craftingToolHardHammer", 'S', "stickWood", 'F', "craftingToolFile", 'I', stack
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(ItemStack.EMPTY, stack, stack, ItemStack.EMPTY, stick, stack, ItemStack.EMPTY, stick)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_axe_" + materialName,
                            result,
                            "XIT", "XS ", "FS ", 'X', plateName, 'T', "craftingToolHardHammer", 'S', "stickWood", 'F', "craftingToolFile", 'I', stack
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(stack, stack, ItemStack.EMPTY, ItemStack.EMPTY, stick, ItemStack.EMPTY, ItemStack.EMPTY, stick)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_hoe_" + materialName,
                            result,
                            "XIT", "FS ", " S ", 'X', plateName, 'T', "craftingToolHardHammer", 'S', "stickWood", 'F', "craftingToolFile", 'I', stack
                    );
                }
                if (!(result = ModHandler.removeCraftingRecipeFromInputs(ItemStack.EMPTY, stack, stack, ItemStack.EMPTY, stick, ItemStack.EMPTY, ItemStack.EMPTY, stick)).isEmpty()) {
                    ModHandler.addShapedRecipe(
                            result.getItem().getRegistryName().getNamespace()+"_hoe_" + materialName,
                            result,
                            "XIT", "FS ", " S ", 'X', plateName, 'T', "craftingToolHardHammer", 'S', "stickWood", 'F', "craftingToolFile", 'I', stack
                    );
                }
            }
        }

        ItemStack unified = OreDictUnificator.getUnifiedOre(name);
        if (!unified.isEmpty()) stack = unified;

        ItemStack result;
        if (!OreDictUnificator.getFirstOre("gearStone").isEmpty() && !(result = ModHandler.getCraftingResult(ItemStack.EMPTY, stack, ItemStack.EMPTY, stack, OreDictUnificator.getFirstOre("gearStone"), stack, ItemStack.EMPTY, stack, ItemStack.EMPTY)).isEmpty()) {
            DynamicRecipes.addAssemblerRecipe(RecipeDualInput.create(RecipeIngredientOre.create("gearStone"), RecipeIngredientItemStack.create(stack, 4), result, 1600, 2));
        } else {
            if (!(result = ModHandler.getCraftingResult(ItemStack.EMPTY, stack, ItemStack.EMPTY, stack, new ItemStack(Items.IRON_INGOT), stack, ItemStack.EMPTY, stack, ItemStack.EMPTY)).isEmpty() ||
                    !(result = ModHandler.getCraftingResult(ItemStack.EMPTY, stack, ItemStack.EMPTY, stack, new ItemStack(Blocks.COBBLESTONE), stack, ItemStack.EMPTY, stack, ItemStack.EMPTY)).isEmpty()) {
                DynamicRecipes.addAssemblerRecipe(RecipeDualInput.create(RecipeIngredientOre.create("stoneCobble"), RecipeIngredientItemStack.create(stack, 4), result, 1600, 2));
            }
        }

        switch (name) {
            case "ingotQuicksilver":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, new ItemStack(BlockItems.Dust.CINNABAR.getInstance()), stack);
                break;
            case "ingotManganese":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, new ItemStack(BlockItems.Dust.MANGANESE.getInstance()), stack);
                break;
            case "ingotMagnesium":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, new ItemStack(BlockItems.Dust.MAGNESIUM.getInstance()), stack);
                break;
            case "ingotAluminium":
            case "ingotTitanium":
            case "ingotChrome":
            case "ingotTungsten":
            case "ingotSteel":
                ModHandler.removeSmeltingRecipe(stack);
                break;
            case "ingotIron":
                if (GregTechMod.classic) DynamicRecipes.addInductionSmelterRecipe("ingotRefinedIron", stack, new ItemStack(Blocks.SAND), StackUtil.setSize(IC2Items.getItem("ingot", "refined_iron"), 2), ModHandler.slag, 400, 25);
                break;
        }

        ItemStack stick = OreDictUnificator.getFirstOre(name.replaceFirst("ingot","stick"));
        if (!stick.isEmpty()) {
            ModHandler.addShapedRecipe(
                    "stick_"+materialName,
                    stick,
                    "F", "I", 'F', "craftingToolFile", 'I', name
            );

            ItemStack smallDust;
            BlockItems.Smalldust gtSmallDust = GtUtil.getEnumConstantSafely(BlockItems.Smalldust.class, materialName.toUpperCase(Locale.ROOT));
            if (gtSmallDust != null) smallDust = new ItemStack(gtSmallDust.getInstance());
            else {
                smallDust = OreDictUnificator.getFirstOre(name.replaceFirst("ingot", "dustSmall"), 2);
            }

            if (!smallDust.isEmpty()) DynamicRecipes.addLatheRecipe(RecipeLathe.create(RecipeIngredientOre.create(name), Arrays.asList(stick, !smallDust.isEmpty() ? smallDust : stick), !smallDust.isEmpty() ? 50 : 150, 16));
        }
    }

    private void processSmalldustRecipe(ItemStack stack, String name, String smalldustName, int count) {
        Recipes.recyclerBlacklist.add(Recipes.inputFactory.forOreDict(name));
        String dustName = name.replaceFirst(smalldustName, "dust");
        ItemStack dust = OreDictUnificator.getFirstOre(dustName);
        if (!dust.isEmpty()) {
            Ingredient ingredient = new OreIngredient(name);
            Ingredient[] ingredients = new Ingredient[count];
            Arrays.fill(ingredients, ingredient);
            ModHandler.addShapelessRecipe(dustName+"FromSmalldusts", dust, ingredients);
            ModHandler.addShapelessRecipe(dustName+"ToSmalldusts", StackUtil.copyWithSize(stack, count), new OreIngredient(dustName));
        }
    }

    private void processDust(ItemStack stack, String name) {
        if (name.startsWith("dustSmall")) processSmalldustRecipe(stack, name, "dustSmall", 4);
        if (name.startsWith("dustTiny")) processSmalldustRecipe(stack, name, "dustTiny", 9);

        switch (name) {
            case "dustSteel":
                if (GregTechAPI.getDynamicConfig("blast_furnace_requirements", "steel", true)) ModHandler.removeSmeltingRecipe(stack);
                else DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, new ItemStack(BlockItems.Ingot.STEEL.getInstance()));
                break;
            case "dustChrome":
                if (GregTechAPI.getDynamicConfig("blast_furnace_requirements", "chrome", true)) ModHandler.removeSmeltingRecipe(stack);
                else DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.CHROME.getInstance());
                break;
            case "dustTitanium":
                if (GregTechAPI.getDynamicConfig("blast_furnace_requirements", "titanium", true)) ModHandler.removeSmeltingRecipe(stack);
                else DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.TITANIUM.getInstance());
                break;
            case "dustAluminium":
            case "dustAluminum":
                if (GregTechAPI.getDynamicConfig("blast_furnace_requirements", "aluminium", true)) ModHandler.removeSmeltingRecipe(stack);
                else DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.ALUMINIUM.getInstance());
                break;
            case "dustTungsten":
                if (GregTechAPI.getDynamicConfig("blast_furnace_requirements", "tungsten", true)) ModHandler.removeSmeltingRecipe(stack);
                else DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.TUNGSTEN.getInstance());
                break;
            case "dustInvar":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.INVAR.getInstance());
                break;
            case "dustBronze":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, IC2Items.getItem("ingot", "bronze"));
                break;
            case "dustBrass":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.BRASS.getInstance());
                break;
            case "dustCopper":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, IC2Items.getItem("ingot", "copper"));
                break;
            case "dustGold":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, Items.GOLD_INGOT);
                break;
            case "dustNickel":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.NICKEL.getInstance());
                break;
            case "dustIron":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, Items.IRON_INGOT);
                break;
            case "dustAntimony":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.ANTIMONY.getInstance());
                break;
            case "dustTin":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, IC2Items.getItem("ingot", "tin"));
                break;
            case "dustZinc":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.ZINC.getInstance());
                break;
            case "dustPlatinum":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.PLATINUM.getInstance());
                break;
            case "dustLead":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.LEAD.getInstance());
                break;
            case "dustSilver":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.SILVER.getInstance());
                break;
            case "dustCoal":
                ModHandler.addLiquidTransposerFillRecipe(stack, new FluidStack(FluidRegistry.WATER, 125), IC2Items.getItem("dust", "coal_fuel"), 1250);
                break;
            case "dustElectrum":
                DynamicRecipes.addDustToIngotSmeltingRecipe(name, stack, BlockItems.Ingot.ELECTRUM.getInstance());
                break;
            case "dustWheat":
            case "dustFlour":
                DynamicRecipes.addSmeltingRecipe(name, stack, new ItemStack(Items.BREAD));
                break;
        }
    }

    private void processOre(ItemStack stack, String name) {
        String dustName = name.replaceFirst("ore", "dust");
        ItemStack dust = OreDictUnificator.getFirstOre(dustName);
        if (!dust.isEmpty()) {
            ModHandler.addShapedRecipe(
                    name+"Crushing",
                    dust,
                    "T", "O", 'T', "craftingToolHardHammer", 'O', name);
        }

        switch (name) {
            case "oreIron":
            case "orePyrite":
                DynamicRecipes.addInductionSmelterRecipe(name, stack, new ItemStack(Blocks.SAND), new ItemStack(Items.IRON_INGOT), ModHandler.slag, 3000, 10);
                DynamicRecipes.addInductionSmelterRecipe(name, stack, ModHandler.slagRich, new ItemStack(Items.IRON_INGOT, 2), ModHandler.slag, 3000, 95);
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, Items.IRON_INGOT);
                break;
            case "oreGold":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, Items.GOLD_INGOT);
                break;
            case "oreSilver":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, new ItemStack(BlockItems.Ingot.SILVER.getInstance()));
                break;
            case "oreLead":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, BlockItems.Ingot.LEAD.getInstance());
                break;
            case "oreCopper":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, IC2Items.getItem("ingot", "copper"));
                break;
            case "oreTin":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, IC2Items.getItem("ingot", "tin"));
                break;
            case "oreZinc":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, BlockItems.Ingot.ZINC.getInstance());
                break;
            case "oreCassiterite":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, StackUtil.copyWithSize(IC2Items.getItem("ingot", "tin"), 2));
                break;
            case "oreAntimony":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, BlockItems.Ingot.ANTIMONY.getInstance());
                break;
            case "oreCooperite":
            case "oreSheldonite":
            case "orePlatinum":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, BlockItems.Ingot.PLATINUM.getInstance());
                break;
            case "oreNickel":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, BlockItems.Ingot.NICKEL.getInstance());
                break;
            case "oreAluminium":
            case "oreAluminum":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, GregTechAPI.getDynamicConfig("blast_furnace_requirements", "aluminium", true) ? BlockItems.Dust.ALUMINIUM.getInstance() : BlockItems.Ingot.ALUMINIUM.getInstance());
                break;
            case "oreSteel":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, GregTechAPI.getDynamicConfig("blast_furnace_requirements", "steel", true) ? BlockItems.Dust.STEEL.getInstance() : BlockItems.Ingot.STEEL.getInstance());
                break;
            case "oreTitan":
            case "oreTitanium":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, GregTechAPI.getDynamicConfig("blast_furnace_requirements", "titanium", true) ? BlockItems.Dust.TITANIUM.getInstance() : BlockItems.Ingot.TITANIUM.getInstance());
                break;
            case "oreChrome":
            case "oreChromium":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, GregTechAPI.getDynamicConfig("blast_furnace_requirements", "chrome", true) ? BlockItems.Dust.CHROME.getInstance() : BlockItems.Ingot.CHROME.getInstance());
                break;
            case "oreElectrum":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, BlockItems.Ingot.ELECTRUM.getInstance());
                break;
            case "oreTungsten":
            case "oreTungstate":
                DynamicRecipes.addSmelterOreToIngotsRecipe(stack, GregTechAPI.getDynamicConfig("blast_furnace_requirements", "tungsten", true) ? BlockItems.Dust.TUNGSTEN.getInstance() : BlockItems.Ingot.TUNGSTEN.getInstance());
                break;
            case "orePhosphorite":
                DynamicRecipes.addSmeltingRecipe(name, stack, new ItemStack(BlockItems.Dust.PHOSPHORUS.getInstance(), 2));
                break;
            case "oreSaltpeter":
                DynamicRecipes.addSmeltingRecipe(name, stack, new ItemStack(BlockItems.Dust.SALTPETER.getInstance(), 3));
                break;
            case "oreSulfur":
                DynamicRecipes.addSmeltingRecipe(name, stack, StackUtil.copyWithSize(IC2Items.getItem("dust", "sulfur"), 3));
                break;
        }
    }
}
