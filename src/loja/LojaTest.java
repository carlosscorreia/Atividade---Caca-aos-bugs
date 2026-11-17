package loja;

import java.util.List;

public class LojaTest {
    private static int totalTestes = 0;
    private static int testesAprovados = 0;

    public static void main(String[] args) {
        System.out.println("--- Executando testes da Loja Virtual ---");

        executarTeste("INC-001: Bloquear adição ao carrinho que ultrapassa o estoque", LojaTest::testInc001EstoqueCumulativo);
        executarTeste("INC-002: Aplicar 10% de desconto com cupom BEMVINDO10", LojaTest::testInc002CupomDezPorCento);
        executarTeste("INC-004: Retornar 404 para consulta de produto inexistente", LojaTest::testInc004BuscarProdutoInexistente);
        executarTeste("INC-005: Ocultar pedidos cancelados da lista de pedidos em aberto", LojaTest::testInc005PedidosCanceladosOcultos);
        executarTeste("Regra de frete grátis para compras a partir de R$ 300,00", LojaTest::testRegraFrete);
        executarTeste("Devolução correta de estoque após cancelamento de pedido", LojaTest::testDevolucaoEstoqueCancelamento);
        executarTeste("Impedir estoque negativo no produto", LojaTest::testProtecaoEstoqueProduto);
        executarTeste("Cálculo correto de subtotal e quantidades no carrinho", LojaTest::testIntegridadeCarrinhoJson);

        System.out.println("----------------------------------------");
        System.out.println(String.format("Total: %d | Aprovados: %d | Falhas: %d",
                totalTestes, testesAprovados, totalTestes - testesAprovados));
        System.out.println("----------------------------------------");

        if (testesAprovados != totalTestes) {
            System.exit(1);
        }
    }

    private static void executarTeste(String nome, Runnable teste) {
        totalTestes++;
        try {
            teste.run();
            testesAprovados++;
            System.out.println("[OK] " + nome);
        } catch (Throwable e) {
            System.err.println("[ERRO] " + nome + ": " + e.getMessage());
        }
    }

    private static void testInc001EstoqueCumulativo() {
        LojaService loja = new LojaService();
        Produto mouse = loja.buscarProduto(2); // Estoque = 3

        // 1ª adição: 2 unidades (carrinho = 2) -> OK
        loja.adicionarAoCarrinho(2, 2);

        // 2ª adição: tentar mais 2 (carrinho iria para 4, mas estoque é 3) -> Deve bloquear
        boolean bloqueado = false;
        try {
            loja.adicionarAoCarrinho(2, 2);
        } catch (ErroNegocio e) {
            bloqueado = (e.getCodigoHttp() == 409);
        }
        if (!bloqueado) {
            throw new AssertionError("Deveria bloquear adição que ultrapassa o estoque total");
        }

        // Adicionar apenas 1 (carrinho = 3) -> OK
        loja.adicionarAoCarrinho(2, 1);

        // Tentar adicionar mais 1 com estoque já no limite -> Deve bloquear
        bloqueado = false;
        try {
            loja.adicionarAoCarrinho(2, 1);
        } catch (ErroNegocio e) {
            bloqueado = (e.getCodigoHttp() == 409);
        }
        if (!bloqueado) {
            throw new AssertionError("Deveria bloquear quando atingir o limite do estoque");
        }
    }

    private static void testInc002CupomDezPorCento() {
        PrecoService precoService = new PrecoService();

        // R$ 179,90 (17990 centavos) -> 10% = 1799 centavos (R$ 17,99)
        int subtotal = 17990;
        int desconto = precoService.desconto(subtotal, "BEMVINDO10");
        if (desconto != 1799) {
            throw new AssertionError("Desconto esperado era 1799, mas retornou " + desconto);
        }

        // R$ 300,00 (30000 centavos) -> 10% = 3000 centavos (R$ 30,00)
        int subtotal300 = 30000;
        int desconto300 = precoService.desconto(subtotal300, "bemvindo10");
        if (desconto300 != 3000) {
            throw new AssertionError("Desconto esperado era 3000, mas retornou " + desconto300);
        }

        // Cupom inválido
        if (precoService.desconto(subtotal, "OUTRO") != 0) {
            throw new AssertionError("Cupom inválido não deve conceder desconto");
        }
    }

    private static void testInc004BuscarProdutoInexistente() {
        LojaService loja = new LojaService();

        // Produto que existe
        Produto p1 = loja.buscarProduto(1);
        if (p1 == null || !"Headset Pixel".equals(p1.getNome())) {
            throw new AssertionError("Produto 1 deveria ser retornado corretamente");
        }

        // Produto que não existe deve lançar 404
        boolean lancou404 = false;
        try {
            loja.buscarProduto(999);
        } catch (ErroNegocio e) {
            lancou404 = (e.getCodigoHttp() == 404);
        }
        if (!lancou404) {
            throw new AssertionError("Consulta de produto inexistente deve retornar erro 404");
        }
    }

    private static void testInc005PedidosCanceladosOcultos() {
        LojaService loja = new LojaService();
        loja.adicionarAoCarrinho(1, 1);
        Pedido pedido = loja.finalizarCompra("");
        int pedidoId = pedido.getId();

        // Antes de cancelar: deve aparecer nos pedidos em aberto
        List<Pedido> abertos = loja.listarPedidos(true);
        if (abertos.stream().noneMatch(p -> p.getId() == pedidoId)) {
            throw new AssertionError("Pedido recém-criado deveria constar nos pedidos em aberto");
        }

        // Cancelar o pedido
        loja.cancelarPedido(pedidoId);

        // Depois de cancelar: NÃO deve aparecer nos pedidos em aberto
        abertos = loja.listarPedidos(true);
        if (abertos.stream().anyMatch(p -> p.getId() == pedidoId)) {
            throw new AssertionError("Pedido cancelado não deve aparecer nos pedidos em aberto");
        }

        // Mas deve continuar aparecendo em 'todos os pedidos'
        List<Pedido> todos = loja.listarPedidos(false);
        Pedido cancelado = todos.stream().filter(p -> p.getId() == pedidoId).findFirst().orElse(null);
        if (cancelado == null || cancelado.getStatus() != Pedido.Status.CANCELADO) {
            throw new AssertionError("Pedido cancelado deve aparecer no histórico completo como CANCELADO");
        }
    }

    private static void testRegraFrete() {
        PrecoService precoService = new PrecoService();

        // Menor que R$ 300,00 -> R$ 18,90 (1890 centavos)
        if (precoService.frete(29990) != 1890) {
            throw new AssertionError("Abaixo de R$ 300,00 o frete deve ser R$ 18,90");
        }

        // R$ 300,00 ou mais -> Grátis (0)
        if (precoService.frete(30000) != 0 || precoService.frete(45000) != 0) {
            throw new AssertionError("A partir de R$ 300,00 o frete deve ser gratuito");
        }
    }

    private static void testDevolucaoEstoqueCancelamento() {
        LojaService loja = new LojaService();
        Produto produto = loja.buscarProduto(4); // Estoque original = 2

        loja.adicionarAoCarrinho(4, 2);
        Pedido pedido = loja.finalizarCompra("");
        if (produto.getEstoque() != 0) {
            throw new AssertionError("Estoque deveria ter zerado após compra de 2 unidades");
        }

        loja.cancelarPedido(pedido.getId());
        if (produto.getEstoque() != 2) {
            throw new AssertionError("Estoque deveria ter voltado para 2 após cancelamento");
        }
    }

    private static void testProtecaoEstoqueProduto() {
        Produto p = new Produto(10, "Item Teste", "Cat", "Desc", "📦", "blue", 1000, 2);

        boolean bloqueou = false;
        try {
            p.diminuirEstoque(3);
        } catch (ErroNegocio e) {
            bloqueou = (e.getCodigoHttp() == 409);
        }
        if (!bloqueou) {
            throw new AssertionError("Deveria bloquear tentativa de diminuir mais do que o estoque");
        }
    }

    private static void testIntegridadeCarrinhoJson() {
        LojaService loja = new LojaService();
        loja.adicionarAoCarrinho(1, 1); // 17990
        loja.adicionarAoCarrinho(2, 2); // 8990 * 2 = 17980
        String json = loja.carrinhoJson();
        if (!json.contains("\"subtotalCentavos\":35970") || !json.contains("\"quantidadeTotal\":3")) {
            throw new AssertionError("JSON do carrinho contém valores incorretos");
        }
    }
}
