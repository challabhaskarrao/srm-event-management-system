package com.srm.events.dto;

import java.util.Map;

public record EventTypeDTO(String code, String name, int sortOrder, String workflowCategory,
                           Map<String, Object> config) {}
