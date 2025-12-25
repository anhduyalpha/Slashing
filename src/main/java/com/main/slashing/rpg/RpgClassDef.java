package com.main.slashing.rpg;

import java.util.Map;

public record RpgClassDef(String id, Map<String, Integer> baseStats, Map<String, RpgResourceDef> resources) {}
