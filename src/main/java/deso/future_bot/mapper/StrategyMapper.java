package deso.future_bot.mapper;

import deso.future_bot.model.dto.StrategyDto;
import deso.future_bot.model.entity.Strategy;
import deso.future_bot.model.rest.AddStrategy;
import deso.future_bot.model.rest.StrategyResponse;
import deso.future_bot.util.EncryptUtil;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

@Mapper(componentModel = "spring")
public abstract class StrategyMapper implements EntityMapper<StrategyDto, Strategy> {

    @Autowired
    EncryptUtil encryptUtil;

    public abstract StrategyResponse toResponse(Strategy entity);

    public abstract StrategyDto create(AddStrategy request);

    @AfterMapping
    void toEntity(@MappingTarget Strategy entity, StrategyDto dto) {
        try {
            entity.setApiKey(encryptUtil.encrypt(dto.getApiKey()));
            entity.setSecretKey(encryptUtil.encrypt(dto.getSecretKey()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @AfterMapping
    void toDto(@MappingTarget StrategyDto dto, Strategy entity) {
        try {
            dto.setApiKey(encryptUtil.decrypt(entity.getApiKey()));
            dto.setSecretKey(encryptUtil.decrypt(entity.getSecretKey()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }


}
