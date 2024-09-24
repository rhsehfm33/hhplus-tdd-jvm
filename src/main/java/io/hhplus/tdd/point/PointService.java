package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    public List<PointHistory> getPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint chargePoint(long userId, long amount) {
        UserPoint currentUserPoint = getUserPoint(userId);
        long newBalance = currentUserPoint.point() + amount;

        UserPoint upadatedUserPoint = userPointTable.insertOrUpdate(currentUserPoint.id(), newBalance);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, currentUserPoint.updateMillis());

        return upadatedUserPoint;
    }

    public UserPoint usePoint(long userId, long amount) {
        UserPoint currentUserPoint = userPointTable.selectById(userId);
        long newBalance = currentUserPoint.point() - amount;

        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userId, newBalance);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, updatedUserPoint.updateMillis());

        return updatedUserPoint;
    }
}
