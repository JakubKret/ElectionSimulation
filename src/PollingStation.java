import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PollingStation implements Runnable {
    private final int stationId;
    private final int numberOfVoters;
    private final Map<String, Integer> localResults;

//    private final SimulationLogger logger;

    public static void main(String[] args) {
        if (args.length < 2) return;
        int id = Integer.parseInt(args[0]);
        int voters = Integer.parseInt(args[1]);
        new PollingStation(id, voters).run();
    }

    public PollingStation(int id, int numberOfVoters) {
        this.stationId = id;
        this.numberOfVoters = numberOfVoters;

//        this.logger = new SimulationLogger("log_okreg_" + id + ".txt", "[Okręg " + id + "]");

        this.localResults = new HashMap<>();
        for (String c : ElectionConfig.CANDIDATES) localResults.put(c, 0);
    }

//    public SimulationLogger getLogger() {
//        return this.logger;
//    }

    public synchronized void addVote(String candidate) {
        localResults.put(candidate, localResults.get(candidate) + 1);
    }

    @Override
    public void run() {
//        logger.log("START (PID=" + ProcessHandle.current().pid() + "). Kolejka: " + numberOfVoters);

        ExecutorService booths = Executors.newFixedThreadPool(5);
        for (int i = 1; i <= numberOfVoters; i++) {
            booths.submit(new Voter(i, this));
        }

        booths.shutdown();
        try {
            if (!booths.awaitTermination(60, TimeUnit.MINUTES)) {
//                logger.log("Timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        String resultsLog = localResults.entrySet().stream()
//                .map(e -> e.getKey() + ": " + e.getValue())
//                .collect(Collectors.joining(", "));
//        logger.log("KONIEC Wyniki: " + resultsLog);

        sendResultsToServer();
//        logger.close();
        System.exit(0);
    }

    private void sendResultsToServer() {
        int retries = 5;
        while (retries > 0) {
            try (Socket socket = new Socket("127.0.0.1", 4444);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

                oos.writeInt(stationId);
                oos.writeObject(localResults);
                return;

            } catch (IOException e) {
                retries--;
                try { Thread.sleep(500); } catch (InterruptedException ex) {}
            }
        }
//        logger.log("Nie udało się połączyć z Centralą po 5 próbach!");
    }
}