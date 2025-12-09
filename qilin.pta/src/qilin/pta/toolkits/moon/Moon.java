package qilin.pta.toolkits.moon;

import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.moon.ObjCollection.ObjCollector;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.traversal.TraversalInitializer;
import qilin.pta.toolkits.moon.traversal.VFGTraversal;
import qilin.util.Stopwatch;

import java.util.Set;

public class Moon{
    public static boolean enableRecursivePRObjs = true;

    private final PTA pta;
    private final int maxMatchLayer;
    public Moon(PTA pta, int maxMatchLayer){
        this.pta = pta;
        this.maxMatchLayer = maxMatchLayer;
    }

    public Set<AllocNode> analyze() {
        if(maxMatchLayer > 2){
            throw new UnsupportedOperationException("Moon only supports 2obj or 3obj analysis for now.");
        }
        Stopwatch dataTimer = Stopwatch.newAndStart("#Moon Data Construction");
        MoonDataConstructor.MoonDataStructure moonData = new MoonDataConstructor(pta).analyze();
        dataTimer.stop();
        System.out.println(dataTimer);

        Stopwatch travTimer = Stopwatch.newAndStart("#VFG Traversal for Object Selection");
        TraversalInitializer traversalInitializer = new TraversalInitializer(moonData, maxMatchLayer);
        var objToBaseVar = traversalInitializer.initializeObjToVarMap();
        VFGTraversal traversal = new VFGTraversal(maxMatchLayer, moonData);
        var traversalRet = traversal.traverse(objToBaseVar);
        travTimer.stop();
        System.out.println(travTimer);

        Stopwatch collTimer = Stopwatch.newAndStart("#Precision-Relevant Object Collection");
        ObjCollector objCollector = new ObjCollector(maxMatchLayer, moonData);
        var prObjs = objCollector.analyze(traversalRet);
        collTimer.stop();
        System.out.println(collTimer);


        System.out.println("MOON: Number of Precision-Relevant objects: " + prObjs.size());
        return prObjs;
    }
}
