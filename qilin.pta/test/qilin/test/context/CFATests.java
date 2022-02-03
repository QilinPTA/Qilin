/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
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

package qilin.test.context;

import driver.Main;
import org.junit.Test;
import qilin.test.util.JunitTests;

public class CFATests extends JunitTests {

    @Test
    public void testCFA1k0() {
        String[] args = generateArguments("qilin.microben.context.cfa.CFA1k0", "1c");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCFA1k1() {
        String[] args = generateArguments("qilin.microben.context.cfa.CFA1k1", "1c");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCFA1k2() {
        String[] args = generateArguments("qilin.microben.context.cfa.CFA1k2", "1c");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCFA2k() {
        String[] args = generateArguments("qilin.microben.context.cfa.CFA2k", "2c");
        checkAssertions(Main.run(args));
    }
}