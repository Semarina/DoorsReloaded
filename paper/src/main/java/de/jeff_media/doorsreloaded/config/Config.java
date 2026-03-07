package de.jeff_media.doorsreloaded.config;

import de.jeff_media.doorsreloaded.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static final String CHECK_FOR_REDSTONE = "check-for-redstone";
    
    public static final String SOUND_KNOCK_DOOR_WOOD = "sound-knock-door-wood";
    public static final String SOUND_KNOCK_DOOR_IRON = "sound-knock-door-iron";
    public static final String SOUND_KNOCK_DOOR_COPPER = "sound-knock-door-copper";
    
    public static final String SOUND_KNOCK_TRAPDOOR_WOOD = "sound-knock-trapdoor-wood";
    public static final String SOUND_KNOCK_TRAPDOOR_IRON = "sound-knock-trapdoor-iron";
    public static final String SOUND_KNOCK_TRAPDOOR_COPPER = "sound-knock-trapdoor-copper";

    public static final String SOUND_KNOCK_GATE_WOOD = "sound-knock-gate-wood";
    public static final String SOUND_KNOCK_GATE_IRON = "sound-knock-gate-iron";
    public static final String SOUND_KNOCK_GATE_COPPER = "sound-knock-gate-copper";

    public static final String SOUND_KNOCK_VOLUME = "sound-knock-volume";
    public static final String SOUND_KNOCK_PITCH = "sound-knock-pitch";
    public static final String SOUND_KNOCK_CATEGORY = "sound-knock-category";
    public static final String ALLOW_KNOCKING_DOORS = "allow-knocking-doors";

    public static final String ALLOW_KNOCKING_TRAPDOORS = "allow-knocking-trapdoors";
    public static final String ALLOW_KNOCKING_GATES = "allow-knocking-gates";

    public static final String KNOCKING_REQUIRES_SHIFT = "knocking-requires-shift";
    public static final String KNOCKING_REQUIRES_EMPTY_HAND = "knocking-requires-empty-hand";
    public static final String DEBUG = "debug";
    public static final String ALLOW_DOUBLEDOORS = "allow-doubledoors";
    public static final String ALLOW_IRONDOORS = "allow-opening-irondoors-with-hands";
    public static final String ALLOW_IRONTRAPDOORS = "allow-opening-irontrapdoors-with-hands";
    public static final String AUTO_CLOSE = "autoclose";

    public static void init() {
        Main main = Main.getInstance();
        FileConfiguration conf = main.getConfig();
        conf.addDefault(CHECK_FOR_REDSTONE, true);
        conf.addDefault(ALLOW_DOUBLEDOORS, true);
        conf.addDefault(SOUND_KNOCK_DOOR_IRON, "minecraft:entity.zombie.attack_iron_door");
        conf.addDefault(SOUND_KNOCK_DOOR_COPPER, "minecraft:entity.zombie.attack_iron_door");
        conf.addDefault(SOUND_KNOCK_DOOR_WOOD, "minecraft:item.shield.block");
        
        conf.addDefault(SOUND_KNOCK_TRAPDOOR_IRON, "minecraft:entity.zombie.attack_iron_door");
        conf.addDefault(SOUND_KNOCK_TRAPDOOR_COPPER, "minecraft:entity.zombie.attack_iron_door");
        conf.addDefault(SOUND_KNOCK_TRAPDOOR_WOOD, "minecraft:item.shield.block");

        conf.addDefault(SOUND_KNOCK_GATE_IRON, "minecraft:entity.zombie.attack_iron_door");
        conf.addDefault(SOUND_KNOCK_GATE_COPPER, "minecraft:entity.zombie.attack_iron_door");
        conf.addDefault(SOUND_KNOCK_GATE_WOOD, "minecraft:item.shield.block");

        conf.addDefault(SOUND_KNOCK_CATEGORY, "BLOCKS");
        conf.addDefault(SOUND_KNOCK_VOLUME, 1.0);
        conf.addDefault(SOUND_KNOCK_PITCH, 1.0);
        conf.addDefault(ALLOW_KNOCKING_DOORS, true);
        conf.addDefault(ALLOW_KNOCKING_TRAPDOORS, false);
        conf.addDefault(ALLOW_KNOCKING_GATES, false);
        conf.addDefault(KNOCKING_REQUIRES_EMPTY_HAND, false);
        conf.addDefault(KNOCKING_REQUIRES_SHIFT, false);
        conf.addDefault(DEBUG, false);
        conf.addDefault(AUTO_CLOSE, 0);

        boolean oldIronDoors = conf.getBoolean("allow-opening-irondoors-with-hands", false);
        conf.addDefault(ALLOW_IRONDOORS, oldIronDoors);
        conf.addDefault(ALLOW_IRONTRAPDOORS, oldIronDoors);
    }
}
