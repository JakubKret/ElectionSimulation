import java.util.concurrent.ThreadLocalRandom;

public class Voter implements Runnable {
    private final int voterId;
    private final PollingStation station;

    public Voter(int id, PollingStation station) {
        this.voterId = id;
        this.station = station;
    }

    @Override
    public void run() {
//        try {
//            Thread.sleep(ThreadLocalRandom.current().nextInt(2, 6));

            String[] candidates = ElectionConfig.CANDIDATES;
            String selectedCandidate = candidates[ThreadLocalRandom.current().nextInt(candidates.length)];

            station.addVote(selectedCandidate);
//
//            String rawThreadName = Thread.currentThread().getName();
//
//            String boothName = "Kabina ?";
//            if (rawThreadName.contains("-")) {
//                String[] parts = rawThreadName.split("-");
//                boothName = "Kabina " + parts[parts.length - 1];
//            }
//
//            String logMsg = String.format("Wyborca nr %-4d | %-10s | Głos: %s", voterId, boothName, selectedCandidate);
//            station.getLogger().log(logMsg);


//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
    }
}