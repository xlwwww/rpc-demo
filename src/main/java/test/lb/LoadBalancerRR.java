package test.lb;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancerRR implements LoadBalancer {
    private AtomicInteger index = new AtomicInteger(0);

    @Override
    public String select(List<String> addrList) {
        return addrList.get(index.getAndIncrement());
    }
}
