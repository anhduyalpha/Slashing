package com.main.slashing.rpg;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class RpgCommands {
    private RpgCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rpg")
            .then(reloadCommand())
            .then(classCommand())
            .then(castCommand())
            .then(profileCommand())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reloadCommand() {
        return Commands.literal("reload")
            .requires(src -> src.hasPermission(2))
            .executes(ctx -> {
                RpgDataManager.loadAll();
                ctx.getSource().sendSuccess(() -> Component.literal("RPG packs reloaded."), true);
                return 1;
            });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> classCommand() {
        return Commands.literal("class")
            .then(Commands.literal("set")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("players", EntityArgument.players())
                    .then(Commands.argument("classId", StringArgumentType.string())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(getSortedClassIds(), builder))
                        .executes(ctx -> {
                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
                            String classId = StringArgumentType.getString(ctx, "classId");
                            RpgClassDef def = RpgDataManager.getClasses().get(classId);
                            if (def == null) {
                                ctx.getSource().sendFailure(Component.literal("Class not found: " + classId));
                                return 0;
                            }
                            for (ServerPlayer player : players) {
                                RpgProfileManager.applyClass(player, def);
                                player.sendSystemMessage(Component.literal("Class set to " + classId));
                            }
                            return 1;
                        }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> castCommand() {
        return Commands.literal("cast")
            .then(Commands.argument("skillId", StringArgumentType.string())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(getSortedSkillIds(), builder))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    String skillId = StringArgumentType.getString(ctx, "skillId");
                    RpgCastManager.cast(player, skillId);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> profileCommand() {
        return Commands.literal("profile")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                PlayerProfile profile = RpgProfileManager.get(player);
                player.sendSystemMessage(Component.literal("Class: " + profile.classId() + " | Level: " + profile.level()));
                player.sendSystemMessage(Component.literal("Stats: " + profile.stats()));
                player.sendSystemMessage(Component.literal("Resources: " + formatResources(profile)));
                player.sendSystemMessage(Component.literal("Statuses: " + profile.statuses().keySet()));
                return 1;
            });
    }

    private static List<String> getSortedClassIds() {
        return RpgDataManager.getClasses().keySet().stream().sorted().toList();
    }

    private static List<String> getSortedSkillIds() {
        return RpgDataManager.getSkills().keySet().stream().sorted().toList();
    }

    private static String formatResources(PlayerProfile profile) {
        return profile.resources().entrySet().stream()
            .sorted(Comparator.comparing(java.util.Map.Entry::getKey))
            .map(entry -> entry.getKey() + "=" + String.format("%.1f", entry.getValue().current()) + "/" + String.format("%.1f", entry.getValue().max()))
            .reduce((a, b) -> a + ", " + b)
            .orElse("none");
    }
}
