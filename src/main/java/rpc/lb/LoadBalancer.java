package rpc.lb;

import java.util.List;

public interface LoadBalancer {
    String select(List<String> addrList);
}
