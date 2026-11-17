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
- O que observamos:
- Causa identificada:
- O que alteramos:
- Como comprovamos a correção:

## INC-004
- Como reproduzimos:
- O que observamos:
- Causa identificada:
- O que alteramos:
- Como comprovamos a correção:

## INC-005
- Como reproduzimos:
- O que observamos:
- Causa identificada:
- O que alteramos:
- Como comprovamos a correção:

## Reteste geral
Descreva brevemente o que foi testado depois das correções para garantir que as demais funções continuam funcionando.
