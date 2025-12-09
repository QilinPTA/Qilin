package qilin.pta.toolkits.moon.Graph;



import qilin.util.collect.multimap.MultiHashMap;
import qilin.util.collect.multimap.MultiMap;
import qilin.util.collect.multimap.UnmodifiableMultiMap;


import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LabeledGraph<N, L>{


    private final MultiMap<N, LabelEdge> outEdges;
    private final MultiMap<N, LabelEdge> inEdges;

    private final Set<N> nodes;

    public LabeledGraph(){
        this.outEdges = new MultiHashMap<>();
        this.inEdges = new MultiHashMap<>();
        this.nodes = new HashSet<>();
    }

    private LabeledGraph(LabeledGraph<N,L> other){
        this.outEdges = new UnmodifiableMultiMap<>(other.outEdges);
        this.inEdges = new UnmodifiableMultiMap<>(other.inEdges);
        this.nodes = Collections.unmodifiableSet(other.nodes);
    }

    public void addEdge(N from, N to, L label){
        LabelEdge labelEdge = new LabelEdge(from, to, label);
        outEdges.put(from, labelEdge);
        inEdges.put(to, labelEdge);
        nodes.add(from);
        nodes.add(to);
    }

    public Set<LabelEdge> getOutEdgesOf(N node) {
        return outEdges.get(node);
    }

    public Set<LabelEdge> getInEdgesOf(N node) {
        return inEdges.get(node);
    }


    public static <N, L> LabeledGraph<N,L> unmodifiableGraph (LabeledGraph<N,L> g){
        return new LabeledGraph<>(g);
    }


    public Set<N> getNodes() {
        return nodes;
    }

    public class LabelEdge{
        N from;
        N to;
        L label;
        public LabelEdge(N from, N to, L label) {
            this.from = from;
            this.to = to;
            this.label = label;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, label);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            LabelEdge other = (LabelEdge) obj;
            return from.equals(other.from) && to.equals(other.to) && label.equals(other.label);
        }


        public N source() {
            return from;
        }


        public N target() {
            return to;
        }

        public L label(){
            return label;
        }
    }
}
