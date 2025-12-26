# BillorTest - Aplicativo de Navegação e Chat

## 📋 Apresentação do Desafio

O BillorTest é um aplicativo Android que combina funcionalidades de navegação GPS em tempo real com um sistema de chat integrado. O desafio consistiu em desenvolver uma solução que permitisse:

- **Navegação GPS**: Implementar navegação turn-by-turn com integração ao Mapbox SDK, incluindo cálculo de rotas, replay de trajetos e controle de câmera
- **Chat em Tempo Real**: Sistema de mensagens instantâneas com suporte a mensagens de texto e áudio, com sincronização via Firebase Firestore
- **Arquitetura Modular**: Estruturar o projeto em módulos independentes seguindo princípios de Clean Architecture
- **Comunicação Inter-Módulos**: Garantir que módulos distintos (mapa e chat) possam compartilhar informações (como localização) de forma desacoplada

O principal desafio arquitetural foi criar uma aplicação escalável e testável, onde cada módulo mantém responsabilidades bem definidas sem criar dependências circulares.

---

## 🚀 Tecnologias Utilizadas

### Bibliotecas Principais

#### Navegação e Mapas
- **Mapbox Maps SDK (v11.8.1)**: Renderização de mapas e manipulação de UI
- **Mapbox Navigation SDK (v3.5.2)**: Sistema completo de navegação turn-by-turn
- **Mapbox Search SDK (v2.5.0)**: Busca de localizações e pontos de interesse
- **Google Play Services Location (v21.3.0)**: Obtenção de localização do dispositivo

#### Chat e Backend
- **Firebase Firestore (v25.1.1)**: Banco de dados NoSQL em tempo real para mensagens
- **Firebase Storage (v21.0.1)**: Armazenamento de arquivos de áudio
- **Room Database (v2.6.1)**: Persistência local de mensagens

#### Injeção de Dependências
- **Hilt/Dagger (v2.51.1)**: Gerenciamento de dependências e lifecycle

#### UI e Mídia
- **Media3 ExoPlayer (v1.5.0)**: Reprodução de áudio
- **Material Design 3 (v1.12.0)**: Componentes de interface

#### Arquitetura e Concorrência
- **Kotlin Coroutines (v1.9.0)**: Programação assíncrona
- **Kotlin Flow**: Streams reativos de dados
- **Lifecycle Components (v2.8.7)**: Gerenciamento de ciclo de vida

### Paradigmas e Conceitos

#### Clean Architecture
A aplicação segue os princípios de Clean Architecture, organizando o código em camadas:
- **Domain**: Lógica de negócio pura (use cases, models, interfaces de repository)
- **Data**: Implementação de repositórios e fontes de dados (local e remota)
- **UI**: Camada de apresentação (ViewModels, Fragments, Views)

#### SOLID Principles
- **Single Responsibility**: Cada classe tem uma única responsabilidade
- **Dependency Inversion**: Módulos dependem de abstrações, não de implementações concretas
- **Interface Segregation**: Interfaces específicas para cada contexto

#### Design Patterns
- **Repository Pattern**: Abstração do acesso a dados
- **Observer Pattern**: Uso de StateFlow e LiveData para UI reativa
- **Dependency Injection**: Todas as dependências são injetadas via Hilt
- **Use Case Pattern**: Lógica de negócio encapsulada em casos de uso únicos

#### Reactive Programming
- Uso de Kotlin Flow para streams de dados reativos
- StateFlow para estado observável da UI
- Lifecycle-aware observers para evitar memory leaks

---

## 🏗️ Modularização

A aplicação está dividida em 5 módulos principais, cada um com responsabilidades bem definidas:

### 1. **app** (Módulo Principal)
**Responsabilidade**: Integração de todos os módulos e gerenciamento da activity principal

**Componentes**:
- `BillorTestApplication`: Inicialização do MapboxNavigationApp
- `MapChatActivity`: Activity principal que gerencia a UI de mapa e chat
- `AppModule`: Configuração de dependências do Hilt

**Dependências**: Depende de todos os outros módulos (feature-map, feature-chat, commons, core-location)

### 2. **feature:map** (Módulo de Navegação)
**Responsabilidade**: Toda a lógica relacionada a mapas e navegação GPS

**Componentes**:
- `MapFragment`: Fragment que renderiza o mapa
- `BillorMapView`: View customizada que encapsula o MapView do Mapbox
- `NavigationManager`: Gerencia o ciclo de vida da navegação (iniciar, pausar, parar)
- `RouteManager`: Calcula e gerencia rotas de navegação
- `CameraManager`: Controla movimentação e estados da câmera (following, overview)
- `ReplayManager`: Reproduz trajetos gravados
- `SearchManager`: Integração com Mapbox Search para busca de locais
- `MapModule`: Provedor de dependências do módulo

**Conceitos**:
- Observadores (RoutesObserver, LocationObserver, RouteProgressObserver)
- StateFlow para estado de navegação (IDLE, READY, NAVIGATING)
- Lifecycle-aware components

**Dependências**: core-location (para acesso ao LocationRepository)

### 3. **feature:chat** (Módulo de Chat)
**Responsabilidade**: Sistema completo de mensagens instantâneas

**Camadas**:

#### Domain
- `ChatMessage`: Modelo de domínio de mensagem
- `ChatRepository`: Interface do repositório
- Use Cases:
  - `GetMessagesUseCase`: Busca mensagens do cache local
  - `SendMessageUseCase`: Envia mensagem de texto
  - `SendAudioMessageUseCase`: Envia mensagem de áudio (upload + envio)
  - `UploadAudioUseCase`: Faz upload de arquivo para Firebase Storage
  - `SubscribeToMessagesUseCase`: Observa mensagens em tempo real
  - `SendTypingStatusUseCase`: Envia status de digitação
  - `SubscribeToTypingStatusUseCase`: Observa status de digitação

#### Data
- `ChatLocalDataSource`: Acesso ao Room Database
- `ChatRemoteDataSource`: Interface de acesso remoto
- `FirestoreChatRemoteDataSource`: Implementação com Firestore
- `ChatRepositoryImpl`: Implementação do repositório (coordena local + remoto)
- `ChatMessageMapper`: Conversão entre DTOs e entidades

#### UI
- `ChatFragment`: Interface do chat
- `ChatViewModel`: ViewModel que expõe estados da UI
- `ChatMessageAdapter`: RecyclerView adapter para mensagens
- `AudioRecorderManager`: Gerenciamento de gravação de áudio

**Conceitos**:
- Offline-first: Mensagens são cacheadas localmente
- Real-time sync: Firestore listeners para atualizações instantâneas
- MVVM pattern

**Dependências**: commons (para AudioPlayer e handlers)

### 4. **commons** (Módulo Comum)
**Responsabilidade**: Componentes reutilizáveis compartilhados entre módulos

**Componentes**:
- `AudioPlayer`: Player de áudio usando ExoPlayer
- `AudioRecordHandler`: Handler para gravação de áudio com gerenciamento de permissões
- `SpeechToTextHandler`: Handler para reconhecimento de voz (future feature)
- Extensions:
  - `DeviceExt`: Obtenção de Device ID
  - `TimeExtension`: Formatação de tempo (mm:ss)
  - `ViewExt`: Utilitários de UI (hide keyboard)

**Conceitos**:
- Lifecycle-aware handlers
- Activity result launchers para permissões
- Callback interfaces

### 5. **core:location** (Módulo de Localização)
**Responsabilidade**: Gerenciamento centralizado de localização do usuário

**Componentes**:

#### Domain
- `GeoPoint`: Data class para coordenadas geográficas

#### Data
- `LocationRepository`: Repositório singleton que mantém a localização atual do usuário
  - Usa StateFlow para emitir atualizações
  - Validação de coordenadas
  - Thread-safe

#### UI
- `LocationHandler`: Handler para obtenção de localização com FusedLocationProviderClient
- `LocationExt`: Extensions para verificar se GPS está habilitado

**Conceitos**:
- Single source of truth: Um único repositório para localização
- Reactive updates: Qualquer componente pode observar mudanças de localização
- Separation of concerns: Lógica de localização isolada

**Dependências**: Nenhuma (módulo base)

---

## 📖 Guia para Rodar o Projeto

### Pré-requisitos

1. **Android Studio**: Versão Hedgehog (2023.1.1) ou superior
2. **JDK**: Java 17
3. **Android SDK**: API Level 34 (Android 14)
4. **Gradle**: 8.9 (incluído no wrapper)

### Configuração

#### 1. Clone o repositório
```bash
git clone <repository-url>
cd BillorTest
```

#### 2. Configure as credenciais do Mapbox

Crie ou edite o arquivo `local.properties` na raiz do projeto:
```properties
sdk.dir=/path/to/Android/sdk
MAPBOX_ACCESS_TOKEN=seu_token_aqui
```

Para obter um token do Mapbox:
- Acesse https://account.mapbox.com/
- Crie uma conta ou faça login
- Vá em "Access Tokens" e crie um novo token
- Copie o token e cole em `MAPBOX_ACCESS_TOKEN`

#### 3. Configure o Firebase

O projeto já possui o arquivo `google-services.json` configurado. Caso precise reconfigurar:
- Acesse https://console.firebase.google.com/
- Crie ou selecione um projeto
- Adicione um app Android com o package name: `com.jeandealmeida_dev.billortest`
- Baixe o `google-services.json` e coloque em `app/`

#### 4. Sync do Projeto
```bash
./gradlew build
```

Ou no Android Studio: `File > Sync Project with Gradle Files`

### Executando

#### Via Android Studio
1. Conecte um dispositivo físico ou inicie um emulador (recomendado: API 34+)
2. Selecione a configuração `app` no dropdown
3. Clique em `Run` (▶️) ou pressione `Shift + F10`

#### Via Linha de Comando
```bash
./gradlew installDebug
```

### Permissões Necessárias

O app solicitará as seguintes permissões em runtime:
- **ACCESS_FINE_LOCATION**: Para obter localização GPS precisa
- **RECORD_AUDIO**: Para gravar mensagens de áudio no chat
- **INTERNET**: Para conexão com Firebase e Mapbox

### Testando Funcionalidades

#### Navegação
1. Ao abrir o app, permita acesso à localização
2. O mapa será centralizado na sua posição atual
3. Toque e segure em qualquer ponto do mapa para criar uma rota
4. Use os controles de navegação para iniciar/pausar/parar

#### Chat
1. Toque no ícone de chat (FAB inferior direito)
2. Digite uma mensagem ou grave um áudio
3. Para gravar áudio: toque e segure o botão do microfone
4. Mensagens são sincronizadas em tempo real via Firestore

---

## 🔮 Próximos Passos (Next Steps)

### Funcionalidades Planejadas

1. **Autenticação de Usuários**
   - Login com Firebase Authentication
   - Suporte a múltiplas contas
   - Chat entre usuários diferentes

2. **Compartilhamento de Localização**
   - Compartilhar localização em tempo real no chat
   - Ver localização de outros usuários no mapa
   - Criar rotas para localizações compartilhadas

3. **Histórico de Navegação**
   - Salvar rotas percorridas
   - Replay de rotas antigas
   - Estatísticas (distância total, tempo, velocidade média)

4. **Melhorias no Chat**
   - Suporte a imagens
   - Reações a mensagens
   - Indicador de mensagens não lidas
   - Notificações push

5. **Offline Mode**
   - Download de mapas para uso offline
   - Queue de mensagens quando offline
   - Sincronização automática ao reconectar

### Melhorias Técnicas

1. **Testes**
   - Aumentar cobertura de testes unitários
   - Adicionar testes de integração
   - Testes de UI com Espresso

2. **Performance**
   - Implementar paginação no chat
   - Cache de tiles do mapa
   - Otimização de consultas do Firestore

3. **Acessibilidade**
   - Suporte completo a TalkBack
   - Contraste de cores para baixa visão
   - Tamanhos de fonte ajustáveis

4. **CI/CD**
   - Pipeline de build automático
   - Deploy automático para Play Store (Alpha/Beta)
   - Análise estática de código (Detekt, ktlint)

5. **Monitoramento**
   - Integração com Firebase Crashlytics
   - Analytics de uso de features
   - Performance monitoring

---

## 📄 Licença

Este projeto foi desenvolvido como parte de um teste técnico para a Billor.

---

## 👤 Autor

**Jean de Almeida**
- Email: jeandealmeida.dev@gmail.com
- GitHub: @jeandealmeida-dev
