package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(0L, 0L, 0L));

    public boolean propose(Bid bid) {
        Bid lastBid;
        do {
            lastBid = latestBid.get();
            if (bid.price < lastBid.price) return false;
        } while (!latestBid.compareAndSet(lastBid, bid));

        notifier.sendOutdatedMessage(lastBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
