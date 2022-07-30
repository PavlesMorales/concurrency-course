package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.NaN;
import static java.util.stream.Collectors.toList;

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
        List<CompletableFuture<Double>> futureList = shopIds.stream()
                .map(shopId -> CompletableFuture
                        .supplyAsync(() -> priceRetriever.getPrice(itemId, shopId))
                        .completeOnTimeout(NaN, 2900, TimeUnit.MILLISECONDS)
                        .handle((res, ex) -> ex != null ? NaN : res))
                .collect(toList());

        return CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new))
                .thenApply(future -> futureList.stream()
                        .map(CompletableFuture::join)
                        .min(Double::compareTo)
                        .orElse(NaN))
                .join();
    }
}
