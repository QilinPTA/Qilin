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

package qilin.test.core;

import driver.Main;
import org.junit.Test;
import qilin.test.util.JunitTests;

public class ArrayTests extends JunitTests {
    @Test
    public void testArrayIndex() {
        String[] args = generateArguments("qilin.microben.core.array.ArrayIndex");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testMultiArraySimple() {
        String[] args = generateArguments("qilin.microben.core.array.MultiArraySimple");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testMultiArrayComplex() {
        String[] args = generateArguments("qilin.microben.core.array.MultiArrayComplex");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testArrayCopy() {
        String[] args = generateArguments("qilin.microben.core.array.ArrayCopy");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testArrayElemTypeFiltering() {
        String[] args = generateArguments("qilin.microben.core.array.ArrayElemTypeFiltering");
        checkAssertions(Main.run(args));
    }
}
