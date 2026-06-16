package com.devsu.banco.service.impl;

import com.devsu.banco.dto.ClienteDto;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.exception.ResourceNotFoundException;
import com.devsu.banco.mapper.ClienteMapper;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public List<ClienteDto> findAll() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteDto findById(Long id) {
        return clienteMapper.toDto(findClientById(id));
    }

    @Override
    @Transactional
    public ClienteDto create(ClienteDto dto) {
        return clienteMapper.toDto(clienteRepository.save(clienteMapper.toEntity(dto)));
    }

    @Override
    @Transactional
    public ClienteDto update(Long id, ClienteDto dto) {
        Cliente client = findClientById(id);
        client.setNombre(dto.getNombre());
        client.setGenero(dto.getGenero());
        client.setEdad(dto.getEdad());
        client.setIdentificacion(dto.getIdentificacion());
        client.setDireccion(dto.getDireccion());
        client.setTelefono(dto.getTelefono());
        client.setClienteId(dto.getClienteId());
        client.setContrasena(dto.getContrasena());
        client.setEstado(dto.getEstado());
        return clienteMapper.toDto(clienteRepository.save(client));
    }

    @Override
    @Transactional
    public ClienteDto partialUpdate(Long id, ClienteDto dto) {
        Cliente client = findClientById(id);
        if (dto.getNombre() != null)         client.setNombre(dto.getNombre());
        if (dto.getGenero() != null)         client.setGenero(dto.getGenero());
        if (dto.getEdad() != null)           client.setEdad(dto.getEdad());
        if (dto.getIdentificacion() != null) client.setIdentificacion(dto.getIdentificacion());
        if (dto.getDireccion() != null)      client.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null)       client.setTelefono(dto.getTelefono());
        if (dto.getClienteId() != null)      client.setClienteId(dto.getClienteId());
        if (dto.getContrasena() != null)     client.setContrasena(dto.getContrasena());
        if (dto.getEstado() != null)         client.setEstado(dto.getEstado());
        return clienteMapper.toDto(clienteRepository.save(client));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findClientById(id);
        clienteRepository.deleteById(id);
    }

    private Cliente findClientById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }
}
