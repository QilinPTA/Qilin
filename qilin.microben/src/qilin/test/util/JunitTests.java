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

package qilin.test.util;

import org.junit.Before;
import org.junit.BeforeClass;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.pta.PTAConfig;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public abstract class JunitTests {
    protected static String appPath, jrePath, refLogPath;

    @BeforeClass
    public static void setUp() throws IOException {
        File currentDir = new File(".");
        File testDir = new File(currentDir, "build" + File.separator + "classes" + File.separator + "java" + File.separator + "test");
        appPath = testDir.getCanonicalPath();
        File refLogDir = new File(currentDir, "src" + File.separator + "qilin" + File.separator + "microben" + File.separator + "core" + File.separator + "reflog");
        refLogPath = refLogDir.getCanonicalPath();
        File jreFile = new File(".." + File.separator + "artifact" + File.separator + "pta" +
                File.separator + "lib" + File.separator + "jre" + File.separator + "jre1.6.0_45");
        jrePath = jreFile.getCanonicalPath();
//        ptaPattern = "e-3o";
//        ptaPattern = "2c";
//        ptaPattern = "insens";
//        ptaPattern = "1c";
//          ptaPattern = "2t";
//          ptaPattern = "2o";
//        ptaPattern = "3o";
//        ptaPattern = "1o";
//        ptaPattern = "2h";
//        ptaPattern = "j-ci";
//        ptaPattern = "m-ci";
//        ptaPattern = "p-ci";
//        ptaPattern = "a-3o";
//        ptaPattern = "n-3h";
//        ptaPattern = "E-2o";
    }

    @Before
    public void resetSootAndStream() {
        System.out.println("reset ...");
        PTAConfig.reset();
        PTAScene.reset();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
    }

    public String[] generateArguments(String mainClass) {
        return generateArguments(mainClass, "insens");
    }

    public String[] generateArguments(String mainClass, String ptaPattern) {
        return new String[]{
                "-singleentry",
                "-pta=" + ptaPattern,
                "-apppath",
                appPath,
                "-mainclass", mainClass,
                "-jre=" + jrePath,
                "-clinit=ONFLY",
                "-lcs",
                "-mh",
                "-pae",
                "-pe",
        };
    }

    protected void checkAssertions(PTA pta) {
        Set<IAssertion> aliasAssertionSet = AssertionsParser.retrieveQueryInfo(pta);
        for (IAssertion mAssert : aliasAssertionSet) {
            boolean answer = mAssert.check();
            System.out.println("Assertion is " + answer);
            assertTrue(answer);
        }
    }
}
