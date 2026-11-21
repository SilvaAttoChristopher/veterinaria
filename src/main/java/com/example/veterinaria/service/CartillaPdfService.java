
package com.example.veterinaria.service;

import com.example.veterinaria.modelo.Cartilla;
import com.example.veterinaria.modelo.Mascota;
import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.modelo.Vacunacion;
import com.example.veterinaria.repository.CartillaRepository;
import com.example.veterinaria.repository.VacunacionRepository;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class CartillaPdfService {

    private final CartillaRepository cartillaRepository;
    private final VacunacionRepository vacunacionRepository;

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font TITULO =
            new Font(Font.HELVETICA, 18, Font.BOLD);

    private static final Font SUBTITULO =
            new Font(Font.HELVETICA, 12, Font.BOLD);

    private static final Font NORMAL =
            new Font(Font.HELVETICA, 10, Font.NORMAL);

    private static final Font CABECERA =
            new Font(Font.HELVETICA, 9, Font.BOLD);

    public CartillaPdfService(
            CartillaRepository cartillaRepository,
            VacunacionRepository vacunacionRepository) {

        this.cartillaRepository = cartillaRepository;
        this.vacunacionRepository = vacunacionRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generarPDF(Long id) {

        Cartilla cartilla = cartillaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la cartilla: " + id));

        Mascota mascota = cartilla.getMascota();
        Usuario propietario = mascota.getCliente();

        List<Vacunacion> vacunaciones =
                vacunacionRepository.findByCartillaIdCartilla(id);

        vacunaciones.sort(
                Comparator.comparing(
                        Vacunacion::getFechaAplicacion,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()))
        );

        try (ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            Document documento = new Document(PageSize.A4);
            documento.setMargins(40, 40, 45, 45);

            PdfWriter.getInstance(documento, salida);

            try {
                documento.open();

                // Encabezado
                Paragraph titulo = new Paragraph("VETWEB", TITULO);
                titulo.setAlignment(Element.ALIGN_CENTER);
                documento.add(titulo);

                Paragraph subtitulo = new Paragraph(
                        "CARTILLA DE VACUNACIÓN", SUBTITULO);
                subtitulo.setAlignment(Element.ALIGN_CENTER);
                subtitulo.setSpacingAfter(12);
                documento.add(subtitulo);

                agregarTexto(documento,
                        "N.º de cartilla: " + cartilla.getIdCartilla());

                agregarTexto(documento,
                        "Fecha de creación: "
                        + fecha(cartilla.getFechaCreacion()));

                agregarTexto(documento,
                        "Fecha de emisión: " + fecha(LocalDate.now()));

                // Datos de mascota
                agregarSeccion(documento, "DATOS DE LA MASCOTA");

                agregarTexto(documento,
                        "Nombre: " + valor(mascota.getNombre()));

                agregarTexto(documento,
                        "Especie: " + valor(mascota.getEspecie()));

                agregarTexto(documento,
                        "Raza: " + valor(mascota.getRaza()));

                agregarTexto(documento,
                        "Sexo: " + sexo(mascota.getSexo()));

                agregarTexto(documento,
                        "Fecha de nacimiento: "
                        + fecha(mascota.getFechaNacimiento()));

                // Datos del propietario
                agregarSeccion(documento, "DATOS DEL PROPIETARIO");

                agregarTexto(documento,
                        "Nombre: "
                        + valor(propietario.getNombres()) + " "
                        + valor(propietario.getApellidos()));

                agregarTexto(documento,
                        "Teléfono: "
                        + valor(propietario.getTelefono()));

                agregarTexto(documento,
                        "Correo: "
                        + valor(propietario.getCorreo()));

                // Vacunaciones
                agregarSeccion(documento, "HISTORIAL DE VACUNACIÓN");

                PdfPTable tabla = new PdfPTable(4);
                tabla.setWidthPercentage(100);
                tabla.setWidths(new float[]{3f, 2f, 2f, 3f});
                tabla.setSpacingAfter(12);

                agregarCelda(tabla, "Vacuna", true);
                agregarCelda(tabla, "Aplicación", true);
                agregarCelda(tabla, "Próxima dosis", true);
                agregarCelda(tabla, "Observaciones", true);

                if (vacunaciones.isEmpty()) {
                    PdfPCell celda = new PdfPCell(
                            new Phrase(
                                    "Sin vacunaciones registradas",
                                    NORMAL));

                    celda.setColspan(4);
                    celda.setPadding(9);
                    tabla.addCell(celda);

                } else {
                    for (Vacunacion vacunacion : vacunaciones) {

                        String vacuna = vacunacion.getVacuna() != null
                                ? vacunacion.getVacuna().getNombre()
                                : "No especificada";

                        agregarCelda(tabla, vacuna, false);

                        agregarCelda(tabla,
                                fecha(vacunacion.getFechaAplicacion()),
                                false);

                        agregarCelda(tabla,
                                fecha(vacunacion.getProximaDosis()),
                                false);

                        agregarCelda(tabla,
                                valor(vacunacion.getObservaciones()),
                                false);
                    }
                }

                documento.add(tabla);

                // Observaciones de cartilla
                agregarSeccion(documento, "OBSERVACIONES GENERALES");

                agregarTexto(documento,
                        valor(cartilla.getObservaciones()));

                // Pie del documento
                Paragraph pie = new Paragraph(
                        "Documento generado por VetWeb",
                        new Font(Font.HELVETICA, 9, Font.ITALIC));

                pie.setAlignment(Element.ALIGN_CENTER);
                pie.setSpacingBefore(25);
                documento.add(pie);

            } finally {
                documento.close();
            }

            return salida.toByteArray();

        } catch (DocumentException e) {
            throw new IllegalStateException(
                    "Error al generar el PDF de la cartilla " + id, e);

        } catch (java.io.IOException e) {
            throw new IllegalStateException(
                    "Error al guardar el PDF en memoria", e);
        }
    }

    // Métodos auxiliares

    private void agregarSeccion(Document documento, String texto)
            throws DocumentException {

        Paragraph seccion = new Paragraph(texto, SUBTITULO);
        seccion.setSpacingBefore(16);
        seccion.setSpacingAfter(8);
        documento.add(seccion);
    }

    private void agregarTexto(Document documento, String texto)
            throws DocumentException {

        Paragraph parrafo = new Paragraph(texto, NORMAL);
        parrafo.setSpacingAfter(5);
        documento.add(parrafo);
    }

    private void agregarCelda(
            PdfPTable tabla, String texto, boolean encabezado) {

        PdfPCell celda = new PdfPCell(
                new Phrase(valor(texto),
                        encabezado ? CABECERA : NORMAL));

        celda.setPadding(7);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (encabezado) {
            celda.setBackgroundColor(
                    new Color(230, 235, 242));
        }

        tabla.addCell(celda);
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank()
                ? "No registrado" : texto;
    }

    private String fecha(LocalDate fecha) {
        return fecha == null
                ? "No registrada" : fecha.format(FECHA);
    }

    private String sexo(Character sexo) {
        if (sexo == null) return "No registrado";

        return switch (Character.toUpperCase(sexo)) {
            case 'M' -> "Macho";
            case 'H', 'F' -> "Hembra";
            default -> "No registrado";
        };
    }
}
