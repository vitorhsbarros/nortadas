/**
 * Composition root — Spring {@code @Configuration} classes wiring infrastructure
 * adapters to application ports (DIP: this is the one place concrete
 * implementations are bound to interfaces; docs/architecture.md §5) plus
 * cross-cutting setup such as security.
 */
package com.nortadas.config;
