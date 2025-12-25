package com.main.slashing.rpg;

import java.util.Map;

public record ActiveStatus(String id, long endMs, Map<String, Boolean> flags) {}
