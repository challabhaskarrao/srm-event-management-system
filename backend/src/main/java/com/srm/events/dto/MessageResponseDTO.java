package com.srm.events.dto;

public record MessageResponseDTO(String message, boolean deliveryConfigured) {
    public MessageResponseDTO(String message) { this(message, false); }
}
