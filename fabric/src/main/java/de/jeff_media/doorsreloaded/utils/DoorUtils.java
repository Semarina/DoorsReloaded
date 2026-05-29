package de.jeff_media.doorsreloaded.utils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class DoorUtils {

    public static boolean isDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock;
    }

    public static boolean isTrapDoor(BlockState state) {
        return state.getBlock() instanceof TrapDoorBlock;
    }

    public static boolean isGate(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.FENCE_GATES);
    }

    public static BlockPos getOtherDoorPart(Level world, BlockPos pos, BlockState state) {
        if (!isDoor(state)) return null;

        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
        
        // Calculate offset to the other door based on hinge
        // If hinge is left, other door is to the right
        // If hinge is right, other door is to the left
        Direction offsetDir;
        if (hinge == DoorHingeSide.LEFT) {
             offsetDir = facing.getClockWise();
        } else {
             offsetDir = facing.getCounterClockWise();
        }

        BlockPos otherPos = pos.relative(offsetDir);
        BlockState otherState = world.getBlockState(otherPos);

        if (isDoor(otherState) && otherState.getBlock() == state.getBlock()) {
            // Check if they match in facing and callback (hinge should be opposite)
            if (otherState.getValue(DoorBlock.FACING) == state.getValue(DoorBlock.FACING) &&
                otherState.getValue(DoorBlock.HINGE) != hinge) {
                 // Also check half to match 
                 if (otherState.getValue(DoorBlock.HALF) == state.getValue(DoorBlock.HALF)) {
                     return otherPos;
                 }
            }
        }
        
        return null;
    }

    public static BlockPos getDoubleDoorNeighbor(Level world, BlockPos pos, BlockState state) {
        // This is effectively the same as getOtherDoorPart but lets be explicit
        // Used to toggle the other door
        return getOtherDoorPart(world, pos, state);
    }
}
