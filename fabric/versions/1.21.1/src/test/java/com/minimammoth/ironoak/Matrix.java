package com.minimammoth.ironoak;

import java.util.ArrayList;
import java.util.List;

import static com.minimammoth.ironoak.IronOak.MOD_ID;

/**
 * The 6x3 matrix, as strings.
 *
 * <p>Deliberately not derived from {@code ModBlocks}, {@code ModSaplingGenerators} or
 * {@code ModConfiguredFeatures}. Those constants <em>were</em> the bug in #30 — their names
 * lied while everything they held was self-consistent — so a test that enumerated them
 * would have restated the bug and passed. The expected set is written out here instead, and
 * every arm is looked up.
 *
 * <p>Adding a metal or a wood type means adding it here too, and then watching eighteen
 * more assertions tell you which of the twelve places you forgot.
 */
public final class Matrix {
    public static final List<String> METALS = List.of("copper", "gold", "iron");
    public static final List<String> WOODS = List.of("oak", "acacia", "birch", "jungle", "spruce", "dark_oak");

    private Matrix() {
    }

    /**
     * One arm of the matrix: a metal on a wood type.
     */
    public record Arm(String metal, String wood) {
        /** {@code copper_dark_oak} — the prefix every id of this arm is built from. */
        public String prefix() {
            return metal + "_" + wood;
        }

        public String saplingId() {
            return MOD_ID + ":" + prefix() + "_sapling";
        }

        public String logId() {
            return MOD_ID + ":" + prefix() + "_log";
        }

        public String treeFeatureId() {
            return MOD_ID + ":" + prefix() + "_tree";
        }

        @Override
        public String toString() {
            return prefix();
        }
    }

    /** All eighteen arms, in a stable order. */
    public static List<Arm> arms() {
        List<Arm> arms = new ArrayList<>(METALS.size() * WOODS.size());
        for (String metal : METALS) {
            for (String wood : WOODS) {
                arms.add(new Arm(metal, wood));
            }
        }
        return arms;
    }
}
