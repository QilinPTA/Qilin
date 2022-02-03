package qilin.test;

import driver.Main;
import org.junit.Test;
import qilin.test.util.JunitTests;

public class SummaryTests extends JunitTests {

    @Override
    public String[] generateArguments(String mainClass) {
        return generateArguments(mainClass, "sum-insens");
    }

    @Test
    public void testArrays() {
        String[] args = generateArguments("qilin.microben.summary.Array");
        checkAssertions(Main.run(args));
    }

//    @Test
//    public void testBug() {
//        String[] args = generateArguments("qilin.microben.summary.Bug");
//        checkAssertions(Main.run(args));
//    }

    @Test
    public void testCallback() {
        String[] args = generateArguments("qilin.microben.summary.Callback");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCast() {
        String[] args = generateArguments("qilin.microben.summary.Cast");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCommonAlloc() {
        String[] args = generateArguments("qilin.microben.summary.CommonAlloc");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCommonCall() {
        String[] args = generateArguments("qilin.microben.summary.CommonCall");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testConstant() {
        String[] args = generateArguments("qilin.microben.summary.Constant");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testCSAlloc() {
        String[] args = generateArguments("qilin.microben.summary.CSAlloc");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testDynamic() {
        String[] args = generateArguments("qilin.microben.summary.Dynamic");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testException() {
        String[] args = generateArguments("qilin.microben.summary.Exception");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testGetter() {
        String[] args = generateArguments("qilin.microben.summary.Getter");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testGlobal() {
        String[] args = generateArguments("qilin.microben.summary.Global");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testId() {
        String[] args = generateArguments("qilin.microben.summary.Id");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testId2() {
        String[] args = generateArguments("qilin.microben.summary.Id2");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testId_V() {
        String[] args = generateArguments("qilin.microben.summary.Id_V");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testInnerVirtual() {
        String[] args = generateArguments("qilin.microben.summary.InnerVirtual");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testLoadCycle() {
        String[] args = generateArguments("qilin.microben.summary.LoadCycle");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testMerged() {
        String[] args = generateArguments("qilin.microben.summary.Merged");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testMultiArray() {
        String[] args = generateArguments("qilin.microben.summary.MultiArray");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testPartialDynamic() {
        String[] args = generateArguments("qilin.microben.summary.PartialDynamic");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testReturnCycle() {
        String[] args = generateArguments("qilin.microben.summary.ReturnCycle");
        checkAssertions(Main.run(args));
    }

    @Test
    public void testSetter() {
        String[] args = generateArguments("qilin.microben.summary.Setter");
        checkAssertions(Main.run(args));
    }
}
