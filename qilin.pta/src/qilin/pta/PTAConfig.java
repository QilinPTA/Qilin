/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021 Dongjie He
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.pta;

import driver.PTAPattern;
import qilin.CoreConfig;

public class PTAConfig extends CoreConfig {
    private static PTAConfig config = null;

    public static PTAConfig v() {
        if (config == null) {
            config = new PTAConfig();
            coreConfig = config;
        }
        return config;
    }

    public static void reset() {
        CoreConfig.reset();
        config = null;
    }

    public static class PointerAnalysisConfiguration extends CorePTAConfiguration {
        public PTAPattern ptaPattern;

        /**
         * If this option is turned on, variables are either analyzed context-sensitive or insensitive.
         */
        public boolean useBinaryLevel = false;
        /**
         * If this option is turned on, only methods use selective context level (for eagle and mercurial only).
         */
        public boolean methodLevel = false;

        /**
         * If this option is turned on, then main analysis will not run.
         */
        public boolean preAnalysisOnly = false;

    }

    /*
     * Notice that the DEFAULT option is equivalent to EXCLUDE_FACTORY_TOP_ONLY.
     * X => EXCLUDE
     * */
    public enum HGConfig {
        DEFAULT, X_FACTORY_NONE, X_FACTORY_BOTH, ZERO_TOP, ZERO_TOP2, ZERO_TOP3, ZERO_BOTTOM, ZERO_BOTTOM2,
        BOTTOM_A, BOTTOM_B, TOP_A, TOP_B, BOTTOM_TOP_A, BOTTOM_TOP_B, PHASE_TWO, PHASE_ONE
    }

    public HGConfig hgConfig = HGConfig.DEFAULT;

    private PTAConfig() {
        this.ptaConfig = new PointerAnalysisConfiguration();
    }

    public PointerAnalysisConfiguration getPtaConfig() {
        return (PointerAnalysisConfiguration) this.ptaConfig;
    }
}
