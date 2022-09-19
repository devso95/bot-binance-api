package deso.future_bot.repository;

import deso.future_bot.bot.data.StrategyState;
import deso.future_bot.model.entity.Strategy;
import deso.future_bot.model.rest.StrategyResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data SQL repository for the Load entity.
 */
@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    List<Strategy> findByState(@NotNull StrategyState active);

    List<Strategy> findByUserId(Long userId);

    Optional<Strategy> findByIdAndUserId(Long id, Long userId);
}
