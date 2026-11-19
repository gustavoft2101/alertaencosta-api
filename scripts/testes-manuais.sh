#!/usr/bin/env bash
# Executa os mesmos cenarios da colecao Insomnia via curl e grava a evidencia em docs/evidencias/.
# Uso: suba a API com banco limpo (apague a pasta data/ e rode mvn spring-boot:run) e execute: ./scripts/testes-manuais.sh
set -u
BASE="${BASE_URL:-http://localhost:8080/api/v1}"
SETOR_ID="${SETOR_ID:-5}"
PONTO_ID="${PONTO_ID:-5}"
MORADOR_ID="${MORADOR_ID:-7}"
ESTACAO_ID="${ESTACAO_ID:-4}"
ALERTA_ID="${ALERTA_ID:-2}"
OCORRENCIA_ID="${OCORRENCIA_ID:-4}"
PONTO_TEMP_ID="${PONTO_TEMP_ID:-6}"
cd "$(dirname "$0")/.."
OUT="docs/evidencias/execucao-testes-manuais.md"
mkdir -p docs/evidencias
OK=0; FALHA=0
{ echo "# Evidencia de execucao dos testes manuais - AlertaEncosta API"; echo; echo "- Data: $(date "+%Y-%m-%d %H:%M:%S")"; echo "- Base URL: $BASE"; echo; } > "$OUT"
req() {
  local n="$1" nome="$2" metodo="$3" url="$4" esperado="$5" corpo="${6:-}"
  local tmp; tmp=$(mktemp)
  if [ -n "$corpo" ]; then
    code=$(curl -s -o "$tmp" -w "%{http_code}" -X "$metodo" "$url" -H "Content-Type: application/json" -d "$corpo")
  else
    code=$(curl -s -o "$tmp" -w "%{http_code}" -X "$metodo" "$url")
  fi
  if [ "$code" = "$esperado" ]; then r="OK"; OK=$((OK+1)); else r="FALHOU"; FALHA=$((FALHA+1)); fi
  printf "%-9s %s %-6s %s -> %s (esperado %s)\n" "[$r]" "$n" "$metodo" "$nome" "$code" "$esperado"
  {
    echo "## $n $metodo $nome"; echo; echo "\`$metodo $url\` -> **$code** (esperado $esperado) $r"; echo
    if [ -n "$corpo" ]; then echo "Requisicao:"; echo "\`\`\`json"; echo "$corpo"; echo "\`\`\`"; fi
    if [ -s "$tmp" ]; then echo "Resposta:"; echo "\`\`\`json"; (python3 -m json.tool "$tmp" 2>/dev/null || python -m json.tool "$tmp" 2>/dev/null || cat "$tmp"); echo "\`\`\`"; fi
    echo
  } >> "$OUT"
  rm -f "$tmp"
}
if ! curl -s -o /dev/null "$BASE/indicadores"; then echo "API nao esta respondendo em $BASE"; exit 1; fi

echo "== 1. Setores de risco =="
req "01" "Cadastrar setor" POST "$BASE/setores" 201 "{\"codigo\": \"BR-05\", \"nome\": \"Viela da Paz\", \"bairro\": \"Vila Brasilandia\", \"tipoRisco\": \"DESLIZAMENTO\", \"latitude\": -23.448, \"longitude\": -46.699, \"limiteChuvaMm\": 70, \"limiteUmidadeSoloPct\": 85}"
req "02" "Cadastrar setor com codigo duplicado" POST "$BASE/setores" 409 "{\"codigo\": \"br-05\", \"nome\": \"Outro\", \"bairro\": \"Vila Brasilandia\", \"tipoRisco\": \"MISTO\", \"latitude\": -23.44, \"longitude\": -46.69, \"limiteChuvaMm\": 70, \"limiteUmidadeSoloPct\": 85}"
req "03" "Cadastrar setor invalido" POST "$BASE/setores" 400 "{\"codigo\": \"SETOR5\", \"nome\": \"\", \"tipoRisco\": \"TERREMOTO\", \"latitude\": -23.44, \"longitude\": -46.69, \"limiteChuvaMm\": 0, \"limiteUmidadeSoloPct\": 120}"
req "04" "Listar setores em risco ALTO" GET "$BASE/setores?nivel=ALTO" 200
req "05" "Buscar setor" GET "$BASE/setores/$SETOR_ID" 200
req "06" "Situacao do BR-02 (painel)" GET "$BASE/setores/2/situacao" 200
req "07" "Atualizar setor (PUT)" PUT "$BASE/setores/$SETOR_ID" 200 "{\"nome\": \"Viela da Paz e Escadaria\", \"bairro\": \"Vila Brasilandia\", \"tipoRisco\": \"DESLIZAMENTO\", \"latitude\": -23.448, \"longitude\": -46.699, \"limiteChuvaMm\": 65, \"limiteUmidadeSoloPct\": 85}"
req "08" "Excluir setor em uso" DELETE "$BASE/setores/2" 409
echo "== 2. Pontos de apoio e rota segura =="
req "09" "Cadastrar ponto de apoio" POST "$BASE/pontos-apoio" 201 "{\"nome\": \"EE Viela da Paz (simulado)\", \"tipo\": \"ESCOLA\", \"endereco\": \"Rua da Paz, 100 - Vila Brasilandia\", \"latitude\": -23.447, \"longitude\": -46.6975, \"capacidade\": 200}"
req "10" "Cadastrar ponto invalido" POST "$BASE/pontos-apoio" 400 "{\"nome\": \"X\", \"tipo\": \"ESCOLA\", \"endereco\": \"Rua\", \"latitude\": -95, \"longitude\": -46.69, \"capacidade\": 0}"
req "11" "Listar pontos ATIVO" GET "$BASE/pontos-apoio?situacao=ATIVO" 200
req "12" "Rota segura a partir do GPS" GET "$BASE/pontos-apoio/mais-proximo?latitude=-23.4525&longitude=-46.6950" 200
req "13" "Rota segura sem longitude" GET "$BASE/pontos-apoio/mais-proximo?latitude=-23.4525" 400
req "14" "Atualizar ponto (PUT)" PUT "$BASE/pontos-apoio/$PONTO_ID" 200 "{\"nome\": \"EE Viela da Paz (simulado)\", \"tipo\": \"ESCOLA\", \"endereco\": \"Rua da Paz, 100 - Vila Brasilandia\", \"latitude\": -23.447, \"longitude\": -46.6975, \"capacidade\": 250, \"situacao\": \"ATIVO\"}"
req "15" "Excluir ponto ja usado em alerta" DELETE "$BASE/pontos-apoio/2" 409
echo "== 3. Moradores =="
req "16" "Cadastrar moradora idosa" POST "$BASE/moradores" 201 "{\"nome\": \"Benedita Alves\", \"email\": \"benedita.alves@email.com\", \"cpf\": \"746.971.314-01\", \"telefone\": \"(11) 96543-2109\", \"endereco\": \"Viela da Paz, 42 - casa 3\", \"latitude\": -23.4483, \"longitude\": -46.6994, \"setorId\": $SETOR_ID, \"necessidadeEspecial\": \"IDOSO\", \"pushToken\": \"ExponentPushToken[simulado-benedita]\"}"
req "17" "Cadastrar morador com e-mail duplicado" POST "$BASE/moradores" 409 "{\"nome\": \"Outra\", \"email\": \"BENEDITA.ALVES@email.com\", \"cpf\": \"123.456.789-09\", \"telefone\": \"11\", \"endereco\": \"Rua\", \"setorId\": 1}"
req "18" "Cadastrar morador invalido" POST "$BASE/moradores" 400 "{\"nome\": \"\", \"email\": \"nao-e-email\", \"cpf\": \"123\", \"setorId\": null}"
req "19" "Cadastrar morador em setor inexistente" POST "$BASE/moradores" 404 "{\"nome\": \"Teste\", \"email\": \"teste.setor@email.com\", \"cpf\": \"123.456.789-09\", \"telefone\": \"11\", \"endereco\": \"Rua\", \"setorId\": 9999}"
req "20" "Listar moradores do BR-02" GET "$BASE/moradores?setorId=2" 200
req "21" "Buscar morador" GET "$BASE/moradores/$MORADOR_ID" 200
req "22" "Rota segura do morador" GET "$BASE/moradores/$MORADOR_ID/rota-segura" 200
req "23" "Atualizar morador (PUT)" PUT "$BASE/moradores/$MORADOR_ID" 200 "{\"nome\": \"Benedita Alves de Souza\", \"email\": \"benedita.souza@email.com\", \"telefone\": \"(11) 96543-0000\", \"endereco\": \"Viela da Paz, 42 - casa 3\", \"latitude\": -23.4483, \"longitude\": -46.6994, \"setorId\": $SETOR_ID, \"necessidadeEspecial\": \"MOBILIDADE_REDUZIDA\", \"pushToken\": \"ExponentPushToken[simulado-benedita]\"}"
req "24" "Buscar morador inexistente" GET "$BASE/moradores/9999" 404
req "25" "Excluir morador com historico" DELETE "$BASE/moradores/1" 409
echo "== 4. Estacoes ESP32 e leituras =="
req "26" "Cadastrar estacao" POST "$BASE/estacoes" 201 "{\"codigo\": \"ESP32-BRA-05\", \"setorId\": $SETOR_ID, \"latitude\": -23.4481, \"longitude\": -46.6991}"
req "27" "Cadastrar estacao duplicada" POST "$BASE/estacoes" 409 "{\"codigo\": \"esp32-bra-05\", \"setorId\": 1, \"latitude\": -23.44, \"longitude\": -46.69}"
req "28" "Listar estacoes do BR-02" GET "$BASE/estacoes?setorId=2" 200
req "29" "Leitura normal (chuva fraca)" POST "$BASE/estacoes/$ESTACAO_ID/leituras" 201 "{\"chuvaMm\": 15.0, \"umidadeSoloPct\": 50.0}"
req "30" "Leitura acima do limite -> alerta automatico" POST "$BASE/estacoes/$ESTACAO_ID/leituras" 201 "{\"chuvaMm\": 75.5, \"umidadeSoloPct\": 92.0}"
req "31" "Nova leitura forte com alerta ja ativo" POST "$BASE/estacoes/$ESTACAO_ID/leituras" 201 "{\"chuvaMm\": 80.0, \"umidadeSoloPct\": 94.0}"
req "32" "Situacao do novo setor" GET "$BASE/setores/$SETOR_ID/situacao" 200
req "33" "Historico de leituras da estacao" GET "$BASE/estacoes/$ESTACAO_ID/leituras" 200
req "34" "Leitura de estacao em MANUTENCAO" POST "$BASE/estacoes/3/leituras" 422 "{\"chuvaMm\": 10.0, \"umidadeSoloPct\": 40.0}"
req "35" "Leitura fora da faixa do sensor" POST "$BASE/estacoes/$ESTACAO_ID/leituras" 400 "{\"chuvaMm\": 10.0, \"umidadeSoloPct\": 150.0}"
req "36" "Excluir estacao com leituras" DELETE "$BASE/estacoes/2" 409
echo "== 5. Previsoes do modelo de ML =="
req "37" "Modelo preve ALTO para o BR-03" POST "$BASE/setores/3/previsoes" 201 "{\"nivel\": \"ALTO\", \"probabilidade\": 0.88, \"versaoModelo\": \"rf-v1.0\"}"
req "38" "Previsao com probabilidade invalida" POST "$BASE/setores/3/previsoes" 400 "{\"nivel\": \"ALTO\", \"probabilidade\": 1.5}"
req "39" "Historico de previsoes do BR-03" GET "$BASE/setores/3/previsoes" 200
echo "== 6. Alertas e busca ativa =="
req "40" "Listar alertas ATIVO" GET "$BASE/alertas?status=ATIVO" 200
req "41" "Buscar alerta automatico" GET "$BASE/alertas/$ALERTA_ID" 200
req "42" "Emitir alerta manual em setor com alerta ativo" POST "$BASE/alertas" 409 "{\"setorId\": $SETOR_ID, \"nivel\": \"ALTO\"}"
req "43" "Emitir alerta manual MEDIO no BR-01" POST "$BASE/alertas" 201 "{\"setorId\": 1, \"nivel\": \"MEDIO\"}"
req "44" "Emitir alerta com nivel BAIXO" POST "$BASE/alertas" 422 "{\"setorId\": 4, \"nivel\": \"BAIXO\"}"
req "45" "Moradora confirma que esta segura" PUT "$BASE/alertas/$ALERTA_ID/confirmacoes/$MORADOR_ID" 200 "{\"situacao\": \"SEGURO\", \"observacao\": \"Cheguei na escola.\"}"
req "46" "Morador de outro setor tenta responder" PUT "$BASE/alertas/$ALERTA_ID/confirmacoes/1" 422 "{\"situacao\": \"SEGURO\"}"
req "47" "Pendencias do alerta do BR-02" GET "$BASE/alertas/1/pendencias" 200
req "48" "Maria e resgatada (atualiza a resposta)" PUT "$BASE/alertas/1/confirmacoes/1" 200 "{\"situacao\": \"SEGURO\", \"observacao\": \"Resgatada pela equipe da Defesa Civil.\"}"
req "49" "Pendencias depois do resgate" GET "$BASE/alertas/1/pendencias" 200
req "50" "Respostas ao alerta do BR-02" GET "$BASE/alertas/1/confirmacoes" 200
req "51" "Encerrar alerta" PATCH "$BASE/alertas/$ALERTA_ID/encerramento" 200 "{\"motivo\": \"Chuva cessou e vistoria nao encontrou movimentacao do solo.\"}"
req "52" "Encerrar alerta ja encerrado" PATCH "$BASE/alertas/$ALERTA_ID/encerramento" 422 "{\"motivo\": \"De novo\"}"
req "53" "Responder alerta encerrado" PUT "$BASE/alertas/$ALERTA_ID/confirmacoes/$MORADOR_ID" 422 "{\"situacao\": \"SEGURO\"}"
req "54" "Encerrar sem motivo" PATCH "$BASE/alertas/4/encerramento" 400 "{}"
echo "== 7. Ocorrencias (reportes) =="
req "55" "Reportar trinca no barranco" POST "$BASE/ocorrencias" 201 "{\"tipo\": \"TRINCA_SOLO\", \"descricao\": \"Rachadura nova no barranco atras da casa, com cerca de 2 metros.\", \"fotoUrl\": \"https://storage.alertaencosta.app/fotos/simulada-001.jpg\", \"latitude\": -23.4484, \"longitude\": -46.6993, \"moradorId\": $MORADOR_ID}"
req "56" "Reportar com tipo invalido" POST "$BASE/ocorrencias" 400 "{\"tipo\": \"TERREMOTO\", \"descricao\": \"Teste de tipo invalido.\", \"latitude\": -23.44, \"longitude\": -46.69, \"moradorId\": 1}"
req "57" "Listar reportes ABERTA" GET "$BASE/ocorrencias?status=ABERTA" 200
req "58" "Consultar por protocolo" GET "$BASE/ocorrencias/protocolo/AE-2026-DEMO02" 200
req "59" "Pular etapa: ABERTA -> RESOLVIDA" PATCH "$BASE/ocorrencias/$OCORRENCIA_ID/status" 422 "{\"status\": \"RESOLVIDA\"}"
req "60" "Status -> EM_VISTORIA" PATCH "$BASE/ocorrencias/$OCORRENCIA_ID/status" 200 "{\"status\": \"EM_VISTORIA\", \"observacao\": \"Vistoria agendada para hoje.\"}"
req "61" "Status -> RESOLVIDA" PATCH "$BASE/ocorrencias/$OCORRENCIA_ID/status" 200 "{\"status\": \"RESOLVIDA\", \"observacao\": \"Barranco contido com lona e sacos de areia.\"}"
req "62" "Historico do reporte" GET "$BASE/ocorrencias/$OCORRENCIA_ID/historico" 200
req "63" "Excluir reporte ja resolvido" DELETE "$BASE/ocorrencias/$OCORRENCIA_ID" 422
req "64" "Descartar sem justificativa" PATCH "$BASE/ocorrencias/2/status" 422 "{\"status\": \"DESCARTADA\"}"
req "65" "Excluir reporte ABERTA (enviado por engano)" DELETE "$BASE/ocorrencias/1" 204
echo "== 8. Indicadores e limpeza =="
req "66" "Painel de indicadores" GET "$BASE/indicadores" 200
req "67" "Endpoint inexistente" GET "$BASE/alertas/1/comentarios" 404
req "68" "Cadastrar ponto temporario" POST "$BASE/pontos-apoio" 201 "{\"nome\": \"Ponto temporario (simulado)\", \"tipo\": \"OUTRO\", \"endereco\": \"Rua Teste, 1\", \"latitude\": -23.4, \"longitude\": -46.6, \"capacidade\": 10}"
req "69" "Excluir ponto sem uso" DELETE "$BASE/pontos-apoio/$PONTO_TEMP_ID" 204
req "70" "Confirmar exclusao" GET "$BASE/pontos-apoio/$PONTO_TEMP_ID" 404

echo
echo "Resultado: $OK OK, $FALHA falha(s). Evidencia gravada em $OUT"
{ echo "---"; echo "**Resultado: $OK cenarios OK, $FALHA falha(s).**"; } >> "$OUT"
[ "$FALHA" -eq 0 ]
