# Evidências da aplicação

Salve os prints nesta pasta (PNG) com os nomes sugeridos.

| # | Arquivo sugerido | O que capturar |
|---|---|---|
| 1 | `01-aplicacao-subindo.png` | Console com o Flyway aplicando `V1`–`V5` (`Successfully applied 5 migrations`) e `Started AlertaEncostaApplication` |
| 2 | `02-swagger-ui.png` | http://localhost:8080/swagger-ui.html com as 7 tags |
| 3 | `03-flyway-schema-history.png` | Console H2 (ou SQL Developer) com `SELECT * FROM "flyway_schema_history";` |
| 4 | `04-tabelas-banco.png` | Tabelas `TB_AE_*` e `SELECT * FROM TB_AE_ALERTA;` |
| 5 | `05-insomnia-colecao.png` | Coleção importada, com as 8 pastas |
| 6 | `06-leitura-gera-alerta.png` | Requisição 30: `acao = ALERTA_EMITIDO` |
| 7 | `07-alerta-com-rota.png` | Requisição 41: mensagem, ponto de apoio, distância e `linkMapa` |
| 8 | `08-pendencias-busca-ativa.png` | Requisição 47: Maria (PRECISA_AJUDA) antes de Antonia (SEM_RESPOSTA) |
| 9 | `09-previsao-ml.png` | Requisição 37: modelo de ML eleva o BR-03 para ALTO |
| 10 | `10-erro-422.png` | Requisição 34 (estação em manutenção) ou 59 (pular etapa), com o Problem Details |
| 11 | `11-erro-409.png` | Requisição 42 (segundo alerta no mesmo setor) |
| 12 | `12-erro-400.png` | Requisição 18, com o mapa `campos` |
| 13 | `13-indicadores.png` | Requisição 66 |
| 14 | `14-mvn-test.png` | `mvn test` com `BUILD SUCCESS` |
| 15 | `15-oracle-fiap.png` (opcional) | Aplicação rodando com o perfil `oracle` e as tabelas no SQL Developer |

## Relatório automático

Com a API rodando e o banco limpo, execute na raiz do projeto:

```bash
./scripts/testes-manuais.sh
```

O script grava `docs/evidencias/execucao-testes-manuais.md` com a requisição, a resposta e o status de cada um dos 70 cenários. Faça o commit desse arquivo junto com os prints.
