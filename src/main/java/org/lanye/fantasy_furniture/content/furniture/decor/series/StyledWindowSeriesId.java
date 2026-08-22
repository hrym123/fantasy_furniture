package org.lanye.fantasy_furniture.content.furniture.decor.series;

/**
 * 型号窗 1～7（含 1.5 / 2.5）表驱动系列 id；注册前缀 {@code styled_window_<token>}。
 */
public enum StyledWindowSeriesId {
    W1("1", "FfStyledWin1Shape"),
    W1_5("1_5", "FfStyledWin1_5Shape"),
    W2("2", "FfStyledWin2Shape"),
    W2_5("2_5", "FfStyledWin2_5Shape"),
    W3("3", "FfStyledWin3Shape"),
    W4("4", "FfStyledWin4Shape"),
    W5("5", "FfStyledWin5Shape"),
    W6("6", "FfStyledWin6Shape"),
    W7("7", "FfStyledWin7Shape");

    private final String token;
    private final String shapeNbtKey;

    StyledWindowSeriesId(String token, String shapeNbtKey) {
        this.token = token;
        this.shapeNbtKey = shapeNbtKey;
    }

    /** 注册 id 中段：{@code styled_window_<token>_<色>}。 */
    public String token() {
        return token;
    }

    public String idPrefix() {
        return "styled_window_" + token;
    }

    public String shapeNbtKey() {
        return shapeNbtKey;
    }

    public String blockEntityTypeId() {
        return idPrefix();
    }
}
