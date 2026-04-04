# spring-mqttx

`spring-mqttx` é uma biblioteca para integração MQTT em aplicações Spring Boot usando anotações para publicação e assinatura de mensagens.

## Estrutura do projeto

- `spring-mqttx-core`: núcleo com serialização, dispatch de mensagens e gateway de publicação, sem dependência de Spring.
- `spring-mqttx-starter`: módulo que a aplicação consumidora declara no `pom.xml`. Ele reúne auto-configuração, anotações, AOP e integração com Spring Boot.

## Requisitos

- Java 17 ou superior
- Spring Boot 3.3+ ou 4.x
- Broker MQTT compatível com Eclipse Paho
- 

## Dependência

Adicione o repositório GitHub Packages e o starter da biblioteca no projeto consumidor.

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/RafaelPinheiroCosta/spring-mqttx</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.rafaelcosta</groupId>
    <artifactId>spring-mqttx-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Uso do script em anexo para instalar a dependência

Como a biblioteca está publicada no **GitHub Packages**, o Maven precisa se autenticar para conseguir **baixar** a dependência.

Em vez de pedir que o usuário edite manualmente o `settings.xml` do Maven, a biblioteca disponibiliza scripts que fazem isso de forma **temporária**, apenas durante o comando executado.

Esses scripts existem para:

1. solicitar seu usuário do GitHub;
2. solicitar seu token do GitHub;
3. criar temporariamente um arquivo de autenticação do Maven;
4. executar o comando Maven para baixar a biblioteca e continuar o build;
5. remover automaticamente o arquivo temporário ao final.

Isso evita deixar credenciais salvas permanentemente na máquina, o que é especialmente importante em computadores compartilhados.

## Passo a passo para baixar a biblioteca com o script

### 1. Adicione primeiro o repositório e a dependência no `pom.xml`

Antes de executar qualquer script, o seu projeto já deve conter:

- o bloco `<repositories>` apontando para o GitHub Packages;
- a dependência `spring-mqttx-starter`.

Sem isso, o Maven não saberá de onde baixar a biblioteca.

### 2. Gere um token no GitHub

Você precisa de um **Personal Access Token (classic)** para o GitHub Packages.

Caminho geral no GitHub:

- **Settings**
- **Developer settings**
- **Personal access tokens**
- **Tokens (classic)**
- **Generate new token (classic)**

Permissão mínima recomendada para o uso da biblioteca:

- `read:packages`

Se o repositório que publica a biblioteca for privado, o seu usuário também precisa ter acesso a ele.

### 3. Execute o script correspondente ao seu sistema operacional

Copie o arquivo que esta no seguinte link e cole na raiz do seu projeto:
windows: https://github.com/RafaelPinheiroCosta/spring-mqttx/blob/master/use-github-packages.ps1
linux/macOS: https://github.com/RafaelPinheiroCosta/spring-mqttx/blob/master/use-github-packages.sh

O script irá:

- pedir seu usuário do GitHub;
- pedir seu token;
- gerar um arquivo temporário de autenticação;
- executar o Maven;
- apagar esse arquivo automaticamente ao final.

### Windows

Exemplo para baixar dependências e compilar o projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\use-github-packages.ps1 -- .\mvnw.cmd clean compile
```

### Linux/macOS

Dê permissão de execução uma vez:

```bash
chmod +x scripts/use-github-packages.sh
```

Depois execute, por exemplo, para compilar:

```bash
./scripts/use-github-packages.sh ./mvnw clean compile
```

## Configuração

### application.yml

```yaml
mqtt:
  enabled: true
  broker-url: tcp://localhost:1883
  client-id: minha-aplicacao
  username: usuario
  password: senha
  clean-session: true
  automatic-reconnect: true
  keep-alive-interval: 60
  connection-timeout: 30
  default-qos: 1
  ssl:
    enabled: false
    trust-store-location: classpath:certs/broker-truststore.p12
    trust-store-password: changeit
    trust-store-type: PKCS12
```

### application.properties

```properties
mqtt.enabled=true
mqtt.broker-url=tcp://localhost:1883
mqtt.client-id=minha-aplicacao
mqtt.username=usuario
mqtt.password=senha
mqtt.clean-session=true
mqtt.automatic-reconnect=true
mqtt.keep-alive-interval=60
mqtt.connection-timeout=30
mqtt.default-qos=1
mqtt.ssl.enabled=false
mqtt.ssl.trust-store-location=classpath:certs/broker-truststore.p12
mqtt.ssl.trust-store-password=changeit
mqtt.ssl.trust-store-type=PKCS12
```

### Propriedades disponíveis

| Propriedade | Descrição |
|---|---|
| `mqtt.enabled` | Ativa ou desativa a auto-configuração da biblioteca. |
| `mqtt.broker-url` | URL de conexão do broker MQTT. |
| `mqtt.client-id` | Client ID do cliente MQTT. Quando omitido, a biblioteca gera um valor automaticamente. |
| `mqtt.username` | Usuário do broker. |
| `mqtt.password` | Senha do broker. |
| `mqtt.clean-session` | Define o comportamento de sessão limpa. |
| `mqtt.automatic-reconnect` | Ativa reconexão automática no cliente MQTT. |
| `mqtt.keep-alive-interval` | Intervalo de keep alive em segundos. |
| `mqtt.connection-timeout` | Tempo limite de conexão em segundos. |
| `mqtt.default-qos` | QoS padrão usado pela aplicação quando fizer sentido para a estratégia adotada. |
| `mqtt.ssl.enabled` | Ativa configuração SSL/TLS para o cliente MQTT. |
| `mqtt.ssl.trust-store-location` | Local do truststore. Pode ser `classpath:` ou caminho absoluto. |
| `mqtt.ssl.trust-store-password` | Senha do truststore. |
| `mqtt.ssl.trust-store-type` | Tipo do truststore, por padrão `PKCS12`. |

## API pública

### `@MqttPublisher`

Anotação aplicada em métodos cuja resposta deve ser publicada em um tópico MQTT.

```java
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
import org.springframework.stereotype.Service;

@Service
public class SensorPublisher {

    @MqttPublisher(value = "sensores/status", qos = 1)
    public SensorStatus gerarStatus() {
        return new SensorStatus("ok", 22.5);
    }

    public record SensorStatus(String status, double temperatura) {}
}
```

Quando o método retorna um valor não nulo, o aspecto publica esse conteúdo no tópico configurado.

### `@MqttSubscriber`

Anotação aplicada em métodos que devem receber mensagens de um tópico MQTT.

```java
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import org.springframework.stereotype.Component;

@Component
public class SensorSubscriber {

    @MqttSubscriber(value = "sensores/status", qos = 1)
    public void receber(@MqttPayload SensorStatus payload) {
        System.out.println(payload.status());
    }

    public record SensorStatus(String status, double temperatura) {}
}
```

## Resolução de argumentos em subscribers

A biblioteca resolve parâmetros de métodos anotados com `@MqttSubscriber` da seguinte forma:

- parâmetro anotado com `@MqttPayload`: desserializa o payload para o tipo declarado;
- `String`: recebe o payload como texto UTF-8;
- `byte[]`: recebe o payload bruto;
- `MqttMessage`: recebe a mensagem do Eclipse Paho;
- outros tipos não suportados: recebem `null`.

## Como a biblioteca funciona

1. O starter registra a auto-configuração se `mqtt.enabled=true`.
2. Um cliente MQTT é criado usando `MqttProperties`.
3. Métodos anotados com `@MqttSubscriber` são encontrados no contexto Spring e registrados no `MqttHandlerRegistry`.
4. O callback do cliente MQTT envia mensagens recebidas para o `MqttMessageDispatcher`.
5. Métodos anotados com `@MqttPublisher` publicam o retorno automaticamente por meio do `MqttPublishingGateway`.

## GitHub Actions

O workflow em `.github/workflows/publish.yml` executa validação em matriz para:

- Java 17 com Spring Boot 3.5.6
- Java 21 com Spring Boot 3.5.6
- Java 17 com Spring Boot 4.0.5
- Java 25 com Spring Boot 4.0.5

Na criação de uma release no GitHub, o mesmo workflow também faz o `deploy` para o GitHub Packages.

## Testes incluídos

O projeto já possui testes automatizados para os componentes centrais da biblioteca:

- serialização de payload;
- dispatch de mensagens;
- publicação condicional com verificação de conexão;
- invocação de handlers Spring;
- carregamento da auto-configuração.

## Conclusão

A proposta da biblioteca é simplificar a integração MQTT em aplicações Spring Boot sem esconder a base técnica usada internamente.

Ela se apoia no **Eclipse Paho** para a comunicação MQTT e no **Jackson** para conversão de payloads, enquanto o Spring Boot fornece a auto-configuração, AOP e a integração com o contexto da aplicação.

Mesmo com essa abstração, o uso em produção exige validação técnica. É importante testar:

- conexão com o broker;
- serialização e desserialização dos payloads;
- recebimento correto em subscribers;
- publicação correta em publishers;
- comportamento de reconexão;
- compatibilidade com a versão de Java e Spring Boot adotada no projeto.

Em outras palavras, a biblioteca reduz código repetitivo e organiza a integração, mas não substitui testes de integração, testes com broker real e validação do cenário específico da aplicação.

## Licença

Este projeto está licenciado sob a licença MIT. Consulte o arquivo `LICENSE`.


## Logs opcionais por grupo

A biblioteca expõe grupos de logs opcionais, todos desabilitados por padrão. Os logs indispensáveis de ciclo de vida, conexão, falhas operacionais e reconexão continuam ativos em `INFO`, `WARN` ou `ERROR`.

Exemplo em `application.properties`:

```properties
mqtt.logs.registry=true
mqtt.logs.subscription=true
mqtt.logs.publish=true
mqtt.logs.receive=true
mqtt.logs.payload=false
mqtt.logs.dispatch=true
mqtt.logs.invocation=true
```

Exemplo em `application.yml`:

```yaml
mqtt:
  logs:
    registry: true
    subscription: true
    publish: true
    receive: true
    payload: false
    dispatch: true
    invocation: true
```

Grupos disponíveis:

- `mqtt.logs.registry`: descoberta e registro de métodos `@MqttSubscriber` e `@MqttPublisher`
- `mqtt.logs.subscription`: inscrições em tópicos e configuração do callback
- `mqtt.logs.publish`: interceptação de publishers, publicação e confirmação de entrega
- `mqtt.logs.receive`: recebimento de mensagens no callback MQTT
- `mqtt.logs.payload`: preview do conteúdo do payload em `TRACE`
- `mqtt.logs.dispatch`: despacho das mensagens para handlers
- `mqtt.logs.invocation`: resolução de argumentos e invocação dos métodos anotados
