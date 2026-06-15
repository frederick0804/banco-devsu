package com.devsu.banco.service.impl;

import com.devsu.banco.dto.ClienteDto;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.exception.ResourceNotFoundException;
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

    @Override
    public List<ClienteDto> findAll() {
        return clienteRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteDto findById(Long id) {
        return toDto(findClienteById(id));
    }

    @Override
    @Transactional
    public ClienteDto create(ClienteDto dto) {
        Cliente cliente = toEntity(dto);
        return toDto(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteDto update(Long id, ClienteDto dto) {
        Cliente cliente = findClienteById(id);
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setClienteId(dto.getClienteId());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(dto.getEstado());
        return toDto(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteDto partialUpdate(Long id, ClienteDto dto) {
        Cliente cliente = findClienteById(id);
        if (dto.getNombre() != null) cliente.setNombre(dto.getNombre());
        if (dto.getGenero() != null) cliente.setGenero(dto.getGenero());
        if (dto.getEdad() != null) cliente.setEdad(dto.getEdad());
        if (dto.getIdentificacion() != null) cliente.setIdentificacion(dto.getIdentificacion());
        if (dto.getDireccion() != null) cliente.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null) cliente.setTelefono(dto.getTelefono());
        if (dto.getClienteId() != null) cliente.setClienteId(dto.getClienteId());
        if (dto.getContrasena() != null) cliente.setContrasena(dto.getContrasena());
        if (dto.getEstado() != null) cliente.setEstado(dto.getEstado());
        return toDto(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findClienteById(id);
        clienteRepository.deleteById(id);
    }

    private Cliente findClienteById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    private ClienteDto toDto(Cliente cliente) {
        ClienteDto dto = new ClienteDto();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setGenero(cliente.getGenero());
        dto.setEdad(cliente.getEdad());
        dto.setIdentificacion(cliente.getIdentificacion());
        dto.setDireccion(cliente.getDireccion());
        dto.setTelefono(cliente.getTelefono());
        dto.setClienteId(cliente.getClienteId());
        dto.setContrasena(cliente.getContrasena());
        dto.setEstado(cliente.getEstado());
        return dto;
    }

    private Cliente toEntity(ClienteDto dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setClienteId(dto.getClienteId());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(dto.getEstado());
        return cliente;
    }
}