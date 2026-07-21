/**
 * Application use cases (Facade pattern) — one coordinating class per
 * client-facing operation, e.g. {@code GetBeachListUseCase},
 * {@code GetBeachDetailUseCase}, {@code DetectNortadaUseCase} (US009–US012).
 *
 * <p>Use cases orchestrate domain objects and depend only on the domain layer
 * and on {@code com.nortadas.application.port} interfaces — never on
 * {@code infrastructure} or {@code web} directly (DIP; docs/architecture.md §1, §5).
 * Empty at US007 scaffolding stage by design.
 */
package com.nortadas.application.usecase;
