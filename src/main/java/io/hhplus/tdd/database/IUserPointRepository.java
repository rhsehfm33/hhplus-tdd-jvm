package io.hhplus.tdd.database;

import io.hhplus.tdd.point.UserPoint;

public interface IUserPointRepository {
    UserPoint selectById(Long id);
    UserPoint insertOrUpdate(long id, long amount);
}
