package com.devsu.banco;

import com.devsu.banco.dto.ClienteDto;
import com.devsu.banco.service.ClienteService;
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
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ClienteControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void findAll_retornaListaClientes() throws Exception {
        ClienteDto dto = new ClienteDto();
        dto.setId(1L);
        dto.setNombre("Jose Lema");
        dto.setClienteId("jose123");
        dto.setEstado(true);

        when(clienteService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Jose Lema"));
    }

    @Test
    public void create_retornaClienteCreado() throws Exception {
        ClienteDto dto = new ClienteDto();
        dto.setNombre("Jose Lema");
        dto.setGenero("Masculino");
        dto.setEdad(30);
        dto.setIdentificacion("1234567890");
        dto.setDireccion("Otavalo sn y principal");
        dto.setTelefono("098254785");
        dto.setClienteId("jose123");
        dto.setContrasena("1234");
        dto.setEstado(true);

        ClienteDto response = new ClienteDto();
        response.setId(1L);
        response.setNombre("Jose Lema");
        response.setClienteId("jose123");
        response.setEstado(true);

        when(clienteService.create(dto)).thenReturn(response);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
