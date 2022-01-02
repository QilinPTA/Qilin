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

package qilin.pta.toolkits.zipper;

import qilin.core.PTA;
import qilin.pta.toolkits.zipper.analysis.Zipper;
import qilin.pta.toolkits.zipper.pta.PointsToAnalysis;
import qilin.pta.toolkits.zipper.pta.WrapperedPointsToAnalysis;
import qilin.util.ANSIColor;
import qilin.util.TimeWatcher;
import soot.SootMethod;

import java.util.Comparator;
import java.util.Set;

public class Main {

    public static void run(PTA prePTA, Set<SootMethod> zipperPCMOutput) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        Global.setThread(numThreads);
        Global.setExpress(false);
        String zipperStr = Global.isExpress() ? "Zipper-e" : "Zipper";
        final PointsToAnalysis pta = readPointsToAnalysis(prePTA);
        TimeWatcher zipperTimer = new TimeWatcher("Zipper Timer");
        System.out.println(ANSIColor.BOLD + ANSIColor.YELLOW + zipperStr + " starts ..." + ANSIColor.RESET);
        String flows = Global.getFlow() != null ? Global.getFlow() : "Direct+Wrapped+Unwrapped";
        System.out.println("Precision loss patterns: " + ANSIColor.BOLD + ANSIColor.GREEN + flows + ANSIColor.RESET);
        Zipper.outputNumberOfClasses(pta);
        zipperTimer.start();
        Zipper zipper = new Zipper(pta);
        Set<SootMethod> pcm = zipper.analyze();
        zipperTimer.stop();
        System.out.print(ANSIColor.BOLD + ANSIColor.YELLOW + zipperStr + " finishes, analysis time: " + ANSIColor.RESET);
        System.out.print(ANSIColor.BOLD + ANSIColor.GREEN);
        System.out.printf("%.2fs", zipperTimer.inSecond());
        System.out.println(ANSIColor.RESET);

        System.out.println("Writing Zipper precision-critical methods ...\n");
        System.out.println();
        writeZipperResults(pcm, zipperPCMOutput);
    }

    public static PointsToAnalysis readPointsToAnalysis(PTA prePTA) {
        final TimeWatcher ptaTimer = new TimeWatcher("Points-to Analysis Timer");
        System.out.println("Reading points-to analysis results ... ");
        ptaTimer.start();
        final PointsToAnalysis pta = new WrapperedPointsToAnalysis(prePTA);
        ptaTimer.stop();
        System.out.print(ANSIColor.BOLD + ANSIColor.YELLOW + "Reading time: " + ANSIColor.RESET);
        System.out.print(ANSIColor.BOLD + ANSIColor.GREEN);
        System.out.printf("%.2fs", ptaTimer.inSecond());
        System.out.println(ANSIColor.RESET);
        return pta;
    }

    private static void writeZipperResults(final Set<SootMethod> results, final Set<SootMethod> outputSet) {
        results.stream().sorted(Comparator.comparing(Object::toString)).forEach(outputSet::add);
    }

}
