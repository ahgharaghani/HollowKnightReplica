package com.sut.hollowknight.model.enums;

public enum MenuTheme {
    THEME_01("ui/menu/background/voidheart-theme.png"),
    THEME_02("ui/menu/background/bg-02.jpg"),
//    THEME_03("ui/menu/background/bg-03.webp"),
    THEME_04("ui/menu/background/grimmtroupe-theme.png"),;

    private String pathToFile;

    MenuTheme(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public String getPathToFile() {
        return pathToFile;
    }
}
