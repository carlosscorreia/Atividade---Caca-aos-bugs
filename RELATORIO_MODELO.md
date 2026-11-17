# Registro de investigação — SOS Loja Virtual

Equipe: Carlos Eduardo

Para cada incidente, registre de maneira objetiva:

## INC-001
- Como reproduzimos:
  Escolhemos o Mouse Orbit (ID 2), que tem estoque inicial de 3 unidades. Clicamos em "+ Adicionar" duas vezes seguidas no produto (ou enviamos duas requisições POST para `/api/cart/items` com quantidade 2). Depois, clicamos em finalizar compra.
- O que observamos (tela, Network, debugger ou logs):
  A requisição foi aceita nas duas vezes com status 200 no Network. O carrinho acumulou 4 unidades, ultrapassando o estoque disponível de 3. Ao finalizar a compra, o estoque do produto ficou negativo (-1).
- Causa identificada:
  No método `adicionarAoCarrinho` em `LojaService.java` (linha 35), o `if` comparava apenas a variável `quantidade` (o incremento da requisição atual) com o estoque, em vez de comparar a quantidade acumulada (`quantidadeNova = quantidadeAtual + quantidade`).
- O que alteramos:
  Alteramos a validação para `if (quantidadeNova > produto.getEstoque())`. Também adicionamos verificações defensivas em `finalizarCompra` e no método `diminuirEstoque` da classe `Produto` para evitar estoque negativo.
- Como comprovamos a correção:
  Tentamos adicionar 2 unidades duas vezes seguidas. Na segunda vez, a requisição retornou status 409 com a mensagem "Quantidade indisponível no estoque." e o carrinho não permitiu exceder as 3 unidades disponíveis.

## INC-002
- Como reproduzimos:
  Adicionamos o Headset Pixel (R$ 179,90) ao carrinho e finalizamos a compra preenchendo o cupom `BEMVINDO10`.
- O que observamos:
  No resumo do pedido, o desconto aplicado foi de apenas R$ 1,79 (1%), totalizando R$ 197,01 com o frete, em vez do desconto esperado de 10% (R$ 17,99).
- Causa identificada:
  Em `PrecoService.java` (linha 9), o método `desconto` calculava `subtotalCentavos / 100`. Dividir por 100 calcula 1% do valor e não 10%.
- O que alteramos:
  Corrigimos a fórmula para `return (subtotalCentavos * 10) / 100;`.
- Como comprovamos a correção:
  Fizemos o checkout usando o cupom `BEMVINDO10` e conferimos o valor retornado. O desconto sobre R$ 179,90 passou a ser de exatamente R$ 17,99 (1799 centavos), gerando o total correto.

## INC-003
- Como reproduzimos:
  Na página inicial da loja, clicamos no botão "+ Adicionar" do Headset Pixel. O carrinho abriu indicando 1 unidade. Em seguida, clicamos novamente no botão "+ Adicionar" do mesmo produto.
- O que observamos:
  A mensagem toast de produto adicionado apareceu na tela, mas o número no botão do carrinho continuou como "1", a gaveta do carrinho continuou marcando "Quantidade: 1" e o subtotal não mudou. Os valores só atualizavam se a página fosse recarregada (F5).
- Causa identificada:
  No arquivo `web/app.js` (linha 117), a função `addProduct` tinha a condição `if (!alreadyListed) await refreshCart();`. Como o produto já estava na lista na segunda adição, `alreadyListed` era `true` e a função nunca chamava `refreshCart()`.
- O que alteramos:
  Removemos a condição `if (!alreadyListed)` de `addProduct`, chamando `await refreshCart()` sempre que um item for adicionado com sucesso.
- Como comprovamos a correção:
  Clicamos várias vezes no botão de adicionar do mesmo produto e verificamos que o contador do topo, o título do carrinho, a quantidade do item e o subtotal atualizam na hora, sem precisar recarregar a página.

## INC-004
- Como reproduzimos:
  Digitamos um código inexistente (como `999`) no campo "Consultar produto por código" e clicamos em "Consultar", ou fizemos uma requisição direta para `GET /api/products/999`.
- O que observamos:
  O terminal do servidor imprimiu um erro de `NullPointerException` e retornou resposta HTTP 500. Na interface apareceu a mensagem "Não foi possível consultar: Ocorreu um erro interno.".
- Causa identificada:
  Em `LojaService.java` (linha 28), o método `buscarProduto` retornava `null` quando o produto não existia. Em `Main.java` (linha 61), o servidor chamava `produto.toJson()` diretamente sobre a variável nula, disparando o `NullPointerException`.
- O que alteramos:
  Em `LojaService.java`, fizemos `buscarProduto` chamar `produtoOuErro(id)`, que já lança a exceção de negócio com status 404. Em `Main.java`, também adicionamos uma checagem defensiva de nulidade retornando 404.
- Como comprovamos a correção:
  Consultamos novamente o código 999. O servidor respondeu com status HTTP 404 e corpo `{"erro":"Produto não encontrado."}`, exibindo a mensagem amigável na tela sem gerar nenhum erro interno no terminal.

## INC-005
- Como reproduzimos:
  Fizemos um pedido na loja, abrimos a aba "Meus pedidos" na visualização "Em aberto" e clicamos em "Cancelar pedido".
- O que observamos:
  O pedido foi cancelado no backend, mas continuou aparecendo na lista da aba "Em aberto", apenas com a etiqueta "Cancelado".
- Causa identificada:
  No método `listarPedidos` em `LojaService.java` (linha 90), a filtragem de pedidos ativos usava `.filter(pedido -> pedido.getStatus() != Pedido.Status.ENTREGUE)`. Como os status possíveis são `EM_ABERTO`, `CANCELADO` e `ENTREGUE`, pedidos cancelados continuavam sendo retornados.
- O que alteramos:
  Ajustamos o filtro para aceitar estritamente pedidos abertos: `.filter(pedido -> pedido.getStatus() == Pedido.Status.EM_ABERTO)`.
- Como comprovamos a correção:
  Cancelamos um pedido aberto e conferimos que ele desapareceu na hora da lista "Em aberto", e continuou aparecendo normalmente na aba "Todos os pedidos" com status Cancelado.

## Reteste geral
Descreva brevemente o que foi testado depois das correções para garantir que as demais funções continuam funcionando.

1. **Fluxo de compras e estoque:** Adicionamos produtos variados, testamos os limites de estoque unitário e acumulado, verificamos o cálculo de subtotal e finalizamos compras.
2. **Frete e cupons:** Testamos compras abaixo de R$ 300,00 (frete R$ 18,90) e acima de R$ 300,00 (frete grátis), além de testar o cupom `BEMVINDO10` e cupons inválidos.
3. **Cancelamento de pedidos e devolução de estoque:** Cancelamos pedidos e confirmamos que eles somem da lista de pedidos em aberto, permanecem no histórico completo e o estoque dos produtos volta a ficar disponível no catálogo.
4. **Consulta de produtos:** Testamos a busca por códigos existentes e inexistentes, confirmando os retornos 200 e 404.
5. **Testes automatizados:** Executamos a classe `LojaTest` para garantir que todas as regras de negócio continuam funcionando conforme o esperado.
