class Main {
    public static void main(String[] args) {
        C c1 = new C(); // C1
        C c2 = c1;
        A a1 = c1.wcreate2(); // c1
        A a2 = c2.wcreate2(); // c2
        Object o1 = new Object(); // O1
        Object o2 = new Object(); // O2
        a1.f = o1;
        a2.f = o2;
        Object v1 = a1.f;
        Object v2 = a2.f;
    }
}

class A {
    Object f;
}

class B {
    A create() {
        A r1 = new A(); // A1
        return r1;
    }

    A wcreate() {
        A r2 = this.create(); // c4
        return r2;
    }
}

class C {
    A wcreate2() {
        B b1 = new B(); // B1
        A r3 = b1.wcreate(); // c3
        return r3;
    }
}