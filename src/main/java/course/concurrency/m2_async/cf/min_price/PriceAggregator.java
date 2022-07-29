package course.concurrency.m2_async.cf.min_price;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.NaN;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        List<CompletableFuture<Double>> listCompletableFuture = new ArrayList<>();
        for (int i = 0; i < shopIds.size(); i++) {
            int shopId = i;
            listCompletableFuture.add(CompletableFuture
                    .supplyAsync(() -> priceRetriever.getPrice(itemId, shopId))
                    .completeOnTimeout(NaN, 2500, TimeUnit.MILLISECONDS)
                    .handle((res, ex) -> ex != null ? NaN : res));

        }

        return listCompletableFuture
                .stream()
                .map(CompletableFuture::join)
                .min(Double::compare)
                .orElse(NaN);
    }

}
