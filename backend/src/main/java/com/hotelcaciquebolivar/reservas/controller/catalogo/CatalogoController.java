package com.hotelcaciquebolivar.reservas.controller.catalogo;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelcaciquebolivar.reservas.dto.catalogo.MunicipioDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.RolDTO;
import com.hotelcaciquebolivar.reservas.dto.catalogo.TipoIdentificacionDTO;
import com.hotelcaciquebolivar.reservas.service.catalogo.CatalogoService;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la consulta de catálogos del sistema.
 * Proporciona listas estáticas de datos de referencia como roles,
 * tipos de identificación y municipios.
 */
@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    /**
     * Obtiene la lista de todos los roles disponibles en el sistema.
     *
     * @return lista de roles
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RolDTO>> obtenerRoles() {
        return ResponseEntity.ok(catalogoService.obtenerRoles());
    }

    /**
     * Obtiene la lista de tipos de identificación disponibles.
     *
     * @return lista de tipos de identificación
     */
    @GetMapping("/tipos-identificacion")
    public ResponseEntity<List<TipoIdentificacionDTO>> obtenerTiposIdentificacion() {
        return ResponseEntity.ok(catalogoService.obtenerTiposIdentificacion());
    }

    /**
     * Obtiene la lista de todos los municipios registrados.
     *
     * @return lista de municipios
     */
    @GetMapping("/municipios")
    public ResponseEntity<List<MunicipioDTO>> obtenerMunicipios() {
        return ResponseEntity.ok(catalogoService.obtenerMunicipios());
    }

    /**
     * Busca municipios por término de búsqueda.
     *
     * @param search término de búsqueda para filtrar municipios
     * @return lista de municipios que coinciden con el término de búsqueda
     */
    @GetMapping("/municipios/buscar")
    public ResponseEntity<List<MunicipioDTO>> buscarMunicipios(
            @RequestParam String search
    ) {

        return ResponseEntity.ok(
                catalogoService.buscarMunicipios(search)
        );
    }
}
