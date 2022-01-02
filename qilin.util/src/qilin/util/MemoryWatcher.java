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

package qilin.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MemoryWatcher extends Timer {
    private final long pid;
    private final String name;
    private final long[] maxMemory;

    public MemoryWatcher(long pid, String name) {
        this.pid = pid;
        this.name = name;
        this.maxMemory = new long[1];
    }

    private TimerTask task;

    public void start() {
        this.maxMemory[0] = 0;
        task = new TimerTask() {
            @Override
            public void run() {
                long mem = getVMRSS(pid);
                if (mem > maxMemory[0]) {
                    maxMemory[0] = mem;
                }
            }
        };
        schedule(task, 0, 100);
    }

    public void stop() {
        task.cancel();
        this.cancel();
    }

    private long getVMRSS(long pid) {
        String s = String.format("/proc/%d/status", pid);
        long[] vmrss = new long[1];
        vmrss[0] = -1;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
            for (String line : br.lines().collect(Collectors.toSet())) {
                if (line.startsWith("VmRSS")) {
                    String mem = line.substring(6, line.length() - 2);
                    mem = mem.trim();
                    vmrss[0] = Long.parseLong(mem);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return vmrss[0];
    }

    public double inKiloByte() {
        return maxMemory[0];
    }

    public double inMegaByte() {
        return maxMemory[0] / 1024.0;
    }

    public double inGigaByte() {
        return maxMemory[0] / (1024.0 * 1024.0);
    }

    @Override
    public String toString() {
        return String.format("%s consumed memory: %.2f MB", this.name, this.inMegaByte());
    }
}
