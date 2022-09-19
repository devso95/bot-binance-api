package deso.future_bot.service

import deso.future_bot.bot.data.StrategyState
import deso.future_bot.bot.modes.StrategyManager
import deso.future_bot.mapper.StrategyMapper
import deso.future_bot.model.rest.AddStrategy
import deso.future_bot.model.rest.StrategyInfo
import deso.future_bot.model.rest.StrategyResponse
import deso.future_bot.model.rest.UpdateStrategy
import deso.future_bot.repository.StrategyRepository
import deso.future_bot.security.SecurityUtils
import deso.future_bot.util.EncryptUtil
import org.springframework.stereotype.Service


@Service
class StrategyService(
        private val repository: StrategyRepository,
        private val mapper: StrategyMapper,
        private val encryptUtil: EncryptUtil,
        private val strategyManager: StrategyManager
) {


    fun deploy(body: AddStrategy): StrategyResponse {
        return mapper.create(body).run {
            if (this.crossRate > 1) {
                crossRate = 1.0
            }
            val entity = mapper.toEntity(this)
            if (body.refId != null) {
                repository.findByIdAndUserId(id, SecurityUtils.getId())
                        .ifPresent { ref ->
                            entity.apiKey = ref.apiKey
                            entity.secretKey = ref.secretKey
                        }
            }
            entity.userId = SecurityUtils.getId()
            repository.save(entity)
            if (entity.state == StrategyState.ACTIVE) {
                strategyManager.update(this)
            }
            mapper.toResponse(entity)
        }
    }

    fun getAll(): List<StrategyResponse> {
        return repository.findByUserId(SecurityUtils.getId()).map { mapper.toResponse(it) }
    }

    fun getAllByAdmin(): List<StrategyResponse> {
        return repository.findAll().map { mapper.toResponse(it) }
    }

    fun update(id: Long, body: UpdateStrategy): StrategyResponse {
        return repository.findByIdAndUserId(id, SecurityUtils.getId()).map {
            it.apply {
                this.liquidation = body.liquidation
                this.stopLoss = body.stopLoss
                this.target = body.target
                this.state = body.state
                if (!body.apiKey.isNullOrEmpty()) {
                    this.apiKey = encryptUtil.encrypt(body.apiKey)
                }
                if (!body.secretKey.isNullOrEmpty()) {
                    this.secretKey = encryptUtil.encrypt(body.secretKey)
                }
                if (body.crossRate <= 1) {
                    this.crossRate = body.crossRate
                }
                repository.save(this)
            }
        }.map {
            strategyManager.update(mapper.toDto(it))
            mapper.toResponse(it)
        }.orElse(null)
    }

    fun get(id: Long): StrategyResponse? {
        return repository.findByIdAndUserId(id, SecurityUtils.getId()).map { mapper.toResponse(it) }.orElse(null)
    }

    fun close(id: Long): StrategyResponse {
        return repository.findByIdAndUserId(id, SecurityUtils.getId()).map {
            it.apply {
                this.state = StrategyState.CLOSE
                strategyManager.close(this.id)
                repository.save(this)
            }
        }.map { mapper.toResponse(it) }.orElse(null)
    }

    fun delete(id: Long): StrategyResponse {
        return repository.findByIdAndUserId(id, SecurityUtils.getId()).map {
            it.apply {
                strategyManager.close(this.id)
                repository.deleteById(id)
            }
        }.map { mapper.toResponse(it) }.orElse(null)
    }

    fun count(): Int {
        return strategyManager.getContracts().count()
    }

    fun getInfo(coin: String): StrategyInfo? {
        return strategyManager.getInfo(coin, SecurityUtils.getId())
    }

    fun getAllInfor(): List<StrategyInfo>? {
        return strategyManager.getInfo(SecurityUtils.getId())
    }

}