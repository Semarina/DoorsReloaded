package de.jeff_media.doorsreloaded.scheduler;

import de.jeff_media.doorsreloaded.config.ModConfig;
import de.jeff_media.doorsreloaded.utils.DoorUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoorScheduler {

    private static final Map<GlobalPos, Long> scheduledClosures = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<GlobalPos, Long>> iterator = scheduledClosures.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<GlobalPos, Long> entry = iterator.next();
                if (currentTime >= entry.getValue()) {
                    GlobalPos globalPos = entry.getKey();
                    ServerLevel world = server.getLevel(globalPos.dimension());
                    if (world != null) {
                        try {
                            closeDoor(world, globalPos.pos());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    iterator.remove();
                }
            }
        });
    }

    public static void scheduleAutoClose(ServerLevel world, BlockPos pos) {
        long delaySeconds = ModConfig.getInstance().autoclose;
        if (delaySeconds <= 0) return;

        GlobalPos globalPos = GlobalPos.of(world.dimension(), pos);
        scheduledClosures.put(globalPos, System.currentTimeMillis() + (delaySeconds * 1000L));
    }

    private static void closeDoor(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!DoorUtils.isDoor(state) && !DoorUtils.isTrapDoor(state)) return;

        boolean isOpen;
        if (DoorUtils.isDoor(state)) {
            isOpen = state.getValue(DoorBlock.OPEN);
        } else {
            isOpen = state.getValue(TrapDoorBlock.OPEN);
        }

        if (isOpen) {
            // Close it
            if (DoorUtils.isDoor(state)) {
                world.setBlock(pos, state.setValue(DoorBlock.OPEN, false), 10);
                world.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f); // Default sound
                
                // Double door check
                if (ModConfig.getInstance().allow_doubledoors) {
                    BlockPos otherPos = DoorUtils.getOtherDoorPart(world, pos, state);
                    if (otherPos != null) {
                        BlockState otherState = world.getBlockState(otherPos);
                        if (otherState.getValue(DoorBlock.OPEN)) {
                             world.setBlock(otherPos, otherState.setValue(DoorBlock.OPEN, false), 10);
                             // Remove other door from schedule if present to avoid double close sound/logic
                             scheduledClosures.remove(GlobalPos.of(world.dimension(), otherPos));
                        }
                    }
                }
            } else {
                 world.setBlock(pos, state.setValue(TrapDoorBlock.OPEN, false), 10);
                 world.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }
}
