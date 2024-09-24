package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.TransactionType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.IPointHistoryRepository;
import io.hhplus.tdd.database.IUserPointRepository;

class PointServiceTest {
    private IUserPointRepository userPointRepository;
    private IPointHistoryRepository pointHistoryRepository;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointRepository = mock(IUserPointRepository.class);
        pointHistoryRepository = mock(IPointHistoryRepository.class);
        pointService = new PointService(userPointRepository, pointHistoryRepository);
    }

    @Test
    void testGetUserPoint() {
        long userId = 1L;
        UserPoint expectedUserPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
        when(userPointRepository.selectById(userId)).thenReturn(expectedUserPoint);

        UserPoint result = pointService.getUserPoint(userId);

        assertEquals(expectedUserPoint, result);
        verify(userPointRepository).selectById(userId);
    }

    @Test
    void testGetPointHistories() {
        long userId = 1L;
        List<PointHistory> expectedHistories = List.of(
                new PointHistory(1L, userId, 500L, CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 200L, USE, System.currentTimeMillis())
        );
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(expectedHistories);

        List<PointHistory> result = pointService.getPointHistories(userId);

        assertEquals(expectedHistories.size(), result.size());
        verify(pointHistoryRepository).selectAllByUserId(userId);
    }

    @Test
    void testChargePointSuccess() {
        long userId = 1L;
        long amount = 500L;
        UserPoint currentUserPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, 1500L, System.currentTimeMillis());

        when(userPointRepository.selectById(userId)).thenReturn(currentUserPoint);
        when(userPointRepository.insertOrUpdate(userId, 1500L)).thenReturn(updatedUserPoint);

        UserPoint result = pointService.chargePoint(userId, amount);

        assertEquals(updatedUserPoint, result);
        verify(pointHistoryRepository).insert(userId, amount, CHARGE, updatedUserPoint.updateMillis());
    }

    @Test
    void testUsePointSuccess() {
        long userId = 1L;
        long amount = 500L;
        UserPoint currentUserPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, 500L, System.currentTimeMillis());

        when(userPointRepository.selectById(userId)).thenReturn(currentUserPoint);
        when(userPointRepository.insertOrUpdate(userId, 500L)).thenReturn(updatedUserPoint);

        UserPoint result = pointService.usePoint(userId, amount);

        assertEquals(updatedUserPoint, result);
        verify(pointHistoryRepository).insert(userId, amount, USE, updatedUserPoint.updateMillis());
    }
}
