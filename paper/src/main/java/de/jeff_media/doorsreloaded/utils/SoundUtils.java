package de.jeff_media.doorsreloaded.utils;
import com.google.common.base.Enums;
import de.jeff_media.doorsreloaded.Main;
import de.jeff_media.doorsreloaded.config.Config;
import org.bukkit.*;
import org.bukkit.block.Block;

public class SoundUtils {

    public static void playKnockSound(Block block) {
        Main.getInstance().debug("Finally playing sound");
        Main main = Main.getInstance();
        Location location = block.getLocation();
        World world = block.getWorld();
        String soundID;
        boolean isDoor = block.getBlockData() instanceof org.bukkit.block.data.type.Door;
        boolean isTrapDoor = block.getBlockData() instanceof org.bukkit.block.data.type.TrapDoor;
        boolean isGate = block.getBlockData() instanceof org.bukkit.block.data.type.Gate;

        String name = block.getType().name();
        boolean isCopper = name.contains("COPPER");
        boolean isIron = name.contains("IRON");

        // Fetch old values in case of unmigrated config
        String oldIron = main.getConfig().getString("sound-knock-iron");
        String oldCopper = main.getConfig().getString("sound-knock-copper");
        String oldWood = main.getConfig().getString("sound-knock-wood");

        String defaultCopper = oldCopper != null ? oldCopper : "minecraft:entity.zombie.attack_iron_door";
        String defaultIron = oldIron != null ? oldIron : "minecraft:entity.zombie.attack_iron_door";
        String defaultWood = oldWood != null ? oldWood : "minecraft:item.shield.block";

        if (isDoor) {
            if (isCopper) soundID = main.getConfig().getString(Config.SOUND_KNOCK_DOOR_COPPER, defaultCopper);
            else if (isIron) soundID = main.getConfig().getString(Config.SOUND_KNOCK_DOOR_IRON, defaultIron);
            else soundID = main.getConfig().getString(Config.SOUND_KNOCK_DOOR_WOOD, defaultWood);
        } else if (isTrapDoor) {
            if (isCopper) soundID = main.getConfig().getString(Config.SOUND_KNOCK_TRAPDOOR_COPPER, defaultCopper);
            else if (isIron) soundID = main.getConfig().getString(Config.SOUND_KNOCK_TRAPDOOR_IRON, defaultIron);
            else soundID = main.getConfig().getString(Config.SOUND_KNOCK_TRAPDOOR_WOOD, defaultWood);
        } else {
            if (isCopper) soundID = main.getConfig().getString(Config.SOUND_KNOCK_GATE_COPPER, defaultCopper);
            else if (isIron) soundID = main.getConfig().getString(Config.SOUND_KNOCK_GATE_IRON, defaultIron);
            else soundID = main.getConfig().getString(Config.SOUND_KNOCK_GATE_WOOD, defaultWood);
        }
        
        if (soundID == null || soundID.isEmpty()) soundID = "minecraft:item.shield.block";
        float volume = (float) main.getConfig().getDouble(Config.SOUND_KNOCK_VOLUME);
        float pitch = (float) main.getConfig().getDouble(Config.SOUND_KNOCK_PITCH);
        SoundCategory category = Enums.getIfPresent(SoundCategory.class,main.getConfig().getString(Config.SOUND_KNOCK_CATEGORY)).or(SoundCategory.BLOCKS);
        NamespacedKey soundKey = NamespacedKey.fromString(soundID) != null ? NamespacedKey.fromString(soundID) : NamespacedKey.fromString("minecraft:item.shield.block");
        Sound sound = Registry.SOUNDS.get(soundKey);
        world.playSound(location, sound, category,volume,pitch);
        Main.getInstance().debug("World: " + world);
        Main.getInstance().debug("Location: " + location);
        Main.getInstance().debug("Sound: " + soundID);
        Main.getInstance().debug("Category: " + category.name());
        Main.getInstance().debug("Volume: " + volume);
        Main.getInstance().debug("Pitch: " + pitch);
    }

}
