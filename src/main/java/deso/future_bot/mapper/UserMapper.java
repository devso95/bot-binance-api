package deso.future_bot.mapper;

import deso.future_bot.model.dto.UserDto;
import deso.future_bot.model.entity.User;
import deso.future_bot.model.rest.AddUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDto, User> {

    User create(AddUser user);

}
