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
import org.junit.Ignore;
import org.junit.Test;
import qilin.test.util.JunitTests;

public class NativeTests extends JunitTests {
    @Test
    public void testArrayCopy() {
        String[] args = generateArguments("qilin.microben.core.natives.ArrayCopy");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testObjectClone() {
        String[] args = generateArguments("qilin.microben.core.natives.ObjectClone");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testPrivilegedActions0() {
        String[] args = generateArguments("qilin.microben.core.natives.PrivilegedActions0");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testPrivilegedActions1() {
        String[] args = generateArguments("qilin.microben.core.natives.PrivilegedActions1");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testPrivilegedActions2() {
        String[] args = generateArguments("qilin.microben.core.natives.PrivilegedActions2", "2o");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testSystemIn() {
        String[] args = generateArguments("qilin.microben.core.natives.SystemIn");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testSystemOut() {
        String[] args = generateArguments("qilin.microben.core.natives.SystemOut");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testSystemErr() {
        String[] args = generateArguments("qilin.microben.core.natives.SystemErr");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testFinalize() {
        String[] args = generateArguments("qilin.microben.core.natives.Finalize");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testTreadRun() {
        String[] args = generateArguments("qilin.microben.core.natives.TreadRun");
        checkAssertions(Main.run(args));
    }

    @Test
    @Ignore
    public void testCurrentThread() {
        String[] args = generateArguments("qilin.microben.core.natives.CurrentThread");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testRefArrayGet() {
        String[] args = generateArguments("qilin.microben.core.natives.RefArrayGet");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testRefArraySet() {
        String[] args = generateArguments("qilin.microben.core.natives.RefArraySet");
        checkAssertions(Main.run(args));
    }
}
