package mods.gregtechmod.objects.blocks.teblocks.base;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.core.ExplosionIC2;
import ic2.core.util.Util;
import mods.gregtechmod.api.cover.ICover;
import mods.gregtechmod.api.machine.IElectricMachine;
import mods.gregtechmod.core.GregTechConfig;
import mods.gregtechmod.objects.blocks.teblocks.component.AdjustableEnergy;
import mods.gregtechmod.util.GtUtil;
import mods.gregtechmod.util.MachineSafety;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TileEntityEnergy extends TileEntityCoverBehavior implements IExplosionPowerOverride, IElectricMachine {
    protected boolean energyCapacityTooltip;
    
    protected AdjustableEnergy energy;
    public final int defaultSinkTier;
    public final double defaultEnergyStorage;
    
    protected double[] averageEUInputs = new double[] { 0,0,0,0,0 };
    protected double[] averageEUOutputs = new double[] { 0,0,0,0,0 };
    protected int averageEUInputIndex = 0;
    protected int averageEUOutputIndex = 0;
    private double previousEU;
    public double averageEUIn;
    public double averageEUOut;
    
    public boolean shouldExplode;
    private boolean explode;
    private float explosionPower;

    public TileEntityEnergy(String descriptionKey) {
        super(descriptionKey);
        this.energy = addComponent(createEnergyComponent());
        this.defaultSinkTier = this.energy.getSinkTier();
        this.defaultEnergyStorage = this.energy.getCapacity();
    }
    
    protected abstract AdjustableEnergy createEnergyComponent();
    
    protected Collection<EnumFacing> getSinkSides() {
        return Collections.emptySet();
    }
    
    protected Collection<EnumFacing> getSourceSides() {
        return Collections.emptySet();
    }
    
    @Override
    public int getSinkTier() {
        return this.energy.getSinkTier();
    }

    @Override
    public int getSourceTier() {
        return this.energy.getSourceTier();
    }
    
    @Override
    public void addEnergy(double amount) {
        if (this.energy.isSink() && amount > getMaxInputEUp()) markForExplosion();
        this.energy.charge(amount);
    }
    
    @Override
    public double getMaxInputEUp() {
        return EnergyNet.instance.getPowerFromTier(getSinkTier());
    }

    @Override
    public double getMaxOutputEUt() {
        return this.energy.getMaxOutputEUt();
    }

    protected int getOutputPackets() {
        return 1;
    }
    
    @Override
    public double getStoredEU() {
        return this.energy.getStoredEnergy();
    }
    
    @Override
    public double getEUCapacity() {
        return this.energy.getCapacity();
    }
    
    @Override
    public double getAverageEUInput() {
        return this.averageEUIn;
    }
    
    @Override
    public double getAverageEUOutput() {
        return this.averageEUOut;
    }

    @Override
    public void updateEnet() {
        Collection<EnumFacing> sinkDirs = filterEnergySides(this.energy.getSinkSides());
        Collection<EnumFacing> sourceDirs = filterEnergySides(this.energy.getSourceSides());
    
        this.energy.setSides(sinkDirs, sourceDirs);
    
        updateSourceTier();
    }
    
    private Collection<EnumFacing> filterEnergySides(Collection<EnumFacing> sides) {
        return sides.stream()
                .filter(side -> {
                    ICover cover = this.coverHandler.covers.get(side);
                    return cover != null && cover.allowEnergyTransfer();
                })
                .collect(Collectors.toList());
    } 
    
    protected void updateSourceTier() {
        int packetCount = getOutputPackets();
        this.energy.setSourcePackets(packetCount);
    
        int sourceTier = getSourceTier();
        this.energy.setSourceTier(sourceTier);
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        
        double currentEU = this.energy.getStoredEnergy();
        double input = currentEU - previousEU;
        this.previousEU = currentEU;
            
        if (input > 0) averageEUInputs[averageEUInputIndex] = input;
        if (++averageEUInputIndex >= averageEUInputs.length) averageEUInputIndex = 0;
        if (++averageEUOutputIndex >= averageEUOutputs.length) averageEUOutputIndex = 0;
        
        if (!this.energy.isSink()) {
            double sum = Arrays.stream(averageEUInputs).sum();
            averageEUIn = sum / averageEUInputs.length;
        } else averageEUIn = 0;
        
        if (!this.energy.isSource()) {
            double sum = Arrays.stream(averageEUOutputs).sum();
            averageEUOut = sum / averageEUOutputs.length;
        } else averageEUOut = 0;
        
        if(this.explode) this.explodeMachine(this.explosionPower);
        if (shouldExplode) this.explode = true; //Extra step so machines don't explode before the packet of death is sent
        if (enableMachineSafety()) MachineSafety.checkSafety(this);
    }

    protected boolean enableMachineSafety() {
        return true;
    }

    @Override
    public void markForExplosion() {
        markForExplosion(getExplosionPower(Math.max(getSinkTier(), getSourceTier()) + 1, 1.5F));
    }

    @Override
    public void markForExplosion(float power) {
        this.shouldExplode = true;
        this.explosionPower = power;
        if (GregTechConfig.MACHINES.machineWireFire) {
            double energy = getStoredEU();
            this.energy.onUnloaded();
            this.energy = AdjustableEnergy.createSource(this, getEUCapacity(), 5, Util.allFacings);
            this.energy.onLoaded();
            this.energy.forceCharge(energy);
        }
    }

    @Override
    public boolean shouldExplode() {
        return true;
    }

    @Override
    public float getExplosionPower(int tier, float defaultPower) {
        switch (tier) {
            case 2:
                return GregTechConfig.BALANCE.MVExplosionPower;
            case 3:
                return GregTechConfig.BALANCE.HVExplosionPower;
            case 4:
                return GregTechConfig.BALANCE.EVExplosionPower;
            case 5:
                return GregTechConfig.BALANCE.IVExplosionPower;
            default:
                return GregTechConfig.BALANCE.LVExplosionPower;
        }
    }
    
    public void explodeMachine(float power) {
        int x = this.pos.getX(), y = this.pos.getY(), z = this.pos.getZ();
        this.energy.onUnloaded();
        world.setBlockToAir(this.pos);
        new ExplosionIC2(world, null, x+0.5, y+0.5, z+0.5, power, 0.5F, ExplosionIC2.Type.Normal).doExplosion();
    }
    
    @Override
    protected boolean isFlammable(EnumFacing face) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, tooltip, advanced);
        if (this.energy.isSink()) tooltip.add(GtUtil.translateInfo("max_energy_in", Math.round(getMaxInputEUp())));
        if (this.energy.isSource()) tooltip.add(GtUtil.translateInfo("max_energy_out", Math.round(getMaxOutputEUt())));
        if (this.energyCapacityTooltip) tooltip.add(GtUtil.translateInfo("eu_storage", GtUtil.formatNumber(this.energy.getCapacity())));
    }
}
