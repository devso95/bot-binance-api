package deso.future_bot.web.rest

import deso.future_bot.model.rest.AddStrategy
import deso.future_bot.model.rest.StrategyInfo
import deso.future_bot.model.rest.StrategyResponse
import deso.future_bot.model.rest.UpdateStrategy
import deso.future_bot.service.StrategyService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class StrategyResource(private val strategyService: StrategyService) {

    companion object {
        private const val ENTITY_NAME = "AccountResource"
    }

    private val log = LoggerFactory.getLogger(StrategyResource::class.java)

    @PostMapping("/strategies")
    fun deploy(@RequestBody @Valid body: AddStrategy): ResponseEntity<StrategyResponse> {
        val response = strategyService.deploy(body)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/strategies")
    fun getAll(): ResponseEntity<List<StrategyResponse>> {
        val result = strategyService.getAll()
        return ResponseEntity.ok(result)
    }

    @PutMapping("/strategies/{id}")
    fun update(
        @PathVariable(value = "id") @Valid id: Long,
        @RequestBody body: UpdateStrategy
    ): ResponseEntity<StrategyResponse> {
        val result = strategyService.update(id, body)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/strategies/{id}")
    fun get(@PathVariable(value = "id") id: Long): ResponseEntity<StrategyResponse> {
        val result = strategyService.get(id)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/strategies/{coin}/info")
    fun get(@PathVariable(value = "coin") coin: String): ResponseEntity<StrategyInfo> {
        val result = strategyService.getInfo(coin)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/strategies/info")
    fun getInfor(): ResponseEntity<List<StrategyInfo>> {
        val result = strategyService.getAllInfor()
        return ResponseEntity.ok(result)
    }

    @PutMapping("/strategies/{id}/close")
    fun close(@PathVariable(value = "id") id: Long): ResponseEntity<StrategyResponse> {
        val contract = strategyService.close(id)
        return ResponseEntity.ok(contract)
    }

    @PutMapping("/strategies/{id}/delete")
    fun delete(@PathVariable(value = "id") id: Long): ResponseEntity<StrategyResponse> {
        val contract = strategyService.delete(id)
        return ResponseEntity.ok(contract)
    }

    @GetMapping("/admin/strategies")
    fun getAllByAdmin(): ResponseEntity<List<StrategyResponse>> {
        val result = strategyService.getAllByAdmin()
        return ResponseEntity.ok(result)
    }

    @GetMapping("/admin/strategies/count")
    fun count(): ResponseEntity<Int> {
        val result = strategyService.count()
        return ResponseEntity.ok(result)
    }

}