# Bolão da Pelada

Aplicativo Android nativo desenvolvido em Kotlin para gerenciamento de bolões esportivos. Permite registrar previsões de jogos, acompanhar resultados, visualizar estatísticas detalhadas com gráficos e exportar relatórios em PDF.

**Projeto acadêmico** — Fatec Campinas
**Devs:** Cristian Pascual Moreira e Guilherme Moreira

---

## Funcionalidades

### Autenticação
- Login com email e senha
- Cadastro de novos usuários
- Recuperação e redefinição de senha
- Gerenciamento de perfil, como edição e exclusão de conta

### Gestão de Partidas
- Adicionar, editar e excluir partidas
- Campos separados para logos dos times A e B
- Seletor de data com calendário nativo
- Campos de resultado real habilitados apenas para jogos passados
- Confirmação de exclusão com diálogo com ação de desfazer
- Validação de formulários em tempo real

### Lista Inteligente
- Abas separadas para jogos **Futuros** e **Passados**
- Barra de busca por nome do time com filtro em tempo real
- Ordenação por: data do jogo (próxima/distante), nome do time (A-Z) e taxa de acerto
- Cards com feedback visual colorido (verde = acerto, vermelho = erro)

### Estatísticas
- Taxa de acerto geral com gráfico de pizza
- Times mais previstos com gráfico de barras
- Evolução mensal da taxa de acerto com gráfico de linhas
- Resumo com total de jogos, acertos, erros e destaques

### Extras
- Exportação de relatório completo em **PDF**
- Compartilhamento direto via WhatsApp, Telegram, email, etc.
- Splash Screen

---

## Tecnologias Utilizadas

| Tecnologia | Descrição |
|---|---|
| **Kotlin** | Linguagem principal |
| **Android SDK 34** | Target SDK |
| **Room e KSP** | Banco de dados local com processamento de anotações |
| **MPAndroidChart** | Gráficos (pizza, barras, linhas) |
| **ViewPager2 e TabLayout** | Navegação por abas |
| **Material Components** | Design system e componentes UI |
| **ViewBinding** | Acesso seguro às views |
| **Coroutines e Flow** | Programação assíncrona e reativa |
| **PdfDocument API** | Geração nativa de PDF |
| **FileProvider** | Compartilhamento seguro de arquivos |

---

## Screenshots

- Iremos disponibilizar em breve.

---

## Como Executar

### Pré-requisitos
- Android Studio Hedgehog ou superior
- JDK 17+
- SDK Android 34
- Dispositivo ou emulador com API 28+

### Passos
1. Clone o repositório:
```bash
   git clone https://github.com/Cristiansuper1/bolao-da-pelada
```
2. Abra o projeto no Android Studio
3. Aguarde a sincronização do Gradle
4. Execute em um dispositivo ou emulador

### Estrutura do Projeto
```
app/src/main/java/com/example/bolaodapelada/
├── data/
│   ├── dao/
│   ├── entity/
│   └── repository/
├── ui/
└── util/
```

### Licença
Este projeto está licenciado sob a MIT License

Fatec Campinas - Tópicos Especiais em Informática
2026
