package net.xmilon.himproveme.perk;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public record PerkDefinition(
        Identifier id,
        String nameKey,
        String descriptionKey,
        String categoryKey,
        String unlockFunctionKey,
        Identifier iconItemId,
        Identifier unlockSoundId,
        Identifier accentSoundId,
        int titleColor,
        int maxLevel,
        int xpLevelCost,
        int row,
        int column,
        List<Identifier> requiredPerkIds,
        boolean toggleable
) {
    public PerkDefinition {
        requiredPerkIds = List.copyOf(requiredPerkIds);
    }

    public Text name() {
        return Text.translatable(nameKey);
    }

    public Text description() {
        return Text.translatable(descriptionKey);
    }

    public Text unlockFunction() {
        return Text.translatable(unlockFunctionKey);
    }

    public boolean hasRequirements() {
        return !requiredPerkIds.isEmpty();
    }
}
