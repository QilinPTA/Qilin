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
import qilin.core.PTA;
import qilin.test.util.JunitTests;
import qilin.util.Util;

import java.io.File;

public class ReflogTests extends JunitTests {
    @Test
    public void testFieldGetStatic() {
        String[] args = generateArguments("qilin.microben.core.reflog.FieldGetStatic");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "FieldGetStatic.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testFieldGet() {
        String[] args = generateArguments("qilin.microben.core.reflog.FieldGet");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "FieldGet.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testFieldSetStatic() {
        String[] args = generateArguments("qilin.microben.core.reflog.FieldSetStatic");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "FieldSetStatic.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testFieldSet() {
        String[] args = generateArguments("qilin.microben.core.reflog.FieldSet");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "FieldSet.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testArrayNewInstance() {
        String[] args = generateArguments("qilin.microben.core.reflog.ArrayNewInstance");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "ArrayNewInstance.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testConstructorNewInstance() {
        String[] args = generateArguments("qilin.microben.core.reflog.ConstructorNewInstance");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "ConstructorNewInstance.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testMethodInvokeStatic() {
        String[] args = generateArguments("qilin.microben.core.reflog.MethodInvokeStatic");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "MethodInvokeStatic.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testMethodInvoke() {
        String[] args = generateArguments("qilin.microben.core.reflog.MethodInvoke");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "MethodInvoke.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testClassNewInstance() {
        String[] args = generateArguments("qilin.microben.core.reflog.ClassNewInstance");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "ClassNewInstance.log"});
        checkAssertions(Main.run(newArgs));
    }

    @Test
    public void testDoopRefBug() {
        String[] args = generateArguments("qilin.microben.core.reflog.DoopRefBug");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "DoopRefBug.log"});
        PTA pta = Main.run(newArgs);
        checkAssertions(pta);
    }

    @Test
    public void testClassForName() {
        String[] args = generateArguments("qilin.microben.core.reflog.ClassForName");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "ClassForName.log"});
        PTA pta = Main.run(newArgs);
        checkAssertions(pta);
    }

    @Test
    public void testClassForName1() {
        String[] args = generateArguments("qilin.microben.core.reflog.ClassForName1");
        String[] newArgs = Util.concat(args, new String[]{"-reflectionlog", refLogPath + File.separator + "ClassForName1.log"});
        PTA pta = Main.run(newArgs);
        checkAssertions(pta);
    }
}