package com.devsu.banco;

import com.devsu.banco.dto.MovimientoDto;
import com.devsu.banco.service.MovimientoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class MovimientoControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private MovimientoService movimientoService;

    @Test
    public void findAll_retornaListaMovimientos() throws Exception {
        MovimientoDto dto = new MovimientoDto();
        dto.setId(1L);
        dto.setTipoMovimiento("Crédito");
        dto.setValor(600.0);
        dto.setSaldo(700.0);
        dto.setCuentaId(1L);
        dto.setFecha(LocalDateTime.now());

        when(movimientoService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoMovimiento").value("Crédito"))
                .andExpect(jsonPath("$[0].valor").value(600.0));
    }

    @Test
    public void create_retornaMovimientoCreado() throws Exception {
        MovimientoDto dto = new MovimientoDto();
        dto.setTipoMovimiento("Crédito");
        dto.setValor(600.0);
        dto.setCuentaId(1L);

        MovimientoDto response = new MovimientoDto();
        response.setId(1L);
        response.setTipoMovimiento("Crédito");
        response.setValor(600.0);
        response.setSaldo(700.0);
        response.setCuentaId(1L);
        response.setFecha(LocalDateTime.now());

        when(movimientoService.create(any(MovimientoDto.class))).thenReturn(response);

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.saldo").value(700.0));
    }
}