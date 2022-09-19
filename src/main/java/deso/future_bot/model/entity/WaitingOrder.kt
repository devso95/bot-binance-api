package deso.future_bot.model.entity

import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "waiting_orders")
class WaitingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderGenerator")
    @SequenceGenerator(name = "orderGenerator")
    var id: Long = 0

    @NotNull
    @Column(name = "price", nullable = false)
    var price: Double = 0.0

    @NotNull
    @Column(name = "amount", nullable = false)
    var amount: Double = 0.0

    @NotNull
    @Column(name = "ref_id", nullable = false)
    var refId: Long = 0

    @Column(name = "explanation")
    var explanation: String? = null

    @NotNull
    @Column(name = "created_date", nullable = false)
    var createdDate: Instant = Instant.now()

    @NotNull
    @Column(name = "strategy_id", nullable = false)
    var strategyId: Long = 0L

    @NotNull
    @Column(name = "user_id", nullable = false)
    var userId: Long = 0L
}
