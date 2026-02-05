# 🚀 Automação de Testes - Zona Verde (Maker No-Code)

Este repositório contém o framework de automação de testes end-to-end (E2E) para o sistema **Zona Verde**, desenvolvido sobre a plataforma **Maker**. O projeto foi estruturado para superar os desafios específicos de sistemas *low-code*, como IDs dinâmicos, múltiplos iframes e carregamentos assíncronos pesados.

---

## 🛠️ Tecnologias e Dependências

* **Linguagem:** Java 24
* **Framework de Testes:** JUnit 4
* **Ferramenta de Automação:** Selenium WebDriver (v4.27.0)
* **Gerenciador de Build:** Maven
* **Arquitetura:** Page Object Model (POM)
* **Logs:** System Out personalizado para rastreabilidade

---

## 🏗️ Estrutura do Projeto

O projeto utiliza o padrão **Page Object Model** para separar a lógica de negócio da infraestrutura de busca de elementos:



- **`src/test/java/tests/BaseTest.java`**: Contém o `setUp` e `tearDown` compartilhado, gerenciando a inicialização do driver e login automático.
- **`src/test/java/pages/`**: Armazena as classes que representam as páginas do sistema, contendo seletores (XPath/CSS) e métodos de ação.
- **`src/test/java/tests/`**: Contém as classes de teste com as validações de negócio (`Assertions`).

---

## 🧠 Soluções para Desafios Técnicos (Maker No-Code)

### 1. Navegação Recursiva de Iframes ("Boneca Russa")
Sistemas Maker organizam componentes em camadas profundas de frames. Criamos um mecanismo de busca recursiva que mergulha em cada iframe, tenta localizar o elemento e retorna o foco para a raiz em caso de falha, garantindo estabilidade total.

### 2. Sincronização e Seletores por Índice
Para lidar com IDs que mudam a cada build e classes CSS repetidas:
* **Indexação:** Utilizamos XPaths agrupados `(xpath)[n]` para diferenciar componentes visualmente idênticos (ex: cards de KPI).
* **Normalização:** Uso de `normalize-space()` para evitar quebra de testes por espaços ou caracteres invisíveis nos labels.

### 3. Execução em Headless com Resolução Fixa
Configurado para rodar em ambientes de CI (como GitHub Actions) utilizando Chrome em modo *headless*, garantindo que o mapa interativo e os gráficos sejam renderizados corretamente em `1920x1080`.

---

## 🚀 Como Executar

### Via IDE (IntelliJ/Eclipse)
1. Importe o projeto como um projeto Maven.
2. Navegue até `src/test/java/tests`.
3. Clique com o botão direito e selecione **Run 'All Tests'**.

### Via Linha de Comando (Maven)
```bash
mvn clean test
