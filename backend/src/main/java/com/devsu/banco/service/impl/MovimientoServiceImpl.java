package com.devsu.banco.service.impl;

import com.devsu.banco.dto.MovimientoDto;
import com.devsu.banco.dto.ReporteDto;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.exception.BusinessException;
import com.devsu.banco.exception.ResourceNotFoundException;
import com.devsu.banco.mapper.MovimientoMapper;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.MovimientoService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
    private final MovimientoMapper movimientoMapper;

    private static final double DAILY_LIMIT = 1000.0;

    @Override
    public List<MovimientoDto> findAll() {
        return movimientoRepository.findAll().stream()
                .map(movimientoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MovimientoDto findById(Long id) {
        return movimientoMapper.toDto(findMovementById(id));
    }

    @Override
    @Transactional
    public MovimientoDto create(MovimientoDto dto) {
        Cuenta account = cuentaRepository.findById(dto.getCuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + dto.getCuentaId()));

        double currentBalance = calculateCurrentBalance(account);
        double amount = dto.getValor();

        if (amount < 0) {
            if (currentBalance <= 0) {
                throw new BusinessException("Saldo no disponible");
            }
            validateDailyLimit(account, amount);
        }

        double newBalance = currentBalance + amount;
        if (newBalance < 0) {
            throw new BusinessException("Saldo no disponible");
        }

        Movimiento movement = new Movimiento();
        movement.setCuenta(account);
        movement.setFecha(LocalDateTime.now());
        movement.setTipoMovimiento(amount >= 0 ? "Crédito" : "Débito");
        movement.setValor(amount);
        movement.setSaldo(newBalance);

        return movimientoMapper.toDto(movimientoRepository.save(movement));
    }

    @Override
    @Transactional
    public MovimientoDto update(Long id, MovimientoDto dto) {
        Movimiento movement = findMovementById(id);
        Cuenta account = cuentaRepository.findById(dto.getCuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + dto.getCuentaId()));
        movement.setFecha(dto.getFecha());
        movement.setTipoMovimiento(dto.getTipoMovimiento());
        movement.setValor(dto.getValor());
        movement.setSaldo(dto.getSaldo());
        movement.setCuenta(account);
        return movimientoMapper.toDto(movimientoRepository.save(movement));
    }

    @Override
    @Transactional
    public MovimientoDto partialUpdate(Long id, MovimientoDto dto) {
        Movimiento movement = findMovementById(id);
        if (dto.getFecha() != null)           movement.setFecha(dto.getFecha());
        if (dto.getTipoMovimiento() != null)  movement.setTipoMovimiento(dto.getTipoMovimiento());
        if (dto.getValor() != null)           movement.setValor(dto.getValor());
        if (dto.getSaldo() != null)           movement.setSaldo(dto.getSaldo());
        if (dto.getCuentaId() != null) {
            Cuenta account = cuentaRepository.findById(dto.getCuentaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + dto.getCuentaId()));
            movement.setCuenta(account);
        }
        return movimientoMapper.toDto(movimientoRepository.save(movement));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findMovementById(id);
        movimientoRepository.deleteById(id);
    }

    @Override
    public List<ReporteDto> getReport(Long clienteId, LocalDateTime start, LocalDateTime end) {
        return movimientoRepository
                .findByCuentaClienteIdAndFechaBetween(clienteId, start, end)
                .stream()
                .map(this::toReportDto)
                .collect(Collectors.toList());
    }

    @Override
    public String getReportPdf(Long clienteId, LocalDateTime start, LocalDateTime end) {
        List<ReporteDto> report = getReport(clienteId, start, end);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());
            document.setMargins(30, 30, 30, 30);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            DeviceRgb headerBg    = new DeviceRgb(30, 64, 117);
            DeviceRgb subHeaderBg = new DeviceRgb(52, 103, 182);
            DeviceRgb rowAlt      = new DeviceRgb(240, 245, 255);
            DeviceRgb positiveColor = new DeviceRgb(21, 128, 61);
            DeviceRgb negativeColor = new DeviceRgb(185, 28, 28);

            String clientName = report.isEmpty() ? "Cliente #" + clienteId
                    : report.get(0).getCliente();

            document.add(new Paragraph("Estado de Cuenta")
                    .setFont(bold).setFontSize(20).setFontColor(headerBg)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

            document.add(new Paragraph("BANCO DEVSU")
                    .setFont(regular).setFontSize(10).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(16));

            Table meta = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .useAllAvailableWidth().setMarginBottom(20);
            meta.addCell(metaCell("Cliente", clientName, subHeaderBg, bold, regular));
            meta.addCell(metaCell("Período desde", start.toLocalDate().toString(), subHeaderBg, bold, regular));
            meta.addCell(metaCell("Período hasta", end.toLocalDate().toString(), subHeaderBg, bold, regular));
            document.add(meta);

            String[] headers = {"Fecha", "Cliente", "N° Cuenta", "Tipo", "Saldo Inicial", "Estado", "Movimiento", "Saldo Disponible"};
            float[]  widths  = {2.2f, 2f, 1.8f, 1.4f, 1.8f, 1.2f, 1.8f, 2f};
            Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();

            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBg).setPadding(6)
                        .setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            }

            for (int i = 0; i < report.size(); i++) {
                ReporteDto row = report.get(i);
                DeviceRgb rowBg = (i % 2 == 0) ? null : rowAlt;
                boolean isPositive = row.getMovimiento() >= 0;

                addDataCell(table, row.getFecha(),            rowBg, TextAlignment.CENTER, 9, null, regular);
                addDataCell(table, row.getCliente(),          rowBg, TextAlignment.LEFT,   9, null, regular);
                addDataCell(table, row.getNumeroCuenta(),     rowBg, TextAlignment.CENTER, 9, null, regular);
                addDataCell(table, row.getTipo(),             rowBg, TextAlignment.CENTER, 9, null, regular);
                addDataCell(table, String.format("$ %.2f", row.getSaldoInicial()), rowBg, TextAlignment.RIGHT, 9, null, regular);
                addDataCell(table, row.getEstado() ? "Activa" : "Inactiva",       rowBg, TextAlignment.CENTER, 9, null, regular);
                addDataCell(table, (isPositive ? "+" : "") + String.format("%.2f", row.getMovimiento()),
                        rowBg, TextAlignment.RIGHT, 9, isPositive ? positiveColor : negativeColor, bold);
                addDataCell(table, String.format("$ %.2f", row.getSaldoDisponible()), rowBg, TextAlignment.RIGHT, 9, null, regular);
            }

            document.add(table);

            double total = report.stream().mapToDouble(ReporteDto::getMovimiento).sum();
            boolean totalPositive = total >= 0;
            document.add(new Paragraph(
                    "Total movimientos en el período: " + (totalPositive ? "+" : "") + String.format("%.2f", total))
                    .setFont(bold).setFontSize(10)
                    .setFontColor(totalPositive ? positiveColor : negativeColor)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(10));

            document.add(new Paragraph("Generado el " + LocalDateTime.now().toLocalDate() + " — Banco Devsu")
                    .setFont(regular).setFontSize(8).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(20));

            document.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("Error al generar el reporte PDF");
        }
    }

    private Cell metaCell(String label, String value, DeviceRgb bg, PdfFont bold, PdfFont regular) {
        return new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(8).setFontColor(ColorConstants.WHITE))
                .add(new Paragraph(value).setFont(regular).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(bg)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);
    }

    private void addDataCell(Table table, String text, DeviceRgb bg, TextAlignment align, float fontSize, DeviceRgb fontColor, PdfFont font) {
        Paragraph p = new Paragraph(text).setFont(font).setFontSize(fontSize);
        if (fontColor != null) p.setFontColor(fontColor);
        Cell cell = new Cell().add(p).setPadding(5).setTextAlignment(align)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(200, 210, 230), 0.5f));
        if (bg != null) cell.setBackgroundColor(bg);
        table.addCell(cell);
    }

    private void validateDailyLimit(Cuenta account, double amount) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        double totalWithdrawnToday = movimientoRepository
                .findByCuentaIdAndFechaBetween(account.getId(), startOfDay, endOfDay)
                .stream()
                .filter(m -> m.getValor() < 0)
                .mapToDouble(m -> Math.abs(m.getValor()))
                .sum();

        if (totalWithdrawnToday + Math.abs(amount) > DAILY_LIMIT) {
            throw new BusinessException("Límite diario de retiros excedido");
        }
    }

    private double calculateCurrentBalance(Cuenta account) {
        return movimientoRepository.findByCuentaId(account.getId())
                .stream()
                .reduce((first, second) -> second)
                .map(Movimiento::getSaldo)
                .orElse(account.getSaldoInicial());
    }

    private Movimiento findMovementById(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
    }

    private ReporteDto toReportDto(Movimiento movement) {
        ReporteDto dto = new ReporteDto();
        dto.setFecha(movement.getFecha().toLocalDate().toString());
        dto.setCliente(movement.getCuenta().getCliente().getNombre());
        dto.setNumeroCuenta(movement.getCuenta().getNumeroCuenta());
        dto.setTipo(movement.getCuenta().getTipoCuenta());
        dto.setSaldoInicial(movement.getCuenta().getSaldoInicial());
        dto.setEstado(movement.getCuenta().getEstado());
        dto.setMovimiento(movement.getValor());
        dto.setSaldoDisponible(movement.getSaldo());
        return dto;
    }
}
