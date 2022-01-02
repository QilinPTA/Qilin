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

package qilin.pta.toolkits.turner;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.core.sets.P2SetVisitor;
import qilin.core.sets.PointsToSetInternal;
import qilin.pta.PTAConfig;
import qilin.util.PTAUtils;
import soot.*;

import java.util.*;

// Object Containment Graph
public class OCG {
    public final PTA pta;
    public final int k;
    protected final Map<LocalVarNode, Set<AllocNode>> pts;
    public Map<AllocNode, OCGNode> nodes;
    private int total_node_count = 0;
    private int total_edge_count = 0;

    public OCG(PTA pta, int k) {
        this.pta = pta;
        this.k = k;
        this.pts = PTAUtils.calcStaticThisPTS(pta);
        this.nodes = new HashMap<>();
        buildGraph();
    }

    protected void buildGraph() {
        PAG pag = pta.getPag();
        pag.getAllocNodes().forEach(this::findOrCreate);
        pag.getContextFields().forEach(contextField -> {
            AllocNode base = contextField.getBase();
            if (base instanceof ConstantNode) {
                return;
            }
//            if (base.getMethod() == null) {
//                return;
//            }
            SparkField f = contextField.getField();
            if (f.getType() instanceof ArrayType at) {
                if (at.baseType instanceof PrimType) {
                    return;
                }
            }
            contextField.getP2Set().mapToCIPointsToSet().forall(new P2SetVisitor() {
                @Override
                public void visit(Node n) {
                    AllocNode h1 = (AllocNode) n;
                    if (h1 instanceof ConstantNode) {
                        return;
                    }
                    addEdge(findOrCreate(base), findOrCreate(h1));
                }
            });
        });
    }

    public Collection<OCGNode> allNodes() {
        return nodes.values();
    }

    public int getTotalNodeCount() {
        return total_node_count;
    }

    public int getTotalEdgeCount() {
        return total_edge_count;
    }

    public boolean isAliasable(AllocNode allocNode) {
        OCGNode node = this.nodes.getOrDefault(allocNode, null);
        if (node == null) {
            return false;
        } else {
            return node.level != 0;
        }
    }

    /**
     * (1) case1: objects on OCG have successors but does not have predecessors.
     * (1-1) factorys
     * (1-2) normal uses.
     * (2) case2: objects on OCG does not have successors.
     * (2-1) no predecessors.
     * (2-2) have predecessors.
     * (3) othercase: objects on OCG have successors and predecessors.
     */
    public void stat() {
        int case1 = 0;
        int total_factory = 0;
        int case1_factory = 0;
        int case1_normal = 0;
        int case2 = 0;
        int case2_noPred = 0;
        int case2_hasPred = 0;
        int otherCase = 0;
        for (OCGNode node : nodes.values()) {
            if (node.successors.size() == 0) {
                case2++;
                if (node.predecessors.size() == 0) {
                    case2_noPred++;
                } else {
                    case2_hasPred++;
                }
                if (isFactoryObject(node.ir)) {
                    total_factory++;
                }
            } else if (node.predecessors.size() == 0) {
                case1++;
                if (isFactoryObject(node.ir)) {
                    case1_factory++;
                    // System.out.println(((AllocNode) node.ir).toString2());
                } else {
                    case1_normal++;
                }
            } else {
                if (isFactoryObject(node.ir)) {
                    total_factory++;
                }
                otherCase++;
            }
        }

        System.out.println("#case1:" + case1);
        System.out.println("#total_factory:" + total_factory);
        System.out.println("#case1_factory:" + case1_factory);
        System.out.println("#case1_normal:" + case1_normal);
        System.out.println("#case2:" + case2);
        System.out.println("#case2_noPred:" + case2_noPred);
        System.out.println("#case2_hasPred:" + case2_hasPred);
        System.out.println("#othercase:" + otherCase);
    }

    private OCGNode findOrCreate(AllocNode ir) {
        if (nodes.containsKey(ir)) {
            return nodes.get(ir);
        } else {
            total_node_count++;
            OCGNode ret = new OCGNode(ir);
            nodes.put(ir, ret);
            return ret;
        }
    }

    private boolean isMidObj(OCGNode node) {
        return !node.successors.isEmpty() && !node.predecessors.isEmpty();
    }

    private boolean isOldTop(OCGNode node) {
        return node.predecessors.isEmpty();
    }

    private boolean isOldBottom(OCGNode node) {
        return node.successors.isEmpty();
    }

    private boolean isNewTop(OCGNode node) {
        return isOldTop(node) && !isFactoryObject(node.ir);
    }

    private boolean isNewBottom(OCGNode node) {
        return isOldBottom(node);
    }

    public boolean isTop(AllocNode heap) {
        return !nodes.containsKey(heap) || isNewTop(findOrCreate(heap));
    }

    public boolean isBottom(AllocNode heap) {
        return !nodes.containsKey(heap) || isNewBottom(findOrCreate(heap));
    }

    /*
     * bottom objects:
     * (a) NEW-BOT = 0 and all others k (compared with 2o), same as ZERO_BOTTOM.
     * (b) NEW-BOT = k and all the others same as p-2o (compared with p-2o).
     * */
    private int bottomExpA(OCGNode node) {
        return isNewBottom(node) ? 0 : k;
    }

    private int bottomExpB(OCGNode node) {
        if (isNewBottom(node)) {
            return k;
        } else {
            return excludeFactoryDefault(node);
        }
    }

    /*
     * top objects:
     * (a) NEW-TOP = 0 and all the others as k (compared with 2o)
     * (b) NEW-TOP = k and all the others same as p-2o (compared with p-2o).
     * */

    private int topExpA(OCGNode node) {
        if (isNewTop(node)) {
            return 0;
        } else {
            return k;
        }
    }

    private int topExpB(OCGNode node) {
        if (isNewTop(node)) {
            return k;
        } else {
            return excludeFactoryDefault(node);
        }
    }

    /*
     * top and bottom:
     * (a) NEW-BOT \ cap NEW-TOP = 0 and all others k (compared with 2o)
     * (b) NEW-BOT \cap NEW-TOP = k and all others as in p-2o (compared with p-2o)
     * */
    private int bottomTopExpA(OCGNode node) {
        if (isNewTop(node) && isNewBottom(node)) {
            return 0;
        } else {
            return k;
        }
    }

    private int bottomTopExpB(OCGNode node) {
        if (isNewTop(node) && isNewBottom(node)) {
            return k;
        } else {
            return excludeFactoryDefault(node);
        }
    }

    /*
     * Four kinds of nodes in OCG in total:
     * (1) N1: |succ(n)| == 0 and |pred(n)| == 0
     * (2) N2: |succ(n)| != 0 and |pred(n)| == 0
     * (3) N3: |succ(n)| == 0 and |pred(n)| != 0
     * (4) N4: |succ(n)| != 0 and |pred(n)| != 0
     * Several configuration:
     * (a) X_FACTORY_TOP_ONLY(DEFAULT): Level(N4|N2 \cap FACTORY) = k, Level(N1|N3| N2 \ FACTORY) = 0.
     * (b) X_FACTORY_NONE: Level(N4) = k, Level(N1|N2|N3) = 0.
     * (c) X_FACTORY_BOTH: Level(N4 | Factory) = k; Level(N1 \cup \N2 \cup N3 \ FACTORY) = 0;
     *
     * (d-I) ZERO_BOTTOM: Level(N4) = k; Level(N1|N3) = 0; Level(N2) = k;
     * (d-II) ZERO_BOTTOM2: Level(N3) = 0; Level(N1|N2|N4) = k;
     * (e-I) ZERO_TOP: Level(N1|N2\FACTORY) = 0; Level(N3|N4|N2 \cap FACTORY) = k;
     * (e-II) ZERO_TOP2: Level(N2\FACTORY) = 0; Level(N1|N3|N4|N2 \cap Factory) = k;
     * (e-III) ZERO_TOP3: Level(N2|N3|N4) = k; Level(N1) = 0;
     * (f) ZERO_NONE: Level(N1|N2|N3|N4) = k;
     * */
    private int excludeFactoryDefault(OCGNode node) {
        if (isNewBottom(node) || isNewTop(node)) {
            return 0;
        } else {
            return k;
        }
    }

    private int excludeFactoryNone(OCGNode node) {
        if (isMidObj(node)) {
            return k;
        } else {
            return 0;
        }
    }

    private int excludeFactoryBoth(OCGNode node) {
        if (isMidObj(node) || isFactoryObject(node.ir)) {
            return k;
        } else {
            return 0;
        }
    }

    private int zeroTop(OCGNode node) {
        return isOldTop(node) && (isOldBottom(node) || !isFactoryObject(node.ir)) ? 0 : k;
    }

    private int zeroTop2(OCGNode node) {
        return isNewTop(node) && !isOldBottom(node) ? 0 : k;
    }

    private int zeroTop3(OCGNode node) {
        return isOldTop(node) && isOldBottom(node) ? 0 : k;
    }

    private int zeroBottom(OCGNode node) {
        return isNewBottom(node) ? 0 : k;
    }

    private int zeroBottom2(OCGNode node) {
        return isNewBottom(node) && !isOldTop(node) ? 0 : k;
    }

    public void run() {
        int[] a = new int[k + 1];
        System.out.println(PTAConfig.v().hgConfig);
        for (OCGNode node : nodes.values()) {
            PTAConfig.HGConfig hgConfig = PTAConfig.v().hgConfig;
            switch (hgConfig) {
                case TOP_A -> node.level = topExpA(node);
                case TOP_B -> node.level = topExpB(node);
                case BOTTOM_A -> node.level = bottomExpA(node);
                case BOTTOM_B -> node.level = bottomExpB(node);
                case BOTTOM_TOP_A -> node.level = bottomTopExpA(node);
                case BOTTOM_TOP_B -> node.level = bottomTopExpB(node);
                case ZERO_TOP -> node.level = zeroTop(node);
                case ZERO_TOP2 -> node.level = zeroTop2(node);
                case ZERO_TOP3 -> node.level = zeroTop3(node);
                case ZERO_BOTTOM -> node.level = zeroBottom(node);
                case ZERO_BOTTOM2 -> node.level = zeroBottom2(node);
                case PHASE_TWO -> node.level = k;
                case X_FACTORY_NONE ->
                        /*
                         * EXCLUDE_FACTORY_NONE: not exclude factory, very fast (faster than both eagle and zipper),
                         * but less precise (still precise than zipper, lose 1.x% of precision).
                         */
                        node.level = excludeFactoryNone(node);
                case X_FACTORY_BOTH ->
                        /*
                         * EXCLUDE_FACTORY_BOTH: exclude factory objects at both top and bottom, very very precise,
                         * but a bit less faster than zipper (still faster than eagle).
                         */
                        node.level = excludeFactoryBoth(node);
                default ->
                        /*
                         * EXCLUDE_FACTORY_TOP_ONLY: exclude factory only at top, very precise
                         * but less faster (is comparable to zipper). This Option is used in paper.
                         */
                        node.level = excludeFactoryDefault(node);
            }
            a[node.level]++;
        }
        for (int i = 0; i <= k; ++i) {
            System.out.println("#level " + i + ": " + a[i]);
        }
        stat();
    }

    public int getLevel(AllocNode allocNode) {
        if (nodes.containsKey(allocNode)) {
            OCGNode hgNode = findOrCreate(allocNode);
            return hgNode.level;
        } else {
            return 0;
        }
    }

    protected void addEdge(OCGNode pre, OCGNode succ) {
        total_edge_count++;
        pre.addSucc(succ);
        succ.addPred(pre);
    }

    public static class OCGNode {
        public final AllocNode ir;
        public Set<OCGNode> successors;
        public Set<OCGNode> predecessors;
        public int level;

        public OCGNode(AllocNode ir) {
            this.ir = ir;
            this.level = 0;
            this.successors = new HashSet<>();
            this.predecessors = new HashSet<>();
        }

        @Override
        public String toString() {
            return ir.toString();
        }

        public void addSucc(OCGNode node) {
            this.successors.add(node);
        }

        public void addPred(OCGNode node) {
            this.predecessors.add(node);
        }
    }

    /**
     * patterns in case1: (1) create one object and never return out of the method.
     * (2) create one object and then return out (factory).
     */
    boolean isFactoryObject(AllocNode heap) {
        SootMethod method = heap.getMethod();
        if (method == null) {
            return false;
        }
        Type retType = method.getReturnType();
        if (!(retType instanceof RefLikeType)) {
            return false;
        }
        if (retType instanceof ArrayType at) {
            if (at.baseType instanceof PrimType) {
                return false;
            }
        }
        PAG pag = pta.getPag();
        MethodPAG methodPAG = pag.getMethodPAG(method);
        MethodNodeFactory factory = methodPAG.nodeFactory();
        Node retNode = factory.caseRet();
        PointsToSetInternal pts = (PointsToSetInternal) pta.reachingObjects(retNode);
        return pts.mapToCIPointsToSet().contains(heap);
    }

}
