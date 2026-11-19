package br.com.fiap.alertaencosta.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(description = "Envelope de paginacao estavel.")
public record PaginaResponse<T>(
        List<T> conteudo,
        @Schema(example = "0") int pagina,
        @Schema(example = "10") int tamanho,
        @Schema(example = "42") long totalElementos,
        @Schema(example = "5") int totalPaginas
) {
    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> mapper) {
        return new PaginaResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
