package com.doopp.gauss.common.mapper;

import com.doopp.gauss.common.entity.Client;
import com.doopp.gauss.common.entity.vo.ClientVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    // @Mapping(target = "session_token", expression = "java(java.util.UUID.randomUUID().toString())")
    // Manager managerDTO(Manager manager);

    ClientVO toClientDTO(Client client);

    List<ClientVO> toClientDTOList(List<Client> client);
}
