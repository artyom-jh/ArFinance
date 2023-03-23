package am.softlab.arfinance;

public class MyPair {
    private final double amount;
    private int fontSize;

    public MyPair(double amount, int fontSize) {
        this.amount = amount;
        this.fontSize = fontSize;
    }

    public double getAmount() {
        return amount;
    }

    public int getFontSize() {
        return fontSize;
    }
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    private static boolean isMyPair(Object o) {
        return o instanceof MyPair;
    }

    private boolean pairEqual(MyPair p) {
        return getAmount() == p.getAmount() && getFontSize() == p.getFontSize();
    }

    private boolean amountEqual(MyPair p) {
        return getAmount() == p.getAmount();
    }

    private boolean fontSizeEqual(MyPair p) {
        return getFontSize() == p.getFontSize();
    }

    public boolean equals(Object o) {
        return isMyPair(o) && pairEqual((MyPair) o);
    }

    public boolean equalsAmount(Object o) {
        return isMyPair(o) && amountEqual((MyPair) o);
    }

    public boolean equalsFontSize(Object o) {
        return isMyPair(o) && fontSizeEqual((MyPair) o);
    }
}
