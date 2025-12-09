package qilin.pta.toolkits.moon.support;

import java.util.Set;

public class Util {
    public static <E> boolean haveOverlap(Set<E> s1, Set<E> s2) {
        Set<E> small, large;
        if (s1.size() <= s2.size()) {
            small = s1;
            large = s2;
        } else {
            small = s2;
            large = s1;
        }
        for (E o : small) {
            if (large.contains(o)) {
                return true;
            }
        }
        return false;
    }
}
