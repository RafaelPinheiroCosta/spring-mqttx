# spring-mqttx

`spring-mqttx` é uma biblioteca para integração MQTT em aplicações Spring Boot usando anotações para publicação e assinatura de mensagens.

A proposta é reduzir boilerplate sem esconder a base técnica do funcionamento MQTT. A biblioteca utiliza o Eclipse Paho para comunicação MQTT, Jackson para serialização de payloads e Spring Boot para auto-configuração, AOP e integração com o contexto da aplicação.

---

## Estrutura do projeto

- `spring-mqttx-core`: núcleo com serialização, despacho de mensagens e definição de subscriptions, sem dependência de Spring
- `spring-mqttx-starter`: módulo que a aplicação consumidora declara no `pom.xml`; reúne auto-configuração, anotações, AOP e integração com o contexto da aplicação

---

## Requisitos

- Java 17 ou superior
- Spring Boot 3.3+ ou 4.x
- Broker MQTT compatível com Eclipse Paho

---

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
    <version>1.1.0</version>
</dependency>
```

---

## Uso do script para instalar a dependência

Como a biblioteca está publicada no GitHub Packages, o Maven precisa se autenticar para conseguir baixar a dependência.

Em vez de editar manualmente o `settings.xml`, a biblioteca disponibiliza scripts que fazem isso de forma temporária, apenas durante o comando executado.

Esses scripts existem para:

- solicitar seu usuário do GitHub
- solicitar seu token do GitHub
- criar temporariamente um arquivo de autenticação do Maven
- executar o Maven
- remover automaticamente o arquivo temporário ao final

Isso evita deixar credenciais salvas permanentemente na máquina.

### Passo a passo

#### 1. Adicione primeiro o repositório e a dependência no `pom.xml`

Sem isso, o Maven não saberá de onde baixar a biblioteca.

#### 2. Gere um token no GitHub

Você precisa de um **Personal Access Token (classic)** com a permissão:

- `read:packages`

#### 3. Execute o script correspondente ao seu sistema operacional

Copie o script da biblioteca para a raiz do projeto consumidor:

- Windows: `use-github-packages.ps1`
- Linux/macOS: `use-github-packages.sh`

### Windows

```powershell
powershell -ExecutionPolicy Bypass -File use-github-packages.ps1 -- ".\mvnw.cmd clean compile"
```

### Linux/macOS

```bash
chmod +x use-github-packages.sh
./use-github-packages.sh ./mvnw clean compile
```

---

## Configuração

## application.yml

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
    protocol: TLS
    trust-store-location: classpath:certs/broker-truststore.p12
    trust-store-password: changeit
    trust-store-type: PKCS12

    trust-certificate-location: classpath:certs/emqxsl-ca.crt
    trust-certificate-format: AUTO

    key-store-location: classpath:certs/client-keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-password: changeit

  logs:
    registry: true
    subscription: true
    publish: true
    receive: true
    payload: false
    dispatch: true
    invocation: true
```

## application.properties

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
mqtt.ssl.protocol=TLS
mqtt.ssl.trust-store-location=classpath:certs/broker-truststore.p12
mqtt.ssl.trust-store-password=changeit
mqtt.ssl.trust-store-type=PKCS12

mqtt.ssl.trust-certificate-location=classpath:certs/emqxsl-ca.crt
mqtt.ssl.trust-certificate-format=AUTO

mqtt.ssl.key-store-location=classpath:certs/client-keystore.p12
mqtt.ssl.key-store-password=changeit
mqtt.ssl.key-store-type=PKCS12
mqtt.ssl.key-password=changeit

mqtt.logs.registry=true
mqtt.logs.subscription=true
mqtt.logs.publish=true
mqtt.logs.receive=true
mqtt.logs.payload=false
mqtt.logs.dispatch=true
mqtt.logs.invocation=true
```

---

## Configuração SSL/TLS

A biblioteca suporta **duas abordagens** para material de confiança:

### 1. Truststore tradicional

Exemplo:

```properties
mqtt.ssl.enabled=true
mqtt.ssl.trust-store-location=classpath:certs/broker-truststore.p12
mqtt.ssl.trust-store-password=changeit
mqtt.ssl.trust-store-type=PKCS12
```

### 2. Certificado CA cru

Agora também é possível usar diretamente:

- `.crt`
- `.cer`
- `.pem`
- `.der`

Exemplo:

```properties
mqtt.ssl.enabled=true
mqtt.ssl.trust-certificate-location=classpath:certs/emqxsl-ca.crt
mqtt.ssl.trust-certificate-format=AUTO
```

Formatos aceitos em `trust-certificate-format`:

- `AUTO`
- `PEM`
- `DER`

### Prioridade interna

A biblioteca resolve o material de confiança nesta ordem:

1. `mqtt.ssl.trust-certificate-location`
2. `mqtt.ssl.trust-store-location`
3. truststore padrão da JVM

---

## Propriedades disponíveis

| Propriedade | Descrição |
|---|---|
| `mqtt.enabled` | Ativa ou desativa a auto-configuração da biblioteca |
| `mqtt.broker-url` | URL de conexão do broker MQTT |
| `mqtt.client-id` | Client ID do cliente MQTT; quando omitido, a biblioteca gera um valor automaticamente |
| `mqtt.username` | Usuário do broker |
| `mqtt.password` | Senha do broker |
| `mqtt.clean-session` | Define o comportamento de sessão limpa |
| `mqtt.automatic-reconnect` | Ativa reconexão automática |
| `mqtt.keep-alive-interval` | Intervalo de keep alive em segundos |
| `mqtt.connection-timeout` | Tempo limite de conexão em segundos |
| `mqtt.default-qos` | QoS padrão usado pela aplicação |
| `mqtt.ssl.enabled` | Ativa configuração SSL/TLS |
| `mqtt.ssl.protocol` | Protocolo SSL/TLS a ser usado, por padrão `TLS` |
| `mqtt.ssl.trust-store-location` | Local do truststore (`classpath:` ou caminho absoluto) |
| `mqtt.ssl.trust-store-password` | Senha do truststore |
| `mqtt.ssl.trust-store-type` | Tipo do truststore, por padrão `PKCS12` |
| `mqtt.ssl.trust-certificate-location` | Local do certificado CA cru |
| `mqtt.ssl.trust-certificate-format` | Formato do certificado cru: `AUTO`, `PEM` ou `DER` |
| `mqtt.ssl.key-store-location` | Local do keystore de cliente |
| `mqtt.ssl.key-store-password` | Senha do keystore |
| `mqtt.ssl.key-store-type` | Tipo do keystore |
| `mqtt.ssl.key-password` | Senha da chave privada do keystore |

---

## API pública

## `@MqttPublisher`

Anotação aplicada em métodos cujo retorno deve ser publicado em um tópico MQTT.

```java
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
import org.springframework.stereotype.Service;

@Service
public class SensorPublisher {

    @MqttPublisher("sensores/status")
    public SensorStatus gerarStatus() {
        return new SensorStatus("ok", 22.5);
    }

    public record SensorStatus(String status, double temperatura) {}
}
```

Quando o método retorna um valor não nulo, o aspecto publica esse conteúdo no tópico configurado.

## `@MqttSubscriber`

Anotação aplicada em métodos que devem receber mensagens de um tópico MQTT.

```java
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import org.springframework.stereotype.Component;

@Component
public class SensorSubscriber {

    @MqttSubscriber("sensores/status")
    public void receber(@MqttPayload SensorStatus payload) {
        System.out.println(payload.status());
    }

    public record SensorStatus(String status, double temperatura) {}
}
```

## Wildcard MQTT

Agora a biblioteca suporta inscrições com:

- `+`
- `#`

Exemplos:

```java
@MqttSubscriber("devices/+/state")
public void receberEstado(String payload) {
    System.out.println(payload);
}
```

```java
@MqttSubscriber("devices/#")
public void receberTudo(String payload) {
    System.out.println(payload);
}
```

## Placeholders declarativos

A biblioteca também suporta placeholders em tópicos:

```java
@MqttSubscriber("devices/{deviceId}/state")
public void receber(@MqttPayload DeviceState payload) {
    System.out.println(payload);
}
```

Internamente, a biblioteca converte esse padrão para um filtro MQTT válido na inscrição do broker e usa o padrão original para fazer o matching do tópico recebido.

## `@MqttTopicParam`

A partir desta versão, é possível extrair variáveis do tópico diretamente nos parâmetros do método:

```java
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttTopicParam;
import org.springframework.stereotype.Component;

@Component
public class DeviceSubscriber {

    @MqttSubscriber("devices/{deviceId}/sensor/{sensorId}/state")
    public void receber(@MqttTopicParam("deviceId") String deviceId,
                        @MqttTopicParam("sensorId") Integer sensorId,
                        @MqttPayload DeviceState payload) {
        System.out.println(deviceId + " / " + sensorId + " -> " + payload);
    }

    public record DeviceState(String power, String mode, int r, int g, int b) {}
}
```

Tipos suportados para `@MqttTopicParam`:

- `String`
- `int` / `Integer`
- `long` / `Long`
- `boolean` / `Boolean`
- `double` / `Double`
- `float` / `Float`
- `short` / `Short`
- `byte` / `Byte`

## Resolução de argumentos em subscribers

A biblioteca resolve parâmetros de métodos anotados com `@MqttSubscriber` da seguinte forma:

- parâmetro anotado com `@MqttPayload`: desserializa o payload para o tipo declarado
- parâmetro anotado com `@MqttTopicParam("...")`: extrai o valor do placeholder correspondente no tópico recebido
- `String`: recebe o payload como texto UTF-8
- `byte[]`: recebe o payload bruto
- `MqttMessage`: recebe a mensagem do Eclipse Paho
- tipos não suportados: recebem `null`

## Como a biblioteca funciona

- O starter registra a auto-configuração se `mqtt.enabled=true`
- Um cliente MQTT é criado usando `MqttProperties`
- Métodos anotados com `@MqttSubscriber` são encontrados no contexto Spring e registrados no `MqttHandlerRegistry`
- O callback do cliente MQTT envia mensagens recebidas para o `MqttMessageDispatcher`
- O dispatcher encontra subscriptions compatíveis por:
    - tópico literal
    - wildcard MQTT
    - placeholder declarativo
- O `SpringMqttMethodHandler` resolve:
    - payload
    - parâmetros do tópico
    - tipos brutos
- Métodos anotados com `@MqttPublisher` publicam o retorno automaticamente por meio do `MqttPublishingGateway`

## Logs opcionais por grupo

A biblioteca expõe grupos de logs opcionais, todos desabilitados por padrão. Logs indispensáveis de ciclo de vida, conexão, falhas operacionais e reconexão continuam ativos em `INFO`, `WARN` ou `ERROR`.

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

- `mqtt.logs.registry`
- `mqtt.logs.subscription`
- `mqtt.logs.publish`
- `mqtt.logs.receive`
- `mqtt.logs.payload`
- `mqtt.logs.dispatch`
- `mqtt.logs.invocation`

## Teste rápido da biblioteca

Crie primeiro um projeto Spring Boot simples e adicione a dependência da biblioteca no `pom.xml`.

Depois disso, tenha um broker MQTT disponível. Para testes iniciais, pode usar Mosquitto ou EMQX.

Com o broker rodando, configure sua aplicação:

```properties
mqtt.enabled=true
mqtt.broker-url=tcp://localhost:1883
mqtt.client-id=teste-app
```

Agora crie uma classe de teste:

```java
@RestController
@RequestMapping("/mqtt")
public class TesteMqtt {

    @PostMapping("/publish")
    @MqttPublisher("sensores/rest/status")
    public SensorStatus gerarStatus() {
        return new SensorStatus("ok", 22.5);
    }

    @MqttSubscriber("sensores/mqtt/status")
    public void receber(@MqttPayload SensorStatus payload) {
        System.out.println(payload);
    }

    public record SensorStatus(String status, double temperatura) {}
}
```

### Testando a inscrição em tópico

#### Windows - CMD
```cmd
mosquitto_pub -h localhost -t sensores/mqtt/status -m "{\"status\":\"ok\",\"temperatura\":25.0}"
```

#### Windows - PowerShell
```powershell
mosquitto_pub -h localhost -t sensores/mqtt/status -m '{"status":"ok","temperatura":25.0}'
```

#### Linux
```bash
mosquitto_pub -h localhost -t sensores/mqtt/status -m '{"status":"ok","temperatura":25.0}'
```

### Testando a publicação MQTT

#### Windows
```cmd
mosquitto_sub -h localhost -t sensores/rest/status
```

#### Linux
```bash
mosquitto_sub -h localhost -t sensores/rest/status
```

Depois faça um `POST` para:

```text
http://localhost:8080/mqtt/publish
```

## Teste rápido com placeholder

```java
@Component
public class DeviceStateSubscriber {

    @MqttSubscriber("devices/{deviceId}/state")
    public void receber(@MqttTopicParam("deviceId") String deviceId,
                        @MqttPayload DeviceState payload) {
        System.out.println(deviceId + " -> " + payload);
    }

    public record DeviceState(String power, String mode, int r, int g, int b) {}
}
```

Publique uma mensagem em:

```text
devices/iot01/state
```

e a aplicação deverá resolver:

- `deviceId = "iot01"`
- payload desserializado normalmente

## GitHub Actions

O workflow em `.github/workflows/publish.yml` executa validação em matriz para combinações de Java e Spring Boot, e na criação de uma release no GitHub faz o deploy para o GitHub Packages.

## Testes incluídos

O projeto possui testes automatizados para:

- serialização de payload
- dispatch de mensagens
- publicação condicional com verificação de conexão
- invocação de handlers Spring
- carregamento da auto-configuração
- matching de tópicos com wildcard e placeholder
- injeção de parâmetros com `@MqttTopicParam`
- construção de material de confiança SSL/TLS

## Conclusão

A proposta da biblioteca é simplificar a integração MQTT em aplicações Spring Boot sem esconder a base técnica usada internamente.

Ela reduz código repetitivo, organiza a integração e evolui o suporte declarativo para cenários mais próximos de produção, mas não substitui testes de integração com broker real, validação de payloads, cenários de reconexão e testes com o ambiente específico da aplicação.

## Licença

Este projeto está licenciado sob a licença MIT.
