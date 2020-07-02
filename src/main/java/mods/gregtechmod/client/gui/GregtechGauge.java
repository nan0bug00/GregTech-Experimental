package mods.gregtechmod.client.gui;

import ic2.core.gui.Gauge;
import mods.gregtechmod.common.core.GregtechMod;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum GregtechGauge implements Gauge.IGaugeStyle {
    ArrowRight((new Gauge.GaugePropertyBuilder(10, 0, 10, 10, Gauge.GaugePropertyBuilder.GaugeOrientation.Right)).withTexture(GregtechMod.common_texture).withSmooth(false).build()),
    ArrowUp((new Gauge.GaugePropertyBuilder(20, 0, 10, 10, Gauge.GaugePropertyBuilder.GaugeOrientation.Up)).withTexture(GregtechMod.common_texture).withSmooth(false).build()),
    ArrowLeft((new Gauge.GaugePropertyBuilder(30, 0, 10, 10, Gauge.GaugePropertyBuilder.GaugeOrientation.Left)).withTexture(GregtechMod.common_texture).withSmooth(false).build()),
    ArrowDown((new Gauge.GaugePropertyBuilder(0, 0, 10, 10, Gauge.GaugePropertyBuilder.GaugeOrientation.Down)).withTexture(GregtechMod.common_texture).withSmooth(false).build());

    private static final Map<String, Gauge.IGaugeStyle> map = getMap();

    private final String name;

    public final Gauge.GaugeProperties properties;
    private Gauge.IGaugeStyle values;

    GregtechGauge(Gauge.GaugeProperties properties) {
        this.name = name().toLowerCase(Locale.ENGLISH);
        this.properties = properties;
    }

    public Gauge.GaugeProperties getProperties() {
        return this.properties;
    }

    public static void addStyle(String name, Gauge.IGaugeStyle style) {
        assert name != null : "Cannot add null name";
        assert style != null : "Cannot add null style";
        if (map.containsKey(name))
            throw new RuntimeException("Duplicate style name for " + name + '!');
        map.put(name, style);
    }

    public static Gauge.IGaugeStyle get(String name) {
        return map.get(name);
    }

    private static Map<String, Gauge.IGaugeStyle> getMap() {
        Gauge.IGaugeStyle[] values = values();
        Map<String, Gauge.IGaugeStyle> ret = new HashMap<>(values.length);
        for (Gauge.IGaugeStyle style : values)
            ret.put(style.getClass().getName(), style);
        return ret;
    }

    static {

    }
}
