package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;

    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }
}
