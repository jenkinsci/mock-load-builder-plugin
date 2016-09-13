package mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MockLoad {

    public static void main(String... args)
            throws Exception {
        long averageDuration;
        if (args.length > 0) {
            try {
                averageDuration = Math.max(1, Long.parseLong(args[0]));
            } catch (NumberFormatException e) {
                averageDuration = 60;
            }
        } else {
            averageDuration = 60;
        }

        if (!build(new File("."), averageDuration, System.out)) {
            System.exit(1);
        }
    }

    public static boolean build(File baseDir, long averageDuration, PrintStream out) throws IOException, InterruptedException {
        Random entropy = new Random();

        long duration = averageDuration + (long) (Math.sqrt(averageDuration) * entropy.nextGaussian());

        return doBuild(baseDir, entropy, duration, out);
    }

    private static boolean doBuild(File baseDir, Random entropy, long duration, PrintStream out)
            throws InterruptedException, IOException {
        out.println("[INFO] Scanning for projects...");
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO] Reactor Build Order:");
        out.println("[INFO]");
        out.println("[INFO] mock-load");
        out.println("[INFO]");
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO] Building mock-load 1.0-SNAPSHOT");
        out.println("[INFO] ------------------------------------------------------------------------");
        doWork(entropy, duration, out, baseDir);
        String result;
        if (runTests(baseDir, entropy, out)) {
            result = "SUCCESS";
            createArtifacts(baseDir, entropy, out);
        } else {
            result = "FAILURE";
        }
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO] Reactor Summary:");
        out.println("[INFO]");
        out.println(
                "[INFO] mock-load ......................................... " + result + " [" + duration + "s]");
        out.println("[INFO]");
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO] BUILD " + result);
        out.println("[INFO] ------------------------------------------------------------------------");
        out.println("[INFO] Total time: " + duration + "s");
        out.println("[INFO] Finished at: " + new Date());
        out.println("[INFO] Final Memory: " + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "M/" + (
                Runtime.getRuntime().maxMemory() / 1024 / 1024) + "M");
        out.println("[INFO] ------------------------------------------------------------------------");
        return "SUCCESS".equals(result);
    }

    private static boolean runTests(File baseDir, Random entropy, PrintStream out) throws IOException {
        int availTests = 0;
        for (String testClassName : testClassNames) {
            availTests += 5 + testClassName.length() % testMethodNames.length;
        }
        int testCount = availTests / 2 + entropy.nextInt(availTests / 2);
        boolean unstable = entropy.nextDouble() < 0.05;
        int failCount = unstable ? entropy.nextInt(Math.min(testCount, 100)) : 0;
        int errorCount = unstable ? entropy.nextInt(Math.min(testCount - failCount, 100)) : 0;
        int skipCount = entropy.nextInt(Math.min(testCount - failCount - errorCount, 100));
        double testTime = (testCount + Math.sqrt(testCount) * entropy.nextGaussian()) / 10;
        FileOutputStream fos = new FileOutputStream(new File(baseDir, "mock-junit.xml"));
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
            try {
                out.println("[INFO]");
                out.println("[INFO] --- maven-surefire-plugin:2.10:test (default-test) @ mock-load ---");
                out.println("[INFO] Surefire report directory: " + baseDir.getAbsolutePath());
                out.println();
                out.println("-------------------------------------------------------");
                out.println(" T E S T S");
                out.println("-------------------------------------------------------");
                out.println();

                int totalCount = 0;
                int totalFailures = 0;
                int totalErrors = 0;
                int totalSkipped = 0;
                double totalDuration = 0;

                pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                pw.println("<testsuite name=\"mock-suite\" tests=\"" + testCount + "\" failures=\"" + failCount
                        + "\" errors=\"" + errorCount + "\" skipped=\"" + skipCount + "\" time=\"" + testTime + "\">");
                int testClassIndex = 0;
                int testNameIndex = 0;
                String testClassName = testClassNames[testClassIndex];
                out.println("Running " + testClassName);
                int runCount = 0;
                int runFailures = 0;
                int runErrors = 0;
                int runSkipped = 0;
                double runDuration = 0;
                for (; testCount > 0; testCount--) {
                    String testName;
                    if (testNameIndex < 5 + testClassName.length() % testMethodNames.length) {
                        testName = testMethodNames[testNameIndex++];
                    } else {
                        out.println(
                                "Tests run: " + runCount + ", Failures: " + runFailures + ", Errors: " + runErrors
                                        + ", Skipped: " + runSkipped + ", Time elapsed: " + runDuration + " sec");
                        totalCount += runCount;
                        totalFailures += runFailures;
                        totalErrors += runErrors;
                        totalSkipped += runSkipped;
                        totalDuration += runDuration;
                        runCount = 0;
                        runFailures = 0;
                        runErrors = 0;
                        runSkipped = 0;
                        runDuration = 0;
                        testClassIndex++;
                        testClassName = testClassNames[testClassIndex];
                        testName = testMethodNames[0];
                        testNameIndex = 1;
                        out.println("Running " + testClassName);
                    }

                    double testDuration =
                            Math.round(1000 * (testCount > 1 ? Math.abs(entropy.nextGaussian() / 10) : testTime))
                                    / 1000.0;
                    runCount++;
                    runDuration += testDuration;
                    testTime -= testDuration;
                    pw.println("  <testcase time=\"" + testDuration + "\" name=\"" + testName + "\" classname=\""
                            + testClassName + "\">");
                    int testNum = entropy.nextInt(testCount);
                    if (testNum < failCount) {
                        runFailures++;
                        failCount--;
                        pw.println(
                                "      <failure message=\"Expected something, got something else\" type=\"java.lang"
                                        + ".AssertionError\">");
                        new AssertionError().printStackTrace(pw);
                        pw.println("      </failure>");
                    } else if (testNum < failCount + errorCount) {
                        runErrors++;
                        errorCount--;
                        pw.println(
                                "      <error message=\"Something unexpected happened\" type=\"java.lang"
                                        + ".OutOfMemoryError\">");
                        new OutOfMemoryError().printStackTrace(pw);
                        pw.println("      </error>");
                    } else if (testNum < failCount + errorCount + skipCount) {
                        runSkipped++;
                        skipCount--;
                        pw.println("      <skipped/>");
                    }
                    pw.println("  </testcase>");
                }
                pw.println("</testsuite>");
                out.println("Tests run: " + runCount + ", Failures: " + runFailures + ", Errors: " + runErrors
                        + ", Skipped: " + runSkipped + ", Time elapsed: " + runDuration + " sec");
                out.println();
                out.println("Results :");
                out.println();
                totalCount += runCount;
                totalFailures += runFailures;
                totalErrors += runErrors;
                totalSkipped += runSkipped;
                totalDuration += runDuration;
                out.println(
                        "Tests run: " + totalCount + ", Failures: " + totalFailures + ", Errors: " + totalErrors
                                + ", Skipped: " + totalSkipped + ", Time elapsed: " + totalDuration + " sec");
                return totalErrors == 0 && totalFailures == 0;
            } finally {
                pw.close();
            }
        } finally {
            fos.close();
        }
    }

    private static void doWork(Random entropy, long duration, PrintStream out, File baseDir) throws InterruptedException {
        out.println("[INFO]");
        out.println("[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ mock-load ---");

        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration);
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("To be a JVM you must support SHA-1", e);
        }
        byte[] buffer = new byte[1024];
        entropy.nextBytes(buffer);
        while (System.currentTimeMillis() < endTime) {
            double kbPerMilli = (1500 + entropy.nextInt(1000)) / 10000.0;
            Thread.sleep(entropy.nextInt(1000));
            int size = entropy.nextInt(2048);
            long t = System.currentTimeMillis();
            out.println(
                    "Downloading: http://repo.maven.apache.org/maven2/org/jenkinsci/plugins/mock-load/foobar/" + t
                            + "/foobar-" + t + ".jar");
            for (int i = size; i > 0; i--) {
                if (i % 64 == 0) {
                    out.print("\r" + (size - i) + "/" + size + " KB");
                    out.flush();
                }
                int j = entropy.nextInt(buffer.length);
                buffer[j] = (byte) entropy.nextInt();
                sha1.update(buffer);
                long delay = t + (long)((size - i) * kbPerMilli) - System.currentTimeMillis();
                if (delay > 0) Thread.sleep(delay);

            }
            out.println();
            out.printf(
                    "Downloaded: http://repo.maven.apache.org/maven2/org/jenkinsci/plugins/mock-load/foobar/%s/foobar-%s.jar (%d KB at %.1f KB/sec)%n", t, t, size, kbPerMilli*1000.0);
            out.println("[INFO] Random hash: " + toHexString(sha1.digest()));
        }
        File[] files = baseDir.listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                if (name.startsWith("mock-artifact-") && name.endsWith(".txt")) {
                    if (!f.delete()) {
                        out.println("[WARNING] Could not delete " + f.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static void createArtifacts(File baseDir, Random entropy, PrintStream out) throws IOException {
        out.println("[INFO] --- maven-jar-plugin:2.3.2:jar (default-jar) @ mock-load ---");
        out.println("[INFO]");
        byte[] buffer = new byte[1024];
        int artifactCount = 2 + entropy.nextInt(25);
        for (int i = 0; i < artifactCount; i++) {
            Arrays.fill(buffer, (byte) '\n');
            int index = 0;
            for (byte b : ("Artifact " + i + "\n" + new Date().toString()).getBytes("UTF-8")) {
                buffer[index++] = b;
            }
            File artifact = new File(baseDir, "mock-artifact-" + i + ".txt");
            System.err.println("Creating " + artifact.getAbsolutePath());
            int size = (1 << entropy.nextInt(20)) + entropy.nextInt(65536);
            FileOutputStream fos = new FileOutputStream(artifact);
            try {
                while (size > 0) {
                    int count = Math.min(buffer.length, size);
                    fos.write(buffer, 0, count);
                    size -= count;
                }
            } finally {
                fos.close();
            }
        }
    }

    public static String toHexString(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte aData : data) {
            int b = aData & 0xFF;
            if (b < 16) {
                buf.append('0');
            }
            buf.append(Integer.toHexString(b));
        }
        return buf.toString();
    }

    private static final String[] testClassNames = {
            "jenkins.plugin.mockloadbuilder.model.FooTest",
            "jenkins.plugin.mockloadbuilder.model.BarTest",
            "jenkins.plugin.mockloadbuilder.model.ManchuTest",
            "jenkins.plugin.mockloadbuilder.model.FooBarTest",
            "jenkins.plugin.mockloadbuilder.model.BarFooTest",
            "jenkins.plugin.mockloadbuilder.model.BarManchuTest",
            "jenkins.plugin.mockloadbuilder.model.ManchuBarTest",
            "jenkins.plugin.mockloadbuilder.model.FooManchuTest",
            "jenkins.plugin.mockloadbuilder.model.ManchuFooTest",
            "jenkins.plugin.mockloadbuilder.model.FooBarManchuTest",
            "jenkins.plugin.mockloadbuilder.model.BarFooManchuTest",
            "jenkins.plugin.mockloadbuilder.model.BarManchuFooTest",
            "jenkins.plugin.mockloadbuilder.model.FooManchuBarTest",
            "jenkins.plugin.mockloadbuilder.model.ManchuFooBarTest",
            "jenkins.plugin.mockloadbuilder.model.ManchuBarFooTest",
            "jenkins.plugin.mockloadbuilder.factories.AbstractFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.FooFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.BarFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.ManchuFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.FooBarFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.BarFooFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.BarManchuFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.ManchuBarFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.FooManchuFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.ManchuFooFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.FooBarManchuFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.BarFooManchuFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.BarManchuFooFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.FooManchuBarFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.ManchuFooBarFactoryTest",
            "jenkins.plugin.mockloadbuilder.factories.ManchuBarFooFactoryTest",
            "jenkins.plugin.mockloadbuilder.managers.AbstractManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.FooManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.BarManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.ManchuManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.FooBarManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.BarFooManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.BarManchuManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.ManchuBarManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.FooManchuManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.ManchuFooManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.FooBarManchuManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.BarFooManchuManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.BarManchuFooManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.FooManchuBarManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.ManchuFooBarManagerTest",
            "jenkins.plugin.mockloadbuilder.managers.ManchuBarFooManagerTest",
    };

    private static final String[] testMethodNames = {
            "smokes",
            "equalsContract",
            "hashCodeContract",
            "toString",
            "defaultConstructor",
            "getSetWidget",
            "getSetThingimys",
            "getSetWhajamacallits",
            "doSomething",
            "doAnotherThing",
            "watchClass",
            "watchSizteenCandles",
            "watchGrandviewUSA",
            "watchTheSureThing",
            "watchTheJourneyOfNattyGann",
            "watchBetterOffDead",
            "watchStandByMe",
            "watchOneCrazySummer",
            "watchHotPursuit",
            "watchBroadcastNews",
            "watchEightMenOut",
            "watchTapeheads",
            "watchSayAnything",
            "watchFatManAndLittleBoy",
            "watchTheGrifters",
            "watchTrueColors",
            "watchShadowsAndFog",
            "watchThePlayer",
            "watchBobRoberts",
            "watchRoadsideProphets",
            "watchMapOfTheHumanHeart",
            "watchMoneyForNothing",
            "watchFloundering",
            "watchBulletsOverBroadway",
            "watchTheRoadToWellville",
            "watchCityHall",
            "watchGrossePointeBlank",
            "watchConAir",
            "watchChicagoCab",
            "watchAnastasia",
            "watchMidnightInTheGardenOfGoodAndEvil",
            "watchThisIsMyFather",
            "watchTheThinRedLine",
            "watchPushingTin",
            "watchCradleWillRock",
            "watchBeingJohnMalkovich",
            "watchHighFidelity",
            "watchAmericasSweethearts",
            "watchSerendipity",
            "watchMax",
            "watchAdaptation",
            "watchIdentity",
            "watchBreakfastWithHunter",
            "watchRunawayJury",
            "watchMustLoveDogs",
            "watchTheIceHarvest",
            "watchTheContract",
            "watchJoeStrummerTheFutureIsUnwritten",
            "watchMartianChild",
            "watch1408",
            "watchGraceIsGone",
            "watchIgor",
            "dontWatchWarInc_IWantThose2HoursOfMyLifeBack",
            "watch2012",
            "watchHotTubTimeMachine",
            "watchShanghai",
            "watchTheFactory",
            "watchTheRaven",
            "watchThePaperboy",
            "watchTheNumbersStation",
            "watchAdultWorld",
            "watchTheFrozenGround",
            "watchTheButler",
            "watchGrandPiano",
            "watchTheSureThing_again",
            "watchGrossePointeBlank_again"
    };

}
