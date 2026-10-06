package com.srm.events.model;

import java.util.Map;

public record EventTypeDefinition(Long eventTypeId, String code, String name, int sortOrder,
                                  String workflowCategory, Map<String, Object> config) {}
