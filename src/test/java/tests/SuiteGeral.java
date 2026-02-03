package tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // Liste aqui as classes que você quer rodar
        CadastroTest.class,
        ConectoresTest.class,
        GrupoCarregamentoTest.class,
        PontoCarregamentoTest.class,
        DashboardTest.class
})
public class SuiteGeral {
    // Essa classe fica vazia mesmo, ela serve só pra agrupar os testes
}