package com.devsu.banco.service.impl;

import com.devsu.banco.dto.MovimientoDto;
import com.devsu.banco.dto.ReporteDto;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.exception.BusinessException;
import com.devsu.banco.exception.ResourceNotFoundException;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.MovimientoService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    private static final double LIMITE_DIARIO = 1000.0;

    @Override
    public List<MovimientoDto> findAll() {
        return movimientoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MovimientoDto findById(Long id) {
        return toDto(findMovimientoById(id));
    }

    @Override
    @Transactional
    public MovimientoDto create(MovimientoDto dto) {
        Cuenta cuenta = cuentaRepository.findById(dto.getCuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + dto.getCuentaId()));

        double saldoActual = calcularSaldoActual(cuenta);
        double valor = dto.getValor();

        if (valor < 0) {
            if (saldoActual <= 0) {
                throw new BusinessException("Saldo no disponible");
            }
            validarLimiteDiario(cuenta, valor);
        }

        double nuevoSaldo = saldoActual + valor;
        if (nuevoSaldo < 0) {
            throw new BusinessException("Saldo no disponible");
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setTipoMovimiento(valor >= 0 ? "Crédito" : "Débito");
        movimiento.setValor(valor);
        movimiento.setSaldo(nuevoSaldo);

        return toDto(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional
    public MovimientoDto update(Long id, MovimientoDto dto) {
        Movimiento movimiento = findMovimientoById(id);
        Cuenta cuenta = cuentaRepository.findById(dto.getCuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + dto.getCuentaId()));
        movimiento.setFecha(dto.getFecha());
        movimiento.setTipoMovimiento(dto.getTipoMovimiento());
        movimiento.setValor(dto.getValor());
        movimiento.setSaldo(dto.getSaldo());
        movimiento.setCuenta(cuenta);
        return toDto(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional
    public MovimientoDto partialUpdate(Long id, MovimientoDto dto) {
        Movimiento movimiento = findMovimientoById(id);
        if (dto.getFecha() != null) movimiento.setFecha(dto.getFecha());
        if (dto.getTipoMovimiento() != null) movimiento.setTipoMovimiento(dto.getTipoMovimiento());
        if (dto.getValor() != null) movimiento.setValor(dto.getValor());
        if (dto.getSaldo() != null) movimiento.setSaldo(dto.getSaldo());
        if (dto.getCuentaId() != null) {
            Cuenta cuenta = cuentaRepository.findById(dto.getCuentaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + dto.getCuentaId()));
            movimiento.setCuenta(cuenta);
        }
        return toDto(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findMovimientoById(id);
        movimientoRepository.deleteById(id);
    }

    @Override
    public List<ReporteDto> getReporte(Long clienteId, LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository
                .findByCuentaClienteIdAndFechaBetween(clienteId, inicio, fin)
                .stream()
                .map(this::toReporteDto)
                .collect(Collectors.toList());
    }

    @Override
    public String getReportePdf(Long clienteId, LocalDateTime inicio, LocalDateTime fin) {
        List<ReporteDto> reporte = getReporte(clienteId, inicio, fin);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Estado de Cuenta"));
            document.add(new Paragraph("Cliente ID: " + clienteId));
            document.add(new Paragraph("Período: " + inicio.toLocalDate() + " al " + fin.toLocalDate()));

            Table table = new Table(8);
            table.addCell(new Cell().add(new Paragraph("Fecha")));
            table.addCell(new Cell().add(new Paragraph("Cliente")));
            table.addCell(new Cell().add(new Paragraph("Número Cuenta")));
            table.addCell(new Cell().add(new Paragraph("Tipo")));
            table.addCell(new Cell().add(new Paragraph("Saldo Inicial")));
            table.addCell(new Cell().add(new Paragraph("Estado")));
            table.addCell(new Cell().add(new Paragraph("Movimiento")));
            table.addCell(new Cell().add(new Paragraph("Saldo Disponible")));

            reporte.forEach(r -> {
                table.addCell(new Cell().add(new Paragraph(r.getFecha())));
                table.addCell(new Cell().add(new Paragraph(r.getCliente())));
                table.addCell(new Cell().add(new Paragraph(r.getNumeroCuenta())));
                table.addCell(new Cell().add(new Paragraph(r.getTipo())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getSaldoInicial()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getEstado()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getMovimiento()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getSaldoDisponible()))));
            });

            document.add(table);
            document.close();

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("Error al generar el reporte PDF");
        }
    }

    private void validarLimiteDiario(Cuenta cuenta, double valor) {
        LocalDateTime inicioDia = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        double totalRetiradoHoy = movimientoRepository
                .findByCuentaIdAndFechaBetween(cuenta.getId(), inicioDia, finDia)
                .stream()
                .filter(m -> m.getValor() < 0)
                .mapToDouble(m -> Math.abs(m.getValor()))
                .sum();

        if (totalRetiradoHoy + Math.abs(valor) > LIMITE_DIARIO) {
            throw new BusinessException("Cupo diario Excedido");
        }
    }

    private double calcularSaldoActual(Cuenta cuenta) {
        return movimientoRepository.findByCuentaId(cuenta.getId())
                .stream()
                .reduce((first, second) -> second)
                .map(Movimiento::getSaldo)
                .orElse(cuenta.getSaldoInicial());
    }

    private Movimiento findMovimientoById(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
    }

    private MovimientoDto toDto(Movimiento movimiento) {
        MovimientoDto dto = new MovimientoDto();
        dto.setId(movimiento.getId());
        dto.setFecha(movimiento.getFecha());
        dto.setTipoMovimiento(movimiento.getTipoMovimiento());
        dto.setValor(movimiento.getValor());
        dto.setSaldo(movimiento.getSaldo());
        dto.setCuentaId(movimiento.getCuenta().getId());
        return dto;
    }

    private ReporteDto toReporteDto(Movimiento movimiento) {
        ReporteDto dto = new ReporteDto();
        dto.setFecha(movimiento.getFecha().toLocalDate().toString());
        dto.setCliente(movimiento.getCuenta().getCliente().getNombre());
        dto.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        dto.setTipo(movimiento.getCuenta().getTipoCuenta());
        dto.setSaldoInicial(movimiento.getCuenta().getSaldoInicial());
        dto.setEstado(movimiento.getCuenta().getEstado());
        dto.setMovimiento(movimiento.getValor());
        dto.setSaldoDisponible(movimiento.getSaldo());
        return dto;
    }
}