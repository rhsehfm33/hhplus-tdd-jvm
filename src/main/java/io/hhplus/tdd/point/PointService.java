package io.hhplus.tdd.point;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.IPointHistoryRepository;
import io.hhplus.tdd.database.IUserPointRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
    private static final long MAX_BALANCE = 1_000_000L;

    private final IUserPointRepository userPointRepository;
    private final IPointHistoryRepository pointHistoryRepository;
    private final ReentrantLock lock = new ReentrantLock(true);

    public UserPoint getUserPoint(long userId) {
        return userPointRepository.selectById(userId);
    }

    public List<PointHistory> getPointHistories(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }

    public UserPoint chargePoint(long userId, long amount) {
        lock.lock();
        try {
            UserPoint currentUserPoint = userPointRepository.selectById(userId);
            long newBalance = currentUserPoint.point() + amount;

            if (newBalance > MAX_BALANCE) {
                throw new IllegalArgumentException("최대 잔액을 초과하여 충전할 수 없습니다.");
            }

            UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(currentUserPoint.id(), newBalance);
            pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis());

            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }

    public UserPoint usePoint(long userId, long amount) {
        lock.lock();
        try {
            UserPoint currentUserPoint = userPointRepository.selectById(userId);
            long newBalance = currentUserPoint.point() - amount;

            if (newBalance < 0) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }

            UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(userId, newBalance);
            pointHistoryRepository.insert(userId, amount, TransactionType.USE, updatedUserPoint.updateMillis());

            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
    }
}
