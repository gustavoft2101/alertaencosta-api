# Evidência de execução dos testes — AlertaEncosta API

- **Data:** 19/11/2026, 16:05 (horário de Brasília)
- **Ambiente:** IntelliJ IDEA 2025.3.2 (Windows), perfil `dev` (H2), banco recém-criado
- **Testes automatizados (`mvn test`):** BUILD SUCCESS — **16 testes, 0 falhas** (7 de integração em `AlertaEncostaApiIntegrationTest`, 9 de domínio em `DominioAlertaEncostaTest`)
- **Testes manuais:** os 70 cenários da coleção `insomnia/alertaencosta-insomnia.json`, executados em ordem contra `http://localhost:8080` — **70/70 com o status HTTP esperado**

## Destaques verificados nas respostas

| Cenário | Verificado |
|---|---|
| 30 — leitura acima do limite | Setor BR-05 foi de BAIXO para ALTO; alerta automático emitido com a mensagem *"ALERTA ALTO de deslizamento no setor BR-05 (Viela da Paz e Escadaria). Saia de casa agora. Va a pe ate EE Viela da Paz (simulado) - Rua da Paz, 100 - Vila Brasilandia (189 m, cerca de 3 min). Emergencia: Defesa Civil 199."* e 1 morador notificado |
| 12 — rota segura pelo GPS | Ponto ativo mais próximo: Paróquia Nossa Senhora (simulado), 439 m, ~7 min, com link do Google Maps |
| 47 — busca ativa | Maria da Silva (IDOSO, PRECISA_AJUDA, "Moro sozinha e nao consigo descer a escada...") listada antes de Antonia Souza (SEM_RESPOSTA) |
| 48/49 — resgate | Após Maria ser marcada como SEGURO (PUT idempotente), resta apenas Antonia Souza nas pendências |
| 59 — pular etapa | 422 Problem Details: *"Transicao de status invalida: ABERTA -> RESOLVIDA. Permitidas a partir de ABERTA: [EM_VISTORIA, DESCARTADA]."* |
| 13 — parâmetro ausente | 400: *"Required request parameter 'longitude' ... is not present"* |
| 66 — indicadores | 3 alertas ativos, 7 moradores (4 prioritários), 33,33% dos alertados confirmaram estar seguros, setores ALTO=3 / BAIXO=2 |

## Resultado por cenário

| # | Pasta | Requisição | Esperado | Obtido | Resultado |
|---|---|---|---|---|---|
| 01 | Setores de risco | POST Cadastrar setor | 201 | 201 | OK |
| 02 | Setores de risco | POST Cadastrar setor com codigo duplicado | 409 | 409 | OK |
| 03 | Setores de risco | POST Cadastrar setor invalido | 400 | 400 | OK |
| 04 | Setores de risco | GET Listar setores em risco ALTO | 200 | 200 | OK |
| 05 | Setores de risco | GET Buscar setor | 200 | 200 | OK |
| 06 | Setores de risco | GET Situacao do BR-02 (painel) | 200 | 200 | OK |
| 07 | Setores de risco | PUT Atualizar setor (PUT) | 200 | 200 | OK |
| 08 | Setores de risco | DELETE Excluir setor em uso | 409 | 409 | OK |
| 09 | Pontos de apoio e rota segura | POST Cadastrar ponto de apoio | 201 | 201 | OK |
| 10 | Pontos de apoio e rota segura | POST Cadastrar ponto invalido | 400 | 400 | OK |
| 11 | Pontos de apoio e rota segura | GET Listar pontos ATIVO | 200 | 200 | OK |
| 12 | Pontos de apoio e rota segura | GET Rota segura a partir do GPS | 200 | 200 | OK |
| 13 | Pontos de apoio e rota segura | GET Rota segura sem longitude | 400 | 400 | OK |
| 14 | Pontos de apoio e rota segura | PUT Atualizar ponto (PUT) | 200 | 200 | OK |
| 15 | Pontos de apoio e rota segura | DELETE Excluir ponto ja usado em alerta | 409 | 409 | OK |
| 16 | Moradores | POST Cadastrar moradora idosa | 201 | 201 | OK |
| 17 | Moradores | POST Cadastrar morador com e-mail duplicado | 409 | 409 | OK |
| 18 | Moradores | POST Cadastrar morador invalido | 400 | 400 | OK |
| 19 | Moradores | POST Cadastrar morador em setor inexistente | 404 | 404 | OK |
| 20 | Moradores | GET Listar moradores do BR-02 | 200 | 200 | OK |
| 21 | Moradores | GET Buscar morador | 200 | 200 | OK |
| 22 | Moradores | GET Rota segura do morador | 200 | 200 | OK |
| 23 | Moradores | PUT Atualizar morador (PUT) | 200 | 200 | OK |
| 24 | Moradores | GET Buscar morador inexistente | 404 | 404 | OK |
| 25 | Moradores | DELETE Excluir morador com historico | 409 | 409 | OK |
| 26 | Estacoes ESP32 e leituras | POST Cadastrar estacao | 201 | 201 | OK |
| 27 | Estacoes ESP32 e leituras | POST Cadastrar estacao duplicada | 409 | 409 | OK |
| 28 | Estacoes ESP32 e leituras | GET Listar estacoes do BR-02 | 200 | 200 | OK |
| 29 | Estacoes ESP32 e leituras | POST Leitura normal (chuva fraca) | 201 | 201 | OK |
| 30 | Estacoes ESP32 e leituras | POST Leitura acima do limite -> alerta automatico | 201 | 201 | OK |
| 31 | Estacoes ESP32 e leituras | POST Nova leitura forte com alerta ja ativo | 201 | 201 | OK |
| 32 | Estacoes ESP32 e leituras | GET Situacao do novo setor | 200 | 200 | OK |
| 33 | Estacoes ESP32 e leituras | GET Historico de leituras da estacao | 200 | 200 | OK |
| 34 | Estacoes ESP32 e leituras | POST Leitura de estacao em MANUTENCAO | 422 | 422 | OK |
| 35 | Estacoes ESP32 e leituras | POST Leitura fora da faixa do sensor | 400 | 400 | OK |
| 36 | Estacoes ESP32 e leituras | DELETE Excluir estacao com leituras | 409 | 409 | OK |
| 37 | Previsoes do modelo de ML | POST Modelo preve ALTO para o BR-03 | 201 | 201 | OK |
| 38 | Previsoes do modelo de ML | POST Previsao com probabilidade invalida | 400 | 400 | OK |
| 39 | Previsoes do modelo de ML | GET Historico de previsoes do BR-03 | 200 | 200 | OK |
| 40 | Alertas e busca ativa | GET Listar alertas ATIVO | 200 | 200 | OK |
| 41 | Alertas e busca ativa | GET Buscar alerta automatico | 200 | 200 | OK |
| 42 | Alertas e busca ativa | POST Emitir alerta manual em setor com alerta ativo | 409 | 409 | OK |
| 43 | Alertas e busca ativa | POST Emitir alerta manual MEDIO no BR-01 | 201 | 201 | OK |
| 44 | Alertas e busca ativa | POST Emitir alerta com nivel BAIXO | 422 | 422 | OK |
| 45 | Alertas e busca ativa | PUT Moradora confirma que esta segura | 200 | 200 | OK |
| 46 | Alertas e busca ativa | PUT Morador de outro setor tenta responder | 422 | 422 | OK |
| 47 | Alertas e busca ativa | GET Pendencias do alerta do BR-02 | 200 | 200 | OK |
| 48 | Alertas e busca ativa | PUT Maria e resgatada (atualiza a resposta) | 200 | 200 | OK |
| 49 | Alertas e busca ativa | GET Pendencias depois do resgate | 200 | 200 | OK |
| 50 | Alertas e busca ativa | GET Respostas ao alerta do BR-02 | 200 | 200 | OK |
| 51 | Alertas e busca ativa | PATCH Encerrar alerta | 200 | 200 | OK |
| 52 | Alertas e busca ativa | PATCH Encerrar alerta ja encerrado | 422 | 422 | OK |
| 53 | Alertas e busca ativa | PUT Responder alerta encerrado | 422 | 422 | OK |
| 54 | Alertas e busca ativa | PATCH Encerrar sem motivo | 400 | 400 | OK |
| 55 | Ocorrencias (reportes) | POST Reportar trinca no barranco | 201 | 201 | OK |
| 56 | Ocorrencias (reportes) | POST Reportar com tipo invalido | 400 | 400 | OK |
| 57 | Ocorrencias (reportes) | GET Listar reportes ABERTA | 200 | 200 | OK |
| 58 | Ocorrencias (reportes) | GET Consultar por protocolo | 200 | 200 | OK |
| 59 | Ocorrencias (reportes) | PATCH Pular etapa: ABERTA -> RESOLVIDA | 422 | 422 | OK |
| 60 | Ocorrencias (reportes) | PATCH Status -> EM_VISTORIA | 200 | 200 | OK |
| 61 | Ocorrencias (reportes) | PATCH Status -> RESOLVIDA | 200 | 200 | OK |
| 62 | Ocorrencias (reportes) | GET Historico do reporte | 200 | 200 | OK |
| 63 | Ocorrencias (reportes) | DELETE Excluir reporte ja resolvido | 422 | 422 | OK |
| 64 | Ocorrencias (reportes) | PATCH Descartar sem justificativa | 422 | 422 | OK |
| 65 | Ocorrencias (reportes) | DELETE Excluir reporte ABERTA (enviado por engano) | 204 | 204 | OK |
| 66 | Indicadores e limpeza | GET Painel de indicadores | 200 | 200 | OK |
| 67 | Indicadores e limpeza | GET Endpoint inexistente | 404 | 404 | OK |
| 68 | Indicadores e limpeza | POST Cadastrar ponto temporario | 201 | 201 | OK |
| 69 | Indicadores e limpeza | DELETE Excluir ponto sem uso | 204 | 204 | OK |
| 70 | Indicadores e limpeza | GET Confirmar exclusao | 404 | 404 | OK |

**Total: 70 cenários OK, 0 falhas.**
