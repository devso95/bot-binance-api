package deso.future_bot.model.entity

import deso.future_bot.bot.data.StrategyState
import deso.future_bot.security.SecurityUtils
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "strategies")
class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "strategyGenerator")
    @SequenceGenerator(name = "strategyGenerator", allocationSize = 1)
    var id: Long = 0

    @NotNull
    @Column(name = "coin", nullable = false)
    var coin: String = ""

    @NotNull
    @Column(name = "stop_loss", nullable = false)
    var stopLoss: Double = 0.0

    @NotNull
    @Column(name = "liquidation", nullable = false)
    var liquidation: Double = 0.0

    @NotNull
    @Column(name = "target", nullable = false)
    var target: Double = 0.0

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    var state: StrategyState = StrategyState.ACTIVE

    @NotNull
    @Column(name = "secret_key", nullable = false)
    var secretKey: String = ""

    @NotNull
    @Column(name = "api_key", nullable = false)
    var apiKey: String = ""

    @NotNull
    @Column(name = "created_date", nullable = false)
    var createdDate: Instant = Instant.now()

    @NotNull
    @Column(name = "user_id", nullable = false)
    var userId: Long = 0L

    @NotNull
    @Column(name = "cross_rate", nullable = false)
    var crossRate: Double = 1.0
}
