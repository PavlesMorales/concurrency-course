package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(new Bid(0L, 0L, 0L), true);

    public boolean propose(Bid bid) {
        Bid lastBid;
        do {
            lastBid = latestBid.getReference();
            if (bid.price < lastBid.price || !latestBid.isMarked()) return false;
        } while (!latestBid.compareAndSet(lastBid, bid, true, true));

        notifier.sendOutdatedMessage(lastBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        Bid lastBid;
        do {
            lastBid = latestBid.getReference();
        } while (!latestBid.attemptMark(lastBid, false));

        return lastBid;
    }
}
