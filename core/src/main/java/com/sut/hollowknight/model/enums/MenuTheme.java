package com.sut.hollowknight.model.enums;

public enum MenuTheme {
    THEME_01("ui/menu/background/bg-01.jpg"),
    THEME_02("ui/menu/background/bg-02.jpg"),
//    THEME_03("ui/menu/background/bg-03.webp"),
    THEME_04("ui/menu/background/bg-04.png"),;

    private String pathToFile;

    MenuTheme(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public String getPathToFile() {
        return pathToFile;
    }
}
