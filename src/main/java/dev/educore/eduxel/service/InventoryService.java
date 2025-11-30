package dev.educore.eduxel.service;

import dev.educore.eduxel.persistence.inventory.DeviceRepository;

public class InventoryService {

    private final DeviceRepository deviceRepository = new DeviceRepository();

    public long createDevice(String assetTag, String type, String status) throws Exception {
        long id = deviceRepository.create(assetTag, type, status);
        ActivityLogger.log("Inventar", "Neues Gerät angelegt: " + assetTag, null);
        return id;
    }
}
