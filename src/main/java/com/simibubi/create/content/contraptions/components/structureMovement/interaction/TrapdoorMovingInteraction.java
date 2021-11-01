package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;

public class TrapdoorMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
		SoundEvent sound = currentState.getValue(TrapDoorBlock.OPEN) ? SoundEvents.WOODEN_TRAPDOOR_CLOSE
			: SoundEvents.WOODEN_TRAPDOOR_OPEN;
		float pitch = player.level.random.nextFloat() * 0.1F + 0.9F;
		playSound(player, sound, pitch);
		return currentState.cycle(TrapDoorBlock.OPEN);
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}