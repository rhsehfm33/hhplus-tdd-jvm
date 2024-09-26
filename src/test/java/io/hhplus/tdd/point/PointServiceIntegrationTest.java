package io.hhplus.tdd.point;

import static io.hhplus.tdd.point.PointService.MAX_BALANCE;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hhplus.tdd.database.IPointHistoryRepository;
import io.hhplus.tdd.database.IUserPointRepository;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private IUserPointRepository userPointRepository;

    @Autowired
    private IPointHistoryRepository pointHistoryRepository;

    private final long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 사용자 포인트 초기화
        userPointRepository.insertOrUpdate(USER_ID, 0L);
    }

    @Test
    void testChargePointOverflow() {
        assertThrows(IllegalArgumentException.class, () -> pointService.chargePoint(USER_ID, MAX_BALANCE + 1L));
    }

    @Test
    void testUsePointUnderflow() {
        assertThrows(IllegalArgumentException.class, () -> pointService.usePoint(USER_ID, 1L));
    }

    @Test
    void testConcurrentChargeAndUsePoint() throws InterruptedException {
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // 작업 제출
        for (int i = 0; i < numThreads; i++) {
            if (i % 2 == 0) {
                executorService.submit(() -> pointService.chargePoint(USER_ID, 100L));
            } else {
                executorService.submit(() -> pointService.usePoint(USER_ID, 50L));
            }

            // ExecutorService는 submit에 따라 task를 순서대로 실행하지 않음
            // 따라서 task 실행에 동시성, 순서를 보장하기 위해 sleep 사용
            Thread.sleep(10);
        }

        // Executor 종료
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

        // 최종 포인트 잔액 확인
        // 예상되는 최종 잔액 계산 = (5 * 100) - (5 * 50) = 250
        UserPoint finalUserPoint = userPointRepository.selectById(USER_ID);
        assertEquals(250L, finalUserPoint.point());

        // 포인트 히스토리 확인
        List<PointHistory> pointHistories = pointHistoryRepository.selectAllByUserId(USER_ID);
        for (int i = 0; i < numThreads; i++) {
            if (i % 2 == 0) {
                assertEquals(pointHistories.get(i).type(), TransactionType.CHARGE);
            } else {
                assertEquals(pointHistories.get(i).type(), TransactionType.USE);
            }
        }
    }
}
