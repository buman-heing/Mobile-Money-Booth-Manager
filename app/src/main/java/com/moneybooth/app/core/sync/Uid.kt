package com.moneybooth.app.core.sync

import java.util.UUID

/**
 * Every synced row carries a device-independent id alongside its local auto-increment key,
 * because two phones (booth and owner) each generate their own local ids and those collide.
 */
fun newUid(): String = UUID.randomUUID().toString()
