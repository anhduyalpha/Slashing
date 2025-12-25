package com.main.slashing.rpg;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class ExpressionEvaluator {
    private ExpressionEvaluator() {}

    public static double evaluate(String expr) {
        if (expr == null || expr.isBlank()) return 0;
        List<String> tokens = tokenize(expr);
        List<String> rpn = toRpn(tokens);
        return evalRpn(rpn);
    }

    private static List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                int start = i;
                i++;
                while (i < expr.length()) {
                    char n = expr.charAt(i);
                    if (Character.isDigit(n) || n == '.') {
                        i++;
                    } else {
                        break;
                    }
                }
                tokens.add(expr.substring(start, i));
                continue;
            }
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')') {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }
            i++;
        }
        return tokens;
    }

    private static int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> 0;
        };
    }

    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private static List<String> toRpn(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();
        for (String token : tokens) {
            if (isOperator(token)) {
                while (!ops.isEmpty() && isOperator(ops.peek()) && precedence(ops.peek()) >= precedence(token)) {
                    output.add(ops.pop());
                }
                ops.push(token);
            } else if (token.equals("(")) {
                ops.push(token);
            } else if (token.equals(")")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                if (!ops.isEmpty() && ops.peek().equals("(")) {
                    ops.pop();
                }
            } else {
                output.add(token);
            }
        }
        while (!ops.isEmpty()) {
            output.add(ops.pop());
        }
        return output;
    }

    private static double evalRpn(List<String> rpn) {
        Deque<Double> stack = new ArrayDeque<>();
        for (String token : rpn) {
            if (isOperator(token)) {
                double b = stack.isEmpty() ? 0 : stack.pop();
                double a = stack.isEmpty() ? 0 : stack.pop();
                double result = switch (token) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    case "/" -> b == 0 ? 0 : a / b;
                    default -> 0;
                };
                stack.push(result);
            } else {
                try {
                    stack.push(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    stack.push(0d);
                }
            }
        }
        return stack.isEmpty() ? 0 : stack.pop();
    }
}
