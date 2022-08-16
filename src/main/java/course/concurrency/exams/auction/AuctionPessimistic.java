package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final Object lock = new Object();

    private volatile Bid latestBid = new Bid(0L, 0L, 0L);

    public boolean propose(Bid bid) {
        if (bid.price > latestBid.price) {
            Bid last = latestBid;
            synchronized (lock) {
                if (bid.price > latestBid.price) {
                    last = latestBid;
                    latestBid = bid;
                }
            }
            notifier.sendOutdatedMessage(last);
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}
