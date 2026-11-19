package ru.darkthrone.gameplay;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class PrimordialFuryEffect extends Effect {
    public PrimordialFuryEffect(EffectType type, int color) {
        super(type, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "5d8e6b7a-3c1f-4a2e-9b60-0d4f7e2a1c11",
                0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "9a7b1c3e-6d2f-4e48-8b15-2c0e5f7a9d22",
                0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
