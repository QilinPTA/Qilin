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
import qilin.pta.PTAConfig;
import qilin.test.util.JunitTests;

public class AssignTests extends JunitTests {
    @Test
    public void testCastFail() {
        String[] args = generateArguments("qilin.microben.core.assign.CastFail");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCastSucc() {
        String[] args = generateArguments("qilin.microben.core.assign.CastSucc");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testReceiver2This() {
        String[] args = generateArguments("qilin.microben.core.assign.Receiver2This");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testSimpleAssign() {
        String[] args = generateArguments("qilin.microben.core.assign.SimpleAssign");
        checkAssertions(Main.run(args));
        System.out.println(PTAConfig.v().getAppConfig().MAIN_CLASS);
    }

    @Test
    public void testReturnValue0() {
        String[] args = generateArguments("qilin.microben.core.assign.ReturnValue0");
        checkAssertions(Main.run(args));
        System.out.println(PTAConfig.v().getAppConfig().MAIN_CLASS);
    }

    @Test
    public void testReturnValue1() {
        String[] args = generateArguments("qilin.microben.core.assign.ReturnValue1");
        checkAssertions(Main.run(args));
        System.out.println(PTAConfig.v().getAppConfig().MAIN_CLASS);
    }

    @Test
    public void testReturnValue2() {
        String[] args = generateArguments("qilin.microben.core.assign.ReturnValue2");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testReturnValue3() {
        String[] args = generateArguments("qilin.microben.core.assign.ReturnValue3");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testInterAssign() {
        String[] args = generateArguments("qilin.microben.core.assign.InterAssign");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testStaticParameter() {
        String[] args = generateArguments("qilin.microben.core.assign.StaticParameter");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testNullPointer() {
        String[] args = generateArguments("qilin.microben.core.assign.NullPointer");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testRecursion() {
        String[] args = generateArguments("qilin.microben.core.assign.Recursion");
        checkAssertions(Main.run(args));
        System.out.println(PTAConfig.v().getAppConfig().MAIN_CLASS);
    }
}
