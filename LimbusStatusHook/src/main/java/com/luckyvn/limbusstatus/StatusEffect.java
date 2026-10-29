package com.luckyvn.limbusstatus;

import java.util.List;

/** Một Keyword Status Effect (Burn, Bleed, ...) và danh sách ID stat có thể cấp nó. */
public record StatusEffect(String id, String name, String color, List<String> stats, String format) {
}
