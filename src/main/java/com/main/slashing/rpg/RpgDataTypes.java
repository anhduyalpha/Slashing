package com.main.slashing.rpg;

import java.util.List;
import java.util.Map;

public record RpgClassDef(String id, Map<String, Integer> baseStats, Map<String, RpgResourceDef> resources) {}

record RpgResourceDef(double max, double regenPerSec, double gainOnCast) {}

record RpgStatusDef(String id, long durationMs, Map<String, Boolean> flags) {}

record RpgSkillDef(String id, RpgCastDef cast, RpgTargetingDef targeting, List<RpgActionDef> pipeline) {}

record RpgCastDef(long cooldownMs, long timeMs, Map<String, Double> cost) {}

record RpgTargetingDef(String type, double range, String fallback) {}
