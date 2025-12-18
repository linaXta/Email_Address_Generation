package lv.alina.emailgen.models.enums;

public enum DefaultSymbol {
	
	PLUS("+"),
    MINUS("-"),
    NONE("");

    private final String value;

    DefaultSymbol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
