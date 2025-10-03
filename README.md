# Java 17 vs Java 21 vs Java 25 (LTS) — Guía técnica y de adopción

Recomendación principal: Para cargas intensivas en I/O, alta concurrencia y modernización de plataformas, migrar directamente a Java 25 (LTS) ofrece el mejor balance entre performance, productividad y soporte, manteniendo compatibilidad con stacks modernos (Spring Boot 3.5/4.0) y habilitando mejoras del runtime como Compact Object Headers, Scoped Values y Shenandoah generacional.[^1][^2][^3][^4]

***

## 📋 Agenda

1. Contexto: ¿Por qué Java 21 y por qué ahora Java 25?
2. Virtual Threads: Revolución en concurrencia (Java 21)
3. Pattern Matching y Record Patterns: Código más expresivo (Java 21)
4. Sequenced Collections: APIs más consistentes (Java 21)
5. Novedades clave de Java 25: lenguaje, runtime y observabilidad
6. Comparativa técnica Java 17 vs 21 vs 25
7. Demo comparativa de performance (escenario)
8. Estrategia de adopción y compatibilidad en ecosistema (Spring, Cloud)
9. Consideraciones, riesgos y mejores prácticas
10. Conclusiones y próximos pasos

***

## 🎯 ¿Por qué Java 21 y ahora Java 25?

- Java 21 consolidó cambios de productividad y concurrencia (Virtual Threads, Pattern Matching para switch, Record Patterns, Sequenced Collections) con soporte LTS hasta 2029.[^1]
- Java 25 es el siguiente LTS (GA 16-Sep-2025), con foco en rendimiento, ergonomía de lenguaje y observabilidad. Recibe soporte extendido por múltiples vendors, incluyendo builds GA de OpenJDK y Oracle, y soporte LTS de Oracle por al menos 8 años.[^5][^6][^7][^1]
- Java 25 integra 18 JEPs finales/preview/incubator, con impacto directo en:
    - Productividad: Compact Source Files e Instance Main; Module Import Declarations; Flexible Constructor Bodies.[^8][^9][^2][^10][^11][^12]
    - Concurrencia/carácter Loom: Scoped Values (final), Structured Concurrency (5º preview).[^13][^8]
    - Performance/GC: Compact Object Headers (final), Generational Shenandoah (final), ergonomía AOT y profiling JFR/AOT.[^9][^2][^14][^8]
    - Seguridad/cripto: Key Derivation Function API; PEM encodings (preview).[^6][^10][^8]

OpenJDK JDK 25 GA y documentación oficial disponibles.[^7][^15][^16][^1]

***

## 🚀 Virtual Threads — El Game Changer (Java 21)

Problema tradicional

```
Threads de SO costosos (~1 MB c/u), límites prácticos ~10–30k, bloqueos I/O caros.
```

Solución: Virtual Threads (Project Loom)

```
Threads ligeros (user-mode), memoria baja (KBs), millones de concurrencias, mismo modelo síncrono.
```

Impacto en banca/finanzas

- APIs de alto throughput (pagos, consultas de fraude), menor coste por request.
- Programación síncrona simple, sin complejidad reactiva.
- Mejor utilización de CPU y reducción de ociosidad por I/O.

Nota: Virtual Threads brillan en I/O-bound, no aceleran CPU-bound por sí solos; medir siempre[—].

Ejemplo comparativo (tu contenido existente): válido y aplicable en 21+.

***

## 🎨 Pattern Matching para switch (Java 21)

Antes (Java 17) vs después (Java 21): tu ejemplo es correcto, con ventajas en reducción de verbosidad, exhaustividad de casos, tratamiento de null.

Guarded Patterns: clarifica reglas de fraude con condiciones en línea; tu ejemplo es idiomático para 21+.

***

## 📚 Sequenced Collections (Java 21)

Unificación de operaciones de “primero/último/reversed” en List/Deque/Map a través de interfaces sequenced. Mejora coherencia API y legibilidad.

***

## 🎯 Record Patterns y destructuración (Java 21)

Permite “desempaquetar” records y anidarlos en patrones, mejorando validaciones y enrutamiento de eventos con menos ruido.

***

## 🛠️ Javadoc Snippets y 🌐 Simple Web Server (Java 21)

- Snippets con validación y highlighting integrados en Javadoc.
- SimpleFileServer para mocks y prototipos rápidos de forma estándar.

***

## 🆕 Java 25: novedades clave que suman a 21

Lenguaje y productividad

- Flexible Constructor Bodies (JEP 513): validación/cálculo de argumentos antes de super()/this(), sin tocar this prematuramente; prologue/epilogue formalizados. Útil para evitar trabajo en jerarquías profundas ante datos inválidos (fail-fast más barato).[^11][^17][^12][^18][^19]
- Compact Source Files e Instance Main Methods (JEP 512): facilitar prototipos, scripts, enseñanza; main de instancia y fuentes compactas.[^2][^10][^8][^9]
- Module Import Declarations (JEP 511): “import module …” para importar APIs exportadas de módulos con más claridad local.[^10][^8]
- Primitive types en patterns/instanceof/switch (3er preview, JEP 507): patrones con tipos primitivos; expresividad y consistencia del modelo de pattern matching.[^8][^10]

Concurrencia (Loom)

- Scoped Values (JEP 506, final): paso de contexto inmutable por árbol de llamadas y a subtareas (ideal con virtual threads), alternativa moderna a ThreadLocal en muchos casos. Ejemplos oficiales muestran logging con Request-ID propagado limpiamente.[^20][^21][^13][^8]
- Structured Concurrency (JEP 505, 5º preview): composición segura y cancelación/propagación de errores entre subtareas; madura y lista para adopción dirigida.[^20][^8]

Rendimiento, GC y arranque

- Compact Object Headers (JEP 519, final): encabezados de objeto de 12→8 bytes; menor huella, mejor locality/cache; activar con -XX:+UseCompactObjectHeaders. Beneficios notorios con muchos objetos pequeños; parte del Project Lilliput.[^9][^2]
- Generational Shenandoah (JEP 521, final): modo generacional para Shenandoah; menos pausas y mejor throughput en workloads con muchos objetos de corta vida.[^2][^9]
- AOT ergonomics y method profiling (JEP 514/515): mejora arranque/warm-up usando perfiles previos; útil en microservicios/serverless para cold-starts predecibles.[^8][^9][^2]
- JFR: CPU-time profiling (exp, JEP 509), cooperative sampling (JEP 518), method timing \& tracing (JEP 520) para observabilidad con menor overhead y granularidad fina.[^14][^2][^8]

Cripto y plataforma

- Key Derivation Function API (JEP 510): API estandar para KDFs.[^8]
- PEM encodings (JEP 470, preview): soporte directo a objetos criptográficos en PEM.[^8]
- Vector API (JEP 508, 10ª incubación): SIMD portable, útil para cargas numéricas.[^8]
- Remoción del port x86 32-bit (JEP 503): solo 64-bit.[^14][^8]

GA, builds y notas

- JDK 25 GA, builds oficiales y release notes consolidadas disponibles.[^15][^16][^7][^1]

***

## 🧪 Comparativa técnica: Java 17 vs 21 vs 25

Compatibilidad y releases

- 17: LTS 2021; baseline moderno.
- 21: LTS 2023; Virtual Threads, Pattern Matching, Record Patterns, Sequenced Collections.
- 25: LTS 2025; Scoped Values, Flexible Constructors, Compact Headers, Shenandoah generacional, ergonomía AOT, mejoras JFR.[^16][^15][^1][^9][^2][^8]

Performance y runtime

- 21 ya aporta mejoras de GC y concurrencia; vendors reportan gains en latencia/throughput frente a 17, con efectos notables en sistemas de alta concurrencia.[^22][^23]
- 25 añade optimizaciones estructurales: headers compactos, GC generacional (Shenandoah), perfiles AOT/JFR; mejora memoria efectiva y warm-up en entornos cloud/microservicios.[^9][^2][^14][^8]

Ecosistema y frameworks

- Spring Boot 3.5 soporta Java 17–25. Spring Boot 4 recomienda JDK 25; Spring Framework 7 se alinea con JDKs modernos y Jakarta EE 11.[^24][^3][^4][^25][^26]

Seguridad/compliance

- KDF API y PEM encodings facilitan estandarización cripto; notas de migración y compatibilidad disponibles en guías oficiales de Oracle.[^27][^28][^16][^8]

***

## 📊 Demo: Comparativa de Performance (escenario)

Escenario: Procesamiento de 100,000 transacciones con mezcla I/O (HTTP externo 100 ms), reglas de fraude y persistencia.

Métricas a comparar

- Tiempo total de ejecución
- Consumo de memoria
- Throughput (tx/s)
- Escalabilidad de concurrencia

Expectativas razonables de tendencia

- Java 17 (threads de plataforma): limitado por bloqueos I/O y stacks; mayor huella por thread.
- Java 21 (virtual threads): mejor escalado en I/O-bound con modelo síncrono; reducción de memoria y mayor throughput.
- Java 25: similar modelo de concurrencia a 21, pero con huella menor por Compact Object Headers y pausas menores con Shenandoah generacional en casos aplicables; startup/warm-up más predecible con AOT ergonomics y method profiling; observabilidad JFR más rica para tuning.[^29][^2][^14][^9][^8]

Nota: Los números exactos dependen de workload y JDK/distribución. Validar con benchs propios y JFR para decisiones de tuning.[^29][^2][^8]

***

## 🎬 Demo en vivo: Código real (propuesta)

1) Servicio de validación de transacciones

- Versión Java 17 (threads tradicionales)
- Versión Java 21/25 (virtual threads)

2) Análisis con Pattern Matching y guarded patterns (Java 21+)
3) Medición

- JMeter/k6 para carga
- JFR: method timing \& tracing y cooperative sampling (25) para perfiles finos[^2][^14][^8]
- VisualVM/async-profiler según necesidad

Repositorio sugerido: Java 21/25 Demo Bancolombia con perfiles JFR y scripts de carga.

***

## ✅ Casos de uso en banca

Fraude/AML

- Paralelizar validaciones, consultas a fuentes externas y scoring con virtual threads y structured concurrency; propagación de contexto con scoped values (correlación, request-id) sin ThreadLocal.[^13][^20]

APIs de alto throughput

- Endpoints síncronos I/O-bound, mayor densidad por pod; observabilidad JFR para latencias y colas de I/O.[^14][^2][^8]

Batch y reporting

- Menor huella efectiva por objeto (headers compactos), GC más predecible (ZGC/Shenandoah generacional) y cálculos vectoriales puntuales con Vector API cuando aplique.[^9][^8]

Cripto/compliance

- KDF API y PEM simplifican integraciones con HSMs y almacenes, alineando formatos y flujos.[^8]

***

## 🚦 Estrategia de adopción (17 → 21 → 25)

Fase 1: Evaluación (4–8 semanas)

- POCs con casos críticos I/O-bound, fraude, conciliaciones.
- Validar compatibilidad frameworks: Spring Boot 3.5/4.0, Spring Framework 6.2/7, Hibernate 6.2+, drivers.[^3][^4][^24]
- Elegir GC según perfil (G1/ZGC/Shenandoah generacional en 25).[^2][^9]
- Capacitación: Virtual Threads, Scoped Values, Pattern Matching, JFR 25.[^20][^13][^14][^8]

Fase 2: Piloto (8–12 semanas)

- Migrar 1–3 microservicios no críticos.
- Activar -XX:+UseCompactObjectHeaders en 25 y medir memoria.[^9]
- Evaluar AOT ergonomics/profiling para cold-start si aplica.[^2][^9][^8]
- Perf/obs: JFR method timing \& tracing; cooperative sampling.[^14][^2][^8]

Fase 3: Rollout gradual (6–12 meses)

- Priorizar dominios de alta concurrencia y I/O.
- Documentar mejores prácticas y flags por servicio.
- Plan de actualización de vendors/JDKs y pipeline CI/CD con matrices (17/21/25).

***

## ⚠️ Consideraciones importantes

Compatibilidad

- Spring Boot 3.5 soporta 17–25; Boot 4 recomienda 25. Verificar librerías que dependan de ThreadLocal, blocking I/O y drivers JDBC bajo virtual threads.[^4][^24][^3]
- Eliminado x86 32-bit en 25 (solo 64-bit).[^14][^8]

Performance

- Virtual threads no aceleran CPU-bound; usar paralelismo controlado y vectorización cuando aplique.[^10][^8]
- Shenandoah generacional y headers compactos benefician heaps grandes y objetos pequeños; medir pausas y throughput.[^9][^2]
- No asumir ganancias; basar decisiones en JFR y benchs internos.[^2][^14][^8]

Testing

- Agregar pruebas de concurrencia y resiliencia (timeouts, cancelación estructurada).
- Validar cambios de locale/data/time y APIs removidas/dep.[^16][^27]

Migración

- Guías oficiales de migración y análisis de esfuerzo disponibles; correr en JDK 25 antes de recompilar, luego recompilar y subir release target; revisar flags obsoletos.[^28][^27]

***

## 🆚 Tabla rápida: Java 17 vs 21 vs 25

| Aspecto | Java 17 | Java 21 | Java 25 |
| :-- | :-- | :-- | :-- |
| Soporte | LTS (2021) | LTS (2023) | LTS (2025) |
| Concurrencia | Threads SO | Virtual Threads (final) | Virtual Threads + Scoped Values (final); Structured Concurrency (preview) |
| Lenguaje | Records estables | Pattern Matching switch, Record Patterns, Sequenced Collections | Flexible Constructor Bodies; Compact Source Files; Module Import; Patterns con primitivos (preview) |
| Performance/GC | G1/ZGC | Mejoras ZGC/G1 | Compact Object Headers; Generational Shenandoah; AOT ergonomics/profiling; JFR mejoras |
| Observabilidad | JFR base | JFR mejoras graduales | JFR CPU-time profiling (exp), cooperative sampling, method timing \& tracing |
| Seguridad/Cripto | — | — | KDF API; PEM encodings (preview) |
| Ecosistema | Soporte amplio | Soporte amplio | Spring Boot 3.5 compatible; Boot 4 recomendado en 25 |


***

## 💼 Valor de negocio

Beneficios técnicos esperables

- Mejor densidad por nodo y reducción de latencias en I/O-bound por virtual threads.
- Menor huella con headers compactos y pausas más predecibles con Shenandoah generacional (si se adopta).[^9][^2]
- Productividad: menos “boilerplate” (constructores, patterns, fuentes compactas), mejor observabilidad con JFR.[^14][^2][^8][^9]

Impacto económico

- Migrar de 17 a 21/25 puede reducir costos de cómputo por mayor throughput y menor huella, especialmente en cloud. Validar con benchmarks propios y JFR.[^23][^22][^29]

***

## 🎯 Conclusiones

¿Migrar a Java 21 o directamente a Java 25?

- Sí, si:
    - Servicios con alta concurrencia I/O-bound, necesidad de throughput y reducción de coste.
    - Se busca simplificar asincronía con un modelo síncrono mantenible (virtual threads) y contextualización limpia (scoped values).[^13]
    - Se quiere mejorar startup/warm-up y observabilidad en producción con AOT/JFR.[^2][^14][^8]
- Aplazar, si:
    - Dependencias críticas aún no soportan 21/25.
    - Workloads estrictamente CPU-bound sin beneficios claros de Loom; evaluar Vector API y perfilamiento previo.[^10][^8]

Recomendación para Bancolombia

- Iniciar migración gradual a Java 25 priorizando servicios de fraude, APIs de alto tráfico y procesos batch con alta cardinalidad de objetos.
- Adoptar Scoped Values donde hoy se usa ThreadLocal de forma contextual, y evaluar Shenandoah generacional/Compact Object Headers en servicios de gran heap.[^13][^8][^9][^2]

***

## 📞 Próximos pasos

Acción inmediata

1. Conformar equipo de evaluación 25 (arquitectura, performance, seguridad).
2. Seleccionar 2–3 candidatos I/O-bound para POCs (fraude, pagos, conciliaciones).
3. Definir métricas de éxito (p99, throughput, memoria, coste por request).
4. Plan de capacitación: Virtual Threads, Scoped Values, JFR 25, GC tuning.[^20][^13][^14][^8]

Seguimiento

- Workshop deep-dive Loom (Virtual Threads, Structured Concurrency, Scoped Values).
- Code review y hardening de patrones con switch/record patterns.
- Performance testing con JFR method timing \& tracing y cooperative sampling; validar AOT ergonomics/profiling para cold-starts.[^14][^8][^2]

Contacto

- Confluence: guía de migración 17→21→25, flags y checklists.
- Canal interno: “Java Moderno 25” para soporte continuo.

***

## 📎 Anexos

Recursos oficiales

- JDK 25 (OpenJDK): características, GA, cronograma.[^1]
- Builds GA OpenJDK 25.[^7]
- Release notes consolidadas JDK 25.[^15][^16]
- Guía de preparación/migración Oracle para JDK 25.[^27][^28]

Novedades destacadas Java 25

- Lista de JEPs y cobertura de features: lenguaje, GC, JFR, AOT.[^10][^8][^9][^2][^14]
- Scoped Values (JEP 506) — especificación y ejemplos.[^30][^21][^13]
- Flexible Constructor Bodies (JEP 513) — especificación y docs.[^17][^12][^11]

Ecosistema

- Spring Boot 3.5 compatibilidad 17–25; documentación oficial de requisitos.[^3][^4]
- Recomendación de JDK 25 en Spring Boot 4/Framework 7.[^25][^26][^24]

Notas

- x86 32-bit removido en JDK 25 (JEP 503).[^8][^14]
- Activación de Compact Object Headers: -XX:+UseCompactObjectHeaders.[^9][^2]
<span style="display:none">[^31][^32][^33][^34][^35][^36][^37][^38][^39][^40][^41][^42][^43][^44][^45][^46][^47][^48][^49]</span>

<div align="center">⁂</div>

[^1]: https://openjdk.org/projects/jdk/25/

[^2]: https://blog.jetbrains.com/idea/2025/09/java-25-lts-and-intellij-idea/

[^3]: https://endoflife.date/spring-boot

[^4]: https://docs.spring.io/spring-boot/system-requirements.html

[^5]: https://www.oracle.com/latam/news/announcement/oracle-releases-java-25-2025-09-16/

[^6]: https://www.oracle.com/news/announcement/oracle-releases-java-25-2025-09-16/

[^7]: https://jdk.java.net/25/

[^8]: https://www.infoq.com/news/2025/09/java25-released/

[^9]: https://www.happycoders.eu/java/java-25-features/

[^10]: https://www.jrebel.com/blog/java-25

[^11]: https://docs.oracle.com/en/java/javase/25/language/flexible-constructor-bodies.html

[^12]: https://openjdk.org/jeps/513

[^13]: https://openjdk.org/jeps/506

[^14]: https://hanno.codes/2025/09/16/heres-java-25/

[^15]: https://www.oracle.com/java/technologies/javase/25all-relnotes.html

[^16]: https://www.oracle.com/java/technologies/javase/25-relnote-issues.html

[^17]: https://www.happycoders.eu/java/flexible-constructor-bodies/

[^18]: https://www.linkedin.com/posts/surinderkmehra_java25-jep513-javaperformance-activity-7367533229400084481-IBSr

[^19]: https://www.baeldung.com/java-25-flexible-constructor-bodies

[^20]: https://softwaremill.com/structured-concurrency-and-scoped-values-in-java/

[^21]: https://download.java.net/java/early_access/loom/docs/api/java.base/java/lang/ScopedValue.html

[^22]: https://www.linkedin.com/posts/brunoss_jvm-performance-and-cost-differences-java-activity-7339689619958947840-NtH_

[^23]: https://www.azul.com/blog/benchmarks-show-faster-java-performance-improvement/

[^24]: https://loiane.com/2025/08/spring-boot-4-spring-framework-7-key-features/

[^25]: https://github.com/spring-projects/spring-boot/issues/47245

[^26]: https://spring.io/blog/2024/10/01/from-spring-framework-6-2-to-7-0

[^27]: https://docs.oracle.com/en/java/javase/25/migrate/preparing-migration.html

[^28]: https://docs.oracle.com/en/java/javase/25/migrate/jdk-migration-guide.pdf

[^29]: https://inside.java/2025/09/05/roadto25-performance/

[^30]: https://www.happycoders.eu/java/scoped-values/

[^31]: https://openjdk.org/projects/jdk-updates/

[^32]: https://www.sqli.com/int-en/insights-news/blog/java-25

[^33]: https://github.com/openjdk/jdk25u

[^34]: https://www.youtube.com/watch?v=lCNNA1erCfk

[^35]: https://openjdk.org

[^36]: https://www.baeldung.com/java-25-features

[^37]: https://www.oracle.com/in/news/announcement/oracle-releases-java-25-2025-09-16/

[^38]: https://javatechonline.com/java-25-new-features-with-examples/

[^39]: https://www.reddit.com/r/java/comments/1jzd8df/scoped_values_final_in_jdk_25/

[^40]: https://javalaunchpad.com/flexible-constructor-bodies-in-java-25/

[^41]: https://www.javacodegeeks.com/flexible-constructor-bodies-in-java-25.html

[^42]: https://katyella.com/blog/complete-guide-java-8-to-17-migration/

[^43]: https://www.reddit.com/r/java/comments/1akcwyg/jvm_performance_comparison_for_jdk_21/

[^44]: https://www.youtube.com/watch?v=9azNjz7s1Ck

[^45]: https://www.azul.com/blog/a-guide-to-easy-java-migration-to-azul/

[^46]: https://www.reddit.com/r/java/comments/1mohrs7/preparing_for_spring_boot_4_and_spring_framework/

[^47]: https://www.linkedin.com/posts/taapti-technologies_new-features-in-java-25-lts-with-practical-activity-7373958541818290176-0O22

[^48]: https://jdkcomparison.com

[^49]: https://dev.to/viksingh/a-guide-to-java-migration-and-modernization-46af