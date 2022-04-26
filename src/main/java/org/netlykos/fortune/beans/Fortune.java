package org.netlykos.fortune.beans;

import java.util.List;

public record Fortune(String category, Integer number, List<String> lines) { }
