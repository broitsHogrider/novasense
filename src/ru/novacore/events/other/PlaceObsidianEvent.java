package ru.novacore.events.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import ru.novacore.events.Event;

@Data
@AllArgsConstructor
public class PlaceObsidianEvent extends Event {
    private Block block;
    private BlockPos pos;
}
