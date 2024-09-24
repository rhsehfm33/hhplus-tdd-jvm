package io.hhplus.tdd.database;

import java.util.List;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;

public interface IPointHistoryRepository {
    PointHistory insert(long userId, long amount, TransactionType type, long updateMillis);
    List<PointHistory> selectAllByUserId(long userId);
}
