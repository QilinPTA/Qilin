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

package qilin.core.solver.concurrent;

import heros.solver.CountingThreadPoolExecutor;
import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.core.builder.CallGraphBuilder;
import qilin.core.pag.*;
import qilin.core.solver.Propagator;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConcurrentSolver extends Propagator {
    private final CallGraphBuilder cgb;

    protected CountingThreadPoolExecutor executor;

    public ConcurrentSolver(PTA pta) {
        System.out.println("this is concurrent solver!");
        this.cgb = pta.getCgb();
        PAG pag = pta.getPag();
        int threadNum = CoreConfig.v().getPtaConfig().threadNum;
        this.executor = new CountingThreadPoolExecutor(threadNum, Integer.MAX_VALUE, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        this.cgb.setThreadExecutor(executor);
        pag.setExecutor(executor);
    }

    @Override
    public void propagate() {
        cgb.initReachableMethods();
        {
            // run executor and await termination of tasks
            runExecutorAndAwaitCompletion();
        }
        // ask executor to shut down;
        // this will cause new submissions to the executor to be rejected,
        // but at this point all tasks should have completed anyway
        executor.shutdown();

        // Wait for the executor to be really gone
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // silently ignore the exception, it's not an issue if the
                // thread gets aborted
            }
        }
        executor = null;
    }

    private void runExecutorAndAwaitCompletion() {
        try {
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Throwable exception = executor.getException();
        if (exception != null) {
            throw new RuntimeException("There were exceptions during IFDS analysis. Exiting.", exception);
        }
    }
}
