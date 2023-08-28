package by.parser.parserostrovshop;

public enum Category {
    PAMPERS("/catalog/tovary-dlya-detey/gigiena-i-ukhod-za-detmi/podguzniki/"),
    PANTIES("/catalog/tovary-dlya-detey/gigiena-i-ukhod-za-detmi/podguzniki-trusiki/"),
    ADULT_PAMPERS("/catalog/krasota-i-zdorove/tovary-meditsinskogo-naznacheniya/podguzniki-pelenki-dlya-vzroslykh-urologicheskie-prokladki/filter/type_v-is-c5adacd1a3e43886e4598c58af8458f2-or-d72eaa52d33d4aba5a6c4ce52f21df7e/apply/"),
    WIPES("/catalog/krasota-i-zdorove/lichnaya-gigiena/vlazhnye-salfetki/"),
    CHILDREN_WIPES("/catalog/tovary-dlya-detey/gigiena-i-ukhod-za-detmi/vlazhnye-salfetki-i-platochki/"),
    TOILET_PAPER("/catalog/krasota-i-zdorove/lichnaya-gigiena/tualetnaya-bumaga/filter/type_v-is-98944757a0d7e1006f461cd10268c066/apply/"),
    NAPPIES("/catalog/tovary-dlya-detey/gigiena-i-ukhod-za-detmi/gigienicheskie-pelenki-i-kleenki/");

    public final String code;

    Category(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }
}
