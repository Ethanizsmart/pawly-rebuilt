package dev.pawly.rebuilt;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.tags.EnchantmentTags;

/** Read-only tooltip augmentation; never changes the inspected stack. */
public final class TooltipFeatures {
    private TooltipFeatures() {}
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flags, lines) -> {
            if (!PawlyClient.active("render_tooltips")) return;
            var feature = PawlyClient.instance().feature("render_tooltips");
            if (feature.flag("curse_marker")) {
                var enchantments = stack.get(DataComponents.ENCHANTMENTS);
                if (enchantments != null && enchantments.entrySet().stream().anyMatch(e -> e.getKey().is(EnchantmentTags.CURSE)))
                    lines.add(Component.literal("Contains a curse").withStyle(ChatFormatting.RED));
            }
            if (feature.flag("shulkers")) {
                var contents = stack.get(DataComponents.CONTAINER);
                if (contents == null) return;
                var items = contents.nonEmptyStream().toList();
                for (int i = 0; i < Math.min(12, items.size()); i++) {
                    var item = items.get(i);
                    lines.add(Component.literal(item.getCount() + " × ").append(item.getHoverName()).withStyle(ChatFormatting.GRAY));
                }
                if (items.size() > 12) lines.add(Component.literal("… " + (items.size() - 12) + " more stacks").withStyle(ChatFormatting.DARK_GRAY));
            }
        });
    }
}
