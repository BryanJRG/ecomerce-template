package com.bjdev.ecomercebase.events;

/**
 * Generic lifecycle event for any admin-managed entity (catalog items, categories, brands,
 * discounts, and future ones) — replaces having a bespoke *CreatedEvent/*UpdatedEvent/*DeactivatedEvent
 * triplet per entity. Carries ids/values only (no entity refs), same reasoning as the old
 * ItemCreatedEvent: listeners re-fetch whatever state they need.
 * <p>
 * {@code entityType} is a stable uppercase key (e.g. "ITEM", "CATEGORY", "BRAND", "DISCOUNT") used
 * by generic listeners to decide what to evict from cache and what text to build for notifications —
 * see EntityCacheEvictionListener and EntityChangeNotificationListener. {@code actorId} lets the
 * notification listener skip notifying the admin who made the change themselves, same as
 * AdminInvitationNotificationListener does with its explicit actor parameter.
 */
public record EntityChangedEvent(String entityType, EntityAction action, Long entityId, String entityLabel,
                                  Long actorId) {
}
