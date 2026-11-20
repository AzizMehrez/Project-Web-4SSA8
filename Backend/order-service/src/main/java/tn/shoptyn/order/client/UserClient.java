package tn.shoptyn.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    UserDto getUser(@PathVariable("userId") String userId);

    record UserDto(
            String id,
            String email,
            String defaultAddress
    ) {
    }
}
