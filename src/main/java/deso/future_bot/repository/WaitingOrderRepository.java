package deso.future_bot.repository;

import deso.future_bot.model.entity.WaitingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Load entity.
 */
@Repository
public interface WaitingOrderRepository extends JpaRepository<WaitingOrder, Long> {

    List<WaitingOrder> findByStrategyId(Long strategyId);

}
