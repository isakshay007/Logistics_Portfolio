package com.lafl.user.event;

import com.lafl.user.domain.UserAccount;

public interface UserEventPublisher {

    void publishUserRegistered(UserAccount userAccount);
}
