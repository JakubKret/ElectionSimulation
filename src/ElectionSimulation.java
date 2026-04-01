import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElectionSimulation {
    private static final int STATIONS = 50;
    private static final int VOTERS = 1000000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("START");
        long startTime = System.currentTimeMillis();
        List<Process> allProcesses = new ArrayList<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Process p : allProcesses) if (p.isAlive()) p.destroyForcibly();
        }));

        String classpath = System.getProperty("java.class.path");
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        ProcessBuilder serverPb = new ProcessBuilder(
                javaBin, "-cp", classpath, "CentralCommission", String.valueOf(STATIONS)
        );
        serverPb.inheritIO();
        Process serverProcess = serverPb.start();
        allProcesses.add(serverProcess);

        System.out.println("(Launcher) Serwer startuje");
        Thread.sleep(1000);

        System.out.println("Uruchamiam " + STATIONS + " procesów komisji");
        for (int i = 1; i <= STATIONS; i++) {
            ProcessBuilder pb = new ProcessBuilder(
                    javaBin, "-cp", classpath, "PollingStation", String.valueOf(i), String.valueOf(VOTERS)
            );

            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);

            allProcesses.add(pb.start());
        }

        for (int i = 1; i < allProcesses.size(); i++) {
            allProcesses.get(i).waitFor();
        }
        System.out.println("Wszystkie komisje zakończyły pracę.");

        serverProcess.waitFor();

        long endTime = System.currentTimeMillis();
        System.out.println("Koniec. Czas: " + (endTime - startTime) + " ms");
    }
}