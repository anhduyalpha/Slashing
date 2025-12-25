package com.main.slashing.rpg;

import java.util.HashMap;
import java.util.Map;

public final class PlayerProfile {
    private String classId = "";
    private int level = 1;
    private final Map<String, Integer> stats = new HashMap<>();
    private final Map<String, ResourceState> resources = new HashMap<>();
    private final Map<String, ActiveStatus> statuses = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();

    public String classId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId == null ? "" : classId;
    }

    public int level() {
        return level;
    }

    public Map<String, Integer> stats() {
        return stats;
    }

    public Map<String, ResourceState> resources() {
        return resources;
    }

    public Map<String, ActiveStatus> statuses() {
        return statuses;
    }

    public Map<String, Long> cooldowns() {
        return cooldowns;
    }
}
