package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(0L, 0L, 0L);
    private final Object lock = new Object();
    private volatile boolean stop;

    public boolean propose(Bid bid) {
        if (bid.price > latestBid.price && !stop) {
            synchronized (lock) {
                if (bid.price > latestBid.price && !stop) {
                    Bid last = latestBid;
                    latestBid = bid;
                    notifier.sendOutdatedMessage(last);
                }
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        // ваш код
        synchronized (lock) {
            stop = true;
        }
        return latestBid;
    }
}
