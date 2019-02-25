package com.doopp.gauss.common.mapper;

import com.doopp.gauss.common.entity.User;
import com.doopp.gauss.common.entity.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    // @Mapping(source = "id", target = "id")
    // @Mapping(source = "user", target = "name", qualifiedByName = "getUserName")
    UserVO toUserDTO(User user);

    // @Named("getUserName")
    // default String getUserName(User user) {
    //     OAuthService oauthService = (OAuthService) ApplicationContextUtil.getBean("oauthService");
    //     return oauthService.getUserName(user);
    // }

    List<UserVO> toUserDTOList(List<User> user);
}
