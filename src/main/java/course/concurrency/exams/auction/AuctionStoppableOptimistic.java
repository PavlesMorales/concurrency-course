package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(0L, 0L, 0L));
    private volatile boolean stop;

    public boolean propose(Bid bid) {
        Bid lastBid;
        do {
            lastBid = latestBid.get();
            if (bid.price < lastBid.price || stop) return false;
        } while (!latestBid.compareAndSet(lastBid, bid));

        notifier.sendOutdatedMessage(lastBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }

    public Bid stopAuction() {
        stop = true;
        return latestBid.get();
    }
}
