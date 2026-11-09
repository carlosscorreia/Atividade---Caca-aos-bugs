# Vértice — SOS Loja Virtual: Caça aos Bugs

**Disciplina:** Desenvolvimento de Sistemas  
**Modalidade:** atividade prática em equipe  
**Tecnologias:** Java, HTML, CSS e JavaScript

Este projeto foi preparado para funcionar, mas contém **defeitos intencionais** que precisam ser investigados e corrigidos.

## Contexto

A **Vértice**, uma pequena loja virtual de acessórios de tecnologia, acabou de disponibilizar uma atualização em seu sistema. A página continua abrindo normalmente e vários recursos parecem funcionar, mas os clientes começaram a relatar comportamentos inesperados.

A equipe de desenvolvimento foi chamada para **investigar e corrigir cinco incidentes**. O código-fonte do sistema está disponível, porém os chamados não indicam em quais arquivos estão as falhas. A empresa não quer uma nova loja: quer que a aplicação existente volte a cumprir suas regras, sem perder funcionalidades que já funcionam.

## Seu desafio

Em equipes, vocês deverão:

1. Executar o projeto inicial e conhecer as funcionalidades.
2. **Reproduzir** cada um dos cinco incidentes antes de tentar corrigi-lo.
3. Investigar com ferramentas de diagnóstico: **breakpoints no Java**, inspeção de variáveis, mensagens de erro e aba **Network** do navegador.
4. Localizar a causa do problema e fazer a menor correção segura possível.
5. **Retestar** os cenários defeituosos e verificar se as outras funcionalidades continuam funcionando.
6. Entregar o código corrigido com o registro resumido da investigação.

## Chamados da equipe de suporte

| Chamado | Relato recebido |
|---|---|
| **INC-001 — Estoque impossível** | Um cliente conseguiu adicionar ao carrinho mais unidades de um produto do que a loja realmente possui. |
| **INC-002 — Cupom suspeito** | O cupom promocional é aceito, mas o desconto calculado não corresponde ao anunciado. |
| **INC-003 — Carrinho desatualizado** | Depois de algumas adições, a quantidade apresentada no carrinho deixa de acompanhar o que realmente foi registrado. |
| **INC-004 — Produto fantasma** | Ao consultar um código de produto que não existe, a loja apresenta uma falha inesperada. |
| **INC-005 — Pedido zumbi** | Um pedido cancelado continua aparecendo na lista de pedidos em aberto. |

## Regras de negócio da Vértice

Estas regras são a referência para determinar se o sistema está correto:

- Os produtos apresentam um **estoque disponível**, e a quantidade acumulada do mesmo produto no carrinho **não pode ultrapassar esse estoque**.
- O cupom **`BEMVINDO10`** concede **10% de desconto sobre o subtotal dos produtos**.
- O frete custa **R$ 18,90** para compras com subtotal inferior a **R$ 300,00**; a partir desse valor, é gratuito. O critério é o subtotal **antes do desconto**.
- Uma inclusão bem-sucedida no carrinho deve atualizar a quantidade e os valores exibidos **sem recarregar a página**.
- Consultar um produto existente deve retornar suas informações. Consultar um produto inexistente deve produzir a resposta HTTP **404 (não encontrado)**, sem provocar erro interno do servidor.
- Um pedido cancelado deve continuar no **histórico completo**, com o status Cancelado, mas **não pode aparecer entre os pedidos em aberto**. O estoque de seus produtos deve ser devolvido.

## Requisitos para executar o projeto

- Java **JDK 17 ou superior** instalado (o comando `javac -version` deve funcionar).
- Navegador web atualizado.
- Uma IDE Java com debugger (IntelliJ IDEA, Eclipse ou VS Code com extensões de Java).
- **Não** é necessário usar Maven, Spring Boot ou instalar dependências externas.

## Como executar

1. Extraia todo o conteúdo do ZIP do projeto em uma pasta.
2. No **Windows**, execute `executar.bat` (ou abra o terminal nessa pasta e execute o arquivo).
3. Em **Linux/macOS**, execute `sh executar.sh`.
4. Abra **http://localhost:8765** no navegador.
5. Não feche o terminal durante o uso. Para encerrar, pressione **Ctrl+C**.

Se usar a IDE, abra a pasta do projeto e execute `loja.Main` com o **diretório de trabalho apontando para a pasta raiz do projeto**, onde existe a pasta `web`.

### Se a porta 8765 estiver ocupada

Encerre o programa que está usando a porta ou altere a porta em `src/loja/Main.java` e use o novo endereço no navegador.

## Estrutura do projeto

```text
src/loja/
    Main.java           -> servidor HTTP, rotas e respostas
    LojaService.java    -> catálogo, carrinho e pedidos
    PrecoService.java   -> regras de preço, desconto e frete
    Produto.java        -> dados de produto
    Pedido.java         -> dados de pedido
    ErroNegocio.java    -> exceções de regra de negócio
    Json.java           -> formatação de respostas JSON
web/
    index.html          -> estrutura da página
    styles.css          -> aparência da loja
    app.js              -> interação com a página e chamadas HTTP
executar.bat / executar.sh
```

## Rotas úteis para observar na aba Network

- `GET /api/products`: lista o catálogo.
- `GET /api/products/{id}`: consulta por código.
- `GET /api/cart`: estado do carrinho.
- `POST /api/cart/items`: adiciona um produto (campos `produtoId` e `quantidade`).
- `POST /api/checkout`: cria um pedido (campo `cupom`).
- `GET /api/orders?view=active`: pedidos em aberto.
- `GET /api/orders?view=all`: todos os pedidos.
- `POST /api/orders/{id}/cancel`: cancela um pedido.

## Requisitos de investigação

Para **cada chamado**, registrem no arquivo `RELATORIO_MODELO.md`:

- **Reprodução:** quais ações provocaram o defeito;
- **Evidência:** o que foi visto na tela, na aba Network, no debugger ou nos logs;
- **Causa:** qual comportamento do código provocava o problema;
- **Correção:** o que foi alterado;
- **Verificação:** como demonstraram que o defeito foi resolvido.

Não é necessário escrever um relatório extenso. O importante é explicar o raciocínio e apresentar evidências verificáveis.

## Orientações importantes

- O projeto inicial está no ZIP entregue pelo professor. As instruções para executá-lo estão na seção **Como executar** deste README.
- Use o navegador para observar o comportamento do usuário e o debugger para acompanhar a execução do Java.
- Na aba **Network**, observe a rota, o método HTTP, o status da resposta e os dados retornados.
- **Não é necessário construir um novo front-end**, refazer a identidade visual, adicionar banco de dados ou criar novos padrões de projeto.
- O sistema utiliza dados **em memória**: ao reiniciar o servidor, o carrinho e os pedidos são apagados e o estoque original é restaurado. Isso é intencional e **não** faz parte dos incidentes.
- Evitem modificar regras corretas para fazer um teste específico passar. O sistema deve continuar funcionando para diferentes produtos e quantidades.
- O projeto é **local**, sem autenticação, pagamentos reais ou dados de clientes. **Não o publique como um e-commerce de produção.**

## Entrega

Enviar **um ZIP da pasta do projeto corrigido**, incluindo:

- O código-fonte com as correções dos cinco incidentes.
- O arquivo `RELATORIO_MODELO.md` preenchido com os registros da investigação.

Arquivos de compilação (`out/`, `.class`) não são necessários.

A atividade será acompanhada em sala e poderá ser concluída em casa, se necessário. **Não haverá apresentação formal de cada equipe nem necessidade de preparar slides.**