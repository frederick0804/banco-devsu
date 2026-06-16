package com.devsu.banco.mapper;

import com.devsu.banco.dto.ClienteDto;
import com.devsu.banco.entity.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper implements Mapper<Cliente, ClienteDto> {

    @Override
    public ClienteDto toDto(Cliente client) {
        ClienteDto dto = new ClienteDto();
        dto.setId(client.getId());
        dto.setNombre(client.getNombre());
        dto.setGenero(client.getGenero());
        dto.setEdad(client.getEdad());
        dto.setIdentificacion(client.getIdentificacion());
        dto.setDireccion(client.getDireccion());
        dto.setTelefono(client.getTelefono());
        dto.setClienteId(client.getClienteId());
        dto.setContrasena(client.getContrasena());
        dto.setEstado(client.getEstado());
        return dto;
    }

    @Override
    public Cliente toEntity(ClienteDto dto) {
        Cliente client = new Cliente();
        client.setNombre(dto.getNombre());
        client.setGenero(dto.getGenero());
        client.setEdad(dto.getEdad());
        client.setIdentificacion(dto.getIdentificacion());
        client.setDireccion(dto.getDireccion());
        client.setTelefono(dto.getTelefono());
        client.setClienteId(dto.getClienteId());
        client.setContrasena(dto.getContrasena());
        client.setEstado(dto.getEstado());
        return client;
    }
}
