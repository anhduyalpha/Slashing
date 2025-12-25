package com.main.slashing.rpg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RpgFormula {
    private static final Pattern STAT_PATTERN = Pattern.compile("STAT\\.([a-zA-Z0-9_]+)");

    private RpgFormula() {}

    public static double evaluate(String expr, PlayerProfile profile) {
        if (expr == null || expr.isBlank()) return 0;
        String replaced = replaceStats(expr, profile);
        return ExpressionEvaluator.evaluate(replaced);
    }

    private static String replaceStats(String expr, PlayerProfile profile) {
        Matcher matcher = STAT_PATTERN.matcher(expr);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String stat = matcher.group(1);
            int value = profile.stats().getOrDefault(stat, 0);
            matcher.appendReplacement(sb, Integer.toString(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
