# 동시성 제어 분석 보고서
## 동시성 제어 의사 결정 흐름
1. `synchronized` 사용 고려
2. 조사 후, 하기의 이유로 `synchronized` 대신 `ReentrantLock`을 사용하기로 결정
    1. 공정성 지원
        - 포인트 충전/사용 기능은 **순차적**, 동기적으로 수행되어야 함
        - 따라서 순서를 보장하지 않는 `synchronized`는 사용하기 부적합
        - 반면, `ReentrantLock`은 공정성 설정으로 순차적 처리 가능
        - 여기서 공정성이란, **가장 대기 시간이 긴** 쓰래드에게 `lock`을 넘겨주는 특성
       ```Java
       // ReentrantLock 공정성 true 초기화
       private final ReentrantLock lock = new ReentrantLock(true);
       ```
    2. 명시적인 락 해제
        - try...finally 블록을 사용하여 직관적으로 lock 범주 설정 가능
        ```Java
        // 포인트 충전 로직에 대해 ReentrantLock 사용
        public UserPoint chargePoint(long userId, long amount) {
            lock.lock();
            try {
                // 비즈니스 로직수행
            } finally {
                lock.unlock();
            }
        }
        ```
3. 이 밖에도 `ConcurrentHashMap`을 이용하여 user id 별로 락 설정 가능<br>
   사용 예시
    ```Java
    public class UserLocker {
        // 사용자별 락을 관리하는 맵
        private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

        public Object getUserLock(long userId) {
            return userLocks.computeIfAbsent(userId, key -> new ReentrantLock(true));
        }
    }
    ```
   다만, 이는 user가 많을 경우 메모리 문제 발생

## 동시성 테스트 코드 의사 결정 흐름
1. 동시에 포인트 충전/사용이 여러 번 발생하는 테스트 코드 작성 필요
2. 이를 `ExecutorService`, `sumbit`을 통해 구현
3. 하지만 포인트 충전/사용은 요청이 온 순서대로 처리했는지도 확인해야 함
4. 따라서 시간차를 두고 제출(submit)해 테스트해야 함
5. 포인트 충전/사용 요청 사이 `Thread.sleep(10)`을 사용하여 순서를 정해줌
6. 최종적으로 `PointHistory`의 `List`를 확인함으로써 순차적으로 수행됐는지 검증
7. 단, `Thread.sleep(10)`의 경우도 테스트 환경에 따라 순차성이 보장되지 않을 수 있으므로 더 나은 테스트 방법 고민 필요